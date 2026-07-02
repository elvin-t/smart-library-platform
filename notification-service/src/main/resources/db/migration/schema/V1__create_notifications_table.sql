CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    email VARCHAR(255),

    type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,

    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,

    book_id BIGINT,
    borrow_record_id BIGINT,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP,

    CONSTRAINT notification_type_check
        CHECK (type IN (
            'BORROW_CONFIRMATION',
            'RETURN_CONFIRMATION',
            'DUE_DATE_REMINDER',
            'OVERDUE_REMINDER'
        )),

    CONSTRAINT notification_channel_check
        CHECK (channel IN ('EMAIL', 'SMS', 'IN_APP')),

    CONSTRAINT notification_status_check
        CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_borrow_record_id ON notifications(borrow_record_id);