create schema dataStorage;

create user liq_user password 'password';
create user dataStorage_user password 'password';

grant all on schema dataStorage to liq_user;

grant usage on schema dataStorage TO dataStorage_user;
grant select, update, insert, delete on all tables in schema dataStorage to dataStorage_user;
grant usage, select on ALL SEQUENCES IN SCHEMA dataStorage to dataStorage_user;
