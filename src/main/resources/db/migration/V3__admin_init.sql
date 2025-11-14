CREATE TABLE MS_MENU (
    id              VARCHAR2(36)        NOT NULL,
    menu_name       VARCHAR2(50)        NOT NULL,
    menu_action     VARCHAR2(50)        NOT NULL,
    action_flow     VARCHAR2(50)        NOT NULL,
    reason          NUMBER(1,0)         NOT NULL,
    created_time    TIMESTAMP           NOT NULL,
    created_by      VARCHAR2(50)        NOT NULL,
    updated_time    TIMESTAMP           NOT NULL,
    updated_by      VARCHAR2(50)        NOT NULL,
    CONSTRAINT PK_MS_MENU PRIMARY KEY (id),
    CONSTRAINT UQ_MS_MENU_MENU_ACTION UNIQUE(menu_action)
);

CREATE TABLE ADMIN_PROFILE(
    id                  VARCHAR2(36)        NOT NULL,
    npp                 VARCHAR2(20)        NOT NULL,
    full_name           VARCHAR2(60)        NOT NULL,
    role_id             VARCHAR2(36)        NOT NULL,
    email_address       VARCHAR2(50)        NOT NULL,
    created_time        TIMESTAMP           NOT NULL,
    created_by          VARCHAR2(50)        NOT NULL,
    updated_time        TIMESTAMP           NOT NULL,
    updated_by          VARCHAR2(50)        NOT NULL,
    CONSTRAINT PK_ADMIN_PROFILE PRIMARY KEY (id),
    CONSTRAINT UQ_ADMIN_PROFILE_NPP UNIQUE (npp),
    CONSTRAINT FK_ADMIN_PROFILE FOREIGN KEY (role_id) REFERENCES ROLE_MANAGEMENT(id),
    CONSTRAINT UQ_ADMIN_PROFILE_EMAIL_ADDRESS UNIQUE (email_address)
);


CREATE TABLE TR_ACTIVITY(
    id                          VARCHAR2(36)        NOT NULL,
    meta_data                   CLOB    DEFAULT EMPTY_CLOB(),
    maker_id                    VARCHAR2(36)        NOT NULL,
    checker_id                  VARCHAR2(36),
    approver_id                 VARCHAR2(36),
    identifier                  VARCHAR2(100),
    menu_id                     VARCHAR2(36)        NOT NULL,
    status                      VARCHAR2(50),
    updated_time_checker        TIMESTAMP,
    updated_time_approver       TIMESTAMP,
    created_time                TIMESTAMP           NOT NULL,
    created_by                  VARCHAR2(100)        NOT NULL,
    updated_time                TIMESTAMP           NOT NULL,
    updated_by                  VARCHAR2(100)       NOT NULL,
    CONSTRAINT PK_TR_ACTIVITY PRIMARY KEY (id),
    CONSTRAINT FK_TR_ACTIVITY_MAKER FOREIGN KEY (maker_id) REFERENCES ADMIN_PROFILE(id),
    CONSTRAINT FK_TR_ACTIVITY_CHECKER FOREIGN KEY (checker_id) REFERENCES ADMIN_PROFILE(id),
    CONSTRAINT FK_TR_ACTIVITY_APPROVER FOREIGN KEY (approver_id) REFERENCES ADMIN_PROFILE(id),
    CONSTRAINT FK_TR_ACTIVITY_MENU FOREIGN KEY (menu_id) REFERENCES MS_MENU(id)
);
