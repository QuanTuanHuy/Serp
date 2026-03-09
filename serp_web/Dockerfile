# --- Stage 1: Install dependencies ---
FROM node:20-alpine AS deps

RUN apk add --no-cache libc6-compat

WORKDIR /app

COPY package.json package-lock.json* ./

RUN npm ci --only=production=false

# --- Stage 2: Build the Next.js app ---
FROM node:20-alpine AS builder

WORKDIR /app

COPY --from=deps /app/node_modules ./node_modules
COPY . .

ARG NEXT_PUBLIC_API_BASE_URL=https://api.serp.texkis.com
ENV NEXT_PUBLIC_API_BASE_URL=${NEXT_PUBLIC_API_BASE_URL}

ARG NEXT_PUBLIC_WS_URL=wss://api.serp.texkis.com/ws
ENV NEXT_PUBLIC_WS_URL=${NEXT_PUBLIC_WS_URL}

ENV NEXT_TELEMETRY_DISABLED=1

RUN npm run build

# --- Stage 3: Production runner (minimal image) ---
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV NEXT_TELEMETRY_DISABLED=1
ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

ARG NEXT_PUBLIC_API_BASE_URL=https://api.serp.texkis.com
ENV NEXT_PUBLIC_API_BASE_URL=${NEXT_PUBLIC_API_BASE_URL}

ARG NEXT_PUBLIC_WS_URL=wss://api.serp.texkis.com/ws
ENV NEXT_PUBLIC_WS_URL=${NEXT_PUBLIC_WS_URL}


COPY --from=builder /app/public ./public
COPY --from=builder /app/.next/standalone ./
COPY --from=builder /app/.next/static ./.next/static

EXPOSE 3000

CMD ["node", "server.js"]
