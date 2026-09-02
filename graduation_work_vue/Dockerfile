FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build


FROM nginx:stable-alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 创建 nginx 运行需要的目录并确保可写
RUN mkdir -p /tmp/nginx \
    && chmod 777 /tmp/nginx

EXPOSE 80

CMD ["nginx", "-g", "daemon off; pid /tmp/nginx.pid;"]