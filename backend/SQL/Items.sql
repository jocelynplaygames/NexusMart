CREATE TABLE Items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sellerId INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    imageUrl VARCHAR(255),
    mainCategoryId INT NOT NULL,
    subCategoryId INT NOT NULL,
    ratingId INT,
    dateListed TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (sellerId) REFERENCES Users(id),
    FOREIGN KEY (mainCategoryId) REFERENCES MainCategories(id),
    FOREIGN KEY (subCategoryId) REFERENCES SubCategories(id),
    FOREIGN KEY (ratingId) REFERENCES Ratings(id)
);
