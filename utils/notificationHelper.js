const Notification = require('../models/Notification');

const createNotification = async ({ recipientId, recipientRole, title, message, type = 'INFO', relatedId = null, lotId = null }) => {
    try {
        const notification = new Notification({
            recipientId,
            recipientRole,
            title,
            message,
            type,
            relatedId,
            lotId
        });
        await notification.save();
        return notification;
    } catch (error) {
        console.error('Error creating notification:', error.message);
    }
};

module.exports = { createNotification };
