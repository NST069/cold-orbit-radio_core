FROM node:22-bullseye

WORKDIR /app

COPY services/tgfetch/package*.json ./
RUN apt-get update 
RUN apt-get install -y ffmpeg
RUN npm install --omit=dev
COPY services/tgfetch/ ./

EXPOSE 4001
CMD ["node", "server.js"]