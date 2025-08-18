-- Drop table CartItems first because it depends on Cart and Items
DROP TABLE IF EXISTS CartItems;

-- Drop table OrderItems first because it depends on Orders and Items
DROP TABLE IF EXISTS OrderItems;

-- Drop the dependent tables
DROP TABLE IF EXISTS Cart;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Feedback;

-- Drop tables that have foreign key dependencies next
DROP TABLE IF EXISTS Addresses;
DROP TABLE IF EXISTS Users;
DROP TABLE IF EXISTS Items;

-- Drop remaining tables
DROP TABLE IF EXISTS Ratings;
DROP TABLE IF EXISTS SubCategories;
DROP TABLE IF EXISTS MainCategories;
DROP TABLE IF EXISTS Cards;
DROP TABLE IF EXISTS ShippingMethods;
