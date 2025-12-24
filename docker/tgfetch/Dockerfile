FROM node:22-slim

ENV NODE_OPTIONS="--max-old-space-size=48"

WORKDIR /app

COPY services/tgfetch/package*.json ./
RUN apt-get update && apt-get install -y ffmpeg && \
    apt-get clean && rm -rf /var/lib/apt/lists/* && \
    npm install --omit=dev

COPY services/tgfetch/ ./

EXPOSE 4001
CMD ["node", "server.js"]
