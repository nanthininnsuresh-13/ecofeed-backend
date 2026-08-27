const express = require('express');
const router = express.Router();
const FoodListing = require('../models/FoodListing');

// Role-specific history
router.get('/:userId', async (req, res) => {
    try {
        const { userId } = req.params;
        const { role } = req.query; // DONOR, NGO, BIOGAS

        let query = {};
        if (role === 'DONOR') {
            query = { donorId: userId };
        } else if (role === 'NGO' || role === 'BIOGAS') {
            query = { acceptedBy: userId };
        }

        const history = await FoodListing.find(query)
            .populate('donorId', 'organizationName firstName lastName')
            .populate('acceptedBy', 'organizationName firstName lastName')
            .sort({ createdAt: -1 });

        const formatted = history.map(item => ({
            id: item._id,
            lotId: `LOT-#${item._id.toString().slice(-4).toUpperCase()}`,
            title: item.title,
            quantity: item.quantity,
            status: item.status,
            createdAt: item.createdAt,
            donorId: item.donorId ? item.donorId._id : null,
            recipientName: item.acceptedBy ? (item.acceptedBy.organizationName || `${item.acceptedBy.firstName} ${item.acceptedBy.lastName}`) : null,
            donorName: item.donorId ? (item.donorId.organizationName || `${item.donorId.firstName} ${item.donorId.lastName}`) : 'Unknown Donor'
        }));

        res.json(formatted);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
