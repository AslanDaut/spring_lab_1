create table app_user (
                          id            bigserial primary key,
                          username      varchar(60)  not null unique,
                          password_hash varchar(100) not null,
                          enabled       boolean      not null default true
);

create table user_role (
                           user_id bigint      not null,
                           role    varchar(20) not null,
                           primary key (user_id, role),
                           foreign key (user_id) references app_user(id)
);

-- Хэши сгенерированы для паролей 'user123' и 'admin123'
insert into app_user (username, password_hash) values
                                                   ('user',  '$2a$10$wJ15DkEtOKMyLryIM3Kf.uJ0bE2tYdZzTiPMv4qotjAuaF91jygq6'),
                                                   ('admin', '$2a$10$8JUiDhHP2d152C2xZHvQauTXlo61tTWg866h6gwb6VnHYNKRvpigS');

insert into user_role (user_id, role) select id, 'USER'  from app_user;
insert into user_role (user_id, role) select id, 'ADMIN' from app_user where username = 'admin';