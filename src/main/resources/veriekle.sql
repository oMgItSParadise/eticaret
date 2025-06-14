INSERT INTO users (username, email, password_hash)
SELECT 'seller1', 'seller1@example.com', 'x9y4ydXGc0l+DVcER2+balSfRP0tgx6iPzgxcEQ5fS5bLsqNPLa/hPFbV22QD5MP'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'seller1');

INSERT INTO users (username, email, password_hash)
SELECT 'customer1', 'customer1@example.com', '87CkTWW1xEVIP7N77i6gL0LLhfPBhP5D2To624QES9jwv9cjsZHqNecKrhe6KtwR'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer1');

INSERT INTO users (username, email, password_hash)
SELECT 'seller1', 'seller1@example.com', 'x9y4ydXGc0l+DVcER2+balSfRP0tgx6iPzgxcEQ5fS5bLsqNPLa/hPFbV22QD5MP'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'seller1');

INSERT INTO users (username, email, password_hash)
SELECT 'customer1', 'customer1@example.com', '87CkTWW1xEVIP7N77i6gL0LLhfPBhP5D2To624QES9jwv9cjsZHqNecKrhe6KtwR'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'customer1');

-- Insert only SELLER and CUSTOMER roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_SELLER'), ('ROLE_CUSTOMER');

-- Assign roles to users
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r 
WHERE (u.username = 'seller1' AND r.name = 'ROLE_SELLER')
   OR (u.username = 'customer1' AND r.name = 'ROLE_CUSTOMER');

INSERT INTO categories (name, description, parent_id)
SELECT 'Elektronik', 'Elektronik ürünler', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Elektronik');

INSERT INTO categories (name, description, parent_id)
SELECT 'Giyim', 'Giyim ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Giyim');

INSERT INTO categories (name, description, parent_id)
SELECT 'Ev & Yaşam', 'Ev ve yaşam ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Ev & Yaşam');

INSERT INTO categories (name, description, parent_id)
SELECT 'Bilgisayar', 'Dizüstü ve masaüstü bilgisayarlar', (SELECT id FROM categories WHERE name = 'Elektronik')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Bilgisayar');

INSERT INTO categories (name, description, parent_id)
SELECT 'Telefon', 'Akıllı telefonlar', (SELECT id FROM categories WHERE name = 'Elektronik')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Telefon');

INSERT INTO categories (name, description, parent_id)
SELECT 'Tablet', 'Tablet bilgisayarlar', (SELECT id FROM categories WHERE name = 'Elektronik')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Tablet');

INSERT INTO categories (name, description, parent_id)
SELECT 'Erkek Giyim', 'Erkek giyim ürünleri', (SELECT id FROM categories WHERE name = 'Giyim')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Erkek Giyim');

INSERT INTO categories (name, description, parent_id)
SELECT 'Aksesuar', 'Aksesuar ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Aksesuar');

INSERT INTO categories (name, description, parent_id)
SELECT 'Ofis', 'Ofis ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Ofis');

INSERT INTO categories (name, description, parent_id)
SELECT 'Çocuk Oyuncakları', 'Çocuklar için oyuncaklar', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Çocuk Oyuncakları');

INSERT INTO categories (name, description, parent_id)
SELECT 'Kadın Giyim', 'Kadın giyim ürünleri', (SELECT id FROM categories WHERE name = 'Giyim')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kadın Giyim');

INSERT INTO categories (name, description, parent_id)
SELECT 'Kadın Ayakkabı', 'Kadın ayakkabı ürünleri', (SELECT id FROM categories WHERE name = 'Kadın Giyim')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kadın Ayakkabı');

INSERT INTO categories (name, description, parent_id)
SELECT 'Erkek Ayakkabı', 'Erkek ayakkabı ürünleri', (SELECT id FROM categories WHERE name = 'Erkek Giyim')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Erkek Ayakkabı');

INSERT INTO categories (name, description, parent_id)
SELECT 'Spor Giyim', 'Spor giyim ürünleri', (SELECT id FROM categories WHERE name = 'Giyim')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Spor Giyim');

INSERT INTO categories (name, description, parent_id)
SELECT 'Çanta', 'Çanta ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Çanta');

INSERT INTO categories (name, description, parent_id)
SELECT 'Saat & Aksesuar', 'Saat ve aksesuar ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Saat & Aksesuar');

INSERT INTO categories (name, description, parent_id)
SELECT 'Elektronik Aksesuar', 'Elektronik aksesuarlar', (SELECT id FROM categories WHERE name = 'Elektronik')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Elektronik Aksesuar');

INSERT INTO categories (name, description, parent_id)
SELECT 'Ev Tekstili', 'Ev tekstil ürünleri', (SELECT id FROM categories WHERE name = 'Ev & Yaşam')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Ev Tekstili');

INSERT INTO categories (name, description, parent_id)
SELECT 'Mobilya', 'Mobilya ürünleri', (SELECT id FROM categories WHERE name = 'Ev & Yaşam')
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Mobilya');

INSERT INTO categories (name, description, parent_id)
SELECT 'Kitap', 'Kitap ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kitap');

INSERT INTO categories (name, description, parent_id)
SELECT 'Spor Ekipmanları', 'Spor ekipmanları', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Spor Ekipmanları');

INSERT INTO categories (name, description, parent_id)
SELECT 'Kozmetik', 'Kozmetik ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Kozmetik');

INSERT INTO categories (name, description, parent_id)
SELECT 'Takı', 'Takı ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Takı');

INSERT INTO categories (name, description, parent_id)
SELECT 'Bebek', 'Bebek ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Bebek');

INSERT INTO categories (name, description, parent_id)
SELECT 'Oyuncak', 'Oyun ve oyuncak ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Oyuncak');

INSERT INTO categories (name, description, parent_id)
SELECT 'Süpermarket', 'Süpermarket ürünleri', NULL
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE name = 'Süpermarket');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Logitech Mouse', 'Kablosuz mouse', 350.00, 20,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Aksesuar'),
  'https://m.media-amazon.com/images/I/61UxfXTUyvL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Logitech Mouse');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Ofis Sandalyesi', 'Ergonomik sandalye', 1200.00, 5,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Ofis'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Ofis Sandalyesi');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Oyuncak Araba', 'Uzaktan kumandalı', 250.00, 15,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Çocuk Oyuncakları'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Oyuncak Araba');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Kadın Topuklu Ayakkabı', 'Siyah, 38 numara', 499.99, 12,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Kadın Ayakkabı'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kadın Topuklu Ayakkabı');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Samsung QLED 4K TV', '55 inç, Akıllı TV', 24999.99, 8,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Elektronik'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Samsung QLED 4K TV');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'iPhone 13 Pro', '128GB, Gümüş', 34999.99, 12,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Elektronik'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'iPhone 13 Pro');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Yemek Takımı', '32 Parça, Porselen', 899.99, 15,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Ev & Yaşam'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Yemek Takımı');

INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Yatak Örtüsü Takımı', 'Pamuklu, Çift Kişilik', 499.99, 20,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Ev & Yaşam'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Yatak Örtüsü Takımı');

-- Giyim - Kadın Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Kadın Kışlık Mont', 'Slim Fit, Siyah', 1299.99, 10,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Kadın Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kadın Kışlık Mont');

-- Giyim - Erkek Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Erkek Gömlek', 'Beyaz, Slim Fit', 299.99, 25,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Erkek Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Erkek Gömlek');

-- Giyim - Spor Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Spor Eşofman Takımı', 'Siyah, L Beden', 599.99, 15,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Spor Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Spor Eşofman Takımı');

-- Giyim - Kadın Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Kadın Elbise', 'Yazlık, Desenli', 499.99, 12,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Kadın Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kadın Elbise');

-- Giyim - Erkek Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Erkek Pantolon', 'Kot, Mavi', 399.99, 18,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Erkek Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Erkek Pantolon');

-- Giyim - Spor Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Spor Ayakkabı', 'Koşu, Siyah', 799.99, 20,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Spor Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Spor Ayakkabı');

-- Erkek Ayakkabı
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Erkek Spor Ayakkabı', 'Siyah, 42 numara', 899.99, 15,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Erkek Ayakkabı'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Erkek Spor Ayakkabı');

-- Spor Giyim
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Spor Eşofman Takımı', 'Siyah, L beden', 599.99, 20,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Spor Giyim'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Spor Eşofman Takımı');

-- Çanta
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Sırt Çantası', 'Su geçirmez, 20L', 349.99, 25,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Çanta'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Sırt Çantası');

-- Saat & Aksesuar
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Kol Saati', 'Klasik erkek saati', 1299.99, 10,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Saat & Aksesuar'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kol Saati');

-- Elektronik Aksesuar
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Kablosuz Kulaklık', 'Bluetooth 5.0, 20 saat pil ömrü', 749.99, 30,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Elektronik Aksesuar'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Kablosuz Kulaklık');

-- Ev Tekstili
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Yatak Örtüsü Takımı', 'Pamuklu, 200x220 cm', 299.99, 15,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Ev Tekstili'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Yatak Örtüsü Takımı');

-- Mobilya
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Çalışma Masası', 'Ahşap, 120x60 cm', 1999.99, 8,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Mobilya'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Çalışma Masası');

-- Kitap
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Küçük Prens', 'Antoine de Saint-Exupéry', 29.90, 50,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Kitap'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Küçük Prens');

-- Spor Ekipmanları
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Dambıl Seti', '2x5 kg, kauçuk kaplama', 349.99, 12,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Spor Ekipmanları'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Dambıl Seti');

-- Kozmetik
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Nemlendirici Krem', 'Hassas ciltler için', 149.99, 40,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Kozmetik'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Nemlendirici Krem');

-- Takı
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Gümüş Kolye', 'İnce zincir, küçük taş detaylı', 499.99, 18,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Takı'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Gümüş Kolye');

-- Bebek
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Bebek Beşiği', 'Ahşap, sallamalı', 1299.99, 6,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Bebek'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Bebek Beşiği');

-- Oyuncak
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Lego Klasik Kutu', '1500 parça, yaratıcı set', 599.99, 10,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Oyuncak'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Lego Klasik Kutu');

-- Süpermarket
INSERT INTO products (name, description, price, stock_quantity, seller_id, category_id, image_url)
SELECT 'Zeytinyağı', 'Naturel sızma, 1L', 149.99, 100,
  (SELECT id FROM users WHERE username = 'seller1'),
  (SELECT id FROM categories WHERE name = 'Süpermarket'),
  'https://m.media-amazon.com/images/I/71XW0N4lZcL._AC_UL320_.jpg'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Zeytinyağı');


UPDATE products
SET is_active = TRUE
WHERE is_active IS NULL;