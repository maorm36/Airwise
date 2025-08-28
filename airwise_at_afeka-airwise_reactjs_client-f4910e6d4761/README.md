# Run ReactJS Client (Vite) with Docker

## 📦 Prerequisites
- Docker Desktop installed
- Git installed

---

## 📂 Folder Structure (expected)
airwise_reactjs_client/
├── Dockerfile
├── docker-compose.yml
├── package.json
├── vite.config.js
├── src/
└── public/

---

## ▶️ How to Run (Development Mode)

### 1. Open terminal and go to the project folder
cd airwise_reactjs_client

### 2. Start the app with Docker
docker compose up --build

---

## 🌐 Access the App
Visit: http://localhost:5173

---

## 🛑 Stop the App
docker compose down

---

## 💡 Notes
- The container runs Vite dev server with hot reload.
- If changes aren't detected, try restarting Docker or your terminal.
