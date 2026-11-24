package com.eliarojr.contact_form.service;

import com.eliarojr.contact_form.exception.BadRequestException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AppointmentSchedule {

    private AppointmentSchedule() {
    }

    public static void validate(LocalDateTime start, LocalDateTime now) {
        if (start == null || !start.isAfter(now)) {
            throw new BadRequestException("Preferred time must be in the future");
        }
        if (start.getHour() < 8 || start.getHour() > 18
                || start.getMinute() != 0 || start.getSecond() != 0 || start.getNano() != 0) {
            throw new BadRequestException("Choose an available hourly slot between 08:00 and 18:00");
        }

        LocalDate lastBookableDay = now.toLocalDate().plusDays(6);
        if (start.toLocalDate().isAfter(lastBookableDay)) {
            throw new BadRequestException("Appointments can only be requested within the next seven days");
        }
    }
}
