--  insert to profile
BEGIN
    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P003', 'CIF003', 'natanaelwira', 'Wira', 'Natanael', 'Uli',
        TO_DATE('1997-09-10', 'YYYY-MM-DD'),
        '6282126145544', 'wira.natanael.u@gmail.com', '32732009109700006',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P004', 'CIF004', 'raihan', 'Raihan', 'Aris', 'Darmawan',
        TO_DATE('2002-01-07', 'YYYY-MM-DD'),
        '62895378072872', 'raihanaris31@gmail.com', '3374160701020003',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P005', 'CIF005', 'adammal', 'Muhammad', 'Adam', 'Alhafizh',
        TO_DATE('2002-08-20', 'YYYY-MM-DD'),
        '6282178230792', 'adamalhafizh23@gmail.com', '3175062305021001',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P006', 'CIF006', 'ridwan', 'Ridwan', 'Surya', 'Ghanu',
        TO_DATE('2002-03-21', 'YYYY-MM-DD'),
        '81311824315', 'ridwansuryaghani@gmail.com', '3674012103040011',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P007', 'CIF007', 'erlangga', 'Erlangga', 'Wahyu', 'Utomo',
        TO_DATE('2001-10-28', 'YYYY-MM-DD'),
        '6282234577260', 'erlanggawahyu21@gmail.com', '3576022810010002',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P008', 'CIF008', 'khairuddin', 'Khairuddin', NULL, 'Nasty',
        TO_DATE('2002-06-26', 'YYYY-MM-DD'),
        '6287860270182', 'nastykhairuddin26@gmail.com', '1601062606020001',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P009', 'CIF009', 'dellapsptaa', 'Della', NULL, 'Puspita',
        TO_DATE('2002-08-30', 'YYYY-MM-DD'),
        '6281253854053', 'dellapsptaaa@gmail.com', '3277017008020004',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    INSERT INTO wandoor.PROFILE (
        id, cif, username, first_name, middle_name, last_name,
        dob, phone_number, email_address, nik,
        created_by, created_time, updated_time, updated_by
    ) VALUES (
        'P010', 'CIF010', 'dery', 'Dery', 'Syams', 'Ahnaf',
        TO_DATE('2002-10-12', 'YYYY-MM-DD'),
        '6285784362341', 'ahnafdery@gmail.com', '3578231210020023',
        'SYSTEM', SYSDATE, SYSDATE, 'SYSTEM'
    );

    COMMIT;
END;


-- insert into user_auth
BEGIN
    FOR i IN 3..10 LOOP
        DECLARE
            v_user_id    VARCHAR2(10) := 'P' || LPAD(i, 3, '0');  -- contoh: P003–P010
            v_email      VARCHAR2(200);
            v_password   VARCHAR2(100) := '$2a$10$wYwXawYF1YAfK0WDbFjV3e0wCGFeM53SRCOQOdwRZljxRzJNHpQZK';
            v_role_id    VARCHAR2(20) := 'R002';  
        BEGIN
            -- Ambil email dari tabel PROFILE
            BEGIN
                SELECT email_address
                INTO v_email
                FROM wandoor.PROFILE
                WHERE id = v_user_id;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_email := v_user_id || '@example.com';  -- fallback jika tidak ada email
            END;

            -- Insert ke tabel USER_AUTH
            INSERT INTO wandoor.USER_AUTH (
                user_id,
                username,
                email_address,
                role_id,
                password,
                is_user_blocked
            )
            VALUES (
                v_user_id,     -- USER_ID (misal P003)
                v_user_id,     -- USERNAME (sama)
                v_email,       -- EMAIL dari PROFILE
                v_role_id,     -- ROLE_ID = R002
                v_password,    -- PASSWORD hash
                0              -- IS_USER_BLOCKED = 0 (aktif)
            );
        END;
    END LOOP;

    COMMIT;
END;


--  insert to table ACCOUNT
DECLARE
    -- Fungsi pembuat NanoID 12 karakter
    FUNCTION gen_nanoid RETURN VARCHAR2 IS
        chars CONSTANT VARCHAR2(62) := 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        nanoid VARCHAR2(12);
    BEGIN
        FOR i IN 1..12 LOOP
            nanoid := nanoid || SUBSTR(chars, TRUNC(DBMS_RANDOM.VALUE(1, 63)), 1);
        END LOOP;
        RETURN nanoid;
    END;
BEGIN
    -- Loop untuk user P001–P010
    FOR i IN 1..10 LOOP
        DECLARE
            v_user_id       VARCHAR2(10) := 'P' || LPAD(i, 3, '0');
            v_cif           VARCHAR2(10) := 'CIF' || LPAD(i, 3, '0');
            v_full_name     VARCHAR2(200);
            v_account_type  VARCHAR2(4);
            v_type_code     NUMBER;
            v_sub_cat       VARCHAR2(3);
            v_id            VARCHAR2(12);
            v_account_number VARCHAR2(30);
        BEGIN
            -- Ambil nama user dari tabel PROFILE
            BEGIN
                SELECT TRIM(NVL(first_name, ''))
                INTO v_full_name
                FROM wandoor.PROFILE
                WHERE id = v_user_id;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_full_name := 'User ' || v_user_id;
            END;

            -- Loop product type
            FOR product IN (
                SELECT column_value AS type_name
                FROM TABLE(SYS.ODCIVARCHAR2LIST('SAV', 'DEP', 'DPLK', 'LFG'))
            ) LOOP
                v_account_type := product.type_name;

                -- Tetapkan kode numeric untuk tiap tipe akun
                v_type_code := CASE v_account_type
                                 WHEN 'SAV' THEN 1
                                 WHEN 'DEP' THEN 2
                                 WHEN 'DPLK' THEN 3
                                 WHEN 'LFG' THEN 4
                               END;

                -- Loop 5 akun per product
                FOR k IN 1..5 LOOP
                    v_id := gen_nanoid();
                    v_sub_cat := LPAD(TRUNC(DBMS_RANDOM.VALUE(1, 4)), 3, '0');

                    -- Pastikan account_number unik
                    v_account_number := '12345' || LPAD(i, 3, '0') || v_type_code || LPAD(k, 2, '0');

                    INSERT INTO wandoor.ACCOUNT (
                        id,
                        user_id,
                        account_number,
                        cif,
                        effective_balance,
                        account_type,
                        sub_cat,
                        currency_code,
                        account_holder_name,
                        is_main_account,
                        account_status,
                        is_deleted,
                        created_by,
                        created_time,
                        updated_time,
                        updated_by
                    )
                    VALUES (
                        v_id,                        -- NanoID unik
                        v_user_id,
                        v_account_number,             -- ✅ unik untuk tiap kombinasi user+type+k
                        v_cif,
                        1000000 * (k + DBMS_RANDOM.VALUE(1, 5)),
                        v_account_type,
                        v_sub_cat,
                        'IDR',
                        v_full_name,
                        CASE WHEN k = 1 THEN 1 ELSE 0 END,
                        'BUKA',
                        0,
                        'SYSTEM',
                        SYSDATE,
                        SYSDATE,
                        'SYSTEM'
                    );
                END LOOP;
            END LOOP;
        END;
    END LOOP;

    COMMIT;
END;


-- insert into dplk account
BEGIN
    FOR rec IN (
        SELECT DISTINCT user_id
        FROM wandoor.ACCOUNT
        WHERE account_type = 'DPLK'
        FETCH FIRST 10 ROWS ONLY
    ) LOOP
        DECLARE
            v_cif                 VARCHAR2(50);
            v_currency_code       VARCHAR2(10);
            v_account_number_sav  VARCHAR2(50);
            v_account_number_dplk VARCHAR2(50);
            v_initial_deposit     NUMBER;
            v_id                  VARCHAR2(30);
        BEGIN
            -- ambil data dari akun DPLK
            SELECT id, cif, account_number, currency_code, effective_balance
            INTO v_id, v_cif, v_account_number_dplk, v_currency_code, v_initial_deposit
            FROM wandoor.ACCOUNT
            WHERE user_id = rec.user_id
              AND account_type = 'DPLK'
              FETCH FIRST 1 ROWS ONLY;

            -- ambil satu akun SAV sebagai connector
            BEGIN
                SELECT account_number
                INTO v_account_number_sav
                FROM wandoor.ACCOUNT
                WHERE user_id = rec.user_id
                  AND account_type = 'SAV'
                  FETCH FIRST 1 ROWS ONLY;
            EXCEPTION
                WHEN NO_DATA_FOUND THEN
                    v_account_number_sav := NULL;
            END;

            -- insert ke DPLK_ACCOUNT
            INSERT INTO wandoor.DPLK_ACCOUNT (
                id,
                user_id,
                cif,
                account_number,
                account_number_dplk,
                currency_code,
                dplk_product_name,
                dplk_initial_deposit,
                product_type,
                created_time,
                created_by,
                updated_time,
                updated_by
            ) VALUES (
                v_id,                                       -- ID dari akun DPLK
                rec.user_id,                                -- USER_ID
                v_cif,                                      -- CIF
                v_account_number_sav,                       -- salah satu akun SAV
                v_account_number_dplk,                      -- akun DPLK utama
                v_currency_code,                            -- currency
                'DPLK FUND ' || rec.user_id,                -- nama produk dummy
                NVL(v_initial_deposit, 0),                  -- deposit awal
                'DPLK',                                     -- tipe produk
                SYSDATE,                                    -- created_time
                'SYSTEM',                                   -- created_by
                SYSDATE,                                    -- updated_time
                'SYSTEM'                                    -- updated_by
            );
        EXCEPTION
            WHEN DUP_VAL_ON_INDEX THEN
                NULL; -- skip jika sudah ada
        END;
    END LOOP;

    COMMIT;
END;


-- insert into lifegoals account
DECLARE
    -- Fungsi pembuat NanoID 12 karakter
    FUNCTION gen_nanoid RETURN VARCHAR2 IS
        chars CONSTANT VARCHAR2(62) := 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
        nanoid VARCHAR2(12);
    BEGIN
        FOR i IN 1..12 LOOP
            nanoid := nanoid || SUBSTR(chars, TRUNC(DBMS_RANDOM.VALUE(1, 63)), 1);
        END LOOP;
        RETURN nanoid;
    END;
BEGIN
    -- Loop akun bertipe LFG dari tabel ACCOUNT
    FOR rec IN (
        SELECT id, user_id, cif, account_number, sub_cat, effective_balance
        FROM wandoor.ACCOUNT
        WHERE account_type = 'LFG'
        FETCH FIRST 50 ROWS ONLY
    ) LOOP
        DECLARE
            v_id_lifegoals     VARCHAR2(12);
            v_trx_creation_id   VARCHAR2(12);
            v_created_time      TIMESTAMP;
            v_duration_months   NUMBER;
            v_maturity_date     TIMESTAMP;
            v_category_name     VARCHAR2(50);
            v_estimation_amount NUMBER;
        BEGIN
            -- Generate ID dan TRX_ID unik
            v_id_lifegoals   := gen_nanoid();
            v_trx_creation_id := gen_nanoid();

            -- Random created_time (1 tahun ke belakang)
            v_created_time := TRUNC(SYSTIMESTAMP - DBMS_RANDOM.VALUE(1, 365));

            -- Pilih random duration (bulan)
            SELECT CASE TRUNC(DBMS_RANDOM.VALUE(1, 6))
                     WHEN 1 THEN 2
                     WHEN 2 THEN 4
                     WHEN 3 THEN 6
                     WHEN 4 THEN 12
                     ELSE 24
                   END
            INTO v_duration_months
            FROM dual;

            -- Hitung maturity_date sebagai TIMESTAMP
            v_maturity_date := ADD_MONTHS(v_created_time, v_duration_months);

            -- Kategori berdasarkan SUB_CAT
            v_category_name := CASE rec.sub_cat
                                  WHEN '001' THEN 'Education'
                                  WHEN '002' THEN 'Retirement'
                                  WHEN '003' THEN 'Vacation'
                                  ELSE 'General'
                               END;

            -- Estimasi kenaikan nilai
            v_estimation_amount := rec.effective_balance * (1 + DBMS_RANDOM.VALUE(0.05, 0.3));

            -- Insert ke LIFEGOALS_ACCOUNT
            INSERT INTO wandoor.LIFEGOALS_ACCOUNT (
                id,
                user_id,
                cif,
                account_number,
                lifegoals_name,
                lifegoals_category_name,
                account_deposit,
                lifegoals_trx_creation_id,
                account_target_amount,
                estimation_amount,
                lifegoals_description,
                maturity_date,              -- ✅ Sekarang TIMESTAMP
                lifegoals_duration,
                created_time,
                created_by,
                updated_time,
                updated_by
            )
            VALUES (
                v_id_lifegoals,                         -- ID unik
                rec.user_id,
                rec.cif,
                rec.account_number,
                'LifeGoals ' || rec.user_id,
                v_category_name,
                rec.effective_balance,
                v_trx_creation_id,
                '50000000',
                v_estimation_amount,
                'Auto generated for ' || rec.user_id,
                v_maturity_date,                        -- ✅ TIMESTAMP langsung
                v_duration_months,
                v_created_time,
                'SYSTEM',
                v_created_time,
                'SYSTEM'
            );
        EXCEPTION
            WHEN DUP_VAL_ON_INDEX THEN
                NULL; -- skip duplikat
        END;
    END LOOP;

    COMMIT;
END;




