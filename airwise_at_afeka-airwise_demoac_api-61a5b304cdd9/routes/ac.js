import { Router } from 'express';
import { getACStateBySerial, setACStateBySerial } from '../controllers/acController.js';

const router = Router();
router.get('/:serial',      getACStateBySerial);
router.post('/:serial/set', setACStateBySerial);

export default router;