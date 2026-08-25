const mongoose = require('mongoose');

const sustainabilityImpactSchema = new mongoose.Schema({
    donorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true, unique: true },
    mealsServed: { type: Number, default: 0 },
    wasteReducedKg: { type: Number, default: 0 },
    co2SavedKg: { type: Number, default: 0 },
    updatedAt: { type: Date, default: Date.now }
});

module.exports = mongoose.model('SustainabilityImpact', sustainabilityImpactSchema);
