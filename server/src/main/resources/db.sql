create table if not exists "User"
(
    id           bigserial
        primary key,
    phone_number varchar(255) not null,
    name         varchar(255) not null,
    surname      varchar(255),
    password     varchar(255) not null
);


create table if not exists "ChatRoom"
(
    id         bigserial
        primary key,
    name       varchar(255) not null,
    is_private boolean      not null
);


create table if not exists "Message"
(
    id           bigserial
        primary key,
    content      text         not null,
    sender_id    bigint       not null
        constraint message_sender_id_foreign
            references "User",
    chat_room_id bigint       not null
        constraint message_chat_room_id_foreign
            references "ChatRoom",
    sent_time    timestamp(0) not null
);


create table if not exists "ChatRoomMember"
(
    id           bigserial
        primary key,
    chat_room_id bigint not null
        constraint chatroommember_chat_room_id_foreign
            references "ChatRoom",
    member_id    bigint not null
        constraint chatroommember_member_id_foreign
            references "User"
);


