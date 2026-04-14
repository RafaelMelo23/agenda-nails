package com.rafael.agendanails.webapp.support;

import com.rafael.agendanails.webapp.SchedulingNailsProApplication;
import com.rafael.agendanails.webapp.domain.repository.*;
import com.rafael.agendanails.webapp.shared.tenant.TenantContext;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Objects;

@Transactional
@ActiveProfiles("it")
@AutoConfigureMockMvc
@SpringBootTest(classes = SchedulingNailsProApplication.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class BaseIntegrationTest {

    @Autowired
    protected AppointmentRepository appointmentRepository;
    @Autowired
    protected ClientRepository clientRepository;
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected RefreshTokenRepository refreshTokenRepository;
    @Autowired
    protected ProfessionalRepository professionalRepository;
    @Autowired
    protected SalonServiceRepository salonServiceRepository;
    @Autowired
    protected SalonProfileRepository salonProfileRepository;
    @Autowired
    protected WorkScheduleRepository workScheduleRepository;
    @Autowired
    protected ScheduleBlockRepository scheduleBlockRepository;
    @Autowired
    protected WhatsappMessageRepository whatsappMessageRepository;
    @Autowired
    protected RetentionForecastRepository retentionForecastRepository;
    @Autowired
    protected ClientAuditMetricsRepository clientAuditMetricsRepository;
    @Autowired
    protected SalonDailyRevenueRepository salonDailyRevenueRepository;
    @Autowired
    protected EntityManager entityManager;
    @Autowired
    protected CacheManager cacheManager;

    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("nailspro_it")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @BeforeEach
    void globalSetUp() {
        TenantContext.setTenant("tenant-test");
    }

    @AfterEach
    void globalTearDown() {
        TenantContext.setIgnoreFilter(true);
        try {
            whatsappMessageRepository.deleteAllInBatch();
            retentionForecastRepository.deleteAllInBatch();
            refreshTokenRepository.deleteAllInBatch();
            appointmentRepository.deleteAllInBatch();
            scheduleBlockRepository.deleteAllInBatch();
            workScheduleRepository.deleteAllInBatch();
            salonServiceRepository.deleteAllInBatch();
            salonDailyRevenueRepository.deleteAllInBatch();
            salonProfileRepository.deleteAllInBatch();
            clientAuditMetricsRepository.deleteAllInBatch();
            clientRepository.deleteAllInBatch();
            professionalRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();

            cacheManager.getCacheNames().forEach(cacheName -> 
                    Objects.requireNonNull(cacheManager.getCache(cacheName)).clear());
            
        } finally {
            TenantContext.clear();
        }
    }
}