CREATE DATABASE IF NOT EXISTS paymentservice;

ALTER USER 'root'@'localhost' IDENTIFIED BY 'your_password';
ALTER USER 'root'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;


