-- ============================================================================
-- LightBorrow 数据库初始化脚本（Spring Boot 自动执行）
-- 使用 PostgreSQL 方言（COMMENT ON 语法）。测试环境通过 sql.init.mode=never 跳过。
-- ============================================================================

-- 资产表
CREATE TABLE IF NOT EXISTS asset (
    id          SERIAL PRIMARY KEY,
    code        VARCHAR(64)  NOT NULL UNIQUE,
    name        VARCHAR(256) NOT NULL,
    description TEXT,
    status      VARCHAR(32)  NOT NULL DEFAULT 'available',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP
);

COMMENT ON TABLE  asset      IS 'IT 资产';
COMMENT ON COLUMN asset.code IS '资产编码，如 IT-001';
COMMENT ON COLUMN asset.status IS '可用状态: available / borrowed / maintenance / retired';

-- 借用记录表
CREATE TABLE IF NOT EXISTS borrow (
    id                 SERIAL PRIMARY KEY,
    user_id            VARCHAR(128) NOT NULL,
    asset_id           INTEGER      NOT NULL REFERENCES asset(id),
    reason             TEXT,
    expected_return_at DATE,
    status             VARCHAR(32)  NOT NULL DEFAULT 'pending',
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_borrow_user_id  ON borrow(user_id);
CREATE INDEX IF NOT EXISTS idx_borrow_status   ON borrow(status);

COMMENT ON TABLE  borrow              IS '借用记录';
COMMENT ON COLUMN borrow.user_id      IS '借用人 ID（来自前端 sessionId）';
COMMENT ON COLUMN borrow.asset_id     IS '关联资产';
COMMENT ON COLUMN borrow.status       IS '状态: pending / active / cancelled / returned';

-- 转借记录表
CREATE TABLE IF NOT EXISTS transfer (
    id            SERIAL PRIMARY KEY,
    from_user_id  VARCHAR(128) NOT NULL,
    borrow_id     INTEGER      NOT NULL REFERENCES borrow(id),
    to_user_id    VARCHAR(128) NOT NULL,
    status        VARCHAR(32)  NOT NULL DEFAULT 'pending',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_transfer_to_user_id ON transfer(to_user_id);
CREATE INDEX IF NOT EXISTS idx_transfer_status     ON transfer(status);

COMMENT ON TABLE  transfer              IS '转借记录';
COMMENT ON COLUMN transfer.from_user_id IS '转出人';
COMMENT ON COLUMN transfer.borrow_id    IS '关联的借用记录';
COMMENT ON COLUMN transfer.to_user_id   IS '接收人';
COMMENT ON COLUMN transfer.status       IS '状态: pending / confirmed / rejected';

-- 用户画像表（Memory 模块）
CREATE TABLE IF NOT EXISTS user_profile (
    user_id       VARCHAR(128) PRIMARY KEY,
    profile_data  TEXT,
    updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

COMMENT ON TABLE  user_profile           IS '用户画像数据';
COMMENT ON COLUMN user_profile.user_id   IS '用户 ID';
COMMENT ON COLUMN user_profile.profile_data IS '画像 JSON 数据';
