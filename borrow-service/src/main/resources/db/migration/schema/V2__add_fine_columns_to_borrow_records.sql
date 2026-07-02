ALTER TABLE borrow_records
ADD COLUMN overdue_days INTEGER NOT NULL DEFAULT 0,
ADD COLUMN fine_amount NUMERIC(10,2) NOT NULL DEFAULT 0.00,
ADD COLUMN fine_paid BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN fine_paid_at TIMESTAMP;

CREATE INDEX idx_borrow_records_fine_paid ON borrow_records(fine_paid);
CREATE INDEX idx_borrow_records_fine_amount ON borrow_records(fine_amount);
