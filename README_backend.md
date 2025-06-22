# Shopee Clone Backend

This is the backend of a Shopee-like e-commerce web application built with Spring Boot.

## 🔧 Tech Stack
- Java 17 + Spring Boot
- Spring Security + JWT
- PostgreSQL + Spring Data JPA
- REST API

## 📦 Features
- User registration & login (JWT-based)
- Product listing & creation
- Role-based access (user/admin-ready)
- Secure API with token validation

## 🛠️ Run Locally
```bash
./mvnw clean install
./mvnw spring-boot:run
```

## ⚙️ CORS Config
Supports Vercel, Postman, local IP (`http://192.168.x.x:3000`)

## 🔐 API Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | User login |
| POST | `/api/auth/register` | Register new user |
| GET  | `/products` | Get product list |
| POST | `/products` | Add new product (requires token) |

## 🌐 Deploy
- Render.com using Docker
- Uses UptimeRobot to prevent sleep