-- MS MENU

INSERT INTO MS_MENU
(id, menu_name, menu_action, action_flow, reason, created_time, created_by, updated_time, updated_by)
VALUES(SYS_GUID(), 'USER_MANAGEMENT', 'BLOCK_USER', 'CHECKER_AND_APPROVER', 1, SYSDATE, 'SYSTEM', SYSDATE, 'SYSTEM');


INSERT INTO MS_MENU
(id, menu_name, menu_action, action_flow, reason, created_time, created_by, updated_time, updated_by)
VALUES(SYS_GUID(), 'USER_MANAGEMENT', 'UNBLOCK', 'CHECKER_AND_APPROVER', 1, SYSDATE, 'SYSTEM', SYSDATE, 'SYSTEM');


-- ADMIN_PROFILE
INSERT INTO ADMIN_PROFILE
(id, npp, full_name, email_address, created_time, created_by, updated_time, updated_by, role_id)
VALUES('ADM002', '64889','Wira Natanael Uli', 'wira.natanael.uli@bni.co.id', SYSDATE, 'SYSTEM', SYSDATE, 'SYSTEM', 'R002');
INSERT INTO ADMIN_PROFILE
(id, npp, full_name, email_address, created_time, created_by, updated_time, updated_by, role_id)
VALUES('ADM003', '64888','Blestro', 'blestro@bni.co.id', SYSDATE, 'SYSTEM', SYSDATE, 'SYSTEM', 'R003');
INSERT INTO ADMIN_PROFILE
(id, npp, full_name, email_address, created_time, created_by, updated_time, updated_by, role_id)
VALUES('ADM004', '64887','Syamsul Rizal', 'syamsul.rizal@bni.co.id', SYSDATE, 'SYSTEM', SYSDATE, 'SYSTEM', 'R004');

-- TR_ACTIVITY_SAMPLE
INSERT INTO TR_ACTIVITY
(id, meta_data, maker_id, checker_id, approver_id, identifier, menu_id, status, updated_time_checker, updated_time_approver, created_time, created_by, updated_time, updated_by)
VALUES(ADM004, EMPTY_CLOB(), '43590E9E955A03D5E063020013ACDA93', '43590E9E955903D5E063020013ACDA93', NULL,'P001', '43590E9E955703D5E063020013ACDA93', 'PENDING_CHECKER', NULL, NULL, SYSDATE, 'SYSTEM', SYSDATE, 'SYSTEM');

