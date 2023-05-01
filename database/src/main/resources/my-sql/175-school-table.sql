CREATE TABLE school
(
    id            INT PRIMARY KEY AUTO_INCREMENT,
    creation_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    name          VARCHAR(255) NOT NULL,
    nickname      VARCHAR(255),
    address       VARCHAR(255),

    district_id   INT          NOT NULL,
    CONSTRAINT school__district_id
        FOREIGN KEY (district_id)
            REFERENCES district (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT
) ENGINE InnoDB
  CHAR SET UTF8MB4;
