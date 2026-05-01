package com.airtribe.meditrack.interfaces;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DateTimeProvider extends Serializable {
    LocalDate today();
    LocalDateTime now();
}