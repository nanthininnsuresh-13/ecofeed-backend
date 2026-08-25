const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const User = require('../models/User');

// Get User Profile
router.get('/profile/:id', async (req, res) => {
    try {
        const user = await User.findById(req.params.id).select('-password');
        if (!user) return res.status(404).json({ message: 'User not found' });
        res.json(user);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Update Profile (Photo & Location)
router.put('/profile', async (req, res) => {
    try {
        const { userId, fullName, phoneNumber, address, organizationName, establishmentName, profileImageUrl } = req.body;

        const updateFields = {};
        if (profileImageUrl !== undefined) updateFields.profileImageUrl = profileImageUrl;
        if (phoneNumber !== undefined) updateFields.phoneNumber = phoneNumber;
        if (address !== undefined) updateFields.address = address;

        const orgName = organizationName || establishmentName;
        if (orgName !== undefined) updateFields.organizationName = orgName;

        if (fullName) {
            const parts = fullName.trim().split(' ');
            updateFields.firstName = parts[0];
            updateFields.lastName = parts.slice(1).join(' ') || ' ';
        }

        const updatedUser = await User.findByIdAndUpdate(
            userId,
            { $set: updateFields },
            { new: true, runValidators: true }
        ).select('-password');

        if (!updatedUser) return res.status(404).json({ success: false, message: 'User not found' });

        res.json({ success: true, user: updatedUser });
    } catch (error) {
        console.error("Profile Update Error:", error.message);
        res.status(500).json({ success: false, message: error.message });
    }
});

// Change Password
router.put('/change-password', async (req, res) => {
    try {
        const { userId, oldPassword, newPassword } = req.body;
        if (!oldPassword || !newPassword) {
            return res.status(400).json({ message: "Current and new passwords are required." });
        }
        const user = await User.findById(userId);
        if (!user) return res.status(404).json({ message: 'User not found' });

        const isMatch = await user.matchPassword(oldPassword);
        if (!isMatch) return res.status(400).json({ message: "Incorrect current password." });

        user.password = newPassword;
        await user.save();
        res.status(200).json({ message: "Password updated successfully." });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

module.exports = router;
