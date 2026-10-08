// Florvvia common: API + auth + cart/wishlist (works with backend, falls back to demo mode)
const API = {
  base: '',
  token() { return localStorage.getItem('florvvia_token') || ''; },
  user() { try { return JSON.parse(localStorage.getItem('florvvia_user') || 'null'); } catch { return null; } },
  isAdmin() { const u = this.user(); return u && (u.role === 'ADMIN'); },
  async req(path, opts = {}) {
    const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
    if (this.token()) headers['Authorization'] = 'Bearer ' + this.token();
    const res = await fetch(this.base + path, { ...opts, headers });
    const text = await res.text();
    let data = null;
    try { data = text ? JSON.parse(text) : null; } catch { data = text; }
    if (!res.ok) throw new Error((data && data.error) || ('Request failed: ' + res.status));
    return data;
  },
  get(p) { return this.req(p); },
  post(p, b) { return this.req(p, { method: 'POST', body: JSON.stringify(b || {}) }); },
  put(p, b) { return this.req(p, { method: 'PUT', body: JSON.stringify(b || {}) }); },
  del(p) { return this.req(p, { method: 'DELETE' }); },
};

const DEMO_PRODUCTS = [
  { id: 1, name: 'Pipe-cleaner Purple Lily', category: 'flowers', price: 99, tag: 'New', code: 'FL-LILY-PURPLE-01', description: 'Elegant handmade purple lily.', images: ['images/flowers/lily-purple1.jpg'], ratingAvg: 4.8, ratingCount: 12 },
  { id: 2, name: 'Pipe-cleaner Cherry Lily', category: 'flowers', price: 119, tag: 'Bestseller', code: 'FL-LILY-CHERRY-01', description: 'Cherry-colored lily.', images: ['images/flowers/lily-cherry1.jpg'], ratingAvg: 4.9, ratingCount: 20 },
  { id: 3, name: 'Pipe-cleaner Pink Lily', category: 'flowers', price: 119, tag: 'New', code: 'FL-LILY-PINK-01', description: 'Pink lily.', images: ['images/flowers/lily-pink1.jpg'], ratingAvg: 4.7, ratingCount: 8 },
  { id: 4, name: 'Pipe-cleaner Sakura Flower', category: 'flowers', price: 99, tag: 'New', code: 'FL-SAKURA-01', description: 'Sakura flower.', images: ['images/flowers/sakura1.jpg'], ratingAvg: 4.6, ratingCount: 5 },
  { id: 5, name: 'Pipe-cleaner Sunflower', category: 'flowers', price: 149, tag: 'New', code: 'FL-SUN-01', description: 'Cheerful sunflower.', images: ['images/flowers/sunflower1.jpg'], ratingAvg: 4.8, ratingCount: 15 },
  { id: 6, name: 'Sunflower Pot', category: 'flowers', price: 249, tag: 'Bestseller', code: 'FL-POT-SUN-01', description: 'Desk decor pot.', images: ['images/flowers/sunflower-pot1.jpg'], ratingAvg: 5.0, ratingCount: 22 },
  { id: 7, name: 'Purple Lily & Tulip Bouquet', category: 'bouquets', price: 799, tag: 'New', code: 'FL-BOUQUET-01', description: 'Elegant bouquet.', images: ['images/bouquets/purple-lily-tulip1.jpg'], ratingAvg: 4.9, ratingCount: 9 },
  { id: 8, name: 'Red Rose Bouquet', category: 'bouquets', price: 799, tag: 'Bestseller', code: 'FL-BOUQUET-02', description: 'Rose bouquet.', images: ['images/bouquets/bouquet1.jpg'], ratingAvg: 4.8, ratingCount: 7 },
  { id: 9, name: 'Heart Keychain', category: 'keychains', price: 99, tag: 'New', code: 'FL-KEY-HEART-01', description: 'Crochet heart.', images: ['images/keychains/heart1.jpg'], ratingAvg: 4.7, ratingCount: 11 },
  { id: 10, name: 'Star Keychain', category: 'keychains', price: 99, tag: 'New', code: 'FL-KEY-STAR-01', description: 'Crochet star.', images: ['images/keychains/star1.jpg'], ratingAvg: 4.5, ratingCount: 4 },
  { id: 11, name: 'Bow Keychain', category: 'keychains', price: 99, tag: 'New', code: 'FL-KEY-BOW-01', description: 'Crochet bow.', images: ['images/keychains/k1.jpg'], ratingAvg: 4.6, ratingCount: 6 },
];

let BACKEND_OK = true;
let SHOP_CONFIG = { whatsappNumber: '917417566249', freeDeliveryAbove: 999, deliveryFee: 49, upiId: 'florvvia@upi' };
let _prodsCache = null, _prodsCacheAt = 0, _prodsInflight = null;

async function loadConfig() {
  try { const c = await API.get('/api/config'); SHOP_CONFIG = { ...SHOP_CONFIG, ...c }; }
  catch { BACKEND_OK = BACKEND_OK; }
}

async function fetchProducts(params = {}) {
  const q = new URLSearchParams(params).toString();
  const now = Date.now();
  // serve repeat loads from a 60s in-memory cache (one shared request, no waterfall)
  if (!q && _prodsCache && now - _prodsCacheAt < 60000) { BACKEND_OK = true; return _prodsCache; }
  if (!q && _prodsInflight) { try { return await _prodsInflight; } catch {} }
  try {
    let list;
    if (!q) {
      _prodsInflight = API.get('/api/products');
      try { list = await _prodsInflight; }
      finally { _prodsInflight = null; }
    } else {
      list = await API.get('/api/products?' + q);
    }
    BACKEND_OK = true;
    if (!q) { _prodsCache = list; _prodsCacheAt = now; }
    return list;
  } catch {
    BACKEND_OK = false;
    let list = [...DEMO_PRODUCTS];
    if (params.category && params.category !== 'all') list = list.filter(p => p.category === params.category);
    if (params.search) { const s = params.search.toLowerCase(); list = list.filter(p => (p.name + ' ' + (p.description || '')).toLowerCase().includes(s)); }
    if (params.tag) list = list.filter(p => p.tag === params.tag);
    return list;
  }
}

// ---- local guest cart/wishlist (used when logged out or backend down) ----
function localCart() { try { return JSON.parse(localStorage.getItem('florvvia_cart') || '[]'); } catch { return []; } }
function saveLocalCart(c) { localStorage.setItem('florvvia_cart', JSON.stringify(c)); updateBadges(); }
function localWish() { try { return JSON.parse(localStorage.getItem('florvvia_wish') || '[]'); } catch { return []; } }
function saveLocalWish(w) { localStorage.setItem('florvvia_wish', JSON.stringify(w)); updateBadges(); }

async function getCart() {
  const u = API.user();
  if (u && BACKEND_OK) {
    try { return await API.get('/api/cart'); } catch { return []; }
  }
  const ids = localCart();
  const prods = await fetchProducts();
  return ids.map(ci => {
    const p = prods.find(x => String(x.id) === String(ci.productId));
    return p ? { product: p, qty: ci.qty } : null;
  }).filter(Boolean);
}

async function addToCart(productId, qty = 1) {
  const u = API.user();
  if (u && BACKEND_OK) {
    try { await API.post('/api/cart/add', { productId, qty }); toast('Added to cart 🛒'); updateBadges(); return; }
    catch (e) { toast(e.message); return; }
  }
  const c = localCart();
  const ex = c.find(x => String(x.productId) === String(productId));
  if (ex) ex.qty = Math.min(99, ex.qty + qty); else c.push({ productId, qty });
  saveLocalCart(c);
  toast('Added to cart 🛒');
}

async function getWishlist() {
  const u = API.user();
  if (u && BACKEND_OK) {
    try { return await API.get('/api/wishlist'); } catch { return []; }
  }
  const ids = localWish();
  const prods = await fetchProducts();
  return ids.map(id => prods.find(x => String(x.id) === String(id))).filter(Boolean).map(p => ({ product: p }));
}

async function toggleWishlist(productId) {
  const u = API.user();
  if (u && BACKEND_OK) {
    try {
      const r = await API.post('/api/wishlist/toggle', { productId });
      toast(r.added ? 'Saved to wishlist ♡' : 'Removed from wishlist');
      updateBadges(); return r.added;
    } catch (e) { toast(e.message); return false; }
  }
  const w = localWish();
  const i = w.findIndex(x => String(x) === String(productId));
  if (i >= 0) { w.splice(i, 1); toast('Removed from wishlist'); }
  else { w.push(productId); toast('Saved to wishlist ♡'); }
  saveLocalWish(w);
  return i < 0;
}

async function updateBadges() {
  try {
    // fast path: logged out + nothing saved locally = badges are 0, no requests needed
    if (!API.user() && !localCart().length && !localWish().length) {
      document.querySelectorAll('[data-cart-count]').forEach(el => el.textContent = 0);
      document.querySelectorAll('[data-wish-count]').forEach(el => el.textContent = 0);
      return;
    }
    const [cart, wish] = await Promise.all([getCart().catch(() => []), getWishlist().catch(() => [])]);
    const cq = cart.reduce((s, ci) => s + (ci.qty || ci.quantity || 1), 0);
    document.querySelectorAll('[data-cart-count]').forEach(el => el.textContent = cq);
    document.querySelectorAll('[data-wish-count]').forEach(el => el.textContent = wish.length);
  } catch {}
}

function toast(msg) {
  document.querySelectorAll('.toast').forEach(t => t.remove());
  const d = document.createElement('div');
  d.className = 'toast'; d.textContent = msg;
  document.body.appendChild(d);
  setTimeout(() => d.remove(), 2200);
}

function money(n) { return '₹' + (Number(n) || 0).toLocaleString('en-IN'); }

const INSTA_URL = 'https://www.instagram.com/florvvia/';

// Display names for the 3 shop categories (slugs stay flowers/bouquets/keychains in the database)
function catLabel(c) {
  const k = (c || '').toLowerCase();
  if (k === 'keychains') return 'Keychains & Clips';
  if (k === 'bouquets') return 'Bouquets';
  if (k === 'flowers') return 'Flowers';
  return c || '';
}

function logout() {
  localStorage.removeItem('florvvia_token');
  localStorage.removeItem('florvvia_user');
  location.href = 'index.html';
}

function headerHTML(active = 'home') {
  const u = API.user();
  return `
  <div class="flex items-center justify-between px-3 sm:px-6 py-2 sm:py-3 border-b border-pink-100">
    <a href="index.html" class="text-xl sm:text-2xl font-semibold text-pink-500">florvvia</a>
    <div class="hidden md:flex flex-1 max-w-md mx-6 gap-2">
      <input id="globalSearch" placeholder="Search flowers, bouquets, keychains & clips…" class="flex-1 border border-pink-200 rounded-full px-4 py-2 text-sm" />
      <button onclick="submitHeaderSearch('globalSearch')" aria-label="Search" class="bg-pink-500 text-white rounded-full w-9 h-9 flex-shrink-0 hover:bg-pink-600">🔍</button>
    </div>
    <div class="flex items-center gap-3 sm:gap-4">
      <a href="account.html?tab=wishlist" class="relative text-lg sm:text-xl text-gray-600 hover:text-pink-500" aria-label="Wishlist">♡<span data-wish-count class="absolute -top-2 -right-2 bg-pink-500 text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center">0</span></a>
      ${u ? `<a href="account.html" class="text-xs sm:text-sm font-medium text-gray-700 hover:text-pink-500 max-w-[70px] sm:max-w-[90px] truncate">${u.name.split(' ')[0]}</a>
             <button onclick="logout()" class="text-[11px] sm:text-xs text-gray-500 hover:text-pink-500">Logout</button>`
          : `<a href="login.html" class="text-lg sm:text-xl text-gray-600 hover:text-pink-500" aria-label="Account">👤</a>`}
      ${u && u.role === 'ADMIN' ? `<a href="admin.html" class="text-[11px] sm:text-xs bg-pink-500 text-white px-2.5 sm:px-3 py-1 rounded-full">Admin</a>` : ''}
      <a href="cart.html" class="relative text-lg sm:text-xl text-gray-600 hover:text-pink-500" aria-label="Cart">🛒<span data-cart-count class="absolute -top-2 -right-2 bg-pink-500 text-white text-[10px] w-4 h-4 rounded-full flex items-center justify-center">0</span></a>
    </div>
  </div>
  <div class="md:hidden px-3 py-2 flex gap-2">
    <input id="globalSearchM" placeholder="Search flowers, bouquets…" class="flex-1 min-w-0 border border-pink-200 rounded-full px-4 py-2 text-sm" />
    <button onclick="submitHeaderSearch('globalSearchM')" class="bg-pink-500 text-white text-sm font-medium rounded-full px-4 hover:bg-pink-600 flex-shrink-0">Go</button>
  </div>
  <nav class="flex gap-4 sm:gap-5 px-3 sm:px-6 py-2 text-[13px] sm:text-sm overflow-x-auto no-scrollbar bg-white">
    <a href="index.html" class="whitespace-nowrap ${active === 'home' ? 'text-pink-500 font-semibold' : 'text-gray-600'}">Home</a>
    <a href="index.html?cat=flowers#shop" class="whitespace-nowrap text-gray-600">Flowers</a>
    <a href="index.html?cat=bouquets#shop" class="whitespace-nowrap text-gray-600">Bouquets</a>
    <a href="index.html?cat=keychains#shop" class="whitespace-nowrap text-gray-600">Keychains & Clips</a>
    <a href="index.html?tag=Bestseller#shop" class="whitespace-nowrap text-gray-600">Best Sellers</a>
    <a href="index.html?tag=New#shop" class="whitespace-nowrap text-gray-600">New Arrivals</a>
    <a href="account.html?tab=orders" class="whitespace-nowrap text-gray-600">My Orders</a>
  </nav>`;
}

function submitHeaderSearch(inputId) {
  const el = document.getElementById(inputId);
  const q = (el ? el.value : '').trim();
  location.href = 'index.html' + (q ? '?search=' + encodeURIComponent(q) : '') + '#shop';
}

function mountHeader(active) {
  const h = document.getElementById('siteHeader');
  if (h) h.innerHTML = headerHTML(active);
  const bind = (id) => {
    const el = document.getElementById(id);
    if (el) el.addEventListener('keydown', e => {
      if (e.key === 'Enter') submitHeaderSearch(id);
    });
  };
  bind('globalSearch'); bind('globalSearchM');
  updateBadges();
}

document.addEventListener('DOMContentLoaded', async () => {
  await loadConfig();
  mountHeader(window.HEADER_ACTIVE || 'home');
});
