-- Addresses table foreign key
ALTER TABLE addresses
    ADD FOREIGN KEY (user_id) REFERENCES users(id);

-- Users table foreign keys
ALTER TABLE users
    ADD FOREIGN KEY (address_id) REFERENCES addresses(id),
    ADD FOREIGN KEY (rating_id) REFERENCES ratings(id),
    ADD FOREIGN KEY (cart_id) REFERENCES carts(id);

-- SubCategories table foreign key
ALTER TABLE sub_categories
    ADD FOREIGN KEY (main_category_id) REFERENCES main_categories(id);

-- Items table foreign keys
ALTER TABLE items
    ADD FOREIGN KEY (seller_id) REFERENCES users(id),
    ADD FOREIGN KEY (main_category_id) REFERENCES main_categories(id),
    ADD FOREIGN KEY (sub_category_id) REFERENCES sub_categories(id),
    ADD FOREIGN KEY (rating_id) REFERENCES ratings(id);

-- Orders table foreign key
ALTER TABLE orders
    ADD FOREIGN KEY (user_id) REFERENCES users(id),
    ADD FOREIGN KEY (shipping_method_id) REFERENCES shipping_methods(id);

-- OrderItems table foreign keys
ALTER TABLE order_items
    ADD FOREIGN KEY (order_id) REFERENCES orders(id),
    ADD FOREIGN KEY (item_id) REFERENCES items(id);

-- Cards table foreign key
ALTER TABLE cards
    ADD FOREIGN KEY (user_id) REFERENCES users(id);

-- Cart table foreign key
ALTER TABLE carts
    ADD FOREIGN KEY (user_id) REFERENCES users(id);

-- CartItems table foreign keys
ALTER TABLE cart_items
    ADD FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    ADD FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE;

-- Feedback table foreign keys
ALTER TABLE feedback
    ADD FOREIGN KEY (item_id) REFERENCES items(id),
    ADD FOREIGN KEY (user_id) REFERENCES users(id);
