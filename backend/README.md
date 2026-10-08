# Florvvia — Full-stack Shop (Java Spring Boot + Responsive Frontend)

Handmade pipe-cleaner flowers, bouquets, keychains.

## Run
```bash
cd backend
mvn spring-boot:run
```
Open http://localhost:8080

- H2 console: http://localhost:8080/h2-console (JDBC `jdbc:h2:file:./data/florvvia`, user `sa`)
- Admin login: `admin@florvvia.in` / `admin123`
- WhatsApp number configurable in `application.properties` (`app.whatsapp-number`)

## Features
- Responsive shop: search, category/tag filter, product detail, reviews
- Cart (DB for logged-in, localStorage for guests), wishlist
- Login/logout (JWT + BCrypt), register
- Address book with default delivery location + pincode
- Checkout: COD / UPI / Razorpay (demo) / WhatsApp confirm — every order generates a `wa.me` link with order details
- Admin (`/admin.html`): stats, add/edit/hide products (with image upload to `/uploads`), all orders + status/payment updates, all customers, all reviews
- Customers can only see products + reviews + own orders. All DB/orders/users visible to admin only.
- Demo mode: frontend works without backend using built-in products (for GitHub Pages). Full features need backend running.

## API
- `GET /api/products?category=&search=&tag=` , `GET /api/products/{id}`
- `POST /api/auth/register|login`, `GET /api/auth/me`
- `GET/POST /api/cart/*`, `/api/wishlist/toggle`
- `GET/POST/PUT/DELETE /api/addresses`
- `POST /api/orders`, `GET /api/orders/mine`
- `GET /api/reviews/product/{id}`, `POST /api/reviews`
- `GET /api/admin/stats|orders|users|reviews`, `POST /api/admin/orders/{id}/status`
- `POST /api/upload` (admin, multipart image)
- `GET /api/config` (whatsapp, delivery fees)

## Deploy notes
- `mvn package` → `target/florvvia-backend-1.0.0.jar` serves frontend + API in one jar.
- For Razorpay live: add Razorpay key in `checkout.html` + verify signature server-side (currently demo mode).
- Change `app.jwt.secret` in production.
