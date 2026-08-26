require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');

const app = express();

// Increased payload limits for Base64 image uploads
app.use(express.json({ limit: '50mb' }));
app.use(express.urlencoded({ limit: '50mb', extended: true }));

// Optimized CORS for Cloud Deployment
// Allows all origins for mobile app accessibility
app.use(cors({
    origin: '*',
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'Authorization']
}));

// MongoDB Connection Verification
mongoose.connect(process.env.MONGODB_URI)
    .then(() => {
        console.log("-----------------------------------------");
        console.log("🚀 SUCCESS: MongoDB Atlas Connected to EcoFeed DB");
        console.log("-----------------------------------------");
    })
    .catch(err => {
        console.log("-----------------------------------------");
        console.log("❌ ERROR: MongoDB Connection Failed!");
        console.error("Details:", err.message);
        console.log("-----------------------------------------");
    });

// Health Check Route
app.get('/api/health', (req, res) => {
    res.json({
        status: "OK",
        database: mongoose.connection.readyState === 1 ? "Connected to EcoFeed DB" : "Disconnected",
        timestamp: new Date()
    });
});

// Main Routes
app.use('/api', require('./routes/ecoFeedRoutes'));
app.use('/api/user', require('./routes/user'));
app.use('/api/history', require('./routes/history'));
app.use('/api/notifications', require('./routes/notification'));

const PORT = process.env.PORT || 5000;
// Bind to 0.0.0.0 to accept connections from other devices on the same Wi-Fi
app.listen(PORT, '0.0.0.0', () => {
    console.log(`✅ EcoFeed Server running on http://0.0.0.0:${PORT}`);
    console.log(`🔗 Local Access: http://localhost:${PORT}/api/health`);
});
