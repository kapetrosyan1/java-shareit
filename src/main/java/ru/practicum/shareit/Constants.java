package ru.practicum.shareit;

import lombok.experimental.UtilityClass;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class Constants {
    public DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("YYYY-MM-DD HH:mm:ss");
}
