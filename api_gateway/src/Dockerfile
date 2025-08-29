# Build stage
FROM golang:1.24-alpine AS builder
ARG BUILD_VERSION
ARG BUILD_COMMIT_HASH
ARG BUILD_TIME

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download && go mod tidy

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app/main ./src

# Final stage
FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/main .

COPY --from=builder /app/src/.env .

COPY --from=builder /app/src/config ./config

EXPOSE 8082

CMD ["./main"]