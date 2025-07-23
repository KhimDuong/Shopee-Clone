# Shopee Clone - Backend

This is the backend of a simple e-commerce application inspired by Shopee. It provides RESTful APIs for user authentication, product management, order processing, and more.

---

## 🚀 Features

* User Registration & Login (with hashed passwords)
* JWT-based Authentication
* Role-based Access Control (Admin, Seller, Buyer)
* CRUD operations for products
* Order creation and management
* RESTful API endpoints for interaction
* Security best practices (input validation, password hashing, authorization middleware)
* File upload support (e.g. product images)

---

## 🛠 Tech Stack

* Node.js
* Express.js
* JSON Web Token (JWT)
* bcrypt
* Multer (for image uploads)
* dotenv

---

## 📁 Folder Structure

```
backend/
├── controllers/
├── models/
├── routes/
├── middleware/
├── uploads/
├── .env
├── server.js
```

---

## 📦 Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/your-username/shopee-clone-backend.git
cd shopee-clone-backend
```

### 2. Install dependencies

```bash
npm install
```

### 3. Set up environment variables

Create a `.env` file in the root directory:

```env
PORT=5000
MONGO_URI=your_mongodb_uri
JWT_SECRET=your_jwt_secret
```

### 4. Start the server

```bash
npm start
```

---

## 📫 Frontend Repository

👉 [Frontend GitHub Repository](https://github.com/KhimDuong/Shopee_Web_clone)

---

## 📮 API Endpoints (Sample)

| Method | Endpoint      | Description         |
| ------ | ------------- | ------------------- |
| POST   | /api/register | Register a new user |
| POST   | /api/login    | Login and get token |
| GET    | /api/products | Get all products    |
| POST   | /api/products | Add new product     |
| POST   | /api/orders   | Place an order      |

---

## ✅ Security & Authentication

* Passwords are hashed using bcrypt
* Authentication is done via JWT
* Protected routes require a valid token

---

## 📌 Future Improvements

* Add payment gateway (e.g. MoMo integration)
* Implement Google/Facebook login
* Enable real-time chat using Socket.IO
* Admin panel for product/user/order management


Built with ❤️ for educational purposes.
