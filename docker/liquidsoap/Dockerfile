FROM node:22-trixie

WORKDIR /app

COPY services/liquidsoap/package*.json ./
RUN apt-get update 
RUN apt-get install -y ffmpeg
RUN apt-get install -y liquidsoap 
RUN npm install --omit=dev
COPY services/liquidsoap .

EXPOSE 4000
CMD ["node", "server.js"]