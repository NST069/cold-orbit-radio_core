FROM node:22-slim

ENV NODE_OPTIONS="--max-old-space-size=64"

WORKDIR /app

RUN apt-get update

COPY services/gateway/package*.json ./
RUN npm install --omit=dev
COPY services/gateway/ .

EXPOSE 3000
CMD ["node", "server.js"]
