--
-- PostgreSQL database dump
--

-- Dumped from database version 15.16
-- Dumped by pg_dump version 15.16

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: appointment; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    appointment_status character varying(255),
    end_date timestamp(6) with time zone NOT NULL,
    observations character varying(120),
    salon_trade_name character varying(255),
    salon_zone_id character varying(255),
    start_date timestamp(6) with time zone NOT NULL,
    total_value numeric(38,2) NOT NULL,
    client_id bigint NOT NULL,
    main_service_id bigint NOT NULL,
    professional_id bigint NOT NULL,
    CONSTRAINT appointment_appointment_status_check CHECK (((appointment_status)::text = ANY ((ARRAY['PENDING'::character varying, 'CONFIRMED'::character varying, 'CANCELLED'::character varying, 'MISSED'::character varying, 'FINISHED'::character varying])::text[])))
);

--
-- Name: appointment_addons_record; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.appointment_addons_record (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    quantity integer,
    unit_price_at_moment integer,
    service_id bigint,
    appointment_addon_id bigint
);

--
-- Name: appointment_addons_record_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.appointment_addons_record_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: appointment_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.appointment_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: client_audit_metrics; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.client_audit_metrics (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    canceled_appointments_count bigint,
    completed_appointments_count bigint,
    last_visit_date timestamp(6) with time zone,
    missed_appointments_count bigint,
    total_spent numeric(19,2),
    client_id bigint NOT NULL
);

--
-- Name: client_audit_metrics_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.client_audit_metrics_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: email_usage_quota; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.email_usage_quota (
    usage_date date NOT NULL,
    daily_count integer
);

--
-- Name: email_usage_quota_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.email_usage_quota_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: refresh_token; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.refresh_token (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    expiry_date timestamp(6) with time zone NOT NULL,
    is_revoked boolean,
    token character varying(255) NOT NULL,
    user_id bigint
);

--
-- Name: refresh_token_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.refresh_token_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: retention_forecast; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.retention_forecast (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    predicted_return_date timestamp(6) with time zone NOT NULL,
    status character varying(255) NOT NULL,
    client_id bigint NOT NULL,
    origin_appointment_id bigint,
    professional_id bigint,
    CONSTRAINT retention_forecast_status_check CHECK (((status)::text = ANY ((ARRAY['PENDING'::character varying, 'NOTIFIED'::character varying, 'CONVERTED'::character varying, 'EXPIRED'::character varying])::text[])))
);

--
-- Name: retention_forecast_salon_services; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.retention_forecast_salon_services (
    retention_forecast_id bigint NOT NULL,
    salon_services_id bigint NOT NULL
);

--
-- Name: retention_forecast_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.retention_forecast_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: salon_daily_revenue; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.salon_daily_revenue (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    appointments_count bigint,
    date date,
    total_revenue numeric(19,2)
);

--
-- Name: salon_daily_revenue_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.salon_daily_revenue_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: salon_profile; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.salon_profile (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    appointment_buffer_minutes integer NOT NULL,
    auto_confirmation_appointment boolean NOT NULL,
    comercial_phone character varying(13) NOT NULL,
    evolution_connection_state character varying(255) NOT NULL,
    full_address character varying(80) NOT NULL,
    is_loyal_clientele_prioritized boolean NOT NULL,
    last_pairing_code character varying(255),
    logo_path character varying(255) NOT NULL,
    loyal_client_booking_window_days integer,
    operational_status character varying(255) NOT NULL,
    primary_color character varying(15) NOT NULL,
    slogan character varying(120),
    social_media_link character varying(50),
    standard_booking_window integer,
    tenant_status character varying(255),
    trade_name character varying(60) NOT NULL,
    warning_message character varying(200),
    whatsapp_last_reset_at timestamp(6) without time zone,
    salon_zone_id character varying(255) NOT NULL,
    owner_id bigint NOT NULL,
    CONSTRAINT salon_profile_evolution_connection_state_check CHECK (((evolution_connection_state)::text = ANY ((ARRAY['CONNECTING'::character varying, 'OPEN'::character varying, 'CLOSE'::character varying])::text[]))),
    CONSTRAINT salon_profile_operational_status_check CHECK (((operational_status)::text = ANY ((ARRAY['OPEN'::character varying, 'CLOSED_TEMPORARY'::character varying])::text[]))),
    CONSTRAINT salon_profile_tenant_status_check CHECK (((tenant_status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SUSPENDED'::character varying])::text[])))
);

--
-- Name: salon_profile_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.salon_profile_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: schedule_block; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.schedule_block (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    end_time timestamp(6) with time zone NOT NULL,
    is_whole_day_blocked boolean,
    reason character varying(300) NOT NULL,
    start_time timestamp(6) with time zone NOT NULL,
    professional_id bigint NOT NULL
);

--
-- Name: schedule_block_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.schedule_block_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: service; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    active boolean NOT NULL,
    description character varying(250) NOT NULL,
    duration_in_seconds integer NOT NULL,
    is_add_on boolean,
    maintenance_interval_days integer,
    nail_count integer,
    name character varying(200) NOT NULL,
    value integer NOT NULL
);

--
-- Name: service_professionals; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.service_professionals (
    salon_service_id bigint NOT NULL,
    professionals_id bigint NOT NULL
);

--
-- Name: service_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.service_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: users; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.users (
    dtype character varying(31) NOT NULL,
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    email character varying(255),
    full_name character varying(255),
    password character varying(255),
    status character varying(255),
    user_role character varying(255) NOT NULL,
    cancelled_appointments integer,
    missed_appointments integer,
    phone_number character varying(13),
    external_id uuid,
    is_active boolean,
    is_first_login boolean,
    professional_picture character varying(255),
    CONSTRAINT users_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'BANNED'::character varying])::text[]))),
    CONSTRAINT users_user_role_check CHECK (((user_role)::text = ANY ((ARRAY['SUPER_ADMIN'::character varying, 'ADMIN'::character varying, 'PROFESSIONAL'::character varying, 'CLIENT'::character varying])::text[])))
);

--
-- Name: users_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.users_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: whatsapp_message; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.whatsapp_message (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    attempts integer NOT NULL,
    destination_number character varying(255),
    external_message_id character varying(255),
    last_attempt_at timestamp(6) with time zone,
    last_error_message character varying(255),
    whatsapp_message_status character varying(255) NOT NULL,
    whatsapp_message_type character varying(255) NOT NULL,
    sent_at timestamp(6) with time zone,
    appointment_id bigint,
    retention_forecast_id bigint,
    CONSTRAINT whatsapp_message_whatsapp_message_status_check CHECK (((whatsapp_message_status)::text = ANY ((ARRAY['PENDING'::character varying, 'SENT'::character varying, 'DELIVERED'::character varying, 'FAILED'::character varying])::text[]))),
    CONSTRAINT whatsapp_message_whatsapp_message_type_check CHECK (((whatsapp_message_type)::text = ANY ((ARRAY['CONFIRMATION'::character varying, 'REMINDER'::character varying, 'RETENTION_MAINTENANCE'::character varying])::text[])))
);

--
-- Name: whatsapp_message_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.whatsapp_message_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: work_schedule; Type: TABLE; Schema: public; Owner: -
--

CREATE TABLE public.work_schedule (
    id bigint NOT NULL,
    tenant_id character varying(255) NOT NULL,
    day_of_week character varying(255) NOT NULL,
    is_active boolean NOT NULL,
    lunch_break_end_time time(6) without time zone NOT NULL,
    lunch_break_start_time time(6) without time zone NOT NULL,
    end_time time(6) without time zone NOT NULL,
    start_time time(6) without time zone NOT NULL,
    professional_id bigint NOT NULL,
    CONSTRAINT work_schedule_day_of_week_check CHECK (((day_of_week)::text = ANY ((ARRAY['MONDAY'::character varying, 'TUESDAY'::character varying, 'WEDNESDAY'::character varying, 'THURSDAY'::character varying, 'FRIDAY'::character varying, 'SATURDAY'::character varying, 'SUNDAY'::character varying])::text[])))
);

--
-- Name: work_schedule_seq; Type: SEQUENCE; Schema: public; Owner: -
--

CREATE SEQUENCE public.work_schedule_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: appointment_addons_record appointment_addons_record_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_addons_record
    ADD CONSTRAINT appointment_addons_record_pkey PRIMARY KEY (id);

--
-- Name: appointment appointment_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT appointment_pkey PRIMARY KEY (id);

--
-- Name: client_audit_metrics client_audit_metrics_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client_audit_metrics
    ADD CONSTRAINT client_audit_metrics_pkey PRIMARY KEY (id);

--
-- Name: email_usage_quota email_usage_quota_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.email_usage_quota
    ADD CONSTRAINT email_usage_quota_pkey PRIMARY KEY (usage_date);

--
-- Name: refresh_token refresh_token_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT refresh_token_pkey PRIMARY KEY (id);

--
-- Name: retention_forecast retention_forecast_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast
    ADD CONSTRAINT retention_forecast_pkey PRIMARY KEY (id);

--
-- Name: salon_daily_revenue salon_daily_revenue_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salon_daily_revenue
    ADD CONSTRAINT salon_daily_revenue_pkey PRIMARY KEY (id);

--
-- Name: salon_profile salon_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salon_profile
    ADD CONSTRAINT salon_profile_pkey PRIMARY KEY (id);

--
-- Name: schedule_block schedule_block_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule_block
    ADD CONSTRAINT schedule_block_pkey PRIMARY KEY (id);

--
-- Name: service service_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service
    ADD CONSTRAINT service_pkey PRIMARY KEY (id);

--
-- Name: service_professionals service_professionals_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_professionals
    ADD CONSTRAINT service_professionals_pkey PRIMARY KEY (salon_service_id, professionals_id);

--
-- Name: whatsapp_message uk1julh4gx70rj5hpxa96fr6g85; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.whatsapp_message
    ADD CONSTRAINT uk1julh4gx70rj5hpxa96fr6g85 UNIQUE (retention_forecast_id);

--
-- Name: users uk_email_per_tenant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_email_per_tenant UNIQUE (tenant_id, email);

--
-- Name: users uk_phone_per_tenant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_phone_per_tenant UNIQUE (tenant_id, phone_number);

--
-- Name: work_schedule uk_professional_day; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.work_schedule
    ADD CONSTRAINT uk_professional_day UNIQUE (professional_id, day_of_week);

--
-- Name: users uk_professional_external_id_per_tenant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_professional_external_id_per_tenant UNIQUE (tenant_id, external_id);

--
-- Name: refresh_token uk_refresh_token_per_tenant; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT uk_refresh_token_per_tenant UNIQUE (tenant_id, token);

--
-- Name: salon_profile uk_salon_owner_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salon_profile
    ADD CONSTRAINT uk_salon_owner_id UNIQUE (owner_id);

--
-- Name: salon_profile uk_salon_tenant_id; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salon_profile
    ADD CONSTRAINT uk_salon_tenant_id UNIQUE (tenant_id);

--
-- Name: retention_forecast ukhybhwstd67u8dj1ocny1yqpxf; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast
    ADD CONSTRAINT ukhybhwstd67u8dj1ocny1yqpxf UNIQUE (origin_appointment_id);

--
-- Name: client_audit_metrics uki686llavv9390id8k4wa2q56e; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client_audit_metrics
    ADD CONSTRAINT uki686llavv9390id8k4wa2q56e UNIQUE (client_id);

--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);

--
-- Name: whatsapp_message whatsapp_message_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.whatsapp_message
    ADD CONSTRAINT whatsapp_message_pkey PRIMARY KEY (id);

--
-- Name: work_schedule work_schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.work_schedule
    ADD CONSTRAINT work_schedule_pkey PRIMARY KEY (id);

--
-- Name: whatsapp_message fk3ny237sxgwlmayxmbc6bwrjvi; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.whatsapp_message
    ADD CONSTRAINT fk3ny237sxgwlmayxmbc6bwrjvi FOREIGN KEY (retention_forecast_id) REFERENCES public.retention_forecast(id);

--
-- Name: work_schedule fk446sa4jc8d5doskvs2grn7iiu; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.work_schedule
    ADD CONSTRAINT fk446sa4jc8d5doskvs2grn7iiu FOREIGN KEY (professional_id) REFERENCES public.users(id);

--
-- Name: retention_forecast fk6s49utjg26tc3nnof4ipmq4fj; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast
    ADD CONSTRAINT fk6s49utjg26tc3nnof4ipmq4fj FOREIGN KEY (client_id) REFERENCES public.users(id);

--
-- Name: whatsapp_message fk7hp3i61bpgseb3agoomrcwppk; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.whatsapp_message
    ADD CONSTRAINT fk7hp3i61bpgseb3agoomrcwppk FOREIGN KEY (appointment_id) REFERENCES public.appointment(id);

--
-- Name: appointment fk7wv46g6c222h1bnk4uk2xjod7; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT fk7wv46g6c222h1bnk4uk2xjod7 FOREIGN KEY (main_service_id) REFERENCES public.service(id);

--
-- Name: retention_forecast_salon_services fk9h6rsbeaebenw4m9asxfdg91l; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast_salon_services
    ADD CONSTRAINT fk9h6rsbeaebenw4m9asxfdg91l FOREIGN KEY (salon_services_id) REFERENCES public.service(id);

--
-- Name: salon_profile fkawi56ubk8dp4yf1r6gvnaalgm; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.salon_profile
    ADD CONSTRAINT fkawi56ubk8dp4yf1r6gvnaalgm FOREIGN KEY (owner_id) REFERENCES public.users(id);

--
-- Name: schedule_block fkbnnboeqyvq0goum81un0pfc92; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.schedule_block
    ADD CONSTRAINT fkbnnboeqyvq0goum81un0pfc92 FOREIGN KEY (professional_id) REFERENCES public.users(id);

--
-- Name: service_professionals fkdl9ahe59gntpe4n1up0eftmf1; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_professionals
    ADD CONSTRAINT fkdl9ahe59gntpe4n1up0eftmf1 FOREIGN KEY (salon_service_id) REFERENCES public.service(id);

--
-- Name: service_professionals fkh2r8ey43mdp6m10i7rwb6471k; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.service_professionals
    ADD CONSTRAINT fkh2r8ey43mdp6m10i7rwb6471k FOREIGN KEY (professionals_id) REFERENCES public.users(id);

--
-- Name: retention_forecast fkh89wh2ip4wtwfqaxypvppmhw5; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast
    ADD CONSTRAINT fkh89wh2ip4wtwfqaxypvppmhw5 FOREIGN KEY (professional_id) REFERENCES public.users(id);

--
-- Name: appointment fkhcamteioleo8c9w7t6vufj0ur; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT fkhcamteioleo8c9w7t6vufj0ur FOREIGN KEY (professional_id) REFERENCES public.users(id);

--
-- Name: refresh_token fkjtx87i0jvq2svedphegvdwcuy; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.refresh_token
    ADD CONSTRAINT fkjtx87i0jvq2svedphegvdwcuy FOREIGN KEY (user_id) REFERENCES public.users(id);

--
-- Name: retention_forecast_salon_services fkjwmgkuet9737178nyjb36op51; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast_salon_services
    ADD CONSTRAINT fkjwmgkuet9737178nyjb36op51 FOREIGN KEY (retention_forecast_id) REFERENCES public.retention_forecast(id);

--
-- Name: retention_forecast fkl3918912lweatg7t7gj6351px; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.retention_forecast
    ADD CONSTRAINT fkl3918912lweatg7t7gj6351px FOREIGN KEY (origin_appointment_id) REFERENCES public.appointment(id);

--
-- Name: client_audit_metrics fklmheeaoa2nndcd22s8gltw1jt; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.client_audit_metrics
    ADD CONSTRAINT fklmheeaoa2nndcd22s8gltw1jt FOREIGN KEY (client_id) REFERENCES public.users(id);

--
-- Name: appointment fklqi1o7adfcj20xt05iinir8a4; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment
    ADD CONSTRAINT fklqi1o7adfcj20xt05iinir8a4 FOREIGN KEY (client_id) REFERENCES public.users(id);

--
-- Name: appointment_addons_record fksi4dirh6fkk6rsbjgb3ai87s; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_addons_record
    ADD CONSTRAINT fksi4dirh6fkk6rsbjgb3ai87s FOREIGN KEY (appointment_addon_id) REFERENCES public.appointment(id);

--
-- Name: appointment_addons_record fkt3gyvigvhli8782hb8os2b6q; Type: FK CONSTRAINT; Schema: public; Owner: -
--

ALTER TABLE ONLY public.appointment_addons_record
    ADD CONSTRAINT fkt3gyvigvhli8782hb8os2b6q FOREIGN KEY (service_id) REFERENCES public.service(id);

--
-- PostgreSQL database dump complete
--
