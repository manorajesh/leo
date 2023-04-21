CREATE TABLE project
(
    id                INT PRIMARY KEY AUTO_INCREMENT,
    creation_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,

    name              VARCHAR(255) NOT NULL,
    short_descr       TEXT         NOT NULL,
    short_descr_quill TEXT         NOT NULL,
    long_descr        TEXT         NOT NULL,
    long_descr_quill  TEXT         NOT NULL,

    ikigai_input_id   INT          NOT NULL,
    CONSTRAINT project_ikigai_input_id
        FOREIGN KEY (ikigai_input_id)
            REFERENCES ikigai_input (id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT
) ENGINE InnoDB
  CHAR SET UTF8MB4;
