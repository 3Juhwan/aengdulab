create database if not exists mail;

use mail;

create table question
(
    id      bigint       not null auto_increment,
    content varchar(255) not null,
    title   varchar(255) not null,
    primary key (id)
);

create table sent_mail_event
(
    id           bigint  not null auto_increment,
    is_succeeded boolean not null,
    question_id  bigint,
    subscribe_id bigint,
    primary key (id)
);

create table subscribe
(
    id               bigint       not null auto_increment,
    email            varchar(255) not null,
    question_id bigint       not null,
    primary key (id)
);

alter table sent_mail_event
    add constraint FK17m4cc9w19g26756wf9xtcm8f
        foreign key (question_id) references question (id);

alter table sent_mail_event
    add constraint FKeyng47xswi660du6w4mw0eqa1
        foreign key (subscribe_id) references subscribe (id);

alter table subscribe
    add constraint FK42rnd7meugcg6netgc78pu8c0
        foreign key (next_question_id) references question (id);
