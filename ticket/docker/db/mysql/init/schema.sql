create database if not exists ticket;

use ticket;

create table if not exists member
(
    id   bigint not null auto_increment,
    name varchar(255),
    primary key (id)
) engine = InnoDB;

create table if not exists member_ticket
(
    id        bigint not null auto_increment,
    member_id bigint,
    ticket_id bigint,
    primary key (id)
) engine = InnoDB;

create table if not exists ticket
(
    id       bigint not null auto_increment,
    quantity bigint,
    version  bigint,
    name     varchar(255),
    primary key (id)
) engine = InnoDB;
