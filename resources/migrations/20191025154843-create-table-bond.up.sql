CREATE TABLE bond
(id                 INTEGER PRIMARY KEY AUTO_INCREMENT,
description         VARCHAR(100),
updated             TIMESTAMP,
principal           DOUBLE,
deposit             DOUBLE,
interest_rate       DOUBLE,
term_years          INTEGER);