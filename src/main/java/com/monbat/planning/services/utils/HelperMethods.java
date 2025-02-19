package com.monbat.planning.services.utils;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class HelperMethods {
    public static Boolean checkIsNumber(String s) {
        if ((s == null)) {
            return false;
        }

        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            ShowAlert.showAlert("Моля, въведете число!");
            return false;
        }
        return true;
    }

    public static Boolean checkIfNegative(String s) {
        if (Integer.parseInt(s) <= 0) {
            ShowAlert.showAlert("Моля, въведете число по-голямо от 0!");
            return true;
        }

        return false;
    }

    public static LocalDate dateToLocalDate(Date date) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate;

        if (date != null) {
            try {
                localDate = LocalDate.parse((CharSequence) date, dateFormat);
                return localDate;
            } catch (Exception e) {
                ShowAlert.showAlert("Датата е въведена в грешен формат. Моля въведете дата във формат dd.MM.yyyy");
                return null;
            }
        }
        return null;
    }

    public static LocalDate formatDateFromString(String date) {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate localDate;

        if (date != null) {
            try {
                localDate = LocalDate.parse(date, dateFormat);
                return localDate;
            } catch (Exception e) {
                ShowAlert.showAlert("Датата е въведена в грешен формат. Моля въведете дата във формат dd.MM.yyyy");
                return null;
            }
        }
        return null;
    }

    public static String formatLocalDateTimeToStringFileName(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy_HH-mm");
        return localDateTime.format(formatter);
    }

    public static String formatLocalDateTimeToString(LocalDateTime localDateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
        return localDateTime.format(formatter);
    }

    public static String formatLocalDate(LocalDate date) {
        String formattedDate = null;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        if (date != null) {
            formattedDate = date.format(dateFormat);
        }

        return formattedDate;
    }

    public static String formatDate(Date date) {
        String formattedDate = null;
        Locale locale = Locale.of("bg", "BG");
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);

        if (date != null) {
            formattedDate = dateFormat.format(date);
        }

        return formattedDate;
    }

    public static String formatTime(LocalTime time) {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

        return time.format(timeFormat);
    }

    public static LocalTime stringToLocalTime(String time) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        LocalTime localTime = null;
        if (time != null) {
            try {
                localTime = LocalTime.parse(time, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid time format: " + e.getMessage());
            }
        }
        return localTime;
    }
}
