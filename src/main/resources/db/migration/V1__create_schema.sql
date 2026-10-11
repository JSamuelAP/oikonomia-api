CREATE TABLE users
(
    id            uuid                     NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name    character varying(100)   NOT NULL,
    last_name     character varying(150)   NOT NULL,
    email         character varying(320)   NOT NULL UNIQUE,
    password_hash character varying(255)   NOT NULL,
    created_at    timestamp with time zone NOT NULL             DEFAULT now(),
    updated_at    timestamp with time zone NOT NULL             DEFAULT now(),
    deleted_at    timestamp with time zone,
    CHECK (email = lower(email))
);


CREATE TABLE refresh_tokens
(
    id          uuid PRIMARY KEY,
    user_id     uuid                     NOT NULL REFERENCES users (id),
    token_hash  character varying(64)    NOT NULL UNIQUE,
    expires_at  timestamp with time zone NOT NULL,
    revoked     boolean                  NOT NULL DEFAULT false,
    created_at  timestamp with time zone NOT NULL DEFAULT now(),
    replaced_by uuid                     REFERENCES refresh_tokens (id) ON DELETE SET NULL
);

CREATE INDEX idx_refresh_tokens_active_by_user
    ON refresh_tokens (user_id) WHERE revoked = false;


CREATE TYPE flow_type AS ENUM ('INCOME', 'EXPENSE');

CREATE TABLE categories
(
    id         uuid                     NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    uuid                     NOT NULL,
    name       character varying(50)    NOT NULL,
    flow_type  flow_type                NOT NULL,
    created_at timestamp with time zone NOT NULL             DEFAULT now(),
    updated_at timestamp with time zone NOT NULL             DEFAULT now(),
    deleted_at timestamp with time zone,
    FOREIGN KEY (user_id) REFERENCES public.users (id)
);

CREATE UNIQUE INDEX idx_categories_unique_active
    ON categories (user_id, name, flow_type) WHERE deleted_at IS NULL;


CREATE TABLE transactions
(
    id               uuid                     NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id          uuid                     NOT NULL,
    category_id      uuid                     NOT NULL,
    amount           numeric(12, 2)           NOT NULL,
    transaction_date date                     NOT NULL,
    notes            character varying(255),
    created_at       timestamp with time zone NOT NULL             DEFAULT now(),
    updated_at       timestamp with time zone NOT NULL             DEFAULT now(),
    deleted_at       timestamp with time zone,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    CHECK (amount > 0)
);

CREATE INDEX idx_transactions_user_date_active
    ON transactions (user_id, transaction_date) WHERE deleted_at IS NULL;


CREATE TABLE monthly_budgets
(
    id              uuid                     NOT NULL PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         uuid                     NOT NULL,
    category_id     uuid                     NOT NULL,
    month           smallint                 NOT NULL,
    year            smallint                 NOT NULL,
    expected_amount numeric(12, 2)           NOT NULL,
    created_at      timestamp with time zone NOT NULL             DEFAULT now(),
    updated_at      timestamp with time zone NOT NULL             DEFAULT now(),
    deleted_at      timestamp with time zone,
    FOREIGN KEY (user_id) REFERENCES users (id),
    FOREIGN KEY (category_id) REFERENCES categories (id),
    CHECK (month >= 1 AND month <= 12
) ,
    CHECK (year >= 2025 AND year <= 2100),
    CHECK (expected_amount > 0)
);

CREATE UNIQUE INDEX idx_monthly_budgets_unique_active
    ON monthly_budgets (user_id, year, month, category_id) WHERE deleted_at IS NULL;