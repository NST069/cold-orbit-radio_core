FROM node:22-slim

WORKDIR /app

RUN apt-get update

COPY services/gateway/package*.json ./
RUN npm install --omit=dev
COPY services/gateway/ .

EXPOSE 3000
CMD ["node", "server.js"]