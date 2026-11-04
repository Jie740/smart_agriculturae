FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm config set registry https://registry.npmmirror.com \
    && npm ci

COPY . .

RUN npm run build

FROM nginx:stable-alpine

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

# 将 Nginx PID 文件放到 /tmp
RUN sed -i 's#pid\s*/run/nginx.pid;#pid /tmp/nginx.pid;#' /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]


