package com.example.ThangLongUniversityWeb.service;

import com.example.ThangLongUniversityWeb.dto.request.BulkClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.ClassSectionRequest;
import com.example.ThangLongUniversityWeb.dto.request.ClassSectionScheduleRequest;
import com.example.ThangLongUniversityWeb.dto.response.BulkClassSectionValidationResponse;
import com.example.ThangLongUniversityWeb.dto.response.ClassSectionValidationResponse;
import com.example.ThangLongUniversityWeb.entity.Period;
import com.example.ThangLongUniversityWeb.repository.ClassSectionRepository;
import com.example.ThangLongUniversityWeb.repository.CourseRepository;
import com.example.ThangLongUniversityWeb.repository.PeriodRepository;
import com.example.ThangLongUniversityWeb.repository.RoomRepository;
import com.example.ThangLongUniversityWeb.repository.SemesterRepository;
import com.example.ThangLongUniversityWeb.repository.TeacherRepository;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BulkClassSectionServiceTest {

    @Test
    void validateRejectsRoomAndTeacherConflictsInsideBatch() {
        ClassSectionService classSectionService = mock(ClassSectionService.class);
        PeriodRepository periodRepository = mock(PeriodRepository.class);
        BulkClassSectionService service = new BulkClassSectionService(
                classSectionService,
                mock(ClassSectionRepository.class),
                mock(CourseRepository.class),
                mock(SemesterRepository.class),
                mock(TeacherRepository.class),
                mock(RoomRepository.class),
                periodRepository);

        ClassSectionValidationResponse valid = ClassSectionValidationResponse.builder()
                .valid(true)
                .errors(List.of())
                .warnings(List.of())
                .infos(List.of())
                .build();
        when(classSectionService.validateClassSection(any(), any(), isNull())).thenReturn(valid);

        Period first = period(1L, 1);
        Period second = period(2L, 2);
        when(periodRepository.findById(1L)).thenReturn(Optional.of(first));
        when(periodRepository.findById(2L)).thenReturn(Optional.of(second));

        BulkClassSectionRequest request = new BulkClassSectionRequest();
        request.setItems(List.of(
                classSection("INT2208-01", 10L, 20L, 2, 1L, 2L),
                classSection("INT2208-02", 10L, 20L, 2, 1L, 2L)));

        BulkClassSectionValidationResponse result = service.validate(request);

        assertThat(result.isValid()).isFalse();
        assertThat(result.getItems().get(0).getValidation().isValid()).isTrue();
        assertThat(result.getItems().get(1).getValidation().getErrors())
                .extracting("code")
                .containsExactlyInAnyOrder("ROOM_CONFLICT_IN_BATCH", "TEACHER_CONFLICT_IN_BATCH");
    }

    private Period period(Long id, int number) {
        Period period = new Period();
        period.setId(id);
        period.setPeriodNumber(number);
        return period;
    }

    private ClassSectionRequest classSection(
            String code,
            Long teacherId,
            Long roomId,
            int day,
            Long startPeriodId,
            Long endPeriodId
    ) {
        ClassSectionScheduleRequest schedule = new ClassSectionScheduleRequest();
        schedule.setRoomId(roomId);
        schedule.setDayOfWeek(day);
        schedule.setStartPeriodId(startPeriodId);
        schedule.setEndPeriodId(endPeriodId);

        ClassSectionRequest request = new ClassSectionRequest();
        request.setClassCode(code);
        request.setCourseId(1L);
        request.setSemesterId(1L);
        request.setTeacherId(teacherId);
        request.setMaxSlots(35);
        request.setSchedules(List.of(schedule));
        return request;
    }
}
