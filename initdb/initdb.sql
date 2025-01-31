create schema library;

create user liq_user password 'password';
create user core_user password 'password';

grant all on schema library to liq_user;

grant usage on schema library TO core_user;
grant select, update, insert, delete on all tables in schema library to core_user;
grant usage, select on ALL SEQUENCES IN SCHEMA library to core_user;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
DO $$
    DECLARE
        func_record RECORD;
    BEGIN
        FOR func_record IN
            SELECT proname, oidvectortypes(proargtypes) AS args
            FROM pg_proc
            WHERE pronamespace = 'public'::regnamespace
              AND proname LIKE '%trgm%'
            LOOP
                EXECUTE format('GRANT EXECUTE ON FUNCTION %I(%s) TO PUBLIC',
                               func_record.proname, func_record.args);
            END LOOP;
    END $$;