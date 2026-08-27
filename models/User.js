const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
    firstName: { type: String, required: true },
    lastName: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    role: { type: String, enum: ['DONOR', 'NGO', 'BIOGAS'], required: true },
    fullName: { type: String },
    phoneNumber: { type: Number },
    organizationName: { type: String },
    profileImageUrl: { type: String, default: '' },
    address: { type: String, default: 'Trichy, Tamil Nadu, India' },
    location: { type: String, default: 'Trichy, Tamil Nadu, India' },
    averageRating: { type: Number, default: 0 },
    reviewCount: { type: Number, default: 0 },
    geoPoint: {
        type: { type: String, enum: ['Point'], default: 'Point' },
        coordinates: { type: [Number], default: [78.6862, 10.7905] } // [lng, lat]
    },
    createdAt: { type: Date, default: Date.now }
});

userSchema.index({ geoPoint: '2dsphere' });

userSchema.methods.matchPassword = async function (enteredPassword) {
    return await bcrypt.compare(enteredPassword, this.password);
};

userSchema.pre('save', async function (next) {
    if (!this.isModified('password')) {
        next();
    }
    const salt = await bcrypt.genSalt(10);
    this.password = await bcrypt.hash(this.password, salt);
});

module.exports = mongoose.model('User', userSchema);
