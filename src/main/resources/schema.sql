CREATE SEQUENCE users_id_seq;
CREATE SEQUENCE items_id_seq;
CREATE SEQUENCE bookings_id_seq;
CREATE SEQUENCE requests_id_seq;
CREATE SEQUENCE comments_id_seq;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL default nextval('users_id_seq') PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(512) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS requests (
    id bigint NOT NULL default nextval('requests_id_seq') primary key,
    description varchar(512) not null,
    requestor_id bigint references users(id),
    created timestamp without time zone not null
);

CREATE TABLE IF NOT EXISTS items (
    id BIGINT NOT NULL default nextval('items_id_seq') PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,
    is_available bool default false,
    owner_id BIGINT references users(id),
    request_id bigint references requests(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id bigint NOT NULL default nextval('bookings_id_seq') primary key,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    item_id bigint references items(id),
    booker_id bigint references users(id),
    status varchar(20) not null
);

CREATE TABLE IF NOT EXISTS comments (
    id bigint NOT NULL default nextval('comments_id_seq') primary key,
    text varchar(512) not null,
    item_id bigint references items(id),
    author_id bigint references users(id),
    created timestamp without time zone not null
);