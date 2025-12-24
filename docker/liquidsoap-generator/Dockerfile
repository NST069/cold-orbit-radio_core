FROM node:22-slim

ENV NODE_OPTIONS="--max-old-space-size=96"

WORKDIR /app

RUN apt-get update && apt-get install -y \
    bash \
    docker.io \
    sudo \
    liquidsoap \
    && rm -rf /var/lib/apt/lists/*

COPY services/liquidsoap-generator/package*.json ./
RUN npm install --omit=dev
COPY services/liquidsoap-generator/ .

RUN chmod +x ./checkRestart.sh

EXPOSE 4000
CMD ["node", "server.js"]
