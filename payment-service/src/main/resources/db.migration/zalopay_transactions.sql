-- Table: zalopay_transactions
CREATE TABLE zalopay_transactions (
                                      id BIGSERIAL PRIMARY KEY,
                                      app_trans_id VARCHAR(40) NOT NULL UNIQUE,
                                      zp_trans_id BIGINT,
                                      app_user VARCHAR(50) NOT NULL,
                                      amount BIGINT NOT NULL,
                                      description VARCHAR(256) NOT NULL,
                                      items TEXT,
                                      embed_data TEXT,
                                      bank_code VARCHAR(20),
                                      zp_trans_token VARCHAR(255),
                                      order_url VARCHAR(500),
                                      qr_code TEXT,
                                      status VARCHAR(20) NOT NULL,
                                      return_code INTEGER,
                                      return_message VARCHAR(255),
                                      sub_return_code INTEGER,
                                      sub_return_message VARCHAR(500),
                                      channel INTEGER,
                                      user_fee_amount BIGINT,
                                      discount_amount BIGINT,
                                      email VARCHAR(100),
                                      title VARCHAR(256),
                                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      paid_at TIMESTAMP
);

-- Indexes for zalopay_transactions
CREATE UNIQUE INDEX idx_app_trans_id ON zalopay_transactions(app_trans_id);
CREATE INDEX idx_zp_trans_id ON zalopay_transactions(zp_trans_id);
CREATE INDEX idx_status ON zalopay_transactions(status);