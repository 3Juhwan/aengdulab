set foreign_key_checks=0;
truncate table sent_mail_event;
truncate table subscribe;
truncate table question;
alter table sent_mail_event auto_increment=1;
alter table subscribe auto_increment=1;
alter table question auto_increment=1;
set foreign_key_checks=1;

insert into question (content, title)
values ('What is your favorite color?', 'Favorite Color'),
       ('What is your favorite food?', 'Favorite Food'),
       ('What is your favorite hobby?', 'Favorite Hobby'),
       ('What is your favorite book?', 'Favorite Book'),
       ('What is your dream job?', 'Dream Job'),
       ("What is yout future?", "Future");

insert into subscribe (question_id, email)
values (1, 'user1@example.com'),
       (2, 'user2@example.com'),
       (3, 'user3@example.com'),
       (4, 'user4@example.com'),
       (5, 'user5@example.com'),
       (1, 'user6@example.com'),
       (2, 'user7@example.com'),
       (3, 'user8@example.com'),
       (4, 'user9@example.com'),
       (5, 'user10@example.com'),
       (1, 'user11@example.com'),
       (2, 'user12@example.com'),
       (3, 'user13@example.com'),
       (4, 'user14@example.com'),
       (5, 'user15@example.com'),
       (1, 'user16@example.com'),
       (2, 'user17@example.com'),
       (3, 'user18@example.com'),
       (4, 'user19@example.com'),
       (5, 'user20@example.com');
