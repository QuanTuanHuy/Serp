# Build stage
FROM golang:1.25-alpine AS builder

WORKDIR /app
COPY go.mod go.sum ./
RUN go mod download && go mod tidy

COPY . .

RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -o /app/main ./src

# Final stage
FROM alpine:latest

WORKDIR /app

COPY --from=builder /app/main .

COPY --from=builder /app/src/config ./config

EXPOSE 8083

CMD ["./main"]