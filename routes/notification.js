const express = require('express');
const router = express.Router();
const Notification = require('../models/Notification');

// Get notifications for a user (by ID or Role)
router.get('/', async (req, res) => {
    try {
        const { userId, role } = req.query;

        const orConditions = [{ recipientRole: role }, { recipientRole: 'ALL' }];

        // Only add recipientId if it's provided to avoid match errors
        if (userId && userId !== 'null' && userId !== 'undefined') {
            orConditions.push({ recipientId: userId });
        }

        const notifications = await Notification.find({
            $or: orConditions
        }).sort({ createdAt: -1 });

        res.json(notifications);
    } catch (error) {
        console.error("Notification Fetch Error:", error.message);
        res.status(500).json({ message: error.message });
    }
});

// Mark as read (using PATCH as requested)
router.patch('/:id/read', async (req, res) => {
    try {
        await Notification.findByIdAndUpdate(req.params.id, { isRead: true });
        res.json({ message: 'Marked as read' });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
