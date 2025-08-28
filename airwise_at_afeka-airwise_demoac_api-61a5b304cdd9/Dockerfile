# Dockerfile
FROM node:20

WORKDIR /app

# Copy package files first and install deps
COPY package*.json ./
RUN npm install

# Then copy rest of the files
COPY . .

EXPOSE 3001

CMD ["npm", "start"]
