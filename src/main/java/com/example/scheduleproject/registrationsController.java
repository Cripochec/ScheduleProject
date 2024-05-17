package com.example.scheduleproject;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class registrationsController{
    @FXML
    public ComboBox<String> subgroups; // Выпадающий список для выбора подгруппы

    @FXML
    public Button signUp; // Кнопка "Зарегистрироваться"

    @FXML
    public Button registrations; // Кнопка "Регистрация"

    @FXML
    private Label notification; // Метка для отображения уведомлений

    @FXML
    private ComboBox<String> groups; // Выпадающий список для выбора группы

    @FXML
    private PasswordField password2; // Поле ввода подтверждения пароля

    @FXML
    private PasswordField password1; // Поле ввода пароля

    @FXML
    private TextField login; // Поле ввода логина

    @FXML
    private Button closeBut; // Кнопка "Закрыть"


    private double xOffset = 0; // Смещение по оси X
    private double yOffset = 0; // Смещение по оси Y
    dataBaseHandler db = new dataBaseHandler(); // Объект для работы с базой данных
    List<String> namePersons = db.retrieveNamePersonData(); // Список имен пользователей


    // Метод инициализации контроллера
    @FXML
    void initialize() {
        groups.getItems().addAll(db.retrieveNameGroupData()); // Заполняем выпадающий список групп из базы данных при инициализации окна

        login.setOnAction(event -> password1.requestFocus());

        password1.setOnAction(event -> password2.requestFocus());

        password2.setOnAction(event -> groups.requestFocus());

        groups.setOnAction(event -> subgroups.requestFocus());

        groups.setOnMouseClicked(event -> {
            if (groups.getItems() == null){
                groups.getItems().addAll(db.retrieveNameGroupData());
            }
        });
    }

    // Обработчик события нажатия кнопки мыши
    @FXML
    private void onMousePressed(MouseEvent event) {
        xOffset = event.getSceneX(); // Запоминаем текущее положение мыши по оси X
        yOffset = event.getSceneY(); // Запоминаем текущее положение мыши по оси Y
    }

    // Обработчик события перемещения мыши
    @FXML
    private void onMouseDragged(MouseEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setX(event.getScreenX() - xOffset); // Задаем новое положение окна по оси X
        stage.setY(event.getScreenY() - yOffset); // Задаем новое положение окна по оси Y
    }

    // Обработчик события нажатия кнопки "Sign Up"
    @FXML
    public void signUp(ActionEvent event) {
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
    }

    // Обработчик события нажатия кнопки "Регистрация"
    @FXML
    public void registrations(ActionEvent event) {
        if (login.getText().equals("") || password1.getText().equals("") || password2.getText().equals("") || groups.getValue() == null || subgroups.getValue() == null){
            showNotification(notification, "Заполните все поля"); // Проверка на заполнение всех полей формы регистрации
        } else if (login.getText().length() < 5 || password1.getText().length() < 5){
            login.clear();
            password1.clear();
            password2.clear();
            showNotification(notification, "Минимальная длина логина и пароля 5 символов"); // Проверка на минимальную длину логина и пароля
        } else if (db.arrayEntry(login.getText(), namePersons)){
            login.clear();
            showNotification(notification, "Логин занят"); // Проверка на уникальность логина
        } else if (!password1.getText().equals(password2.getText())){
            password1.clear();
            password2.clear();
            showNotification(notification, "Пароли не совпадают"); // Проверка на совпадение паролей
        } else {
            int subgroup;
            try {
                subgroup = Integer.parseInt(subgroups.getValue());
            } catch (NumberFormatException e) {
                subgroup = 0;
            }

            db.insertPersons(subgroup, login.getText(), dataBaseHandler.encodePassword(password1.getText()), groups.getValue()); // Регистрация пользователя в базе данных

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
        }
    }

    // Метод для закрытия окна
    @FXML
    public void closeBut() {
        db.close();
        Stage stage = (Stage) closeBut.getScene().getWindow();
        stage.close();
    }

    // Метод для отображения уведомления
    @FXML
    private void showNotification(Label notificationLabel, String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), evt -> notificationLabel.setVisible(false)));
        timeline.play();
    }
}
