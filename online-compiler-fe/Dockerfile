# --- Build stage ---
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./
# --legacy-peer-deps: ngx-monaco-editor-v2@20 declares a peer on @angular/common@20,
# while this project is still on Angular 19; this mirrors how the lockfile was generated.
RUN npm ci --legacy-peer-deps

COPY . .
RUN npx ng build --configuration production

# --- Serve stage ---
FROM nginx:1.27-alpine AS serve

# Backend host/port are substituted into the nginx config at container start
# (via the official nginx image's envsubst templating) so they can be
# overridden with `-e BACKEND_HOST=... -e BACKEND_PORT=...` at `docker run`.
ENV BACKEND_HOST=online-compiler-be
ENV BACKEND_PORT=9191

COPY nginx.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist/online-compiler-fe/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
