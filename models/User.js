const mongoose = require('mongoose');
const bcrypt = require('bcryptjs');

const userSchema = new mongoose.Schema({
    firstName: { type: String, required: true },
    lastName: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    password: { type: String, required: true },
    role: { type: String, enum: ['DONOR', 'NGO', 'BIOGAS'], required: true },
    fullName: { type: String },
    phoneNumber: { type: String }, // Switched to String for flexible formatting
    organizationName: { type: String },
    profileImageUrl: { type: String, default: '' },
    address: { type: String, default: 'Trichy, Tamil Nadu, India' },
    location: { type: String, default: 'Trichy, Tamil Nadu, India' }, // Strictly String, no geo index
    averageRating: { type: Number, default: 0 },
    reviewCount: { type: Number, default: 0 },
    createdAt: { type: Date, default: Date.now }
});

// Explicitly ensure NO geo index is present on text fields
// userSchema.index({ location: '2dsphere' }); // Deleted

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
