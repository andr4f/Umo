package unimag.proyect.services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import unimag.proyect.api.dto.response.reports.AvailabilitySlotResponse;
import unimag.proyect.entities.AppointmentType;
import unimag.proyect.entities.DoctorSchedule;
import unimag.proyect.enums.ScheduleStatus;
import unimag.proyect.enums.WeekDay;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.repositories.AppointmentRepository;
import unimag.proyect.repositories.AppointmentTypeRepository;
import unimag.proyect.repositories.DoctorScheduleRepository;
import unimag.proyect.services.impl.AvailabilityServiceImpl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AvailabilityServiceImplTest {

    @Mock
    private DoctorScheduleRepository doctorScheduleRepository;
    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private AppointmentTypeRepository appointmentTypeRepository;

    @InjectMocks
    private AvailabilityServiceImpl service;

    @Test
    void getAvailableSlots_shouldThrow_whenTypeNotFound() {
        UUID doctorId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);

        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getAvailableSlots(doctorId, date, typeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAvailableSlots_shouldReturnContinuousSlotsWithoutConflicts() {
        UUID doctorId = UUID.randomUUID();
        UUID typeId = UUID.randomUUID();
        LocalDate date = LocalDate.now().plusDays(1);

        AppointmentType type = new AppointmentType();
        type.setIdAppointmentType(typeId);
        type.setDuration(30);

        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setWeekDay(WeekDay.MONDAY);
        schedule.setStartTime(LocalTime.of(8, 0));
        schedule.setEndTime(LocalTime.of(10, 0));
        schedule.setStatus(ScheduleStatus.AVAILABLE);

        when(appointmentTypeRepository.findById(typeId)).thenReturn(Optional.of(type));
        when(doctorScheduleRepository.findByDoctor_IdPersonAndWeekDay(eq(doctorId), any()))
                .thenReturn(List.of(schedule));

        // Sin conflictos de citas para ningún bloque
        when(appointmentRepository.existsDoctorConflict(eq(doctorId), any(), any()))
                .thenReturn(false);

        List<AvailabilitySlotResponse> slots =
                service.getAvailableSlots(doctorId, date, typeId);

        // 8-10 con bloques de 30min => 4 slots
        assertThat(slots).hasSize(4);

        assertThat(slots.get(0).startTime().toLocalTime()).isEqualTo(LocalTime.of(8, 0));
        assertThat(slots.get(0).endTime().toLocalTime()).isEqualTo(LocalTime.of(8, 30));

        assertThat(slots.get(3).startTime().toLocalTime()).isEqualTo(LocalTime.of(9, 30));
        assertThat(slots.get(3).endTime().toLocalTime()).isEqualTo(LocalTime.of(10, 0));
    }
}