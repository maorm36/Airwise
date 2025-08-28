# Run Smart AC API (Node.js + MongoDB) with Docker

## Prerequisites
- Docker Desktop installed
- Git installed

---

## 📂 Folder Structure (expected)
smart-ac-api/
├── Dockerfile
├── docker-compose.yml
├── package.json
├── app.js
├── seed.js
└── ...

---

## ▶️ How to Run

### 1. Open terminal and go to the project folder
cd smart-ac-api

### 2. Start the API and MongoDB with Docker
docker compose up --build

---

## What Happens:
- Installs dependencies
- Seeds the database (npm run seed)
- Starts the API server on http://localhost:3001
- MongoDB runs on port 27018

---

## 🛑 Stop the App
docker compose down

---

## 🐒 MongoDB Access (Optional)
Use MongoDB Compass and connect to:
mongodb://localhost:27018

---

## ⚠️ Notes
- The API depends on MongoDB, so it will wait until MongoDB is ready.
- Ensure port 3001 and 27018 are free before starting.
