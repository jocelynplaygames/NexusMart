-- Create Items table
CREATE TABLE IF NOT EXISTS items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id VARCHAR(36) NOT NULL,
    title VARCHAR(255) NOT NULL,
    short_description TEXT NOT NULL,
    long_description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    sub_image_url1 VARCHAR(255),
    sub_image_url2 VARCHAR(255),
    sub_image_url3 VARCHAR(255),
    sub_image_url4 VARCHAR(255),
    city VARCHAR(255) NOT NULL,
    state VARCHAR(255) NOT NULL,
    country VARCHAR(255) NOT NULL,
    main_category_id BIGINT NOT NULL,
    sub_category_id BIGINT NOT NULL,
    rating_id BIGINT,
    date_listed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    quantity INT NOT NULL DEFAULT 0,
    deleted BOOLEAN NOT NULL DEFAULT FALSE
);

-- Create MainCategories table
CREATE TABLE IF NOT EXISTS main_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create SubCategories table
CREATE TABLE IF NOT EXISTS sub_categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    main_category_id BIGINT NOT NULL,
    image_url VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (main_category_id) REFERENCES itemservice.main_categories(id)
);

-- Items table foreign keys
ALTER TABLE items
    ADD FOREIGN KEY (seller_id) REFERENCES userservice.users(id),
    ADD FOREIGN KEY (main_category_id) REFERENCES itemservice.main_categories(id),
    ADD FOREIGN KEY (sub_category_id) REFERENCES itemservice.sub_categories(id),
    ADD FOREIGN KEY (rating_id) REFERENCES userservice.ratings(id);