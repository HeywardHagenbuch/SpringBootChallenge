CREATE TABLE wood_type
(
    type VARCHAR(64) NOT NULL PRIMARY KEY
);

CREATE TABLE wood
(
    type  VARCHAR(64) NOT NULL,
    id    BIGINT      NOT NULL,
    price NUMERIC(10,2)     NOT NULL,
    PRIMARY KEY (type, id),
    FOREIGN KEY (type) REFERENCES wood_type (type)
);