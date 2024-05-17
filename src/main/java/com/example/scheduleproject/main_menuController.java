package com.example.scheduleproject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;


import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class main_menuController {

    @FXML
    private Label dateWeek;  // Метка для отображения текущей недели

    @FXML
    private AnchorPane rootPane; // Основная область приложения

    @FXML
    private Label schedulDay;  // Метка для отображения расписания на выбранный день

    @FXML
    private VBox taskContainer;  // Контейнер для задач пользователя

    @FXML
    private TextField taskTextField;  // Поле ввода для добавления новой задачи

    @FXML
    private AnchorPane menu;  // Панель для отображения меню пользователя

    @FXML
    private AnchorPane blackout;  // Затемненная область при открытом меню

    @FXML
    private Label login;  // Метка для отображения имени пользователя

    @FXML
    private ComboBox<String> group;  // Выпадающий список для выбора группы пользователя

    @FXML
    private ComboBox<String> subgroup;  // Выпадающий список для выбора подгруппы пользователя

    @FXML
    private ToggleButton darkStyle;  // Кнопка для включения/выключения темной темы

    @FXML
    private PasswordField oldPassword;  // Поле ввода текущего пароля пользователя

    @FXML
    private PasswordField newPassword;  // Поле ввода нового пароля пользователя

    @FXML
    private Label notification;  // Метка для отображения уведомлений

    @FXML
    private Button rightBut;  // Кнопка для переключения на следующую неделю

    @FXML
    private Button leftBut;  // Кнопка для переключения на предыдущую неделю

    @FXML
    private Button pn;  // Кнопка для выбора понедельника

    @FXML
    private Button vt;  // Кнопка для выбора вторника

    @FXML
    private Button sr;  // Кнопка для выбора среды

    @FXML
    private Button ct;  // Кнопка для выбора четверга

    @FXML
    private Button pt;  // Кнопка для выбора пятницы

    @FXML
    private Button sb;  // Кнопка для выбора субботы

    @FXML
    private Button vs;  // Кнопка для выбора воскресенья

    @FXML
    private Button closeBut;  // Кнопка для закрытия приложения

    private int page = 1;  // Текущая страница (1 - первая неделя)
    private int activeDay;  // Активный день недели
    private String personName;  // Имя пользователя
    private String personGroup;  // Группа пользователя
    private int personSubgroup;  // Подгруппа пользователя
    private boolean personTheme;  // Тема интерфейса пользователя (светлая/темная)
    private double xOffset = 0;  // Смещение по оси X для перемещения окна
    private double yOffset = 0;  // Смещение по оси Y для перемещения окна
    dataBaseHandler db = new dataBaseHandler();  // Обработчик базы данных
    LocalDate today = LocalDate.now();  // Текущая дата
    Timer timer = new Timer();
    DayOfWeek dayOfWeek = today.getDayOfWeek();  // День недели текущей даты
    int numericValue = dayOfWeek.getValue();  // Числовое значение дня недели (1-понедельник, 2-вторник и т.д.)
    LocalDate startDate = today.minus(numericValue - 1, ChronoUnit.DAYS);  // Дата начала текущей недели
    private Scene scene;

    @FXML
    private void onMousePressed(MouseEvent event) {
        xOffset = event.getSceneX();
        yOffset = event.getSceneY();
    }

    @FXML
    private void onMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset);
        stage.setY(event.getScreenY() - yOffset);
    }

    @FXML
    void initialize() {
        // Стартовые действия видимости объектов
        leftBut.setStyle("-fx-opacity: 0;");
        rightBut.setStyle("-fx-opacity: .50;");
        blackout.setVisible(false);
        menu.setVisible(false);
//        rootPane.setVisible(false);


        // Установка активной недели
        dateWeek.setText(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " - " + startDate.plus(6, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));



        TimerTask start = new TimerTask() {
            @Override
            public void run() {
                scene = rootPane.getScene();

                // Установка стартовой темы
                if (getPersonTheme()) {
                    setTheme("dark_style.css");
                    darkStyle.fire();
                    darkStyle.setText("On");
                } else {
                    setTheme("style.css");
                    darkStyle.setText("Off");
                }

                // Установка активного дня
                setActiveDay();
            }
        };

        timer.schedule(start, 10);

        oldPassword.setOnAction(event -> newPassword.requestFocus());
    }

    @FXML
    public void menuBut() {
        // Установка логина пользователя
        login.setText(getPersonName());

        blackout.setVisible(true);
        menu.setVisible(true);

        // Установка группы пользователя
        setPersonGroup(db.getGroup(getPersonName()));
        group.getItems().addAll(db.retrieveNameGroupData());
        group.setValue(getPersonGroup());

        // Установка подгруппы пользователя
        setPersonSubgroup(db.getSubgroup(getPersonName()));
        subgroup.setValue(String.valueOf(getPersonSubgroup()));
    }

    @FXML
    public void OkMenuBut() {
        // Обновление группы, подгруппы и темы пользователя
        int subgroups;
        try {
            subgroups = Integer.parseInt(subgroup.getValue());
        } catch (NumberFormatException e) {
            subgroups = 0;
        }

        setPersonGroup(group.getValue());
        setPersonSubgroup(subgroups);
        db.updateGroupAndSubGroupAndTheme(getPersonName(), getPersonGroup(), getPersonSubgroup(), getPersonTheme());

        // Обновление пароля пользователя
        notification.setStyle("-fx-text-fill: #eb5643;");
        if (!oldPassword.getText().equals("") || !newPassword.getText().equals("")) {
            if (oldPassword.getText().equals(newPassword.getText())) {
                newPassword.clear();
                showNotification(notification, "Пароли совпадают");
            } else if (newPassword.getText().length() < 5) {
                newPassword.clear();
                showNotification(notification, "Минимальная длина 5");
            } else if (!db.accountLogin(getPersonName(), dataBaseHandler.encodePassword(oldPassword.getText()))) {
                oldPassword.clear();
                showNotification(notification, "Пароль не верен");
            } else {
                db.updatePassword(getPersonName(), dataBaseHandler.encodePassword(newPassword.getText()));
                notification.setStyle("-fx-text-fill: #3f7733;");
                notification.setText("Данные изменены");
                notification.setVisible(true);
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
                    oldPassword.clear();
                    newPassword.clear();
                    notification.setVisible(false);
                    blackout.setVisible(false);
                    menu.setVisible(false);
                    if (page == 1) {
                        setActiveDay();
                    } else {
                        pn.fire();
                    }
                }));
                timeline.play();
            }
        } else {
            notification.setStyle("-fx-text-fill: #3f7733;");
            notification.setText("Ok");
            notification.setVisible(true);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), evt -> {
                oldPassword.clear();
                newPassword.clear();
                notification.setVisible(false);
                blackout.setVisible(false);
                menu.setVisible(false);
                if (page == 1) {
                    setActiveDay();
                } else {
                    pn.fire();
                }
            }));
            timeline.play();
        }
    }

    @FXML
    public void closeMenuBut() {
        oldPassword.clear();
        newPassword.clear();
        blackout.setVisible(false);
        menu.setVisible(false);
        if (page == 1) {
            setActiveDay();
        } else {
            pn.fire();
        }
    }


    @FXML
    public void addTaskBut() {
        String taskText = taskTextField.getText();

        if (!taskText.isEmpty()) {
            db.insertTask(getPersonName(), taskText, startDate.plus(activeDay, ChronoUnit.DAYS), false);
            CheckBox taskCheckBox = new CheckBox(taskText);
            taskCheckBox.setOnAction(event -> handleCheckBoxAction(taskCheckBox));
            taskContainer.getChildren().add(taskCheckBox);
            taskTextField.clear();
        }
    }

    private void handleCheckBoxAction(CheckBox checkBox) {
        db.updateTask(getPersonName(), startDate.plus(activeDay, ChronoUnit.DAYS),checkBox.getText(), checkBox.isSelected());
    }

    @FXML
    public void darkStyle() {
        if (darkStyle.isSelected()) {
            darkStyle.setText("On");
            setTheme("dark_style.css");
            setPersonTheme(true);
        } else {
            darkStyle.setText("Off");
            setTheme("style.css");
            setPersonTheme(false);
        }
    }


    @FXML
    public void rightBut() {
        page = 2;
        rightBut.setStyle("-fx-opacity: 0;");
        leftBut.setStyle("-fx-opacity: .50;");
        dateWeek.setText(startDate.plus(7, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " - " + startDate.plus(13, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        pn.fire();
    }

    @FXML
    public void leftBut() {
        page = 1;
        leftBut.setStyle("-fx-opacity: 0;");
        rightBut.setStyle("-fx-opacity: .50;");
        dateWeek.setText(startDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) + " - " + startDate.plus(6, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));

        setActiveDay();
    }


    @FXML
    public void pn() {
        setDayStyle(1);
    }
    @FXML
    public void vt() {
        setDayStyle(2);
    }
    @FXML
    public void sr() {
        setDayStyle(3);
    }
    @FXML
    public void ct() {
        setDayStyle(4);
    }
    @FXML
    public void pt() {
        setDayStyle(5);
    }
    @FXML
    public void sb() {
        setDayStyle(6);
    }
    @FXML
    public void vs() {
        setDayStyle(7);
    }



    @FXML
    public void closeBut() {
        db.close();
        Stage stage = (Stage) closeBut.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void out(ActionEvent event) {
        try {
            db.close();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("logins.fxml"));
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        db.close();
    }


    // Метод для установки стиля и выполнения общих операций для дней недели
    private void setDayStyle(int day) {
        pn.setStyle("-fx-opacity: " + (day == 1 ? "1" : ".32") + ";");
        vt.setStyle("-fx-opacity: " + (day == 2 ? "1" : ".32") + ";");
        sr.setStyle("-fx-opacity: " + (day == 3 ? "1" : ".32") + ";");
        ct.setStyle("-fx-opacity: " + (day == 4 ? "1" : ".32") + ";");
        pt.setStyle("-fx-opacity: " + (day == 5 ? "1" : ".32") + ";");
        sb.setStyle("-fx-opacity: " + (day == 6 ? "1" : ".32") + ";");
        vs.setStyle("-fx-opacity: " + (day == 7 ? "1" : ".32") + ";");

        if (page == 1) {
            schedulDay.setText(db.getScheduleDay(startDate.plus(day - 1, ChronoUnit.DAYS), getPersonGroup(), getPersonSubgroup()));
            activeDay = day - 1;
        } else {
            schedulDay.setText(db.getScheduleDay(startDate.plus(day + 6, ChronoUnit.DAYS), getPersonGroup(), getPersonSubgroup()));
            activeDay = day + 6;
        }

        taskContainer.getChildren().clear();
        for (String e : db.getTasks(getPersonName(), startDate.plus(activeDay, ChronoUnit.DAYS))) {

            CheckBox taskCheckBox = new CheckBox(e);
            taskCheckBox.setSelected(db.getTaskStatus(getPersonName(), startDate.plus(activeDay, ChronoUnit.DAYS), e));
            taskCheckBox.setOnAction(event -> handleCheckBoxAction(taskCheckBox));
            taskContainer.getChildren().add(taskCheckBox);
        }
    }

    // Метод для установки активного дня недели
    private void setActiveDay() {
        switch (numericValue) {
            case 1 -> pn.fire();
            case 2 -> vt.fire();
            case 3 -> sr.fire();
            case 4 -> ct.fire();
            case 5 -> pt.fire();
            case 6 -> sb.fire();
            case 7 -> vs.fire();
        }
    }

    // Метод для вывода уведомления на объект label на 2 екунды
    private void showNotification(Label notificationLabel, String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), evt -> notificationLabel.setVisible(false)));
        timeline.play();
    }

    // Устанавливает тему css
    private void setTheme(String theme) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource(theme)).toExternalForm());
    }

    // Возвращает имя персоны
    public String getPersonName() {
        return personName;
    }

    // Устанавливает имя персоны
    public void setPersonName(String personName) {
        this.personName = personName;
    }

    // Устанавливает группу персоны
    public void setPersonGroup(String personGroup) {
        this.personGroup = personGroup;
    }

    // Возвращает группу персоны
    public String getPersonGroup() {
        return personGroup;
    }

    // Устанавливает подгруппу персоны
    public void setPersonSubgroup(int personSubgroup) {
        this.personSubgroup = personSubgroup;
    }

    // Возвращает подгруппу персоны
    public int getPersonSubgroup() {
        return personSubgroup;
    }

    // Возвращает тему персоны
    public boolean getPersonTheme() {
        return personTheme;
    }

    // Устанавливает тему персоны
    public void setPersonTheme(boolean personTheme) {
        this.personTheme = personTheme;
    }
}

