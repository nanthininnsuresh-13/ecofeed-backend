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

// POST /api/feedback - Save rating and comments from NGO or BIOGAS
router.post('/', async (req, res) => {
    try {
        const { donationId, donorId, reviewerId, reviewerRole, rating, comments, ngoId, biogasId } = req.body;

        // Comprehensive ID resolution with fallback for older payloads
        const resolvedReviewerId = reviewerId || ngoId || biogasId;
        const resolvedReviewerRole = normalizeReviewerRole(reviewerRole || (ngoId ? 'NGO' : (biogasId ? 'BIOGAS' : 'NGO')));

        if (!donationId || !donorId || !resolvedReviewerId || !rating) {
            console.error("Feedback missing fields:", { donationId, donorId, resolvedReviewerId, rating });
            return res.status(400).json({ message: "donationId, donorId, reviewerId, and rating are mandatory." });
        }

        const feedback = await Feedback.create({
            donationId: new mongoose.Types.ObjectId(donationId),
            donorId: new mongoose.Types.ObjectId(donorId),
            reviewerId: new mongoose.Types.ObjectId(resolvedReviewerId),
            reviewerRole: resolvedReviewerRole,
            rating: Number(rating),
            comments: comments || ""
        });

        // Recalculate Donor Average Rating strictly using valid ObjectIds
        const donorObjectId = new mongoose.Types.ObjectId(donorId);
        const stats = await Feedback.aggregate([
            { $match: { donorId: donorObjectId } },
            { $group: { _id: '$donorId', averageRating: { $avg: '$rating' }, reviewCount: { $sum: 1 } } }
        ]);

        if (stats.length > 0) {
            await User.findByIdAndUpdate(donorId, {
                averageRating: Number(stats[0].averageRating).toFixed(1),
                reviewCount: stats[0].reviewCount
            });
        }

        // Automatically Notify Donor
        const donation = await FoodListing.findById(donationId);
        const foodTitle = donation ? donation.title : "your donation";

        await createNotification({
            recipientId: donorId,
            recipientRole: 'DONOR',
            title: '⭐ New NGO Rating Received!',
            message: `An NGO gave your donation '${foodTitle}' a ${rating}-star review: '${comments || "No comments"}'.`,
            type: 'SUCCESS',
            relatedId: donationId
        });

        res.status(201).json({ success: true, message: "Feedback saved successfully", feedback });
    } catch (error) {
        console.error("Feedback Save Error:", error.message);
        res.status(500).json({ success: false, message: error.message });
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
