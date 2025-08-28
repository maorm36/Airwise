FROM node:20

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy the rest of the project
COPY . .

# Expose React's default dev server port
EXPOSE 5173

# Start the React dev server
CMD ["npm", "run", "dev", "--", "--host"]
