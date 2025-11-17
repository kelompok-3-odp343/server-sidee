-- Members for Split Bill 1
INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM001A', 'SB001A1B2C3D4E5F6G7H', 'P001', 10000, 0, NULL, 'Ulion Pardede', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM002A', 'SB001A1B2C3D4E5F6G7H', 'P001', 10000, 0, NULL, 'Andre Setiawan', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM003A', 'SB001A1B2C3D4E5F6G7H', 'P001', 10000, 0, NULL, 'Maya Siregar', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM004A', 'SB001A1B2C3D4E5F6G7H', 'P001', 10000, 0, NULL, 'Reza Fadillah', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM005A', 'SB001A1B2C3D4E5F6G7H', 'P001', 10000, 0, NULL, 'Tania Wijaya', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

-- Members for Split Bill 2
INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM006A', 'SB002A1B2C3D4E5F6G7H', 'P001', 15000, 0, NULL, 'Budi Santoso', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM007A', 'SB002A1B2C3D4E5F6G7H', 'P001', 15000, 0, NULL, 'Rani Amelia', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM008A', 'SB002A1B2C3D4E5F6G7H', 'P001', 15000, 0, NULL, 'David Gunawan', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM009A', 'SB002A1B2C3D4E5F6G7H', 'P001', 15000, 0, NULL, 'Nina Simanjuntak', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);

INSERT INTO WANDOOR.SPLIT_BILL_MEMBER (id, split_bill_id, user_id, amount_share, has_paid, payment_date, member_name, is_deleted, created_by, created_time, updated_by, updated_time)
VALUES ('SBM010A', 'SB002A1B2C3D4E5F6G7H', 'P001', 15000, 0, NULL, 'Ferdi Putra', 0, 'Nina', SYSDATE, 'Nina', SYSDATE);


SELECT count(*) FROM wandoor.TRX_HISTORY th GROUP BY account_number;

SELECT count(*) FROM wandoor.TRX_HISTORY th;
