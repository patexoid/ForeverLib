create schema library;

create user liq_user password 'password';
create user core_user password 'password';

grant all on schema library to liq_user;

grant usage on schema library TO core_user;
grant select, update, insert, delete on all tables in schema library to core_user;
grant usage, select on ALL SEQUENCES IN SCHEMA library to core_user;
