CREATE TABLE IF NOT EXISTS users (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(30) NOT NULL,
    email varchar(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS items (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    name varchar(30) NOT NULL,
    description varchar(400) NOT NULL,
    is_available boolean NOT NULL,
    owner_id int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    request_id bigint REFERENCES requests(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    start_date timestamp without time zone,
    end_date timestamp without time zone,
    item_id int NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    booker_id int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status varchar(8) NOT NULL
);

CREATE TABLE IF NOT EXISTS requests (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    description varchar(400) NOT NULL,
    requestor_id int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created timestamp without time zone
);

CREATE TABLE IF NOT EXISTS comments (
    id bigint GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    text varchar(400) NOT NULL,
    item_id int NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    author_id int NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created timestamp without time zone
);