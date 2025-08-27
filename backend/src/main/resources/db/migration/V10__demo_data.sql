-- Demo data for testing invoice-payment flow
-- Sử dụng dữ liệu từ V2, V3, V4 đã có

-- Insert demo appointments (sử dụng patient_id 1, 2 từ V4)
INSERT INTO appointments (id, patient_id, notes, status, created_at, updated_at) VALUES
(1001, 1, 'Khám tổng quát định kỳ', 'CONFIRMED', NOW(), NOW()),
(1002, 2, 'Tái khám sau điều trị', 'CONFIRMED', NOW(), NOW()),
(1003, 1, 'Khám chuyên khoa tim mạch', 'PENDING', NOW(), NOW()),
(1004, 2, 'Khám răng định kỳ', 'REJECTED', NOW(), NOW()),
(1005, 1, 'Tư vấn dinh dưỡng', 'CONFIRMED', NOW(), NOW()),
(1006, 2, 'Khám da liễu', 'REJECTED', NOW(), NOW());

-- Insert demo appointment slots (sử dụng doctor_id 1-5 từ V2)
-- Đặt vào tuần sau (sau ngày 28/08)
INSERT INTO appointment_slots (id, doctor_id, start_time, end_time, status, appointment_id, created_at, updated_at) VALUES
(1001, 1, '2025-09-04 09:00:00', '2025-09-04 09:30:00', 'BOOKED', 1001, NOW(), NOW()),
(1002, 1, '2025-09-04 10:00:00', '2025-09-04 10:30:00', 'BOOKED', 1002, NOW(), NOW()),
(1003, 2, '2025-09-05 14:00:00', '2025-09-05 14:30:00', 'BOOKED', 1003, NOW(), NOW()),
(1004, 3, '2025-09-03 15:00:00', '2025-09-03 15:30:00', 'AVAILABLE', NULL, NOW(), NOW()),
(1005, 1, '2025-09-06 11:00:00', '2025-09-06 11:30:00', 'BOOKED', 1005, NOW(), NOW()),
(1006, 3, '2025-09-07 16:00:00', '2025-09-07 16:30:00', 'BOOKED', 1006, NOW(), NOW());

-- Insert demo appointment services (sử dụng service_id 1-3 từ V3)
INSERT INTO appointment_services (appointment_id, service_id, price_at_booking, created_at, updated_at) VALUES
(1001, 1, 250000.00, NOW(), NOW()),
(1001, 2, 300000.00, NOW(), NOW()),
(1002, 3, 220000.00, NOW(), NOW()),
(1003, 1, 250000.00, NOW(), NOW()),
(1004, 1, 250000.00, NOW(), NOW()),
(1005, 3, 220000.00, NOW(), NOW()),
(1006, 1, 250000.00, NOW(), NOW());

-- Insert demo invoices (chỉ cho CONFIRMED appointments)
INSERT INTO invoices (id, appointment_id, invoice_code, final_amount, status, issued_date, created_at, updated_at) VALUES
(1001, 1001, 'INV-2025-001', 550000.00, 'PAID', NOW(), NOW(), NOW()),
(1002, 1002, 'INV-2025-002', 220000.00, 'PENDING', NOW(), NOW(), NOW()),
(1005, 1005, 'INV-2025-005', 220000.00, 'PENDING', NOW(), NOW(), NOW()),
(1006, 1006, 'INV-2025-006', 250000.00, 'CANCELLED', NOW(), NOW(), NOW());

-- Insert demo payments (chỉ cho invoices thực sự tồn tại)
INSERT INTO payments (id, invoice_id, amount, payment_method, status, transaction_code, created_at, updated_at) VALUES
-- Successful payment for invoice 1001
(1001, 1001, 550000.00, 'MOMO', 'SUCCESSFUL', 'MOMO_202509040900001', NOW(), NOW()),

-- Pending payments for invoice 1002
(1002, 1002, 220000.00, 'MOMO', 'PENDING', 'MOMO_202509041000001', NOW(), NOW()),
(1003, 1002, 220000.00, 'VNPAY', 'PENDING', 'VNPAY_202509041000002', NOW(), NOW()),

-- Pending payment for invoice 1005
(1004, 1005, 220000.00, 'BANK_TRANSFER', 'PENDING', 'BANK_202509061100001', NOW(), NOW()),

-- Refunded payment for invoice 1006 (appointment rejected after payment)
(1005, 1006, 250000.00, 'MOMO', 'REFUNDED', 'MOMO_202509071600001', NOW(), NOW());

-- Insert demo notifications (sử dụng user_id từ V2, V4)
INSERT INTO notifications (id, user_id, content, type, status, created_at, updated_at) VALUES
(1001, 1, 'Lịch hẹn khám tổng quát ngày 04/09/2025 đã được bác sĩ xác nhận', 'APPOINTMENT_CONFIRMATION', 'UNREAD', NOW(), NOW()),
(1002, 2, 'Lịch hẹn tái khám ngày 04/09/2025 đã được bác sĩ xác nhận', 'APPOINTMENT_CONFIRMATION', 'UNREAD', NOW(), NOW()),
(1003, 1, 'Lịch hẹn khám tim mạch ngày 05/09/2025 đã được tạo và đang chờ xác nhận', 'APPOINTMENT_REMINDER', 'UNREAD', NOW(), NOW()),
(1004, 2, 'Lịch hẹn khám răng ngày 03/09/2025 đã bị từ chối', 'GENERAL_ANNOUNCEMENT', 'UNREAD', NOW(), NOW()),
(1005, 1, 'Thanh toán cho lịch hẹn đã hoàn tất thành công', 'PAYMENT_SUCCESS', 'UNREAD', NOW(), NOW()),
(1006, 2, 'Lịch hẹn khám da liễu ngày 07/09/2025 đã bị từ chối, tiền sẽ được hoàn lại', 'GENERAL_ANNOUNCEMENT', 'UNREAD', NOW(), NOW());

-- Insert demo reviews
INSERT INTO reviews (id, appointment_id, rating, comment, created_at, updated_at) VALUES
(1001, 1001, 5, 'Bác sĩ rất tận tâm và chuyên nghiệp', NOW(), NOW()),
(1002, 1002, 4, 'Dịch vụ tốt, thời gian chờ hợp lý', NOW(), NOW());

-- Insert demo audit logs
INSERT INTO audit_logs (id, user_id, action_type, target_entity, target_id, created_at, updated_at) VALUES
(1001, 1, 'CREATE', 'APPOINTMENT', 1001, NOW(), NOW()),
(1002, 1, 'UPDATE', 'APPOINTMENT', 1001, NOW(), NOW()),
(1003, 1, 'CREATE', 'INVOICE', 1001, NOW(), NOW()),
(1004, 1, 'UPDATE', 'PAYMENT', 1001, NOW(), NOW()),
(1005, 1, 'UPDATE', 'APPOINTMENT', 1004, NOW(), NOW()),
(1006, 1, 'CREATE', 'APPOINTMENT', 1003, NOW(), NOW()),
(1007, 2, 'CREATE', 'APPOINTMENT', 1006, NOW(), NOW()),
(1008, 2, 'UPDATE', 'APPOINTMENT', 1006, NOW(), NOW()),
(1009, 2, 'UPDATE', 'PAYMENT', 1005, NOW(), NOW()); 