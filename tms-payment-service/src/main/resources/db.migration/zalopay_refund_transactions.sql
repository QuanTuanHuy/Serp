-- Table: zalopay_refund_transactions
CREATE TABLE zalopay_refund_transactions (
                                             id BIGSERIAL PRIMARY KEY,
                                             m_refund_id VARCHAR(50) NOT NULL UNIQUE,
                                             zp_trans_id VARCHAR(255) NOT NULL, -- Theo Entity là String
                                             app_trans_id VARCHAR(40),
                                             refund_id BIGINT,
                                             amount BIGINT NOT NULL,
                                             refund_fee_amount BIGINT,
                                             description VARCHAR(256) NOT NULL,
                                             status VARCHAR(20) NOT NULL,
                                             return_code INTEGER,
                                             return_message VARCHAR(255),
                                             sub_return_code INTEGER,
                                             sub_return_message VARCHAR(500),
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                             refunded_at TIMESTAMP
);

-- Indexes for zalopay_refund_transactions
CREATE UNIQUE INDEX idx_m_refund_id ON zalopay_refund_transactions(m_refund_id);
CREATE INDEX idx_refund_zp_trans_id ON zalopay_refund_transactions(zp_trans_id);
CREATE INDEX idx_refund_status ON zalopay_refund_transactions(status);
CREATE INDEX idx_refund_app_trans_id ON zalopay_refund_transactions(app_trans_id);