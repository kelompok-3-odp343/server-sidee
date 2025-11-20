ALTER TABLE TRX_HISTORY MODIFY party_detail NULL;

ALTER TABLE TRX_HISTORY DROP CONSTRAINT FK_TRX_HISTORY_SPLIT_BILL;

ALTER TABLE TRX_HISTORY MODIFY split_bill_id null;

DECLARE
    col_count INTEGER;
BEGIN
    -- Check if the column exists
    SELECT COUNT(*)
    INTO col_count
    FROM user_tab_cols
    WHERE table_name = 'ADMIN_PROFILE'
      AND column_name = 'EMAIL_ADRESS';   -- pastikan nama sesuai (case-insensitive)

    -- If exists then drop it
    IF col_count > 0 THEN
        EXECUTE IMMEDIATE 'ALTER TABLE ADMIN_PROFILE DROP COLUMN EMAIL_ADRESS';
    END IF;
END;
/