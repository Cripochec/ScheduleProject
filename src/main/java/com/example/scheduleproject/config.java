package com.example.scheduleproject;

// Класс для хранения конфигурации
public class config {
    // Переменные для подключения к базе данных
    protected String USER = "postgres"; // Имя пользователя
    protected String PASSWORD = "3966"; // Пароль
    protected String URL = "jdbc:postgresql://localhost/ScheduleDB"; // URL-адрес базы данных

    // URL-адреса API
    protected String SCHEDULE = "http://forum.rsvpu.ru//contents/api/rasp.php"; // Адрес для получения расписания
    protected String GROUPS = "http://forum.rsvpu.ru/contents/api/groups.php"; // Адрес для получения групп
    protected String AUDIENCES = "http://forum.rsvpu.ru/contents/api/auds.php"; // Адрес для получения аудиторий
    protected String LECTURERS = "http://forum.rsvpu.ru/contents/api/preps.php"; // Адрес для получения преподавателей
}

