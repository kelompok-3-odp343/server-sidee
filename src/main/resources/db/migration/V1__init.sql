-- PROFILE
CREATE TABLE PROFILE (
  id             VARCHAR2(36)     NOT NULL,
  cif            VARCHAR2(21)     NOT NULL,
  username       VARCHAR2(30),
  first_name     VARCHAR2(21)     NOT NULL,
  middle_name    VARCHAR2(65),
  last_name      VARCHAR2(65),
  dob            TIMESTAMP,
  phone_number   NUMBER(15,0)     NOT NULL,
  email_address  VARCHAR2(50)     NOT NULL,
  nik            VARCHAR2(32)     NOT NULL,
  created_by     VARCHAR2(50)     NOT NULL,
  created_time   TIMESTAMP        NOT NULL,
  updated_by     VARCHAR2(50)     NOT NULL,
  updated_time   TIMESTAMP        NOT NULL,
  CONSTRAINT PK_PROFILE PRIMARY KEY (id),
  CONSTRAINT UQ_PROFILE_CIF UNIQUE (cif),
  CONSTRAINT UQ_PROFILE_EMAIL UNIQUE (email_address)
);

CREATE INDEX IDX_PROFILE_USERNAME ON PROFILE(username);

-- role_management (penetapan role per user)
CREATE TABLE role_management (
  id            VARCHAR2(36)   NOT NULL,
  user_id       VARCHAR2(36)   NOT NULL, -- FK ke PROFILE.id
  role_name     VARCHAR2(21)   NOT NULL,
  created_by    VARCHAR2(50)   NOT NULL,
  created_time  TIMESTAMP      NOT NULL,
  updated_by    VARCHAR2(50)   NOT NULL,
  updated_time  TIMESTAMP      NOT NULL,
  CONSTRAINT PK_ROLE_MGMT PRIMARY KEY (id),
  CONSTRAINT FK_ROLE_MGMT_PROFILE FOREIGN KEY (user_id) REFERENCES PROFILE(id)
);

CREATE UNIQUE INDEX UQ_ROLE_PER_USER ON role_management(user_id, role_name);

-- USER_AUTH
CREATE TABLE USER_AUTH (
  user_id          VARCHAR2(36)   NOT NULL, -- FK to PROFILE.id
  username         VARCHAR2(30),            -- shadow username (opsional)
  email_address    VARCHAR2(50)   NOT NULL,
  role_id          VARCHAR2(21)   NOT NULL, -- FK ke role_management.id
  password         VARCHAR2(250)  NOT NULL, -- hashed (BCrypt)
  is_user_blocked  NUMBER(1,0),
  CONSTRAINT PK_USER_AUTH PRIMARY KEY (user_id),
  CONSTRAINT FK_USER_AUTH_PROFILE FOREIGN KEY (user_id) REFERENCES PROFILE(id),
  CONSTRAINT FK_USER_AUTH_ROLE FOREIGN KEY (role_id) REFERENCES role_management(id)
);

CREATE INDEX IDX_USER_AUTH_USERNAME ON USER_AUTH(username);

-- ACCOUNT
CREATE TABLE ACCOUNT (
    id                  VARCHAR2(36)    NOT NULL,
    user_id             VARCHAR2(36)    NOT NULL,
    account_number      VARCHAR2(21)    NOT NULL,
    cif                 VARCHAR2(21)    NOT NULL,
    effective_balance   NUMBER(15,4)    NOT NULL,
    account_type        VARCHAR2(65)    NOT NULL,
    product_name        VARCHAR2(100)   NOT NULL,
    sub_cat             VARCHAR2(65)    NOT NULL,
    currency_code       VARCHAR2(10)    NOT NULL,
    account_holder_name VARCHAR2(100)    NOT NULL,
    is_main_account     NUMBER(1,0)     NOT NULL,
    account_status      VARCHAR2(20)    NOT NULL,
    is_deleted          NUMBER(1,0)     DEFAULT 0 NOT NULL,
    created_by          VARCHAR2(50)    NOT NULL,
    created_time        TIMESTAMP       DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by          VARCHAR2(50)    NOT NULL,
    updated_time        TIMESTAMP       NOT NULL,
    CONSTRAINT PK_ACCOUNT PRIMARY KEY (id),
    CONSTRAINT FK_ACCOUNT_USER FOREIGN KEY (user_id) REFERENCES PROFILE(id),
    CONSTRAINT CK_ACCOUNT_MAIN CHECK (is_main_account IN (0,1)),
    CONSTRAINT CK_ACCOUNT_DELETED CHECK (is_deleted IN (0,1)),
    CONSTRAINT CK_ACCOUNT_TYPE CHECK (account_type IN ('LFG', 'SVG', 'DEP', 'DPLK')),
    CONSTRAINT CK_ACCOUNT_STATUS CHECK (account_status IN ('BUKA','BARU','DORM','TUTUP'))
);

CREATE INDEX IDX_ACCOUNT_USER_ID ON ACCOUNT(user_id);
CREATE INDEX IDX_ACCOUNT_NUMBER  ON ACCOUNT(account_number);

-- CARD_PRODUCT
CREATE TABLE CARD_PRODUCT (
    id                   VARCHAR2(21)   NOT NULL,
    product_type         VARCHAR2(20)   NOT NULL,
    sub_cat              VARCHAR2(20)   NOT NULL,
    product_name         VARCHAR2(50)   NOT NULL,
    is_eligible_product  NUMBER(1,0)    DEFAULT 1,
    is_deleted           NUMBER(1,0)    DEFAULT 0,
    created_by           VARCHAR2(50)   NOT NULL,
    created_time         TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by           VARCHAR2(50)   NOT NULL,
    updated_time         TIMESTAMP      NOT NULL,
    CONSTRAINT PK_CARD_PRODUCT PRIMARY KEY (id),
    CONSTRAINT CK_CARD_ELIGIBLE CHECK (is_eligible_product IN (0,1)),
    CONSTRAINT CK_CARD_DELETED CHECK (is_deleted IN (0,1))
);

CREATE INDEX IDX_CARD_PRODUCT_TYPE ON CARD_PRODUCT(product_type);
CREATE INDEX IDX_CARD_PRODUCT_NAME ON CARD_PRODUCT(product_name);

-- TIME_DEPOSIT_ACCOUNT
CREATE TABLE TIME_DEPOSIT_ACCOUNT (
    id                         VARCHAR2(36)   NOT NULL,
    user_id                    VARCHAR2(36)   NOT NULL,
    deposit_account_number     VARCHAR2(21)   NOT NULL,
    cif                        VARCHAR2(21)   NOT NULL,
    effective_balance          NUMBER(15,0),
    account_type               VARCHAR2(65)   NOT NULL, -- DEP / DPLK
    sub_cat                    VARCHAR2(65)   NOT NULL,
    account_holder_name        VARCHAR2(10)   NOT NULL,
    is_deleted                 NUMBER(1,0)    DEFAULT 0 NOT NULL,
    disbursement_account_number VARCHAR2(21)  NOT NULL,
    currency_code              VARCHAR2(10)   NOT NULL,
    tenor_months               NUMBER(2,0)    NOT NULL,
    maturity_date              TIMESTAMP      NOT NULL,
    interest_rate              NUMBER(5,2)    NOT NULL,
    interest_payment_type      VARCHAR2(20)   NOT NULL, -- rollover, autorollover, etc
    deposit_account_status     VARCHAR2(20)   NOT NULL,
    created_by                 VARCHAR2(50)   NOT NULL,
    created_time               TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by                 VARCHAR2(50)   NOT NULL,
    updated_time               TIMESTAMP      NOT NULL,
    CONSTRAINT PK_TIME_DEPOSIT_ACCOUNT PRIMARY KEY (id),
    CONSTRAINT FK_TDA_USER FOREIGN KEY (user_id) REFERENCES PROFILE(id),
    CONSTRAINT CK_TDA_TYPE CHECK (account_type IN ('DEP','DPLK')),
    CONSTRAINT CK_TDA_DELETED CHECK (is_deleted IN (0,1))
);

CREATE INDEX IDX_TDA_USER_ID ON TIME_DEPOSIT_ACCOUNT(user_id);
CREATE INDEX IDX_TDA_CIF ON TIME_DEPOSIT_ACCOUNT(cif);
CREATE INDEX IDX_TDA_STATUS ON TIME_DEPOSIT_ACCOUNT(deposit_account_status);

-- SPLIT BILL
CREATE TABLE SPLIT_BILL (
    id               VARCHAR2(36)   NOT NULL,
    user_id          VARCHAR2(36)   NOT NULL,
    cif              VARCHAR2(21)   NOT NULL,
    account_number   VARCHAR2(50)   NOT NULL,
    transaction_id   VARCHAR2(21)   NOT NULL,
    split_bill_title VARCHAR2(50)   NOT NULL,
    currency         VARCHAR2(10)   NOT NULL,
    total_amount     NUMBER(18,2)   NOT NULL,
    has_paid         NUMBER(1,0)    DEFAULT 0 NOT NULL,
    payment_time     TIMESTAMP,
    is_deleted       NUMBER(1,0)    DEFAULT 0 NOT NULL,
    created_by       VARCHAR2(50)   NOT NULL,
    created_time     TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by       VARCHAR2(50)   NOT NULL,
    updated_time     TIMESTAMP      NOT NULL,
    CONSTRAINT PK_SPLIT_BILL PRIMARY KEY (id),
    CONSTRAINT FK_SPLIT_BILL_USER FOREIGN KEY (user_id) REFERENCES PROFILE(id),
    CONSTRAINT CK_SPLIT_BILL_PAID CHECK (has_paid IN (0,1)),
    CONSTRAINT CK_SPLIT_BILL_DELETED CHECK (is_deleted IN (0,1))
);

CREATE INDEX IDX_SPLIT_BILL_USER ON SPLIT_BILL(user_id);
CREATE INDEX IDX_SPLIT_BILL_ACC  ON SPLIT_BILL(account_number);

-- SPLIT_BILL_MEMBER
CREATE TABLE SPLIT_BILL_MEMBER (
    id              VARCHAR2(36)   NOT NULL,
    split_bill_id   VARCHAR2(36)   NOT NULL,
    user_id         VARCHAR2(36)   NOT NULL,
    amount_share    NUMBER(18,2)   NOT NULL,
    has_paid        NUMBER(1,0)    DEFAULT 0 NOT NULL,
    payment_date    TIMESTAMP,
    member_name     VARCHAR2(50)   NOT NULL,
    is_deleted      NUMBER(1,0)    DEFAULT 0 NOT NULL,
    created_by      VARCHAR2(50)   NOT NULL,
    created_time    TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by      VARCHAR2(50)   NOT NULL,
    updated_time    TIMESTAMP      NOT NULL,
    CONSTRAINT PK_SPLIT_BILL_MEMBER PRIMARY KEY (id),
    CONSTRAINT FK_SPLIT_MEMBER_BILL FOREIGN KEY (split_bill_id) REFERENCES SPLIT_BILL(id),
    CONSTRAINT CK_SPLIT_MEMBER_PAID CHECK (has_paid IN (0,1)),
    CONSTRAINT CK_SPLIT_MEMBER_DELETED CHECK (is_deleted IN (0,1))
);

CREATE INDEX IDX_SPLIT_MEMBER_BILL ON SPLIT_BILL_MEMBER(split_bill_id);

-- TRX_CATEGORY
CREATE TABLE TRX_CATEGORY (
    id              VARCHAR2(21)   NOT NULL,
    category_name   VARCHAR2(21),
    created_by      VARCHAR2(50)   NOT NULL,
    created_time    TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by      VARCHAR2(50)   NOT NULL,
    updated_time    TIMESTAMP      NOT NULL,
    CONSTRAINT PK_TRX_CATEGORY PRIMARY KEY (id)
);

-- TRX_HISTORY
CREATE TABLE TRX_HISTORY (
    id                            VARCHAR2(36)   NOT NULL,
    user_id                       VARCHAR2(36)   NOT NULL,
    account_number                VARCHAR2(21)   NOT NULL,
    transaction_date              TIMESTAMP      DEFAULT SYSTIMESTAMP,
    transaction_amount            NUMBER(15,2)   NOT NULL,
    transaction_description       VARCHAR2(100),
    transaction_type              VARCHAR2(65)   NOT NULL, -- DEBIT / CREDIT
    party_name                    VARCHAR2(55)   NOT NULL,
    party_detail                  VARCHAR2(55)   NOT NULL,
    payment_method                VARCHAR2(55),
    debit_credit                  VARCHAR2(20)   NOT NULL,
    split_bill_id                 VARCHAR2(36)   NOT NULL,
    ref_id                       VARCHAR2(22)   NOT NULL,
    created_by                    VARCHAR2(50)   NOT NULL,
    created_time                  TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
    updated_by                    VARCHAR2(50)   NOT NULL,
    updated_time                  TIMESTAMP      NOT NULL,
    CONSTRAINT PK_TRX_HISTORY PRIMARY KEY (id),
    CONSTRAINT FK_TRX_HISTORY_USER FOREIGN KEY (user_id) REFERENCES PROFILE(id),
    CONSTRAINT FK_TRX_HISTORY_SPLIT_BILL FOREIGN KEY (split_bill_id) REFERENCES SPLIT_BILL(id)
);

CREATE INDEX IDX_TRX_HISTORY_USER ON TRX_HISTORY(user_id);
CREATE INDEX IDX_TRX_HISTORY_ACC  ON TRX_HISTORY(account_number);

-- LIFEGOALS_ACCOUNT
CREATE TABLE LIFEGOALS_ACCOUNT (
    id                      VARCHAR2(36)     NOT NULL,
    user_id                 VARCHAR2(36)     NOT NULL,
    cif                     VARCHAR2(21)     NOT NULL,
    account_number          VARCHAR2(21),
    lifegoals_name          VARCHAR2(255),
    lifegoals_category_name VARCHAR2(100),
    account_deposit         NUMBER(15,3),
    lifegoals_trx_creation_id VARCHAR2(21),
    account_target_amount   NUMBER(15,3),
    estimation_amount       NUMBER(15,3),
    lifegoals_description   VARCHAR2(255),
    maturity_date           TIMESTAMP,
    lifegoals_duration      NUMBER(3),
    created_time            TIMESTAMP        NOT NULL,
    created_by              VARCHAR2(50)     NOT NULL,
    updated_time            TIMESTAMP        NOT NULL,
    updated_by              VARCHAR2(50)     NOT NULL,

    CONSTRAINT PK_LIFEGOALS_ACCOUNT PRIMARY KEY (id),
    CONSTRAINT FK_LIFEGOALS_ACCOUNT_PROFILE FOREIGN KEY (user_id)
        REFERENCES PROFILE(id)
);

CREATE INDEX IDX_LIFEGOALS_ACCOUNT_USER_CIF ON LIFEGOALS_ACCOUNT(user_id, cif);
CREATE INDEX IDX_LIFEGOALS_ACCOUNT_NUMBER ON LIFEGOALS_ACCOUNT(account_number);

-- DPLK_ACCOUNT
CREATE TABLE DPLK_ACCOUNT (
  id                    VARCHAR2(36)   NOT NULL,
  user_id               VARCHAR2(36)   NOT NULL,            -- FK -> PROFILE(id) [36]
  cif                   VARCHAR2(21)   NOT NULL,
  account_number        VARCHAR2(21),                       -- FK -> ACCOUNT(account_number)
  account_number_dplk   VARCHAR2(21),
  currency_code         VARCHAR2(10),
  dplk_product_name     VARCHAR2(100),
  dplk_initial_deposit  NUMBER(15,3),
  product_type          VARCHAR2(20),
  created_time          TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
  created_by            VARCHAR2(50)   NOT NULL,
  updated_time          TIMESTAMP      DEFAULT SYSTIMESTAMP NOT NULL,
  updated_by            VARCHAR2(50)   NOT NULL,
  CONSTRAINT PK_DPLK_ACCOUNT PRIMARY KEY (id),
  CONSTRAINT FK_DPLK_USER FOREIGN KEY (user_id) REFERENCES PROFILE(id)
);

CREATE INDEX IDX_DPLK_USER_ID          ON DPLK_ACCOUNT (user_id);
CREATE INDEX IDX_DPLK_CIF              ON DPLK_ACCOUNT (cif);
CREATE INDEX IDX_DPLK_ACCNO_DPLK       ON DPLK_ACCOUNT (account_number_dplk);
CREATE INDEX IDX_DPLK_ACCNO_REF        ON DPLK_ACCOUNT (account_number);
