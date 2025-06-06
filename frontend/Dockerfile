FROM node:18 AS builder

WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration=production

FROM nginx:stable-alpine
# COPY wait-for-backend.sh /usr/local/bin/wait-for-backend.sh
# RUN chmod +x /usr/local/bin/wait-for-backend.sh

RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# ENTRYPOINT [ "/usr/local/bin/wait-for-backend.sh", "nginx", "-g", "daemon off;" ]
EXPOSE 80

