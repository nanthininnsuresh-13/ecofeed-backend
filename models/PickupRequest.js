const mongoose = require('mongoose');

const pickupRequestSchema = new mongoose.Schema({
    listingId: { type: mongoose.Schema.Types.ObjectId, required: true },
    donorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    receiverId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    requestType: { type: String, enum: ['FOOD', 'WASTE'], required: true },
    status: {
        type: String,
        enum: ['PENDING', 'ACCEPTED', 'IN_TRANSIT', 'COMPLETED'],
        default: 'PENDING'
    },
    otpCode: { type: String },
    acceptedAt: { type: Date },
    completedAt: { type: Date },
    createdAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('PickupRequest', pickupRequestSchema);
