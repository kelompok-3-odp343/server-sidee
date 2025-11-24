---------------------------------------------
-- 1. Make TRX_HISTORY.party_detail nullable
---------------------------------------------
DECLARE
    v_nullable VARCHAR2(1);
BEGIN
    SELECT nullable INTO v_nullable
    FROM user_tab_cols
    WHERE table_name = 'TRX_HISTORY'
      AND column_name = 'PARTY_DETAIL';

    IF v_nullable = 'N' THEN
        EXECUTE IMMEDIATE 'ALTER TABLE TRX_HISTORY MODIFY party_detail DROP NOT NULL';
    END IF;
END;
/
---------------------------------------------
-- 2. Drop FK_TRX_HISTORY_SPLIT_BILL if exists
---------------------------------------------
DECLARE
    v_count NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_count
    FROM user_constraints
    WHERE table_name = 'TRX_HISTORY'
      AND constraint_name = 'FK_TRX_HISTORY_SPLIT_BILL';

    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE TRX_HISTORY DROP CONSTRAINT FK_TRX_HISTORY_SPLIT_BILL';
    END IF;
END;
/
---------------------------------------------
-- 3. Make TRX_HISTORY.split_bill_id nullable
---------------------------------------------
DECLARE
    v_nullable VARCHAR2(1);
BEGIN
    SELECT nullable INTO v_nullable
    FROM user_tab_cols
    WHERE table_name = 'TRX_HISTORY'
      AND column_name = 'SPLIT_BILL_ID';

    IF v_nullable = 'N' THEN
        EXECUTE IMMEDIATE 'ALTER TABLE TRX_HISTORY MODIFY split_bill_id DROP NOT NULL';
    END IF;
END;
/
---------------------------------------------
-- 4. Drop ADMIN_PROFILE.EMAIL_ADRESS or EMAIL_ADDRESS if exists
---------------------------------------------
DECLARE
    v_count INTEGER;
BEGIN
    -- Drop wrong spelling
    SELECT COUNT(*) INTO v_count
    FROM user_tab_cols
    WHERE table_name = 'ADMIN_PROFILE'
      AND column_name = 'EMAIL_ADRESS';

    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADMIN_PROFILE DROP COLUMN EMAIL_ADRESS';
    END IF;

    -- Drop correct spelling
    SELECT COUNT(*) INTO v_count
    FROM user_tab_cols
    WHERE table_name = 'ADMIN_PROFILE'
      AND column_name = 'EMAIL_ADDRESS';

    IF v_count > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADMIN_PROFILE DROP COLUMN EMAIL_ADDRESS';
    END IF;
END;
/
