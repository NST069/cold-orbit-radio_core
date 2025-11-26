FROM node:22-alpine

WORKDIR /app

RUN apk add --no-cache postgresql-client

COPY package*.json ./
RUN npm install --omit=dev

COPY . .

CMD ["sh", "-c", "npm run db:migrate && node server.js"]