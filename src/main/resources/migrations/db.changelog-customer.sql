--liquibase formatted sql

--changeset gzockoll:1 attribute1:value1 attribute2:value2 [...]

CREATE table customer (
  id int primary key,
  first_name varchar(30) not null,
  initial varchar(1) null,
  last_name varchar(30) not null
)

--rollback drop table customer;
