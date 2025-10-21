# --- Stage 1: Build the Next.js app ---
FROM node:18-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./

# Cài dependencies (dùng npm ci để đảm bảo build reproducible)
RUN npm ci

# Copy toàn bộ mã nguồn
COPY . .

ENV NEXT_PUBLIC_API_BASE_URL=https://api.serp.texkis.com

# Build Next.js app (xuất ra thư mục .next)
RUN npm run build

# --- Stage 2: Tạo image production nhỏ gọn ---
FROM node:18-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production
ENV PORT=3000
ENV NEXT_PUBLIC_API_BASE_URL=https://api.serp.texkis.com

# Copy các file cần thiết từ stage builder
COPY --from=builder /app/public ./public
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/next.config.ts ./next.config.ts
# Nếu bạn dùng .env.production, có thể COPY thêm:
# COPY .env.production .env.production

EXPOSE 3000

# Chạy app ở chế độ production
CMD ["npm", "start"]
