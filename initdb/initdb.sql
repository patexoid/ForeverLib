create schema core;

create user liq_user password 'password';
create user core_user password 'password';

grant all on schema core to liq_user;

grant usage on schema core TO core_user;
grant select, update, insert, delete on all tables in schema core to core_user;
grant usage, select on ALL SEQUENCES IN SCHEMA core to core_user;
