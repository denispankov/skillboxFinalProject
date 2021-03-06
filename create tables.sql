create database search_engine;
CREATE USER search_engine WITH PASSWORD 'search_engine';
GRANT CONNECT ON DATABASE search_engine TO search_engine;

create table page(
id serial PRIMARY key,
path TEXT NOT NULL,
code smallint NOT NULL,
content text NOT null,
site_id integer not null
);
create unique index path_idx on page(path, site_id);
grant select, insert on page to search_engine;


create table field(
id serial PRIMARY key,
name text NOT NULL,
selector text NOT null,
weight real NOT null CHECK (weight >= 0 and weight <= 1)
);

insert into field(name, selector, weight)
values('title', 'title', 1.0),('body', 'body', 0.8);

grant select, insert on field to search_engine;

create table lemma(
id serial PRIMARY key,
lemma text NOT null,
frequency integer NOT null,
site_id integer not null);

create unique index lemma_idx on lemma(lemma);

grant select, insert, update on lemma to search_engine;

create table index(
id serial PRIMARY key,
page_id integer  NOT NULL,
lemma_id integer NOT NULL,
rank numeric NOT NULL
);

grant select, insert on index to search_engine;

CREATE TYPE site_status AS ENUM ('INDEXING', 'INDEXED', 'FAILED');

create table site(
id serial PRIMARY key,
status site_status not null,
status_time timestamp with time zone NOT null,
last_error text,
url text NOT null,
name text NOT NULL
);

create unique index site_url_idx on site(url);

grant select, insert, update on site to search_engine;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO search_engine;

