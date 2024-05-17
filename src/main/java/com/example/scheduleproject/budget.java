package com.example.scheduleproject;


import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class budget extends Application {

    private double xOffset = 0; // Смещение по оси X
    private double yOffset = 0; // Смещение по оси Y

    @Override
    public void start(Stage stage) throws IOException {
        var fxmlLoader = new FXMLLoader(logins.class.getResource("budget.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("budget"); // Заголовок окна
        stage.setResizable(false); // Запрет изменения размеров окна
        stage.setScene(scene);

        scene.setOnMousePressed(event -> { // Обработчик события нажатия кнопки мыши на сцене
            xOffset = event.getSceneX();
            yOffset = event.getSceneY();
        });

        scene.setOnMouseDragged(event -> { // Обработчик события перемещения мыши по сцене
            stage.setX(event.getScreenX() - xOffset);
            stage.setY(event.getScreenY() - yOffset);
        });
        stage.show(); // Отображение окна
    }

    public static void main(String[] args) {
        launch(); // Запуск приложения
    }
}
