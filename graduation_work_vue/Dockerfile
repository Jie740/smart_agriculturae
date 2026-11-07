FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
<<<<<<< HEAD
RUN npm config set registry https://registry.npmmirror.com && npm ci
=======

RUN npm config set registry https://registry.npmmirror.com \
    && npm ci
>>>>>>> origin/master

COPY . .

<<<<<<< HEAD
=======
RUN npm run build

>>>>>>> origin/master
FROM nginx:stable-alpine

COPY --from=build /app/dist /usr/share/nginx/html

COPY nginx.conf /etc/nginx/conf.d/default.conf

<<<<<<< HEAD
# 把默认 pid 路径从 /run 改到 /tmp（CentOS 7 下 /run 不可写）
RUN sed -i 's|^pid[[:space:]]*/run/nginx.pid;|pid /tmp/nginx.pid;|' /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
=======
# 将 Nginx PID 文件放到 /tmp
RUN sed -i 's#pid\s*/run/nginx.pid;#pid /tmp/nginx.pid;#' /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]


>>>>>>> origin/master
