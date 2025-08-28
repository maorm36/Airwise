import express from 'express';
import acRoutes from './routes/ac.js';

const app = express();
app.use(express.json());
app.use('/api/ac', acRoutes);

export default app;