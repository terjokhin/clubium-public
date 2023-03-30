CREATE TABLE users
(
    id        INTEGER PRIMARY KEY,
    name      TEXT     NOT NULL,
    lastname  TEXT,
    active    BOOLEAN  NOT NULL CHECK (active IN (0, 1)),
    lastseen  DATETIME
);