create schema dataStorage;
create schema users;

create user liq_user password 'password';
create user users_user password 'password';
create user dataStorage_user password 'password';

grant all on schema dataStorage to liq_user;
grant all on schema users to liq_user;

grant usage on schema dataStorage TO dataStorage_user;
grant select, update, insert, delete on all tables in schema dataStorage to dataStorage_user;
grant usage, select on ALL SEQUENCES IN SCHEMA dataStorage to dataStorage_user;

grant usage on schema users TO users_user;
grant select, update, insert, delete on all tables in schema users to users_user;
grant usage, select on ALL SEQUENCES IN SCHEMA users to users_user;
