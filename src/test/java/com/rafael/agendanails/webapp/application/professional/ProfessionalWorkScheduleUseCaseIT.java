package com.rafael.agendanails.webapp.application.professional;

import com.rafael.agendanails.webapp.domain.model.Professional;
import com.rafael.agendanails.webapp.domain.model.WorkSchedule;
import com.rafael.agendanails.webapp.domain.repository.ProfessionalRepository;
import com.rafael.agendanails.webapp.domain.repository.WorkScheduleRepository;
import com.rafael.agendanails.webapp.infrastructure.dto.professional.schedule.WorkScheduleRecordDTO;
import com.rafael.agendanails.webapp.infrastructure.exception.BusinessException;
import com.rafael.agendanails.webapp.support.BaseIntegrationTest;
import com.rafael.agendanails.webapp.support.factory.TestProfessionalFactory;
import com.rafael.agendanails.webapp.support.factory.TestWorkScheduleRecordDTO;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("it")
class ProfessionalWorkScheduleUseCaseIT extends BaseIntegrationTest {

    @Autowired
    private ProfessionalWorkScheduleUseCase professionalWorkScheduleUseCase;

    @Autowired
    private ProfessionalRepository professionalRepository;

    @Autowired
    private WorkScheduleRepository workScheduleRepository;

    @Autowired
    private EntityManager entityManager;

    private Professional createValidProfessional() {
        Professional p = TestProfessionalFactory.standardForIt();
        System.out.println(p.getTenantId());
        professionalRepository.save(p);
        return p;
    }

    @Test
    void createSchedules_creates_whenDtoIsValid() {
        Professional p = createValidProfessional();
        WorkScheduleRecordDTO monday = TestWorkScheduleRecordDTO.createMondayStandardShift();
        WorkScheduleRecordDTO tuesday = TestWorkScheduleRecordDTO.createTuesdayEarlyShift();

        List<WorkSchedule> schedules = professionalWorkScheduleUseCase.createSchedules(List.of(monday, tuesday), p.getId());

        assertThat(schedules).hasSize(2);
        assertThat(workScheduleRepository.findAllByProfessionalId(p.getId())).hasSize(2);
    }

    @Test
    void createSchedules_throwsBusinessException_whenProfessionalDoesNotExist() {
        Long invalidId = 9999L;
        List<WorkScheduleRecordDTO> dtos = List.of(TestWorkScheduleRecordDTO.createMondayStandardShift());

        BusinessException exception = assertThrows(BusinessException.class, () ->
                professionalWorkScheduleUseCase.createSchedules(dtos, invalidId)
        );

        assertThat(exception.getMessage()).isEqualTo("Profissional não encontrado(a)");
    }

    @Test
    void createSchedules_returnsEmptyList_whenDtoListIsEmpty() {
        Professional p = createValidProfessional();

        List<WorkSchedule> result = professionalWorkScheduleUseCase.createSchedules(List.of(), p.getId());

        assertThat(result).isEmpty();
        assertThat(workScheduleRepository.findAllByProfessionalId(p.getId())).isEmpty();
    }

    @Test
    void createSchedules_throwsBusinessException_whenStartTimeIsAfterEndTime() {
        Professional p = createValidProfessional();
        WorkScheduleRecordDTO invalidDto = WorkScheduleRecordDTO.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(18, 0))
                .endTime(LocalTime.of(0, 0))
                .isActive(true)
                .build();

        assertThrows(BusinessException.class, () ->
                professionalWorkScheduleUseCase.createSchedules(List.of(invalidDto), p.getId())
        );
    }

    @Test
    void createSchedules_rollsBack_whenOneScheduleInListIsInvalid() {
        Professional p = createValidProfessional();
        WorkScheduleRecordDTO validMonday = TestWorkScheduleRecordDTO.createMondayStandardShift();

        WorkScheduleRecordDTO invalidTuesday = WorkScheduleRecordDTO.builder()
                .dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(10, 0))
                .endTime(null)
                .isActive(true)
                .build();

        List<WorkScheduleRecordDTO> dtos = List.of(validMonday, invalidTuesday);

        assertThrows(Exception.class, () -> professionalWorkScheduleUseCase.createSchedules(dtos, p.getId()));

        entityManager.clear();
        assertThat(workScheduleRepository.findAllByProfessionalId(p.getId())).isEmpty();
    }

    @Test
    void createSchedules_preventsDuplicateDaysInSameRequest() {
        Professional p = createValidProfessional();
        WorkScheduleRecordDTO monday1 = TestWorkScheduleRecordDTO.createMondayStandardShift();
        WorkScheduleRecordDTO monday2 = TestWorkScheduleRecordDTO.createMondayStandardShift();

        assertThrows(BusinessException.class, () -> professionalWorkScheduleUseCase.createSchedules(List.of(monday1, monday2), p.getId()));
    }

    @Test
    void createSchedules_throwsBusinessException_whenLunchBreakIsInvalid() {
        Professional p = createValidProfessional();
        WorkScheduleRecordDTO invalidLunch = WorkScheduleRecordDTO.builder()
                .dayOfWeek(DayOfWeek.FRIDAY)
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(18, 0))
                .lunchBreakStartTime(LocalTime.of(19, 0))
                .lunchBreakEndTime(LocalTime.of(20, 0))
                .isActive(true)
                .build();

        assertThrows(BusinessException.class, () ->
                professionalWorkScheduleUseCase.createSchedules(List.of(invalidLunch), p.getId())
        );
    }

    @Test
    void getWorkSchedules_returnsEmptySet_whenNoSchedulesExist() {
        Professional p = createValidProfessional();

        Set<WorkScheduleRecordDTO> schedules = professionalWorkScheduleUseCase.getWorkSchedules(p.getId());

        assertThat(schedules).isEmpty();
    }
}