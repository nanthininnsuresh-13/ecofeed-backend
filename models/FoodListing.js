const mongoose = require('mongoose');

const parseFlexibleDate = (value) => {
    if (!value) return null;
    if (value instanceof Date) return Number.isNaN(value.getTime()) ? null : value;
    if (typeof value === 'string') {
        const trimmed = value.trim();
        if (!trimmed) return null;
        if (/^\d{2}\/\d{2}\/\d{4}$/.test(trimmed)) {
            const [dd, mm, yyyy] = trimmed.split('/');
            const parsed = new Date(`${yyyy}-${mm}-${dd}`);
            return Number.isNaN(parsed.getTime()) ? null : parsed;
        }
        const parsed = new Date(trimmed);
        return Number.isNaN(parsed.getTime()) ? null : parsed;
    }
    return null;
};

const foodListingSchema = new mongoose.Schema({
    donorId: { type: mongoose.Schema.Types.ObjectId, ref: 'User', required: true },
    title: { type: String, required: true },
    foodName: { type: String },
    quantity: { type: String, default: '0 kg' },
    quantityKg: { type: Number, default: 0 },
    category: {
        type: String,
        enum: ['LUNCH', 'DINNER', 'BREAKFAST', 'SNACKS', 'BAKERY', 'MEAL', 'EXPIRED', 'VEGETABLE_WASTE', 'KITCHEN_PEELS', 'GENERAL'],
        required: true,
        default: 'LUNCH'
    },
    donorSourceType: {
        type: String,
        default: 'Household'
    },
    establishmentName: { type: String, default: '' },
    hotelName: { type: String, default: '' },
    dietaryType: { type: String, enum: ['VEG', 'NON_VEG', 'VEGAN', 'BOTH'], default: 'VEG' },
    dietaryCategory: { type: String, enum: ['VEG', 'NON_VEG', 'BOTH'], default: 'VEG' },
    donorPhoneNumber: {
        type: String,
        default: '',
        validate: {
            validator: function (v) {
                return !v || /^[0-9]{10}$/.test(v.replace(/\s+/g, ''));
            },
            message: 'donorPhoneNumber must be a valid 10-digit mobile number'
        }
    },
    mealComposition: { type: [String], default: [] },
    description: { type: String, default: '' },
    storageCondition: { type: String, enum: ['ROOM_TEMP', 'REFRIGERATED', 'FROZEN'], default: 'ROOM_TEMP' },
    packagingType: { type: String, enum: ['PACKED_CONTAINERS', 'BULK_VESSELS', 'UNPACKED'], default: 'PACKED_CONTAINERS' },
    expiryDate: { type: String, default: null },
    expiryTime: { type: String, default: null },
    prepTime: { type: String, default: null },
    donorName: { type: String },
    address: { type: String, default: 'Location Not Provided' },
    isEdible: { type: Boolean, default: true },
    priorityLevel: { type: String, enum: ['HIGH', 'MEDIUM', 'LOW'], default: 'MEDIUM' },
    imageUrls: { type: [String], default: [] },
    status: {
        type: String,
        enum: ['AVAILABLE', 'ACCEPTED', 'PICKED_UP', 'COMPLETED'],
        default: 'AVAILABLE'
    },
    targetAudience: { type: String, enum: ['NGO', 'BIOGAS', 'DONOR'], default: 'NGO' },
    acceptedBy: { type: mongoose.Schema.Types.ObjectId, ref: 'User' },
    acceptedAt: { type: Date, default: null },
    rating: { type: Number, default: 0 },
    coordinates: { type: [Number], default: [0, 0] },
    location: {
        type: { type: String, enum: ['Point'], default: 'Point' },
        coordinates: { type: [Number], default: [0, 0] }
    },
    createdAt: { type: Date, default: Date.now }
});

foodListingSchema.index({ location: '2dsphere' });

foodListingSchema.pre('save', function (next) {
    const now = new Date();
    const expiryValue = this.expiryDate || this.expiryTime;
    const parsedExpiry = parseFlexibleDate(expiryValue);
    const shouldRouteToBiogas =
        (parsedExpiry && parsedExpiry < now) ||
        this.category === 'VEGETABLE_WASTE' ||
        this.category === 'KITCHEN_PEELS' ||
        this.isEdible === false ||
        (this.wasteType && ['VEGETABLE_PEELS', 'KITCHEN_WASTE', 'EXPIRED_FOOD'].includes(this.wasteType));

    if (shouldRouteToBiogas) {
        this.isEdible = false;
        this.targetAudience = 'BIOGAS';
    }

    if (!this.title && this.foodName) {
        this.title = this.foodName;
    }

    if (!this.location || !this.location.coordinates || this.location.coordinates.length !== 2) {
        this.location = {
            type: 'Point',
            coordinates: this.coordinates && this.coordinates.length === 2 ? this.coordinates : [0, 0]
        };
    }

    next();
});

module.exports = mongoose.model('FoodListing', foodListingSchema);
