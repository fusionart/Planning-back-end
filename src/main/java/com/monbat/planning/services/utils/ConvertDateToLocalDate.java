package com.monbat.planning.services.utils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class ConvertDateToLocalDate {
    public static LocalDate convertDateToLocalDate(Date dateToConvert) {
        return LocalDate.ofInstant(dateToConvert.toInstant(), ZoneId.systemDefault());
    }
}
