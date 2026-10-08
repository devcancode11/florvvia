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

## Go live (frontend on Netlify + backend on Render)

The Netlify site is frontend-only — login/cart/orders need the Java backend
running online too. Do this once:

**1. Deploy the backend (free)**
- Render → New → Blueprint → connect repo `devcancode11/florvvia` (uses `render.yaml`).
- Or manually: New → Web Service → Docker, root directory `backend`.
- Health check path: `/api/config`. It can take 2–5 min on first deploy.
- Free services sleep when idle — first visit after a while takes ~30–60s to wake up.
- Note the service URL, e.g. `https://florvvia-backend.onrender.com`.

**2. Persistent database (recommended)**
- Render's disk is ephemeral: without a real DB, products/orders/users reset on every restart.
- Create a free Postgres in Render, then set on the web service:
  - `SPRING_DATASOURCE_URL=jdbc:postgresql://HOST:5432/DATABASE`
  - `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`
  - `SPRING_DATASOURCE_DRIVER=org.postgresql.Driver`
  - `APP_JWT_SECRET` = any long random string (Render can generate it).
- Tables are created automatically on first boot; the admin account is seeded.

**3. Point the site at the backend**
- Edit `backend/src/main/resources/static/js/config.js`:
  `window.FLORVVIA_API_URL = 'https://florvvia-backend.onrender.com';`
- Copy the same file to `js/config.js` (both must match), commit, push.
- Netlify redeploys automatically. Login pill on `login.html` should turn green.
- Shortcut (no redeploy): open any page with `?api=https://your-backend-url` once — it saves in that browser.

**Notes**
- Uploaded product photos (`/uploads`) vanish on free redeploys — prefer pasting image URLs for products, or attach a persistent disk (paid).
- CORS is already open for `/api/**`, so Netlify ↔ Render works.

## Deploy notes
- `mvn package` → `target/florvvia-backend-1.0.0.jar` serves frontend + API in one jar.
- For Razorpay live: add Razorpay key in `checkout.html` + verify signature server-side (currently demo mode).
- Change `app.jwt.secret` in production (or set `APP_JWT_SECRET` env var).
