import mongoose from 'mongoose';
import dotenv from 'dotenv';
import fs from 'fs';
import path from 'path';
import AC from './models/AC.js';

dotenv.config();

const filePath = path.join('./acdata', 'acs.json');

console.log(`Reading AC data from: ${filePath}`);

if (!fs.existsSync(filePath)) {
  console.error(`File not found: ${filePath}`);
  process.exit(1);
}
async function seedDatabase() {
  try {
    await mongoose.connect(process.env.MONGODB_URI);
    console.log('Connected to MongoDB');

    const existing = await AC.find();
    if (existing.length > 0) {
      console.log('AC collection already contains data. Skipping seed.');
      return;
    }

    const rawData = fs.readFileSync(filePath, 'utf-8');
    const acList = JSON.parse(rawData);

    await AC.insertMany(acList);
    console.log(`Inserted ${acList.length} AC records.`);
  } catch (err) {
    console.error('Error during seeding:', err);
  } finally {
    mongoose.disconnect();
  }
}

seedDatabase();
