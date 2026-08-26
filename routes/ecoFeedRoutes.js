const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const jwt = require('jsonwebtoken');
const User = require('../models/User');
const FoodListing = require('../models/FoodListing');
const PickupRequest = require('../models/PickupRequest');
const Review = require('../models/Review');
const { createNotification } = require('../utils/notificationHelper');

// Helper to generate JWT
const generateToken = (id) => {
    return jwt.sign({ id }, process.env.JWT_SECRET, { expiresIn: '30d' });
};

// 1. AUTH ROUTES
router.post('/auth/register', async (req, res) => {
    console.log("Registration payload received:", req.body);
    const { fullName, firstName, lastName, email, password, role, phoneNumber, organizationName, location } = req.body;

    try {
        const normalizedEmail = email ? email.toLowerCase().trim() : '';
        if (!normalizedEmail || !password) {
            return res.status(400).json({ message: "Email and password are required" });
        }

        const userExists = await User.findOne({ email: normalizedEmail });
        if (userExists) return res.status(400).json({ message: 'User already exists with this email' });

        const normalizedRole = role ? role.toUpperCase() : 'DONOR';

        let fName = firstName;
        let lName = lastName;

        if (fullName && !fName) {
            const parts = fullName.trim().split(' ');
            fName = parts[0];
            lName = parts.slice(1).join(' ') || ' ';
        }

        const user = await User.create({
            firstName: fName || 'User',
            lastName: lName || '',
            email: normalizedEmail,
            password, // Password will be hashed by User model pre-save hook
            role: normalizedRole,
            phoneNumber,
            organizationName,
            location: (typeof location === 'object') ? location : { type: "Point", coordinates: [78.6862, 10.7905] },
            address: (typeof location === 'string') ? location : (req.body.address || 'Trichy, Tamil Nadu, India')
        });

        res.status(201).json({
            success: true,
            message: "User registered successfully",
            _id: user._id,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            role: user.role,
            token: generateToken(user._id),
            user: {
                _id: user._id,
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                role: user.role
            }
        });
    } catch (error) {
        console.error("Register Error:", error);
        res.status(500).json({ message: error.message });
    }
});

router.post('/auth/login', async (req, res) => {
    console.log("Login payload received:", req.body);
    const { email, password } = req.body;

    try {
        if (!email || !password) {
            return res.status(400).json({ message: "Email and password are required" });
        }

        const normalizedEmail = email.toLowerCase().trim();
        const user = await User.findOne({ email: normalizedEmail });

        if (user && (await user.matchPassword(password))) {
            res.json({
                success: true,
                message: "Login successful",
                token: generateToken(user._id),
                userId: user._id,
                _id: user._id,
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                role: user.role,
                organizationName: user.organizationName,
                profileImageUrl: user.profileImageUrl,
                phoneNumber: user.phoneNumber,
                address: user.address,
                user: {
                    _id: user._id,
                    firstName: user.firstName,
                    lastName: user.lastName,
                    email: user.email,
                    role: user.role
                }
            });
        } else {
            res.status(401).json({ message: 'Invalid email or password' });
        }
    } catch (error) {
        console.error("Login Error:", error);
        res.status(500).json({ message: error.message });
    }
});

// 2. DONATION ROUTES
router.post('/donations/add', async (req, res) => {
    try {
        const { latitude, longitude, foodName, address, donorId, ...rest } = req.body;
        const donation = await FoodListing.create({
            ...rest,
            donorId,
            title: foodName,
            address: address || 'Trichy, Tamil Nadu, India',
            location: { type: "Point", coordinates: [parseFloat(longitude || 78.6862), parseFloat(latitude || 10.7905)] }
        });

        // Broadcast Notification to all NGOs/Biogas
        const lotId = `LOT-#${donation._id.toString().slice(-4).toUpperCase()}`;
        const targetRole = (donation.isEdible === false || donation.category === 'EXPIRED') ? 'BIOGAS' : 'NGO';

        await createNotification({
            recipientRole: targetRole,
            title: targetRole === 'NGO' ? '⚡ Urgent Food Alert' : '♻️ New Organic Waste Available',
            message: targetRole === 'NGO'
                ? `Fresh surplus (${donation.quantity}) available nearby at ${donation.address}.`
                : `${donation.quantity} of organic waste available for collection at ${donation.address}.`,
            type: targetRole === 'NGO' ? 'URGENT' : 'INFO',
            relatedId: donation._id,
            lotId: lotId
        });

        res.status(201).json(donation);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

// NGO Feed: Edible + Available
router.get('/donations/ngo', async (req, res) => {
    try {
        const donations = await FoodListing.find({ isEdible: true, status: 'AVAILABLE' })
            .populate('donorId', 'firstName lastName organizationName averageRating reviewCount')
            .sort({ createdAt: -1 });

        const result = donations.map(d => {
            const donor = d.donorId;
            const donorIdValue = donor && donor._id ? String(donor._id) : (d.donorId ? String(d.donorId) : '');
            return {
                ...d._doc,
                donorId: donorIdValue,
                donorName: donor ? (donor.organizationName || `${donor.firstName || ''} ${donor.lastName || ''}`.trim() || 'Nearby Donor') : (d.donorName || d.establishmentName || 'Nearby Donor'),
                averageRating: donor ? (donor.averageRating || 0) : 0,
                reviewCount: donor ? (donor.reviewCount || 0) : 0,
                expiryTime: d.expiryTime || d.expiryDate || null,
                imageUrls: Array.isArray(d.imageUrls) ? d.imageUrls : [],
                isAiRecommended: false,
                aiReason: null
            };
        });
        res.json(result);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.get('/donations/ngo/recommended', async (req, res) => {
    try {
        const donations = await FoodListing.find({ isEdible: true, status: 'AVAILABLE' })
            .populate('donorId', 'firstName lastName organizationName averageRating reviewCount location')
            .sort({ priorityLevel: -1, expiryTime: 1, createdAt: -1 });

        const normalized = donations.map(d => {
            const priorityScore = { HIGH: 3, MEDIUM: 2, LOW: 1 }[d.priorityLevel] || 1;
            const expiryDate = new Date(d.expiryTime || d.expiryDate || Date.now());
            const hoursRemaining = Math.max(1, (expiryDate - Date.now()) / 3600000);
            const score = priorityScore * 100 + Math.max(0, 24 - hoursRemaining) * 2;
            const aiReason = `⚡ Urgent: Expires in ${Math.max(1, Math.round(hoursRemaining))} hours`;
            return { ...d._doc, score, isAiRecommended: score > 150, aiReason, expiryWindowHours: hoursRemaining };
        });

        const ranked = normalized.sort((a, b) => b.score - a.score);
        res.json(ranked.map((d, index) => ({
            ...d,
            isAiRecommended: index < 3,
            aiReason: index < 3 ? `⚡ Urgent: Expires in ${Math.max(1, Math.round(d.expiryWindowHours))} hours` : null
        })));
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Biogas Feed: Non-Edible/Expired + Available
router.get('/donations/biogas', async (req, res) => {
    try {
        const now = new Date();
        const donations = await FoodListing.find({
            status: 'AVAILABLE',
            $or: [
                { isEdible: false },
                { expiryTime: { $lt: now } },
                { expiryDate: { $lt: now } },
                { category: { $in: ['VEGETABLE_WASTE', 'KITCHEN_PEELS', 'EXPIRED'] } },
                { wasteType: { $in: ['VEGETABLE_PEELS', 'KITCHEN_WASTE', 'EXPIRED_FOOD'] } }
            ]
        })
        .populate('donorId', 'firstName organizationName averageRating reviewCount')
        .sort({ createdAt: -1 });

        const result = donations.map(d => {
            const donor = d.donorId;
            const donorIdValue = donor && donor._id ? String(donor._id) : (d.donorId ? String(d.donorId) : '');
            return {
                ...d._doc,
                donorId: donorIdValue,
                donorName: donor ? (donor.organizationName || donor.firstName || 'Nearby') : (d.donorName || d.establishmentName || 'Nearby'),
                averageRating: donor ? (donor.averageRating || 0) : 0,
                reviewCount: donor ? (donor.reviewCount || 0) : 0,
                imageUrls: Array.isArray(d.imageUrls) ? d.imageUrls : [],
                expiryFlag: d.expiryTime || d.expiryDate ? `Expired on ${new Date(d.expiryTime || d.expiryDate).toLocaleDateString()}` : 'No expiry date'
            };
        });
        res.json(result);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get Nearby Edible Listings
router.get('/donations/nearby', async (req, res) => {
    try {
        const { lat, lng, maxDistance } = req.query;
        const donations = await FoodListing.find({
            isEdible: true,
            status: 'AVAILABLE',
            location: {
                $near: {
                    $geometry: { type: "Point", coordinates: [parseFloat(lng), parseFloat(lat)] },
                    $maxDistance: parseFloat(maxDistance) || 5000
                }
            }
        }).populate('donorId', 'firstName lastName organizationName').sort({ createdAt: -1 });

        const result = donations.map(d => {
            const donor = d.donorId;
            return {
                ...d._doc,
                donorId: donor ? donor._id : d.donorId,
                donorName: donor ? (donor.organizationName || `${donor.firstName} ${donor.lastName}`) : 'Nearby Donor'
            };
        });
        res.json(result);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.get('/donations/detail/:donationId', async (req, res) => {
    try {
        const donation = await FoodListing.findById(req.params.donationId);
        if (!donation) return res.status(404).json({ message: 'Donation not found' });

        const donorIdValue = donation.donorId ? String(donation.donorId) : '';
        const detail = donation.toObject();

        res.json({
            ...detail,
            donorId: donorIdValue,
            foodName: detail.foodName || detail.title || 'Donation',
            title: detail.title || detail.foodName || 'Donation',
            donorName: detail.donorName || detail.establishmentName || 'Nearby Donor',
            establishmentName: detail.establishmentName || detail.donorName || 'Nearby Donor',
            donorPhoneNumber: detail.donorPhoneNumber || '',
            dietaryCategory: detail.dietaryCategory || detail.dietaryType || 'VEG',
            mealComposition: Array.isArray(detail.mealComposition) && detail.mealComposition.length ? detail.mealComposition : ['Rice', 'Sambar'],
            description: detail.description || '',
            imageUrls: Array.isArray(detail.imageUrls) ? detail.imageUrls : []
        });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get Donation History for a Donor
router.get('/donations/history/:donorId', async (req, res) => {
    try {
        const donations = await FoodListing.find({ donorId: req.params.donorId })
            .sort({ createdAt: -1 });
        res.json(donations);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Accept Donation
router.put('/donations/accept/:id', async (req, res) => {
    try {
        const { acceptedBy } = req.body;
        const donation = await FoodListing.findByIdAndUpdate(
            req.params.id,
            { status: 'ACCEPTED', acceptedBy, acceptedAt: new Date() },
            { new: true }
        );
        if (!donation) return res.status(404).json({ message: 'Donation not found' });

        // Trigger Notification for Donor
        const lotId = `LOT-#${donation._id.toString().slice(-4).toUpperCase()}`;
        await createNotification({
            recipientId: donation.donorId,
            recipientRole: 'DONOR',
            title: '✅ Donation Accepted',
            message: `Your donation '${donation.title}' (${lotId}) was accepted. Pickup estimated in 30 minutes!`,
            type: 'SUCCESS',
            relatedId: donation._id,
            lotId: lotId
        });

        // Notification for Recipient
        await createNotification({
            recipientId: acceptedBy,
            recipientRole: 'NGO', // Fixed for this endpoint
            title: '✅ Claim Confirmed',
            message: `You have claimed '${donation.title}' (${lotId}). Tap to view pickup route.`,
            type: 'SUCCESS',
            relatedId: donation._id,
            lotId: lotId
        });

        res.json({ message: 'Donation Accepted successfully', donation });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.put('/donations/deliver/:id', async (req, res) => {
    try {
        const donation = await FoodListing.findByIdAndUpdate(
            req.params.id,
            { status: 'DELIVERED', deliveredAt: new Date() },
            { new: true }
        );
        if (!donation) return res.status(404).json({ message: 'Donation not found' });

        const lotId = `LOT-#${donation._id.toString().slice(-4).toUpperCase()}`;

        // Notify Donor
        await createNotification(
            donation.donorId,
            '🎉 Delivery Complete',
            `Your donation '${donation.title}' (${lotId}) was delivered successfully!`,
            'SUCCESS',
            donation._id,
            lotId
        );

        res.json({ message: 'Marked as Delivered', donation });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.put('/donations/biogas/accept/:id', async (req, res) => {
    try {
        const { acceptedBy } = req.body;
        const donation = await FoodListing.findByIdAndUpdate(
            req.params.id,
            { status: 'ACCEPTED', acceptedBy, acceptedAt: new Date(), targetAudience: 'BIOGAS' },
            { new: true }
        );
        if (!donation) return res.status(404).json({ message: 'Donation not found' });

        // Trigger Notification for Donor
        const lotId = `LOT-#${donation._id.toString().slice(-4).toUpperCase()}`;
        await createNotification(
            donation.donorId,
            '♻️ Waste Redirected',
            `Your item '${donation.title}' (${lotId}) successfully claimed by Biogas Partner.`,
            'INFO',
            donation._id,
            lotId
        );

        res.json({ message: 'Biogas pickup accepted successfully', donation });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

router.post('/reviews/add', async (req, res) => {
    try {
        const { donationId, donorId, ngoId, rating, feedbackText } = req.body;
        if (!donationId || !donorId || !ngoId || !rating) {
            return res.status(400).json({ message: 'donationId, donorId, ngoId and rating are required' });
        }

        const review = await Review.create({ donationId, donorId, ngoId, rating, feedbackText: feedbackText || '' });

        const stats = await Review.aggregate([
            { $match: { donorId: mongoose.Types.ObjectId(donorId) } },
            { $group: { _id: '$donorId', averageRating: { $avg: '$rating' }, reviewCount: { $sum: 1 } } }
        ]);

        const userStats = stats[0] || { averageRating: 0, reviewCount: 0 };
        await User.findByIdAndUpdate(donorId, {
            averageRating: Number(userStats.averageRating || 0),
            reviewCount: Number(userStats.reviewCount || 0)
        });

        res.status(201).json({ message: 'Review saved successfully', review, averageRating: userStats.averageRating || 0, reviewCount: userStats.reviewCount || 0 });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
