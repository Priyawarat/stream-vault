# Build Stage
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./

RUN npm install

COPY . .

RUN npm run build

# Production Stage

FROM node:22-alpine

WORKDIR /app

RUN npm i -g serve

ENV PATH="/usr/local/bin:$PATH"
COPY --from=build /app/dist ./dist

EXPOSE 3000

CMD ["serve", "-l", "3000", "-s", "dist"]
