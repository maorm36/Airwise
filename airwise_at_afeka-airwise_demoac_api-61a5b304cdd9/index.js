import app from './app.js';
import connectToMongo from './db.js';
import { startRandomMotionSimulation } from './controllers/acController.js';

const PORT = process.env.PORT || 3001;

async function startServer() {
  await connectToMongo();
  startRandomMotionSimulation();

  app.listen(PORT, () => {
    console.log(`Server is listening on port ${PORT}`);
  });
}

startServer();
