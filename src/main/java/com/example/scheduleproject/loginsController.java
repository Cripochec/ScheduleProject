package com.example.scheduleproject;

import java.io.IOException;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class loginsController {

    @FXML
    public Button signUp; // Кнопка "Зарегистрироваться"

    @FXML
    public Button registrations; // Кнопка "Регистрация"

    @FXML
    private Button closeBut; // Кнопка "Закрыть"

    @FXML
    private Label notification; // Метка для отображения уведомлений

    @FXML
    private PasswordField password; // Поле ввода пароля

    @FXML
    private TextField login; // Поле ввода логина


    private double xOffset = 0; // Смещение по оси X
    private double yOffset = 0; // Смещение по оси Y
    dataBaseHandler db = new dataBaseHandler(); // Объект для работы с базой данных
    List<String> namePersons = db.retrieveNamePersonData(); // Список имен пользователей


    // Метод инициализации контроллера
    @FXML
    void initialize() {
        login.setOnAction(event -> password.requestFocus());

        password.setOnAction(event -> signUp.fire());
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
        if (login.getText().isEmpty() || password.getText().isEmpty()){
            login.clear();
            password.clear();
            showNotification(notification, "Пустое поле логин или пароль"); // Показываем уведомление о пустых полях логина или пароля
        } else if (!db.arrayEntry(login.getText(), namePersons)){
            login.clear();
            password.clear();
            showNotification(notification, "Такого пользователя нет"); // Показываем уведомление о несуществующем пользователе
        } else if (!db.accountLogin(login.getText(), dataBaseHandler.encodePassword(password.getText()))){
            password.clear();
            showNotification(notification, "Пароль неправильный"); // Показываем уведомление о неправильном пароле
        } else {
            try {

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("main_menu.fxml"));
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(new Scene(fxmlLoader.load()));
                stage.setResizable(false);

                main_menuController controller = fxmlLoader.getController();
                controller.setPersonName(login.getText());
                controller.setPersonGroup(db.getGroup(login.getText()));
                controller.setPersonSubgroup(db.getSubgroup(login.getText()));
                controller.setPersonTheme(db.getTheme(login.getText()));
//                controller.setScene(scene);
                db.close();

                stage.show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Обработчик события нажатия кнопки "Регистрация"
    @FXML
    public void registrations(ActionEvent event) {
        try {
            db.close();
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("registrations.fxml"));
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
