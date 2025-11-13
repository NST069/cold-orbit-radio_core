FROM node:22-alpine

WORKDIR /app

COPY services/rotation/package*.json ./
RUN npm install --omit=dev
COPY services/rotation .

EXPOSE 4002
CMD ["node", "server.js"]