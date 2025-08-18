CREATE DATABASE IF NOT EXISTS itemservice;

ALTER USER 'root'@'localhost' IDENTIFIED BY 'wxwwxw123';
ALTER USER 'root'@'%' IDENTIFIED BY 'wxwwxw123';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%';
FLUSH PRIVILEGES;

CHANGE REPLICATION SOURCE TO
    SOURCE_HOST='itemservice-master',  -- Adjust to your source host
    SOURCE_USER='replica',
    SOURCE_PASSWORD='replica_password',
    SOURCE_LOG_FILE='mysql-bin.000001',  -- Replace with the actual log file from SHOW MASTER STATUS
    SOURCE_LOG_POS=0;  -- Replace with the actual log position from SHOW MASTER STATUS

START REPLICA;






