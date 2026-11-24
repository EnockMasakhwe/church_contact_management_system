package com.eliarojr.contact_form;

import com.eliarojr.contact_form.exception.BadRequestException;
import com.eliarojr.contact_form.service.AppointmentSchedule;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppointmentScheduleTest {

    private final LocalDateTime now = LocalDateTime.of(2026, 11, 20, 7, 0);

    @Test
    void acceptsHourlySlotsWithinTheSevenDayWindow() {
        assertDoesNotThrow(() -> AppointmentSchedule.validate(
                LocalDateTime.of(2026, 11, 20, 8, 0), now));
        assertDoesNotThrow(() -> AppointmentSchedule.validate(
                LocalDateTime.of(2026, 11, 26, 18, 0), now));
    }

    @Test
    void rejectsPastTimesAndTimesOutsideTheCalendarHours() {
        assertThrows(BadRequestException.class, () -> AppointmentSchedule.validate(
                LocalDateTime.of(2026, 11, 20, 6, 0), now));
        assertThrows(BadRequestException.class, () -> AppointmentSchedule.validate(
                LocalDateTime.of(2026, 11, 20, 19, 0), now));
        assertThrows(BadRequestException.class, () -> AppointmentSchedule.validate(
                LocalDateTime.of(2026, 11, 20, 8, 30), now));
    }

    @Test
    void rejectsTimesOutsideTheSevenCalendarDays() {
        assertThrows(BadRequestException.class, () -> AppointmentSchedule.validate(
                LocalDateTime.of(2026, 11, 27, 8, 0), now));
    }
}
