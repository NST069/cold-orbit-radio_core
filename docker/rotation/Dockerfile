FROM node:22-slim

ENV NODE_OPTIONS="--max-old-space-size=32"

WORKDIR /app

RUN apt-get update

COPY services/rotation/package*.json ./
RUN npm install --omit=dev
COPY services/rotation .

EXPOSE 4002
CMD ["node", "server.js"]
