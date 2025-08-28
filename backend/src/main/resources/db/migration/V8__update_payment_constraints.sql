-- Cập nhật payment_method constraints
ALTER TABLE payments DROP CHECK chk_payment_method;
ALTER TABLE payments 
ADD CONSTRAINT chk_payment_method 
CHECK (payment_method IN ('MOMO', 'VNPAY', 'BANK_TRANSFER', 'CASH', 'CREDIT_CARD'));

-- Cập nhật payment_status constraints
ALTER TABLE payments DROP CHECK chk_payment_status;
ALTER TABLE payments 
ADD CONSTRAINT chk_payment_status 
CHECK (`status` IN ('PENDING', 'PROCESSING', 'SUCCESSFUL', 'FAILED', 'CANCELLED', 'REFUNDED', 'EXPIRED'));

-- Thêm index cho performance
CREATE INDEX idx_payments_invoice_id ON payments(invoice_id);
CREATE INDEX idx_payments_status ON payments(`status`);
CREATE INDEX idx_payments_transaction_code ON payments(transaction_code);
CREATE INDEX idx_payments_created_at ON payments(created_at);