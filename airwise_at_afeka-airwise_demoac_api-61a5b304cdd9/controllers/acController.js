import AC from '../models/AC.js';

export async function getACStateBySerial(req, res) {
  try {
    const serial = req.params.serial;
    const ac = await AC.findOne({ serial });

    if (!ac) {
      return res.status(404).json({ message: 'AC not found', acState: null, code: 404 });
    }

    return res.json({ message: 'Success', acState: ac, code: 200 });
  } catch (err) {
    return res.status(500).json({ message: 'Server error', code: 500 });
  }
}

export async function setACStateBySerial(req, res) {
  const { serial } = req.params;
  const { power, temperature, mode, fanSpeed } = req.body;

  if (!serial || typeof serial !== 'string') {
    return res.status(400).json({ message: 'Invalid or missing serial number.', code: 400 });
  }

  if (power !== undefined && typeof power !== 'boolean') {
    return res.status(400).json({ message: 'Invalid type for power. Expected boolean.', code: 400 });
  }

  if (temperature !== undefined && (typeof temperature !== 'number' || temperature < 16 || temperature > 30)) {
    return res.status(400).json({ message: 'Invalid temperature. Must be between 16 and 30.', code: 400 });
  }

  if (mode !== undefined && !['AUTO', 'COOL', 'HEAT', 'FAN', 'DRY'].includes(mode)) {
    return res.status(400).json({ message: 'Invalid mode value.', code: 400 });
  }

  if (fanSpeed !== undefined && !['AUTO', 'LOW', 'MEDIUM', 'HIGH'].includes(fanSpeed)) {
    return res.status(400).json({ message: 'Invalid fanSpeed value.', code: 400 });
  }

  try {
    let ac = await AC.findOne({ serial });
    if (!ac) {
      return res.status(404).json({ message: 'AC not found', acState: null, code: 404 });
    }

    if (power !== undefined) ac.power = power;
    if (temperature !== undefined) ac.temperature = temperature;
    if (mode !== undefined) ac.mode = mode;
    if (fanSpeed !== undefined) ac.fanSpeed = fanSpeed;

    await ac.save();
    return res.json({ message: 'AC state updated', acState: ac, code: 200 });
  } catch (err) {
    return res.status(500).json({ message: 'Server error', code: 500 });
  }
}

export function startRandomMotionSimulation() {
  setInterval(async () => {
    const acList = await AC.find();
    const randomIndex = Math.floor(Math.random() * acList.length);
    const ac = acList[randomIndex];
    if (!ac || ac.serial === '1415161718') return;

    ac.motion = !ac.motion;
    await ac.save();
    console.log(`Motion for AC serial ${ac.serial} changed to ${ac.motion}`);
  }, Math.floor(Math.random() * 20000) + 10000);
}
