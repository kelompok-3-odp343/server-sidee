DECLARE
    v_count NUMBER;
BEGIN
    -- Drop FK_USER_AUTH_PROFILE if exists
    SELECT COUNT(*)
    INTO v_count
    FROM user_constraints
    WHERE constraint_name = 'FK_USER_AUTH_PROFILE'
      AND table_name = 'USER_AUTH'
      AND constraint_type = 'R';

    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE USER_AUTH DROP CONSTRAINT FK_USER_AUTH_PROFILE';
    END IF;

    -- Add FK_PROFILE_USER_AUTH if not exists
    SELECT COUNT(*)
    INTO v_count
    FROM user_constraints
    WHERE constraint_name = 'FK_PROFILE_USER_AUTH'
      AND table_name = 'PROFILE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE PROFILE ADD CONSTRAINT FK_PROFILE_USER_AUTH FOREIGN KEY (id) REFERENCES USER_AUTH(user_id)';
    END IF;

    -- Add FK_ADMIN_PROFILE_USER_AUTH if not exists
    SELECT COUNT(*)
    INTO v_count
    FROM user_constraints
    WHERE constraint_name = 'FK_ADMIN_PROFILE_USER_AUTH'
      AND table_name = 'ADMIN_PROFILE';

    IF v_count = 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADMIN_PROFILE ADD CONSTRAINT FK_ADMIN_PROFILE_USER_AUTH FOREIGN KEY (id) REFERENCES USER_AUTH(user_id)';
    END IF;
END;
/
