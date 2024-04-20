-- auto-generated definition
create table user
(
    id            bigint auto_increment comment 'id' primary key,
    username      varchar(255)                       null comment '用户昵称',
    user_account  varchar(255)                       null comment '账号',
    avatar_url    varchar(1024)                      null comment '头像',
    gender        tinyint                            null comment '性别',
    user_password varchar(512)                       not null comment '密码',
    user_profile  varchar(512)                       null comment '个人简介',
    phone         varchar(128)                       null comment '电话',
    email         varchar(512)                       null comment '邮箱',
    user_status   int      default 0                 null comment '用户状态 0-正常',
    create_time   datetime default CURRENT_TIMESTAMP null comment '创建时间（数据插入时间）',
    update_time   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间（数据更新时间）',
    deleted       tinyint  default 0                 null comment '是否删除 0 1（逻辑删除）',
    user_role     tinyint  default 0                 not null comment '用户角色 0-普通用户 1-管理员',
    tags          varchar(1024)                      null comment '标签 JSON 列表'
)
    comment '用户';