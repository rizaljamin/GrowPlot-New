FROM node:22-slim
WORKDIR /app
COPY package.json /app/package.json
RUN npm install --omit=dev --no-audit --no-fund
COPY server.js /app/server.js
ENV NODE_ENV=production PORT=8080
EXPOSE 8080
CMD ["npm", "start"]
