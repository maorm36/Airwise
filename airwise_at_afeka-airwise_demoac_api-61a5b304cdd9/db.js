import mongoose from 'mongoose';
import dotenv from 'dotenv';

dotenv.config();

const connectToMongo = async () => {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected to MongoDB');
  } catch (err) {
    console.error('MongoDB connection error:', err);
    process.exit(1);
  }
};

export default connectToMongo;
