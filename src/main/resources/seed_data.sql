-- PVR Prime Naturals - Comprehensive Database Seed Script
SET FOREIGN_KEY_CHECKS = 0;
-- Total Products: ~120
-- To use: Run this script in your MySQL workbench or command line.

-- 1. CLEANUP (Optional: Uncomment to start fresh)
DELETE FROM products;
DELETE FROM product_sub_categories;
DELETE FROM product_types;

-- 2. INSERT MAIN CATEGORIES (product_types)
INSERT INTO product_types (name, description) VALUES 
('Spices', 'Authentic Indian Spices, hand-picked and stone-ground.'),
('Coffee Powder', 'Premium coffee blends from the finest estates.'),
('Tea Powder', 'Traditional Indian tea blends and gourmet infusions.'),
('Dry Fruits', 'Healthy, nutritious, and premium quality dry fruits.'),
('Indian Spices & Groceries', 'Essential Indian kitchen staples and masalas.'),
('Snacks & Savories', 'Crispy and delicious traditional Indian snacks.'),
('Gourmet Combos', 'Curated gift sets and value combos.'),
('Pickles & Preserves', 'Hand-made pickles using traditional recipes.');

-- 3. INSERT SUB-CATEGORIES (product_sub_categories)
-- Using subqueries to get product_type_id

-- Spices
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Chili Powder', (SELECT id FROM product_types WHERE name = 'Spices')),
('Turmeric Powder', (SELECT id FROM product_types WHERE name = 'Spices')),
('Whole Spices', (SELECT id FROM product_types WHERE name = 'Spices')),
('Masala Powders', (SELECT id FROM product_types WHERE name = 'Spices'));

-- Coffee
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Filter Coffee', (SELECT id FROM product_types WHERE name = 'Coffee Powder')),
('Instant Coffee', (SELECT id FROM product_types WHERE name = 'Coffee Powder')),
('Coffee Beans', (SELECT id FROM product_types WHERE name = 'Coffee Powder'));

-- Tea
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Assam Tea', (SELECT id FROM product_types WHERE name = 'Tea Powder')),
('Green Tea', (SELECT id FROM product_types WHERE name = 'Tea Powder')),
('Masala Chai', (SELECT id FROM product_types WHERE name = 'Tea Powder'));

-- Dry Fruits
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Almonds', (SELECT id FROM product_types WHERE name = 'Dry Fruits')),
('Cashews', (SELECT id FROM product_types WHERE name = 'Dry Fruits')),
('Pistachios', (SELECT id FROM product_types WHERE name = 'Dry Fruits')),
('Raisins', (SELECT id FROM product_types WHERE name = 'Dry Fruits'));

-- Snacks
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Chips', (SELECT id FROM product_types WHERE name = 'Snacks & Savories')),
('Murukku', (SELECT id FROM product_types WHERE name = 'Snacks & Savories')),
('Peanuts', (SELECT id FROM product_types WHERE name = 'Snacks & Savories'));

-- Pickles
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Mango Pickles', (SELECT id FROM product_types WHERE name = 'Pickles & Preserves')),
('Garlic Pickles', (SELECT id FROM product_types WHERE name = 'Pickles & Preserves'));

-- 4. INSERT PRODUCTS
-- Format: INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at)

-- Category: Spices (15 Items)
INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Guntur Red Chili Powder', 'Extra spicy and vibrant red chili powder from Guntur farms.', 12.99, 100, '/images/ChiliPowder.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Chili Powder'), NOW(), NOW()),
('Kashmiri Mirch', 'Low heat, high-color premium Kashmiri chili powder.', 15.50, 80, '/images/ChiliPowder.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Chili Powder'), NOW(), NOW()),
('Organinc Turmeric Powder', 'High-curcumin organic turmeric, best for health.', 9.00, 150, '/images/Turmeric.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Turmeric Powder'), NOW(), NOW()),
('Alleppey Finger Turmeric', 'Premium whole turmeric fingers with high essential oil.', 11.00, 120, '/images/Turmeric.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Turmeric Powder'), NOW(), NOW()),
('Malabar Black Pepper', 'Bold and aromatic whole black peppercorns from Malabar.', 8.50, 200, '/images/BlackPepper.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('White Pepper Powder', 'Fine white pepper powder for delicate sauces and soups.', 14.00, 60, '/images/BlackPepper.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Green Cardamom Pods', 'Lush green 8mm+ cardamom pods from Idukki.', 22.00, 50, '/images/Cardamom.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Big Cardamom (Badi Elachi)', 'Smoky and rich black cardamom for authentic masalas.', 18.00, 70, '/images/Cardamom.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Cumin Seeds (Jeera)', 'Premium cumin seeds with high thymol content.', 6.50, 250, '/images/Cumin.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Cumin Powder', 'Roasted and ground cumin powder for everyday use.', 7.00, 180, '/images/Cumin.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Masala Powders'), NOW(), NOW()),
('Ceylon Cinnamon Sticks', 'True Ceylon cinnamon, sweet and fragile.', 16.00, 40, '/images/Cinnamon.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Cinnamon Powder', 'Aromatic cinnamon powder for baking and tea.', 18.00, 50, '/images/Cinnamon.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Masala Powders'), NOW(), NOW()),
('Whole Cloves', 'Strong and spicy whole cloves from Sri Lanka.', 12.00, 90, '/images/Cloves.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Star Anise', 'Beautiful and fragrant star anise for biryanis.', 14.50, 60, '/images/PremiumSpice.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW()),
('Nutmeg & Mace', 'Combination pack of whole nutmeg and mace.', 19.00, 30, '/images/PremiumSpice.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Whole Spices'), NOW(), NOW());

-- Category: Coffee Powder (15 Items)
INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Heritage Filter Coffee', 'Traditional 80:20 blend of Arabica and Chicory.', 14.50, 100, '/images/FilterCoffee.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('Peaberry Special Blend', 'Premium filter coffee made exclusively from Peaberry beans.', 18.00, 60, '/images/FilterCoffee.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('Classic Instant Coffee', '100% pure spray-dried instant coffee.', 15.00, 120, '/images/InstantCoffee.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Instant Coffee'), NOW(), NOW()),
('Gold Instant Coffee', 'Freeze-dried premium coffee for rich flavor.', 22.00, 90, '/images/InstantCoffee.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Instant Coffee'), NOW(), NOW()),
('Roasted Arabica Beans', 'Slow-roasted Arabica beans from Baba Budangiri.', 25.00, 40, '/images/ArabicaCoffee.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Coffee Beans'), NOW(), NOW()),
('Deep Dark Robusta', 'High-caffeine robusta beans for a strong punch.', 19.00, 50, '/images/ArabicaCoffee.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Coffee Beans'), NOW(), NOW()),
('Vanilla Infused Coffee', 'Aromatic coffee with natural vanilla notes.', 20.00, 70, '/images/CoffeeCombo.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('Hazelnut Gourmet Coffee', 'Premium instant coffee with hazelnut flavor.', 24.00, 60, '/images/CoffeeCombo.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Instant Coffee'), NOW(), NOW()),
('Cold Brew Blend', 'Coarse ground coffee specifically for cold brewing.', 28.00, 30, '/images/ColdCoffee.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Coffee Beans'), NOW(), NOW()),
('Decaf Smooth Roast', '100% Arabica decaf filter coffee.', 21.00, 45, '/images/FilterCoffee.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('Espresso Grind Special', 'Fine grind coffee for perfect espresso shots.', 19.50, 80, '/images/FilterCoffee.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('South Indian Degree Coffee', 'Traditional strong blend for degree coffee.', 15.00, 110, '/images/FilterCoffee.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('Mocha Blend', 'Coffee blend with subtle hints of chocolate.', 23.00, 50, '/images/CoffeeCombo.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Filter Coffee'), NOW(), NOW()),
('Single Estate Selection', 'Luxury coffee from a single high-altitude estate.', 35.00, 20, '/images/ArabicaCoffee.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Coffee Beans'), NOW(), NOW()),
('Coffee Taster Pack', '4 varieties of coffee in one gift pack.', 45.00, 30, '/images/CoffeeCombo.webp', '400g', (SELECT id FROM product_sub_categories WHERE name = 'Coffee Beans'), NOW(), NOW());

-- Category: Tea Powder (15 Items)
INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Assam Strong Leaf', 'Classic strong tea for the perfect morning start.', 7.50, 200, '/images/AssamTea.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Assam Tea'), NOW(), NOW()),
('Orthodox Tea Leaves', 'Long leaf premium tea for a refined taste.', 12.00, 100, '/images/AssamTea.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Assam Tea'), NOW(), NOW()),
('Pure Green Tea', 'Catechin-rich natural green tea leaves.', 14.00, 150, '/images/GreenTea.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Green Tea'), NOW(), NOW()),
('Jasmine Green Tea', 'Elegant green tea scented with real jasmine.', 18.50, 80, '/images/GreenTea.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Green Tea'), NOW(), NOW()),
('Cardamom Masala Chai', 'Premium tea leaves blended with crushed cardamom.', 9.00, 180, '/images/MasalaChai.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Masala Chai'), NOW(), NOW()),
('Ginger & Lemon Tea', 'Refreshing tea blend with dried ginger and lime.', 10.50, 140, '/images/LemonTea.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Masala Chai'), NOW(), NOW()),
('Tulsi Divine Blend', 'Holy basil infused tea for immunity and calm.', 11.00, 120, '/images/TulsiTea.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Masala Chai'), NOW(), NOW()),
('Earl Grey Special', 'Bergamot infused luxury black tea.', 16.00, 70, '/images/AssamTea.webp', '125g', (SELECT id FROM product_sub_categories WHERE name = 'Assam Tea'), NOW(), NOW()),
('Darjeeling First Flush', 'The "Champagne of Teas" from Darjeeling.', 30.00, 40, '/images/AssamTea.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Assam Tea'), NOW(), NOW()),
('Hibiscus & Rose Tea', 'Beautiful floral tea infusion.', 22.00, 50, '/images/HerbalTea.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Green Tea'), NOW(), NOW()),
('Ashwagandha Tea', 'Restorative tea blend with ayurvedic herbs.', 24.00, 60, '/images/HerbalTea.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Green Tea'), NOW(), NOW()),
('CTC Strong Dust', 'Extra strong tea for a milky Indian chai.', 6.00, 300, '/images/AssamTea.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Assam Tea'), NOW(), NOW()),
('Lemon Ice Tea Mix', 'Instant mix for a refreshing cold tea.', 12.00, 100, '/images/LemonTea.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Masala Chai'), NOW(), NOW()),
('White Tea Pear', 'Rare white tea with subtle pear hints.', 40.00, 15, '/images/HerbalTea.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Green Tea'), NOW(), NOW()),
('Imperial Tea Tins', 'Gift tin set with 3 luxury tea blends.', 55.00, 25, '/images/TeaCombo.webp', '300g', (SELECT id FROM product_sub_categories WHERE name = 'Assam Tea'), NOW(), NOW());

-- Category: Dry Fruits (15 Items)
INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Roasted Salted Almonds', 'Crunchy California almonds, perfectly salted.', 25.00, 100, '/images/almonds.png', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Organic Raw Almonds', 'Direct from farms, nutrient dense raw almonds.', 22.00, 120, '/images/almonds.png', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Jumbo Cashews W180', 'The largest and king of cashews.', 28.00, 80, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Cashews'), NOW(), NOW()),
('Roasted Masala Cashews', 'Spicy coated cashews for gourmet snacking.', 30.00, 70, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Cashews'), NOW(), NOW()),
('Premium Pistachios Shell', 'Salted and roasted open-shell pistachios.', 26.00, 90, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Pistachios'), NOW(), NOW()),
('Saffron Pistachios', 'Luxury pistachios coated with real saffron.', 35.00, 50, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Pistachios'), NOW(), NOW()),
('Green Raisins Special', 'Sweet and delicious long green raisins.', 12.00, 150, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Raisins'), NOW(), NOW()),
('Black Seedless Raisins', 'Antioxidant rich black raisins.', 14.50, 130, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Raisins'), NOW(), NOW()),
('Chilean Walnuts Kernels', 'Premium grade walnut halves from Chile.', 32.00, 60, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Dried Afghan Figs', 'Naturally sweet and fiber rich Anjeer.', 38.00, 40, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Dates Medjool Luxury', 'Giant, soft, and sweet Medjool dates.', 45.00, 30, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Dates Ajwa Medina', 'Prophetic Ajwa dates from Medina.', 55.00, 25, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Mix Superfood Box', 'Mix of Almonds, Cashews, Walnuts & Raisins.', 48.00, 50, '/images/SnackCombo.webp', '800g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Brazil Nuts Whole', 'Selenium rich premium Brazil nuts.', 50.00, 20, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW()),
('Pecan Nut Halves', 'Buttery and sweet pecan nuts from USA.', 52.00, 15, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Almonds'), NOW(), NOW());

-- Category: Snacks & Savories (15 Items)
INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Classic Banana Chips', 'Thin and crispy chips fried in coconut oil.', 4.50, 200, '/images/BananaChips.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Chips'), NOW(), NOW()),
('Pepper Banana Chips', 'Banana chips with a spicy black pepper coating.', 5.00, 180, '/images/BananaChips.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Chips'), NOW(), NOW()),
('Traditional Murukku', 'Rice flour savory snacks with sesame seeds.', 3.50, 150, '/images/Murukku.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Kai Murukku', 'Beautiful hand-twisted traditional Murukku.', 6.00, 80, '/images/Murukku.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Spicy Masala Peanuts', 'Crunchy peanuts with a secret spice coating.', 3.50, 220, '/images/Peanuts.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Peanuts'), NOW(), NOW()),
('Roasted Peanuts Plain', 'Healthy dry-roasted skinless peanuts.', 4.00, 180, '/images/Peanuts.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Peanuts'), NOW(), NOW()),
('Kerala Special Mixture', 'Assorted savory mix with various crispies.', 5.50, 160, '/images/Mixture.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Corn Flakes Mixture', 'Light and sweet-spicy corn flakes mix.', 4.00, 140, '/images/MasalaCorn.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Chakli Rice Rounds', 'Crunchy rice spirals with cumin flavor.', 3.80, 120, '/images/Chakli.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Methi Khakhra', 'Thin whole wheat crackers with fenugreek.', 4.50, 200, '/images/Khakhra.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Ribbon Pakoda', 'Thin and light rice ribbon crispies.', 3.50, 170, '/images/RibbonPakoda.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Seedai Traditional', 'Traditional small rice balls, crispy and salty.', 4.00, 110, '/images/Seedai.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW()),
('Roasted Makhana Plain', 'Puffed lotus seeds, zero-fat healthy snack.', 8.00, 100, '/images/Makana.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Peanuts'), NOW(), NOW()),
('Masala Makhana', 'Lotus seeds with spicy peri-peri flavors.', 9.00, 90, '/images/Makana.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Peanuts'), NOW(), NOW()),
('Snack Heritage Box', 'Mix of 5 traditional savory snacks.', 25.00, 40, '/images/SnackCombo.webp', '1kg', (SELECT id FROM product_sub_categories WHERE name = 'Murukku'), NOW(), NOW());

-- Category: Pickles & Preserves (15 Items)
INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Classic Mango Pickle', 'Traditional sun-dried mangoes in spicy oil.', 6.50, 150, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Tender Mango (Kadumango)', 'Small whole baby mangoes in vinegary spice.', 8.50, 100, '/images/PremiumSpice.webp', '400g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Hot Garlic Pickle', 'Spicy cloves of garlic in rich chili oil.', 7.00, 120, '/images/PremiumSpice.webp', '300g', (SELECT id FROM product_sub_categories WHERE name = 'Garlic Pickles'), NOW(), NOW()),
('Ginger & Garlic Mix', 'Perfect blend for digestion and taste.', 7.50, 110, '/images/PremiumSpice.webp', '350g', (SELECT id FROM product_sub_categories WHERE name = 'Garlic Pickles'), NOW(), NOW()),
('Zesty Lime Pickle', 'Thin-skinned limes in a sharp salt-spice mix.', 6.00, 180, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Chili Preserve in Oil', 'Fermented green chilies for extra punch.', 6.80, 90, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Mixed Veg Heritage', 'Assorted farm vegetables in secret spices.', 7.00, 130, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Ginger Pickle Special', 'Shredded ginger pickle, great for cold flavor.', 8.00, 80, '/images/PremiumSpice.webp', '300g', (SELECT id FROM product_sub_categories WHERE name = 'Garlic Pickles'), NOW(), NOW()),
('Gongura Leaves Pickle', 'Andhra style sorrel leaves pickle.', 6.50, 110, '/images/PremiumSpice.webp', '350g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Avakaya Mango Special', 'Spicy mustard-based Andhra mango pickle.', 9.00, 70, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Amla (Gooseberry) Pickle', 'Healthy amla pieces in traditional spices.', 8.50, 100, '/images/PremiumSpice.webp', '350g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Dates & Garlic Pickle', 'Unique sweet and spicy fusion pickle.', 12.00, 40, '/images/PremiumSpice.webp', '300g', (SELECT id FROM product_sub_categories WHERE name = 'Garlic Pickles'), NOW(), NOW()),
('Lemon preserve (Salt)', 'Oil-free pure salted lime for long life.', 5.50, 120, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW()),
('Prawn Pickle Special', 'Gourmet non-veg pickle with fresh prawns.', 22.00, 30, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Garlic Pickles'), NOW(), NOW()),
('Pickle Taster Set', 'Mini pack of 4 popular pickles.', 30.00, 20, '/images/PremiumSpice.webp', '600g', (SELECT id FROM product_sub_categories WHERE name = 'Mango Pickles'), NOW(), NOW());

-- Category: Indian Spices & Groceries (15 Items)
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Sambar & Rasam', (SELECT id FROM product_types WHERE name = 'Indian Spices & Groceries')),
('Authentic Hing', (SELECT id FROM product_types WHERE name = 'Indian Spices & Groceries')),
('Groceries', (SELECT id FROM product_types WHERE name = 'Indian Spices & Groceries'));

INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('Madras Sambar Powder', 'Authentic Madras style sambar masala for daily use.', 8.00, 200, '/images/SambarPowder.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Sambar & Rasam'), NOW(), NOW()),
('Tangy Rasam Powder', 'Peppery and sour rasam mix for the perfect soup.', 7.50, 150, '/images/RasamPowder.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Sambar & Rasam'), NOW(), NOW()),
('Pure Crystal Hing', 'Strong aromatic Asafoetida crystals from Afghanistan.', 12.00, 80, '/images/SpiceHing.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Authentic Hing'), NOW(), NOW()),
('Hing Powder Special', 'Kitchen-ready asafoetida powder for every dal.', 6.50, 200, '/images/SpiceHing.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Authentic Hing'), NOW(), NOW()),
('Black Mustard Seeds', 'Small and pungent black mustard seeds for tempering.', 4.00, 300, '/images/PremiumSpice.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Fenugreek Seeds (Methi)', 'Nutritious and bitter-sweet fenugreek seeds.', 5.00, 250, '/images/PremiumSpice.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Whole Corriander', 'Clean and golden coriander seeds with sweet aroma.', 7.00, 220, '/images/PremiumSpice.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Sultana Tamarind', 'Seedless and clean tamarind pulp for sambar.', 10.00, 100, '/images/PremiumSpice.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Tamarind Paste Concentrated', 'Pure tamarind extract, ready to use.', 15.00, 60, '/images/PremiumSpice.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Rock Salt Crystals', 'Traditional unprocessed Himalayan rock salt.', 3.50, 400, '/images/PremiumSpice.webp', '1kg', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Bay Leaves Special', 'Large and fragrant dried bay leaves.', 6.00, 90, '/images/PremiumSpice.webp', '50g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Curry Leaves Dried', 'Sun-dried aromatic curry leaves.', 5.50, 110, '/images/PremiumSpice.webp', '30g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Amchur (Mango) Powder', 'Tangy green mango powder for chaats.', 9.00, 130, '/images/PremiumSpice.webp', '100g', (SELECT id FROM product_sub_categories WHERE name = 'Groceries'), NOW(), NOW()),
('Kabab Masala Special', 'Secret blend for authentic grilled chicken.', 12.50, 70, '/images/PremiumSpice.webp', '200g', (SELECT id FROM product_sub_categories WHERE name = 'Sambar & Rasam'), NOW(), NOW()),
('Bisi Bele Bath Mix', 'Karnataka style spiced rice lentil mix.', 14.00, 60, '/images/PremiumSpice.webp', '250g', (SELECT id FROM product_sub_categories WHERE name = 'Sambar & Rasam'), NOW(), NOW());

-- Category: Gourmet Combos (15 Items)
INSERT INTO product_sub_categories (name, product_type_id) VALUES 
('Gift Boxes', (SELECT id FROM product_types WHERE name = 'Gourmet Combos')),
('Essentials Set', (SELECT id FROM product_types WHERE name = 'Gourmet Combos'));

INSERT INTO products (name, description, price, stock_quantity, image_url, weight, sub_category_id, created_at, updated_at) VALUES
('PVR Grand Spice Box', 'Set of 12 essential spices in a luxury gift box.', 55.00, 50, '/images/SpiceCombo.webp', '1.5kg', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Breakfast Essentials Pack', 'Coffee, Tea, and Nut Butter combo.', 35.00, 60, '/images/SpiceCombo.webp', '1kg', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('Home Chef Gold Kit', 'Curated selection of Whole Spices and Masalas.', 45.00, 40, '/images/SpiceCombo.webp', '1.2kg', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('Gourmet Tea Set', '4 varieties of premium tea in glass jars.', 65.00, 30, '/images/TeaCombo.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Dry Fruit Celebration', 'Exotic mix of 6 premium dry fruits.', 75.00, 25, '/images/SnackCombo.webp', '1.2kg', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Healthy Snacking Combo', 'Makhana, Peanuts, and Banana Chips set.', 22.00, 100, '/images/SnackCombo.webp', '600g', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('Pickle Sampler Box', '6 mini jars of our best-selling pickles.', 38.00, 45, '/images/SpiceCombo.webp', '800g', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Royal Coffee Experience', '3 Estate coffees with a ceramic filter.', 50.00, 35, '/images/CoffeeCombo.webp', '1kg', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Saffron & Nuts Luxury', 'Kashmiri Saffron and Roasted Almonds kit.', 95.00, 15, '/images/SnackCombo.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Daily Masala Bundle', 'Sambar, Rasam, and Chili powder value pack.', 25.00, 120, '/images/SpiceCombo.webp', '750g', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('South Indian Heritage Kit', 'Coffee, Chips, and Sambar powder.', 32.00, 80, '/images/SpiceCombo.webp', '1kg', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('Festival Special Sweets', 'Limited edition combo of festival savories.', 42.00, 50, '/images/SnackCombo.webp', '1kg', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW()),
('Travel Spice Kit', 'Mini shaker jars of 6 essential spices.', 20.00, 150, '/images/SpiceCombo.webp', '300g', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('Tea & Snacks Evening', 'Masala tea and Banana chips combo.', 18.00, 90, '/images/SnackCombo.webp', '500g', (SELECT id FROM product_sub_categories WHERE name = 'Essentials Set'), NOW(), NOW()),
('The Whole Nine Yards', 'Ultimate PVR Prime Naturals collection (25 items).', 199.00, 10, '/images/SpiceCombo.webp', '5kg', (SELECT id FROM product_sub_categories WHERE name = 'Gift Boxes'), NOW(), NOW());

SET FOREIGN_KEY_CHECKS = 1;
