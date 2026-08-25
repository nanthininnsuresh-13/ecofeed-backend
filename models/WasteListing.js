const mongoose = require('mongoose');

const wasteListingSchema = new mongoose.Schema({
    donorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    wasteType: {
        type: String,
        enum: ['EXPIRED_FOOD', 'KITCHEN_WASTE', 'VEGETABLE_PEELS'],
        required: true
    },
    quantityKg: { type: Number, required: true },
    estimatedBiogasM3: { type: Number },
    status: {
        type: String,
        enum: ['AVAILABLE', 'ACCEPTED', 'PICKED_UP'],
        default: 'AVAILABLE'
    },
    location: {
        type: { type: String, enum: ['Point'], default: 'Point' },
        coordinates: { type: [Number], required: true } // [lng, lat]
    },
    createdAt: { type: Date, default: Date.now }
});

wasteListingSchema.index({ location: '2dsphere' });

module.exports = mongoose.model('WasteListing', wasteListingSchema);
