DELETE FROM public.whatsapp_message WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.appointment_addons_record WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.retention_forecast_salon_services WHERE retention_forecast_id IN (SELECT id FROM public.retention_forecast WHERE tenant_id = 'demo-salon-2026');
DELETE FROM public.retention_forecast WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.client_audit_metrics WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.refresh_token WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.work_schedule WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.schedule_block WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.service_professionals WHERE salon_service_id IN (SELECT id FROM public.service WHERE tenant_id = 'demo-salon-2026');
DELETE FROM public.salon_daily_revenue WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.appointment WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.salon_profile WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.service WHERE tenant_id = 'demo-salon-2026';
DELETE FROM public.users WHERE tenant_id = 'demo-salon-2026';

INSERT INTO public.users
(dtype, id, tenant_id, email, full_name, password, status, user_role, phone_number, external_id, is_active, is_first_login)
VALUES
    ('Professional', 501, 'demo-salon-2026', 'ana.unhas@salaodemo.com.br', 'Ana Paula Oliveira', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'ADMIN', '5511999990001', gen_random_uuid(), true, false),
    ('Professional', 502, 'demo-salon-2026', 'roberto.hair@salaodemo.com.br', 'Roberto Ferreira', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'PROFESSIONAL', '5511999990002', gen_random_uuid(), true, false),
    ('Professional', 503, 'demo-salon-2026', 'carla.estetica@salaodemo.com.br', 'Carla Mendes', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'PROFESSIONAL', '5511999990003', gen_random_uuid(), true, false),
    ('Client', 600, 'demo-salon-2026', 'fernanda.lima@email.com', 'Fernanda Lima', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988881111', null, true, false),
    ('Client', 601, 'demo-salon-2026', 'gabriele.santos@email.com', 'Gabriele Santos', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988882222', null, true, false),
    ('Client', 602, 'demo-salon-2026', 'marcos.oliveira@email.com', 'Marcos Oliveira', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988883333', null, true, false),
    ('Client', 603, 'demo-salon-2026', 'juliana.paes@email.com', 'Juliana Paes', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988884444', null, true, false),
    ('Client', 604, 'demo-salon-2026', 'ricardo.almeida@email.com', 'Ricardo Almeida', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988885555', null, true, false),
    ('Client', 605, 'demo-salon-2026', 'patricia.rocha@email.com', 'Patrícia Rocha', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988886666', null, true, false),
    ('Client', 606, 'demo-salon-2026', 'lucas.martins@email.com', 'Lucas Martins', '$2a$12$PB6L0TJuwgAOh9HMRkkkTusUO8XsbpMPTJzETlvOOEJaQCbfyxF2.', 'ACTIVE', 'CLIENT', '5511988887777', null, true, false);

INSERT INTO public.salon_profile
(id, tenant_id, appointment_buffer_minutes, auto_confirmation_appointment, comercial_phone, evolution_connection_state, full_address, is_loyal_clientele_prioritized, logo_path, operational_status, primary_color, slogan, trade_name, salon_zone_id, owner_id, tenant_status)
VALUES
    (10, 'demo-salon-2026', 15, true, '551140028922', 'CLOSE', 'Rua Principal, 100 - Bairro Central', true, 'logo_padrao.png', 'OPEN', '#4F46E5', 'Excelência em cada detalhe', 'Demo Salon', 'America/Sao_Paulo', 501, 'ACTIVE');

INSERT INTO public.service
(id, tenant_id, active, description, duration_in_seconds, is_add_on, maintenance_interval_days, name, value)
VALUES
    (101, 'demo-salon-2026', true, 'Serviço completo de manicure e pedicure', 3600, false, 15, 'Manicure e Pedicure', 75),
    (102, 'demo-salon-2026', true, 'Aplicação de unhas de gel com acabamento premium', 7200, false, 25, 'Unhas de Gel', 180),
    (103, 'demo-salon-2026', true, 'Esmaltação em gel com secagem UV', 1800, true, null, 'Esmaltação em Gel', 45),
    (104, 'demo-salon-2026', true, 'Design de sobrancelhas com visagismo', 2400, false, 20, 'Design de Sobrancelha', 50),
    (105, 'demo-salon-2026', true, 'Manutenção de unhas de gel', 5400, false, null, 'Manutenção Gel', 120);

INSERT INTO public.work_schedule
(id, tenant_id, day_of_week, is_active, lunch_break_start_time, lunch_break_end_time, start_time, end_time, professional_id)
VALUES

    (1001, 'demo-salon-2026', 'MONDAY', true, '12:00:00', '13:00:00', '09:00:00', '19:00:00', 501),
    (1002, 'demo-salon-2026', 'TUESDAY', true, '12:00:00', '13:00:00', '09:00:00', '19:00:00', 501),
    (1003, 'demo-salon-2026', 'WEDNESDAY', true, '12:00:00', '13:00:00', '09:00:00', '19:00:00', 501),
    (1004, 'demo-salon-2026', 'THURSDAY', true, '12:00:00', '13:00:00', '09:00:00', '19:00:00', 501),
    (1005, 'demo-salon-2026', 'FRIDAY', true, '12:00:00', '13:00:00', '09:00:00', '19:00:00', 501),
    (1006, 'demo-salon-2026', 'SATURDAY', true, '12:00:00', '13:00:00', '08:00:00', '14:00:00', 501),


    (1007, 'demo-salon-2026', 'TUESDAY', true, '13:00:00', '14:00:00', '10:00:00', '20:00:00', 502),
    (1008, 'demo-salon-2026', 'WEDNESDAY', true, '13:00:00', '14:00:00', '10:00:00', '20:00:00', 502),
    (1009, 'demo-salon-2026', 'THURSDAY', true, '13:00:00', '14:00:00', '10:00:00', '20:00:00', 502),
    (1010, 'demo-salon-2026', 'FRIDAY', true, '13:00:00', '14:00:00', '10:00:00', '20:00:00', 502),
    (1011, 'demo-salon-2026', 'SATURDAY', true, '13:00:00', '14:00:00', '09:00:00', '15:00:00', 502),


    (1012, 'demo-salon-2026', 'MONDAY', true, '12:00:00', '13:00:00', '09:00:00', '18:00:00', 503),
    (1013, 'demo-salon-2026', 'TUESDAY', true, '12:00:00', '13:00:00', '09:00:00', '18:00:00', 503),
    (1014, 'demo-salon-2026', 'WEDNESDAY', true, '12:00:00', '13:00:00', '09:00:00', '18:00:00', 503),
    (1015, 'demo-salon-2026', 'THURSDAY', true, '12:00:00', '13:00:00', '09:00:00', '18:00:00', 503),
    (1016, 'demo-salon-2026', 'FRIDAY', true, '12:00:00', '13:00:00', '09:00:00', '18:00:00', 503);
INSERT INTO public.appointment
(id, tenant_id, appointment_status, start_date, end_date, total_value, client_id, main_service_id, professional_id, salon_trade_name, salon_zone_id)
VALUES
    (2001, 'demo-salon-2026', 'FINISHED', (CURRENT_DATE - INTERVAL '3 days') + TIME '09:00:00', (CURRENT_DATE - INTERVAL '3 days') + TIME '10:00:00', 75.00, 600, 101, 501, 'Demo Salon', 'America/Sao_Paulo'),
    (2002, 'demo-salon-2026', 'FINISHED', (CURRENT_DATE - INTERVAL '2 days') + TIME '14:00:00', (CURRENT_DATE - INTERVAL '2 days') + TIME '16:00:00', 180.00, 601, 102, 502, 'Demo Salon', 'America/Sao_Paulo'),
    (2003, 'demo-salon-2026', 'FINISHED', (CURRENT_DATE - INTERVAL '1 day') + TIME '11:00:00', (CURRENT_DATE - INTERVAL '1 day') + TIME '12:00:00', 50.00, 602, 104, 503, 'Demo Salon', 'America/Sao_Paulo'),
    (2004, 'demo-salon-2026', 'CONFIRMED', CURRENT_DATE + TIME '09:00:00', CURRENT_DATE + TIME '10:00:00', 75.00, 603, 101, 501, 'Demo Salon', 'America/Sao_Paulo'),
    (2005, 'demo-salon-2026', 'CONFIRMED', CURRENT_DATE + TIME '10:30:00', CURRENT_DATE + TIME '11:30:00', 50.00, 604, 104, 503, 'Demo Salon', 'America/Sao_Paulo'),
    (2006, 'demo-salon-2026', 'CONFIRMED', CURRENT_DATE + TIME '15:00:00', CURRENT_DATE + TIME '17:00:00', 180.00, 600, 102, 502, 'Demo Salon', 'America/Sao_Paulo'),
    (2007, 'demo-salon-2026', 'CONFIRMED', (CURRENT_DATE + INTERVAL '1 day') + TIME '09:00:00', (CURRENT_DATE + INTERVAL '1 day') + TIME '10:00:00', 75.00, 601, 101, 501, 'Demo Salon', 'America/Sao_Paulo'),
    (2008, 'demo-salon-2026', 'CONFIRMED', (CURRENT_DATE + INTERVAL '1 day') + TIME '13:00:00', (CURRENT_DATE + INTERVAL '1 day') + TIME '14:30:00', 120.00, 602, 105, 502, 'Demo Salon', 'America/Sao_Paulo'),
    (2009, 'demo-salon-2026', 'FINISHED', (CURRENT_DATE - INTERVAL '5 days') + TIME '10:00:00', (CURRENT_DATE - INTERVAL '5 days') + TIME '11:00:00', 75.00, 605, 101, 501, 'Demo Salon', 'America/Sao_Paulo'),
    (2010, 'demo-salon-2026', 'FINISHED', (CURRENT_DATE - INTERVAL '4 days') + TIME '15:00:00', (CURRENT_DATE - INTERVAL '4 days') + TIME '17:00:00', 180.00, 606, 102, 502, 'Demo Salon', 'America/Sao_Paulo'),
    (2011, 'demo-salon-2026', 'CONFIRMED', CURRENT_DATE + TIME '11:30:00', CURRENT_DATE + TIME '12:30:00', 75.00, 605, 101, 501, 'Demo Salon', 'America/Sao_Paulo'),
    (2012, 'demo-salon-2026', 'CONFIRMED', CURRENT_DATE + TIME '16:00:00', CURRENT_DATE + TIME '17:30:00', 120.00, 606, 105, 502, 'Demo Salon', 'America/Sao_Paulo'),
    (2013, 'demo-salon-2026', 'CONFIRMED', (CURRENT_DATE + INTERVAL '2 days') + TIME '09:00:00', (CURRENT_DATE + INTERVAL '2 days') + TIME '10:00:00', 50.00, 603, 104, 503, 'Demo Salon', 'America/Sao_Paulo'),
    (2014, 'demo-salon-2026', 'CONFIRMED', (CURRENT_DATE + INTERVAL '3 days') + TIME '14:00:00', (CURRENT_DATE + INTERVAL '3 days') + TIME '16:00:00', 180.00, 604, 102, 502, 'Demo Salon', 'America/Sao_Paulo');

INSERT INTO public.client_audit_metrics
(id, tenant_id, completed_appointments_count, total_spent, client_id, last_visit_date)
VALUES
    (3001, 'demo-salon-2026', 1, 75.00, 600, CURRENT_DATE - INTERVAL '3 days'),
    (3002, 'demo-salon-2026', 1, 180.00, 601, CURRENT_DATE - INTERVAL '2 days'),
    (3003, 'demo-salon-2026', 1, 50.00, 602, CURRENT_DATE - INTERVAL '1 day'),
    (3004, 'demo-salon-2026', 1, 75.00, 605, CURRENT_DATE - INTERVAL '5 days'),
    (3005, 'demo-salon-2026', 1, 180.00, 606, CURRENT_DATE - INTERVAL '4 days');

INSERT INTO public.salon_daily_revenue
(id, tenant_id, appointments_count, date, total_revenue)
VALUES
    (5001, 'demo-salon-2026', 1, CURRENT_DATE - INTERVAL '3 days', 75.00),
    (5002, 'demo-salon-2026', 1, CURRENT_DATE - INTERVAL '2 days', 180.00),
    (5003, 'demo-salon-2026', 1, CURRENT_DATE - INTERVAL '1 day', 50.00),
    (5004, 'demo-salon-2026', 1, CURRENT_DATE - INTERVAL '5 days', 75.00),
    (5005, 'demo-salon-2026', 1, CURRENT_DATE - INTERVAL '4 days', 180.00);

INSERT INTO public.schedule_block
(id, tenant_id, start_time, end_time, reason, professional_id, is_whole_day_blocked)
VALUES
    (6001, 'demo-salon-2026', CURRENT_DATE + TIME '12:00:00', CURRENT_DATE + TIME '13:00:00', 'Dentista', 501, false),
    (6002, 'demo-salon-2026', (CURRENT_DATE + INTERVAL '1 day') + TIME '08:00:00', (CURRENT_DATE + INTERVAL '1 day') + TIME '10:00:00', 'Consulta Médica', 501, false),
    (6003, 'demo-salon-2026', CURRENT_DATE + TIME '13:30:00', CURRENT_DATE + TIME '15:00:00', 'Resolução de Problema Bancário', 502, false),
    (6004, 'demo-salon-2026', (CURRENT_DATE + INTERVAL '2 days') + TIME '16:00:00', (CURRENT_DATE + INTERVAL '2 days') + TIME '18:00:00', 'Reunião Escolar', 502, false),
    (6005, 'demo-salon-2026', CURRENT_DATE + TIME '11:00:00', CURRENT_DATE + TIME '12:00:00', 'Dentista', 503, false),
    (6006, 'demo-salon-2026', (CURRENT_DATE + INTERVAL '4 days'), (CURRENT_DATE + INTERVAL '4 days') + TIME '23:59:59', 'Folga Compensatória', 503, true),
    (6007, 'demo-salon-2026', (CURRENT_DATE + INTERVAL '1 day') + TIME '14:00:00', (CURRENT_DATE + INTERVAL '1 day') + TIME '15:00:00', 'Renovação de CNH', 503, false);

INSERT INTO public.service_professionals (salon_service_id, professionals_id)
SELECT s.id, u.id
FROM public.service s
         CROSS JOIN public.users u
WHERE s.tenant_id = 'demo-salon-2026'
  AND u.tenant_id = 'demo-salon-2026'
  AND u.user_role IN ('ADMIN', 'PROFESSIONAL')
  AND u.dtype = 'Professional';

SELECT pg_catalog.setval('public.users_seq', (SELECT COALESCE(MAX(id), 1) + 50 FROM public.users), false);
SELECT pg_catalog.setval('public.appointment_seq', (SELECT COALESCE(MAX(id), 1) + 50 FROM public.appointment), false);
SELECT pg_catalog.setval('public.service_seq', (SELECT COALESCE(MAX(id), 1) + 50 FROM public.service), false);
SELECT pg_catalog.setval('public.salon_daily_revenue_seq', (SELECT COALESCE(MAX(id), 1) + 50 FROM public.salon_daily_revenue), false);
SELECT pg_catalog.setval('public.schedule_block_seq', (SELECT COALESCE(MAX(id), 1) + 50 FROM public.schedule_block), false);
SELECT pg_catalog.setval('public.client_audit_metrics_seq', (SELECT COALESCE(MAX(id), 1) + 50 FROM public.client_audit_metrics), false);