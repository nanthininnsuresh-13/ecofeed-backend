const express = require('express');
const router = express.Router();
const mongoose = require('mongoose');
const FoodListing = require('../models/FoodListing');

router.post('/', async (req, res) => {
    try {
        const listing = new FoodListing(req.body);
        await listing.save();
        res.status(201).json(listing);
    } catch (err) {
        res.status(400).json({ message: err.message });
    }
});

router.get('/nearby', async (req, res) => {
    try {
        const { lat, lng, maxDistance } = req.query;
        const donations = await FoodListing.find({
            isEdible: true,
            status: 'AVAILABLE',
            expiryTime: { $gt: new Date() },
            location: {
                $near: {
                    $geometry: { type: 'Point', coordinates: [parseFloat(lng), parseFloat(lat)] },
                    $maxDistance: parseFloat(maxDistance) || 5000
                }
            }
        }).sort({ expiryTime: 1 });

        res.json(donations);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

router.get('/ngo/recommended', async (req, res) => {
    try {
        const donations = await FoodListing.find({ isEdible: true, status: 'AVAILABLE' }).sort({ priorityLevel: -1, expiryTime: 1 });
        const ranked = donations
            .map(d => {
                const priorityScore = { HIGH: 3, MEDIUM: 2, LOW: 1 }[d.priorityLevel] || 1;
                const expiry = new Date(d.expiryTime || d.expiryDate || Date.now());
                const hoursRemaining = Math.max(1, (expiry - Date.now()) / 3600000);
                return { ...d.toObject(), score: priorityScore * 100 + Math.max(0, 24 - hoursRemaining) * 2, aiReason: `⚡ Urgent: Expires in ${Math.max(1, Math.round(hoursRemaining))} hours` };
            })
            .sort((a, b) => b.score - a.score)
            .map((d, index) => ({ ...d, isAiRecommended: index < 3, aiReason: index < 3 ? d.aiReason : null }));
        res.json(ranked);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

router.get('/biogas', async (req, res) => {
    try {
        const now = new Date();
        const donations = await FoodListing.find({
            status: 'AVAILABLE',
            $or: [
                { isEdible: false },
                { expiryTime: { $lt: now } },
                { expiryDate: { $lt: now } },
                { category: { $in: ['VEGETABLE_WASTE', 'KITCHEN_PEELS', 'EXPIRED'] } }
            ]
        }).sort({ createdAt: -1 });
        res.json(donations);
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

router.put('/accept/:id', async (req, res) => {
    try {
        const { acceptedBy } = req.body;
        const donation = await FoodListing.findByIdAndUpdate(req.params.id, { status: 'ACCEPTED', acceptedBy, acceptedAt: new Date() }, { new: true });
        if (!donation) return res.status(404).json({ message: 'Donation not found' });
        res.json({ message: 'Donation accepted', donation });
    } catch (err) {
        res.status(500).json({ message: err.message });
    }
});

module.exports = router;
