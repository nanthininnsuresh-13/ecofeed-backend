const express = require('express');
const router = express.Router();
const bcrypt = require('bcryptjs');
const User = require('../models/User');
const cloudinary = require('cloudinary').v2;

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

// Update Profile (Persistence & Correct Data Types with Cloudinary)
router.put('/profile', async (req, res) => {
    try {
        const { userId, fullName, phoneNumber, location, address, organizationName, profileImageUrl, profilePicture } = req.body;

        const updateData = {};

        // Handle Cloudinary Upload if a new photo is provided (Base64)
        const incomingImage = profileImageUrl || profilePicture;
        if (incomingImage && incomingImage.startsWith('data:image')) {
            try {
                const uploadRes = await cloudinary.uploader.upload(incomingImage, {
                    folder: "ecofeed_profiles",
                    resource_type: "image"
                });
                updateData.profileImageUrl = uploadRes.secure_url;
            } catch (err) {
                console.error("Cloudinary Upload Error:", err.message);
                // Continue without updating image if upload fails
            }
        }

        if (phoneNumber !== undefined) {
            const cleanPhone = String(phoneNumber).replace(/\D/g, '');
            updateData.phoneNumber = Number(cleanPhone) || 0;
        }

        const loc = location || address;
        if (loc !== undefined) {
            updateData.location = String(loc);
            updateData.address = String(loc);
        }

        if (fullName !== undefined) {
            updateData.fullName = String(fullName);
            const parts = fullName.trim().split(' ');
            updateData.firstName = parts[0];
            updateData.lastName = parts.slice(1).join(' ') || ' ';
        }

        if (organizationName !== undefined) updateData.organizationName = String(organizationName);

        const updatedUser = await User.findByIdAndUpdate(
            userId,
            { $set: updateData },
            { new: true, runValidators: false }
        ).select('-password');

        if (!updatedUser) return res.status(404).json({ success: false, message: 'User not found' });

        res.json({ success: true, user: updatedUser });
    } catch (error) {
        console.error("Profile Save Error:", error.message);
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
