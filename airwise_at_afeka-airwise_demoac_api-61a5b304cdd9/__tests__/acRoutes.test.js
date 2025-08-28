// __tests__/acRoutes.test.js
import { jest, describe, it, expect } from '@jest/globals';
import request from 'supertest';

// 1) Mock fs before controllers load
jest.unstable_mockModule('fs', () => {
  const readFileSync  = jest.fn(() => JSON.stringify([
    { serial: 'S1', power: true,  temperature: 20, mode: 'cool' },
    { serial: 'S2', power: false, temperature: 25, mode: 'heat' }
  ]));
  const writeFileSync = jest.fn();
  return { default: { readFileSync, writeFileSync }, readFileSync, writeFileSync };
});

// 2) Import app (and controllers) after mock is registered
const { default: app } = await import('../app.js');

// 3) Grab the mocked writeFileSync for assertions
const { writeFileSync } = await import('fs');

describe('Smart AC API (/api/ac)', () => {
  describe('GET /api/ac/:serial', () => {
    it('200 + JSON when exists', async () => {
      const res = await request(app).get('/api/ac/S2');
      expect(res.status).toBe(200);
      expect(res.body).toEqual({
        message: 'Sucess',
        acState: { serial: 'S2', power: false, temperature: 25, mode: 'heat' }
      });
    });

    it('404 when not found', async () => {
      const res = await request(app).get('/api/ac/UNKNOWN');
      expect(res.status).toBe(404);
      expect(res.body).toEqual({
        message: 'AC not found',
        acState: null
      });
    });
  });

  describe('POST /api/ac/:serial/set', () => {
    it('400 on invalid power', async () => {
      const res = await request(app)
        .post('/api/ac/S1/set')
        .send({ power: 'yes' });
      expect(res.status).toBe(400);
      expect(res.body).toEqual({
        message: 'Invalid type for power. Expected boolean.'
      });
    });

    it('400 on out-of-range temperature', async () => {
      const res = await request(app)
        .post('/api/ac/S1/set')
        .send({ temperature: 10 });
      expect(res.status).toBe(400);
      expect(res.body).toEqual({
        message: 'Invalid type for temperature. Expected number between 16 and 30.'
      });
    });

    it('404 when serial missing', async () => {
      const res = await request(app)
        .post('/api/ac/NOPE/set')
        .send({ power: false });
      expect(res.status).toBe(404);
      expect(res.body).toEqual({
        message: 'AC not found',
        acState: null
      });
    });

    it('200 + writes file on valid input', async () => {
      const res = await request(app)
        .post('/api/ac/S1/set')
        .send({ power: false, temperature: 24, mode: 'cool' });

      expect(res.status).toBe(200);
      expect(res.body).toEqual({
        message: 'AC state updated',
        acState: { serial: 'S1', power: false, temperature: 24, mode: 'cool' }
      });

      expect(writeFileSync).toHaveBeenCalledWith(
        expect.any(String),
        JSON.stringify([
          { serial: 'S1', power: false, temperature: 24, mode: 'cool' },
          { serial: 'S2', power: false, temperature: 25, mode: 'heat' }
        ], null, 2),
        'utf8'
      );
    });
  });
});
