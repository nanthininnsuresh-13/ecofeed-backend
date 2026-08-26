const mongoose = require('mongoose');

const notificationSchema = new mongoose.Schema({
    recipientId: { type: String, default: null },
    recipientRole: { type: String, enum: ['DONOR', 'NGO', 'BIOGAS', 'ALL'], required: true },
    title: { type: String, required: true },
    message: { type: String, required: true },
    type: { type: String, enum: ['INFO', 'SUCCESS', 'WARNING', 'URGENT'], default: 'INFO' },
    relatedId: { type: String }, // e.g. Donation ID
    lotId: { type: String }, // e.g. LOT-#8392
    isRead: { type: Boolean, default: false },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('Notification', notificationSchema);
