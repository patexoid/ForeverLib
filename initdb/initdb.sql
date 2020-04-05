create schema library;

create user liq_user password 'password';
create user lib_user password 'password';

grant all on schema library to liq_user;

grant usage on schema library TO lib_user;
grant select, update, insert, delete on all tables in schema library to lib_user;
grant usage, select on ALL SEQUENCES IN SCHEMA library to lib_user;
