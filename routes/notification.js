const express = require('express');
const router = express.Router();
const Notification = require('../models/Notification');

// GET /api/notifications - Safe retrieval by ID or Role
router.get('/', async (req, res) => {
    try {
        const { userId, role } = req.query;

        if (!role && !userId) {
            return res.status(200).json([]); // Return empty list safely if params are missing during login initialization
        }

        const queryConditions = [{ recipientRole: 'ALL' }];
        if (role) queryConditions.push({ recipientRole: role });
        if (userId && userId !== 'null' && userId !== 'undefined') queryConditions.push({ recipientId: userId });

        const notifications = await Notification.find({
            $or: queryConditions
        }).sort({ createdAt: -1 }).limit(50);

        return res.status(200).json(notifications);
    } catch (error) {
        console.error("GET /api/notifications Error:", error.message);
        return res.status(200).json([]); // Fallback to empty array instead of throwing 500
    }
});

// Mark as read (using PATCH)
router.patch('/:id/read', async (req, res) => {
    try {
        if (!req.params.id || req.params.id === 'undefined') {
            return res.status(400).json({ message: 'Invalid ID' });
        }
        await Notification.findByIdAndUpdate(req.params.id, { isRead: true });
        res.json({ message: 'Marked as read' });
    } catch (error) {
        console.error("PATCH notification/read Error:", error.message);
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
