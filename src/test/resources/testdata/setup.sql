-- Drop tables if they exist
DROP TABLE IF EXISTS wood;
DROP TABLE IF EXISTS wood_type;

-- Then create tables
CREATE TABLE wood_type (
                           type VARCHAR(255) PRIMARY KEY
);

CREATE TABLE wood (
                      id INT,
                      type VARCHAR(255),
                      price DECIMAL(10, 2),
                      PRIMARY KEY (id),
                      FOREIGN KEY (type) REFERENCES wood_type(type)
);

-- Insert wood types if not exists
INSERT INTO wood_type (type)
SELECT 'OAK' WHERE NOT EXISTS (SELECT 1 FROM wood_type WHERE type = 'OAK');
INSERT INTO wood_type (type)
SELECT 'PINE' WHERE NOT EXISTS (SELECT 1 FROM wood_type WHERE type = 'PINE');
INSERT INTO wood_type (type)
SELECT 'MAPLE' WHERE NOT EXISTS (SELECT 1 FROM wood_type WHERE type = 'MAPLE');

-- Insert wood records if not exists
INSERT INTO wood (id, type, price)
SELECT 1, 'OAK', 10.00 WHERE NOT EXISTS (SELECT 1 FROM wood WHERE id = 1 AND type = 'OAK' AND price = 10.00);
INSERT INTO wood (id, type, price)
SELECT 2, 'OAK', 15.00 WHERE NOT EXISTS (SELECT 1 FROM wood WHERE id = 2 AND type = 'OAK' AND price = 15.00);
INSERT INTO wood (id, type, price)
SELECT 3, 'PINE', 8.00 WHERE NOT EXISTS (SELECT 1 FROM wood WHERE id = 3 AND type = 'PINE' AND price = 8.00);
INSERT INTO wood (id, type, price)
SELECT 4, 'MAPLE', 12.00 WHERE NOT EXISTS (SELECT 1 FROM wood WHERE id = 4 AND type = 'MAPLE' AND price = 12.00);

COMMIT;
