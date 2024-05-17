package com.example.scheduleproject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class dataBaseHandler extends config {

    private static final Logger LOGGER = Logger.getLogger(dataBaseHandler.class.getName());

    private Connection conn;

    // Соеденения с БД
    public dataBaseHandler() {
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            LOGGER.warning("[Error] DB connection: " + e.getMessage());
        }
    }

    // Закрытие соеденения с БД
    public void close() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB disconnection: " + e.getMessage());
        }
    }

    // Метод проверки входа элемента в массив
    public boolean arrayEntry(String name, List<String> arr) {
        return arr.contains(name);
    }

    // Хэширование пароля
    public static String encodePassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] mdBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte mdByte : mdBytes) {
                sb.append(Integer.toString((mdByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    // Запросник расписания
    public JSONArray jsonSchedule() {
        JSONArray schedule = new JSONArray();
        try {
            // Make API request to retrieve group data
            String groupUrl = GROUPS;
            String groupResponse = makeApiRequest(groupUrl);
            JSONArray groupArray = new JSONArray(groupResponse);

            for (int i = 0; i < groupArray.length(); i++) {
                JSONObject group = groupArray.getJSONObject(i);

                var groupId = group.getInt("oid");

                // Make API request to retrieve schedule data for each group
                String scheduleUrl = SCHEDULE + "?&v_gru=" + groupId;
                String scheduleResponse = makeApiRequest(scheduleUrl);
                if (scheduleResponse.isEmpty()) {
                    continue;
                }
                JSONArray scheduleArray = new JSONArray(scheduleResponse);

                for (int j = 0; j < scheduleArray.length(); j++) {
                    JSONObject scheduleObject = scheduleArray.getJSONObject(j);
                    JSONObject contentObject = scheduleObject.getJSONObject("content");

                    JSONObject arr = new JSONObject();
                    // Добавление date, time, lesson, group в schedule
                    arr.put("date", scheduleObject.getString("date"));
                    arr.put("time", scheduleObject.getString("time"));
                    arr.put("lesson", scheduleObject.getInt("lesson"));
                    arr.put("group", Integer.toString(groupId));

                    JSONObject content = new JSONObject();

                    // Добавление disciplina в content
                    content.put("disciplina", contentObject.getString("disciplina"));

                    // Добавление type_disciplina в content
                    if (!contentObject.isNull("type_disciplina")) {
                        content.put("type_disciplina", contentObject.getString("type_disciplina"));
                    } else {
                        content.put("type_disciplina", "-"); // Set empty string if null
                    }

                    // Добавление aud в content
                    if (!contentObject.isNull("aud")) {
                        content.put("aud", contentObject.getString("aud"));
                    } else {
                        content.put("aud", "-"); // Set empty string if null
                    }

                    // Добавление lecturer в content
                    content.put("lecturer", contentObject.getString("lecturer"));

                    // Добавление subgroupname в content
                    if (!contentObject.isNull("subgroupname")) {
                        // Определяем регулярное выражение для поиска числа в круглых скобках
                        Pattern pattern = Pattern.compile("\\((\\d+)\\)");

                        // Создаем Matcher объект для строки str
                        Matcher matcher = pattern.matcher(contentObject.getString("subgroupname"));

                        // Проверяем, есть ли совпадение
                        if (matcher.find()) {
                            // Извлекаем найденное число
                            String number = matcher.group(1);
                            content.put("subgroupname", number);
                        }
                    } else {
                        content.put("subgroupname", "0");
                    }

                    // Добавление content в schedule
                    arr.put("content", content);

                    schedule.put(arr);
                }
            }
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to retrieve json schedule:" + e.getMessage());
        }
        return schedule;
    }

    // Метод создания API запросов
    private String makeApiRequest(String url) throws IOException {
        URL apiURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) apiURL.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            throw new IOException("Failed to make API request. Response code: " + responseCode);
        }
    }

    // Метод получения доступа к аккаунту пользователя
    public boolean accountLogin(String login, String password) {
        boolean flag = false;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT person_password FROM public.persons WHERE person_name=?;");
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String passwordFromDb = rs.getString("person_password");
                flag = passwordFromDb.equals(password);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query accountLogin: " + e.getMessage());
        }
        return flag;
    }


    // Set zone
    // Добовление пользователя
    public void insertPersons(int subgroup, String personName, String personPassword, String groupName) {
        try {
            String query = "INSERT INTO persons(subgroup, person_name, person_password, name_group) VALUES(?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, subgroup);
            pstmt.setString(2, personName);
            pstmt.setString(3, personPassword);
            pstmt.setString(4, groupName);
            pstmt.executeUpdate();
            pstmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] User not added: " + e.getMessage());
        }
    }

    // Добавление задачи ежедневника
    public void insertTask(String person_name, String task, LocalDate task_date, boolean status) {
        try {
            String query = "INSERT INTO person_task(person_name, task, task_date, task_status) VALUES(?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, person_name);
            stmt.setString(2, task);
            stmt.setDate(3, Date.valueOf(task_date));
            stmt.setBoolean(4, status);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] Task not added: " + e.getMessage());
        }
    }

    // Добавление групп в словарь
    public void parsingGroups() {
        try {
            // Make API request to retrieve group data
            String groupUrl = GROUPS;
            String groupResponse = makeApiRequest(groupUrl);
            JSONArray groupArray = new JSONArray(groupResponse);

            try (Statement statement = conn.createStatement()) {
                for (int i = 0; i < groupArray.length(); i++) {
                    JSONObject group = groupArray.getJSONObject(i);
                    int groupId = group.getInt("oid");
                    String groupName = group.getString("name");

                    String query = "INSERT INTO groups (id_group, name_group) VALUES ('" + groupId + "', '" + groupName + "')";
                    statement.execute(query);
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to retrieve json data:" + e.getMessage());
        }
    }

    // Добавление аудиторий в словарь
    public void parsingAudience() {
        try {
            // Make API request to retrieve audience data
            String audienceUrl = AUDIENCES;
            String audienceResponse = makeApiRequest(audienceUrl);
            JSONArray audienceArray = new JSONArray(audienceResponse);

            try (Statement statement = conn.createStatement()) {
                for (int i = 0; i < audienceArray.length(); i++) {
                    JSONObject audience = audienceArray.getJSONObject(i);

                    int audienceId = audience.getInt("oid");

                    String audienceName = "";

                    if (!audience.isNull("name")) {
                        audienceName = audience.getString("name");
                    }

                    String query = "INSERT INTO audience (id_audience, name_audience) VALUES ('" + audienceId + "', '" + audienceName + "')";
                    statement.execute(query);
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to retrieve json data:" + e.getMessage());
        }
    }

    // Добавление преподавателей в словарь
    public void parsingLecturers() {
        try {
            // Make API request to retrieve audience data
            String lecturerUrl = LECTURERS;
            String lecturerResponse = makeApiRequest(lecturerUrl);
            JSONArray lecturerArray = new JSONArray(lecturerResponse);

            try (Statement statement = conn.createStatement()) {
                for (int i = 0; i < lecturerArray.length(); i++) {
                    JSONObject audience = lecturerArray.getJSONObject(i);

                    int lecturerId = audience.getInt("pid");
                    String fullName = audience.getString("fio");

                    String[] names = fullName.split(" ");
                    StringBuilder abbreviatedName = new StringBuilder();

                    // Добавляем фамилию
                    abbreviatedName.append(names[0]).append(" ");

                    // Добавляем инициалы имени и отчества
                    for (int n = 1; n < names.length; n++) {
                        abbreviatedName.append(names[n].charAt(0)).append(".");
                    }
                    String lecturerName = String.valueOf(abbreviatedName);

                    try {
                        String query = "INSERT INTO lecturers (id_lecturer, fio) VALUES ('" + lecturerId + "', '" + lecturerName + "')";
                        statement.execute(query);
                    } catch (SQLException e) {
                        String query = "UPDATE lecturers SET fio = '"+lecturerName+"' WHERE id_lecturer = '"+lecturerId+"';";
                        statement.executeUpdate(query);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to lecturers json data:" + e.getMessage());
        }
    }

    // Добавление типов дисцеплин в словарь
    public void parsingTypeDisciplines(JSONArray schedule) {
        try {
            JSONArray seenIds = new JSONArray();
            for (int i = 0; i < schedule.length(); i++) {
                JSONObject row = schedule.getJSONObject(i);
                String typeDiscipline = row.getJSONObject("content").getString("type_disciplina");
                if (!seenIds.toString().contains(typeDiscipline)) {
                    seenIds.put(typeDiscipline);
                }
            }

            try (Statement statement = conn.createStatement()) {
                for (int i = 0; i < seenIds.length(); i++) {
                    String typeDiscipline = seenIds.getString(i);
                    String query = "INSERT INTO type_disciplines (name_type) VALUES ('" + typeDiscipline + "')";
                    statement.execute(query);
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to parse type disciplines:" + e.getMessage());
        }
    }

    // Добавление дисцеплин в словарь
    public void parsingDisciplines(JSONArray schedule) {
        try {
            JSONArray seenIds = new JSONArray();
            for (int i = 0; i < schedule.length(); i++) {
                JSONObject row = schedule.getJSONObject(i);
                String discipline = row.getJSONObject("content").getString("disciplina");
                if (!seenIds.toString().contains(discipline)) {
                    seenIds.put(discipline);
                }
            }

            try (Statement statement = conn.createStatement()) {
                for (int i = 0; i < seenIds.length(); i++) {
                    String discipline = seenIds.getString(i);
                    String query = "INSERT INTO disciplines (name_discipline) VALUES ('" + discipline + "')";
                    statement.execute(query);
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to parse disciplines:" + e.getMessage());
        }
    }

    // Добавление расписания звонков для всех курсов в словарь
    public void parsingCalls(JSONArray schedule) {
        try {
            JSONArray seenIds = new JSONArray();
            for (int i = 0; i < schedule.length(); i++) {
                JSONObject row = schedule.getJSONObject(i);
                JSONObject arr = new JSONObject();

                if (row.has("lesson")) {
                    Object lessonValue = row.get("lesson");
                    if (lessonValue instanceof String) {
                        arr.put("lesson", row.getString("lesson"));
                    } else if (lessonValue instanceof Integer) {
                        arr.put("lesson", String.valueOf(row.getInt("lesson")));
                    }
                }

                arr.put("lesson_time", row.getString("time"));

                if (!seenIds.toString().contains(arr.toString())) {
                    seenIds.put(arr);
                }
            }

            try (Statement statement = conn.createStatement()) {
                for (int i = 0; i < seenIds.length(); i++) {
                    JSONObject row = seenIds.getJSONObject(i);
                    String lesson = row.getString("lesson");
                    String lessonTime = row.getString("lesson_time");

                    String query = "INSERT INTO calls (lesson, lesson_time) VALUES ('" + lesson + "', '" + lessonTime + "')";
                    statement.execute(query);
                }
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query:" + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to parse calls:" + e.getMessage());
        }
    }

    // Добавление расписания в основную таблицу
    public void parsingSchedule(JSONArray schedule) {
        try (PreparedStatement preparedStatement = conn.prepareStatement(
                "INSERT INTO schedule (date, call_id, group_id, subgroup_name, discipline_id, type_discipline_id, audience_id, lecturer_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (int i = 0; i < schedule.length(); i++) {
                JSONObject gru = schedule.getJSONObject(i);
                List<String> record = new ArrayList<>();

                // Date
                String dateString = gru.optString("date");
                LocalDate date = LocalDate.parse(dateString, dateFormatter);

                // Lesson and Time
                int lesson = gru.optInt("lesson");
                String time = gru.optString("time");
                String callId = getCallIdByLessonAndTime(lesson, time);
                record.add(callId);

                // Groups
                String groupId = gru.optString("group");
                record.add(groupId);

                // Content
                JSONObject content = gru.optJSONObject("content");
                if (content == null) {
                    continue; // Skip this iteration if content is null
                }

                String subgroupName = content.optString("subgroupname");
                record.add(subgroupName);

                String discipline = content.optString("disciplina");
                String disciplineId = getDisciplineIdByName(discipline);
                record.add(disciplineId);

                String typeDiscipline = content.optString("type_disciplina");
                String typeDisciplineId = getTypeDisciplineIdByName(typeDiscipline);
                record.add(typeDisciplineId);

                String audience = content.optString("aud");
                String audienceId = getAudienceIdByName(audience);
                record.add(audienceId);

                String lecturer = content.optString("lecturer");
                String lecturerId = getLecturerIdByName(lecturer);
                record.add(lecturerId);

                // Check for null values
                if (record.contains(null)) {
//                    System.out.println(record);
                    continue; // Skip this iteration and continue with the next iteration
                }

                preparedStatement.setDate(1, java.sql.Date.valueOf(date));
                for (int j = 0; j < record.size(); j++) {
                    preparedStatement.setInt(j + 2, Integer.parseInt(record.get(j))); // Corrected the index
                }
                preparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to execute SQL query: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.warning("[Error] Failed to parse schedule: " + e.getMessage());
        }
    }


    // Get zone
    // Получения списка названий всех групп
    public String[] retrieveNameGroupData() {
        List<String> nameGroups = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT name_group FROM groups");

            while (rs.next()) {
                String nameGroup = rs.getString("name_group");
                nameGroups.add(nameGroup);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query retrieveNameGroupData: " + e.getMessage());
        }

        return nameGroups.toArray(new String[0]);
    }

    // Получения списка пользователей
    public List<String> retrieveNamePersonData() {
        List<String> namePersons = new ArrayList<>();

        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT person_name FROM public.persons");

            while (rs.next()) {
                String namePerson = rs.getString("person_name");
                namePersons.add(namePerson);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query retrieveNamePersonData: " + e.getMessage());
        }

        return namePersons;
    }

    // Получения расписание на определённый день для определённого пользователя
    public String getScheduleDay(LocalDate date, String group, int subgroup) {
        StringBuilder schedule = new StringBuilder();

        try {
            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT DISTINCT lesson, lesson_time, name_discipline, name_type, name_audience, fio " +
                            "FROM schedule, groups, calls, disciplines, type_disciplines, audience, lecturers " +
                            "WHERE schedule.call_id = calls.id_call and schedule.discipline_id = disciplines.id_discipline " +
                            "and schedule.group_id = groups.id_group and schedule.type_discipline_id = type_disciplines.id_type_discipline and " +
                            "schedule.audience_id = audience.id_audience and schedule.lecturer_id = lecturers.id_lecturer and " +
                            "groups.name_group = ? and (schedule.subgroup_name = ? or schedule.subgroup_name = 0) and schedule.date = ?");
            stmt.setString(1, group);
            stmt.setInt(2, subgroup);
            stmt.setDate(3, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                StringBuilder num = new StringBuilder();
                String lesson = rs.getString("lesson");
                String lessonTime = rs.getString("lesson_time");
                String nameDiscipline = rs.getString("name_discipline");
                String nameType = rs.getString("name_type");
                String nameAudience = rs.getString("name_audience");
                String fioLecturer = rs.getString("fio");
                num.append(lesson).append(". ").append(lessonTime).append(" - ").append(nameDiscipline);

                if (num.length() < 45){
                    num.append(" (").append(nameType).append(") ");
                } else {
                    num.append("\n(").append(nameType).append(") ");
                    schedule.append(num);
                    num = new StringBuilder();
                }

                if (num.length() < 45){
                    num.append(nameAudience).append(" ");
                } else {
                    num.append("\n").append(nameAudience).append(" ");
                    schedule.append(num);
                    num = new StringBuilder();
                }

                if (num.length() < 45){
                    num.append(fioLecturer).append("\n");
                } else {
                    num.append("\n").append(fioLecturer).append("\n");
                    schedule.append(num);
                }
                schedule.append(num);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query getScheduleDay: " + e.getMessage());
        }
        return schedule.toString();
    }

    // Получения группы пользователя
    public String getGroup(String login) {
        String group = "";
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT name_group FROM persons WHERE person_name=?;");
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                group = rs.getString("name_group");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query getGroup: " + e.getMessage());
        }
        return group;
    }

    // Получения подгруппы пользователя
    public int getSubgroup(String login) {
        int subgroup = 0;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT subgroup FROM public.persons WHERE person_name=?;");
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                subgroup = rs.getInt("subgroup");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query getSubgroup: " + e.getMessage());
        }
        return subgroup;
    }

    // Получения темы пользователя
    public boolean getTheme(String login) {
        boolean theme = true;
        try {
            PreparedStatement stmt = conn.prepareStatement("SELECT theme FROM public.persons WHERE person_name=?;");
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                theme = rs.getBoolean("theme");
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query getTheme: " + e.getMessage());
        }
        return theme;
    }

    // Получения списка задач на день
    public List<String> getTasks(String person_name, LocalDate task_date) {
        List<String> tasks = new ArrayList<>();

        try {
            String query = "SELECT task FROM person_task WHERE person_name=? and task_date=?;";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, person_name);
            stmt.setDate(2, Date.valueOf(task_date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String task = rs.getString("task");
                tasks.add(task);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query getTasks: " + e.getMessage());
        }

        return tasks;
    }

    // Получения статуса задачи
    public Boolean getTaskStatus(String person_name, LocalDate task_date, String task) {

        try {
            String query = "SELECT task_status FROM person_task WHERE person_name=? and task_date=? and task=?;";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, person_name);
            stmt.setDate(2, Date.valueOf(task_date));
            stmt.setString(3, task);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBoolean(1);
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB query getTaskStatus: " + e.getMessage());
        }
        return false;
    }

    // Получения id расписания звонка для отдельного занятия по разным курсам
    private String getCallIdByLessonAndTime(int lesson, String time) throws SQLException {
        String query = "SELECT id_call FROM calls WHERE lesson = ? AND lesson_time = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setInt(1, lesson);
        preparedStatement.setString(2, time);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }

    // Получения id дисцеплины по названию
    private String getDisciplineIdByName(String name) throws SQLException {
        String query = "SELECT id_discipline FROM disciplines WHERE name_discipline = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }

    // Получения id типа дисцеплины по названию
    private String getTypeDisciplineIdByName(String name) throws SQLException {
        String query = "SELECT id_type_discipline FROM type_disciplines WHERE name_type = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }

    // Получения id аудитории по названию
    private String getAudienceIdByName(String name) throws SQLException {
        String query = "SELECT id_audience FROM audience WHERE name_audience = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }

    // Получения id преподователя по фио
    private String getLecturerIdByName(String name) throws SQLException {
        String query = "SELECT id_lecturer FROM lecturers WHERE fio = ?";
        PreparedStatement preparedStatement = conn.prepareStatement(query);
        preparedStatement.setString(1, name);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getString(1);
        }
        return null;
    }


    // update zone
    // Метод обнавления пароля пользователя
    public void updatePassword(String login, String newPassword) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE persons SET person_password = ? WHERE person_name = ?;");
            stmt.setString(1, newPassword);
            stmt.setString(2, login);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB updatePassword: " + e.getMessage());
        }
    }

    // Метод обнавления пароля пользователя
    public void updateTask(String login, LocalDate date, String task, boolean status) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE person_task SET task_status = ? WHERE person_name = ? and task_date = ? and task = ?;");
            stmt.setBoolean(1, status);
            stmt.setString(2, login);
            stmt.setDate(3, Date.valueOf(date));
            stmt.setString(4, task);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB updateTask: " + e.getMessage());
        }
    }

    // Метод обнавления группы, подгруппы и темы пользователя
    public void updateGroupAndSubGroupAndTheme(String login, String group, int subGroup, boolean theme) {
        try {
            PreparedStatement stmt = conn.prepareStatement("UPDATE persons SET name_group = ?, subgroup = ?, theme = ? WHERE person_name = ?;");
            stmt.setString(1, group);
            stmt.setInt(2, subGroup);
            stmt.setBoolean(3, theme);
            stmt.setString(4, login);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            LOGGER.warning("[Error] DB updateGroupAndSubGroupAndTheme: " + e.getMessage());
        }
    }

    // Отчистка всех таблиц словарей
    public void clearTables() {
        try {
            try (Statement statement = conn.createStatement()) {
                statement.execute("TRUNCATE groups CASCADE");
                statement.execute("TRUNCATE audience CASCADE");
                statement.execute("TRUNCATE lecturers CASCADE");
                statement.execute("TRUNCATE type_disciplines CASCADE");
                statement.execute("TRUNCATE disciplines CASCADE");
                statement.execute("TRUNCATE calls CASCADE");
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to clear tables:" + e.getMessage());
        }
    }

    // Отчистка таблицы расписания
    public void clearScheduleTable() {
        try {
            try (Statement statement = conn.createStatement()) {
                statement.execute("TRUNCATE schedule");
            }
        } catch (SQLException e) {
            LOGGER.warning("[Error] Failed to clear schedule table:" + e.getMessage());
        }
    }


    // Pars data zone
    // Обновление таблиц словарей
    public void parsingDictionaries(){
        clearTables();
        JSONArray schedule = jsonSchedule();
        parsingGroups();
        parsingAudience();
        parsingLecturers();
        parsingDisciplines(schedule);
        parsingTypeDisciplines(schedule);
        parsingCalls(schedule);
    }

    // Обновление таблицы расписания
    public void parsingOnlySchedule(){
        clearScheduleTable();
        JSONArray schedule = jsonSchedule();
        parsingSchedule(schedule);
    }

    // Обнавление всех таблиц связанных с расисанием
    public void parsingAll(){
        clearTables();
        clearScheduleTable();
        JSONArray schedule = jsonSchedule();
        parsingGroups();
        parsingAudience();
        parsingLecturers();
        parsingDisciplines(schedule);
        parsingTypeDisciplines(schedule);
        parsingCalls(schedule);
        parsingSchedule(schedule);
    }
}

