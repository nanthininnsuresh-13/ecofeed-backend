const express = require('express');
const router = express.Router();
const Feedback = require('../models/Feedback');
const User = require('../models/User');
const FoodListing = require('../models/FoodListing');
const { createNotification } = require('../utils/notificationHelper');
const mongoose = require('mongoose');

const normalizeReviewerRole = (role = '') => {
    const normalized = String(role).trim().toUpperCase();
    return normalized === 'BIOGAS' || normalized === 'NGO' ? normalized : 'NGO';
};

// POST /api/feedback - Save rating and comments
router.post('/', async (req, res) => {
    try {
        const { donationId, donorId, reviewerId, reviewerRole, rating, comments, ngoId, biogasId } = req.body;
        const resolvedReviewerId = reviewerId || ngoId || biogasId;
        const resolvedReviewerRole = normalizeReviewerRole(reviewerRole || (ngoId ? 'NGO' : (biogasId ? 'BIOGAS' : 'NGO')));

        if (!donationId || !donorId || !resolvedReviewerId || !rating) {
            return res.status(400).json({ message: "All fields are required" });
        }

        const feedback = await Feedback.create({
            donationId,
            donorId,
            reviewerId: resolvedReviewerId,
            reviewerRole: resolvedReviewerRole,
            rating,
            comments
        });

        const donorObjectId = mongoose.Types.ObjectId.isValid(donorId) ? new mongoose.Types.ObjectId(donorId) : donorId;
        const stats = await Feedback.aggregate([
            { $match: { donorId: donorObjectId } },
            { $group: { _id: '$donorId', averageRating: { $avg: '$rating' }, reviewCount: { $sum: 1 } } }
        ]);

        if (stats.length > 0) {
            await User.findByIdAndUpdate(donorId, {
                averageRating: stats[0].averageRating,
                reviewCount: stats[0].reviewCount
            });
        }

        const donation = await FoodListing.findById(donationId);
        const title = donation ? donation.title : "your donation";

        await createNotification({
            recipientId: donorId,
            recipientRole: 'DONOR',
            title: '⭐ New Feedback Received',
            message: `You received a ${rating}-star review for '${title}' from an ${resolvedReviewerRole}!`,
            type: 'SUCCESS',
            relatedId: donationId
        });

        res.status(201).json({ success: true, feedback });
    } catch (error) {
        console.error("Feedback Save Error:", error.message);
        res.status(500).json({ message: error.message });
    }
});

// GET /api/feedback/donor/:donorId - Get all feedback for a donor
router.get('/donor/:donorId', async (req, res) => {
    try {
        const donorFilter = mongoose.Types.ObjectId.isValid(req.params.donorId)
            ? new mongoose.Types.ObjectId(req.params.donorId)
            : req.params.donorId;

        const feedback = await Feedback.find({ donorId: donorFilter })
            .populate('reviewerId', 'firstName lastName organizationName profileImageUrl role')
            .sort({ createdAt: -1 });

        res.json(feedback);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
