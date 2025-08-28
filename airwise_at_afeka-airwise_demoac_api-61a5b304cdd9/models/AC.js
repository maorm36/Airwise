import mongoose from 'mongoose';

const acSchema = new mongoose.Schema({
  serial: { type: String, required: true, unique: true },
  power: { type: Boolean, default: false },
  temperature: { type: Number, default: 24 },
  mode: { type: String, enum: ['AUTO', 'COOL', 'HEAT', 'FAN', 'DRY'], default: 'AUTO' },
  fanSpeed: { type: String, enum: ['AUTO', 'LOW', 'MEDIUM', 'HIGH'], default: 'AUTO' },
  motion: { type: Boolean, default: false }
});

const AC = mongoose.model('AC', acSchema);
export default AC;
