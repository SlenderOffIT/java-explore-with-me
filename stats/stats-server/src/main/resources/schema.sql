drop table if exists hits;

create table if not exists hits (
    id serial not null primary key,
    app varchar(255) not null,
    uri varchar(255) not null,
    ip varchar(16) not null,
    timestamp timestamp without time zone
)