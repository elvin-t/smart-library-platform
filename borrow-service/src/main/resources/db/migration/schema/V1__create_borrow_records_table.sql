CREATE TABLE borrow_records (
    id BIGSERIAL PRIMARY KEY,

    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,

    borrowed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    due_date TIMESTAMP NOT NULL,
    returned_at TIMESTAMP,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT borrow_status_check
        CHECK (status IN ('BORROWED', 'RETURNED', 'OVERDUE')),

    CONSTRAINT borrow_due_date_check
        CHECK (due_date > borrowed_at)
);

CREATE INDEX idx_borrow_records_user_id ON borrow_records(user_id);
CREATE INDEX idx_borrow_records_book_id ON borrow_records(book_id);
CREATE INDEX idx_borrow_records_status ON borrow_records(status);
CREATE INDEX idx_borrow_records_due_date ON borrow_records(due_date);
CREATE INDEX idx_borrow_records_user_status ON borrow_records(user_id, status);

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_borrow_records_updated_at
    BEFORE UPDATE ON borrow_records
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();