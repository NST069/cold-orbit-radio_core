FROM node:22-slim

WORKDIR /app

RUN apt-get update && apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y ffmpeg liquidsoap && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

COPY services/liquidsoap/package*.json ./
RUN npm install --omit=dev
COPY services/liquidsoap/ .

EXPOSE 4000
CMD ["node", "server.js"]