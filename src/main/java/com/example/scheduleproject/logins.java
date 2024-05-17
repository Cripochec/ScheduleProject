package com.example.scheduleproject;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class logins extends Application {

    private double xOffset = 0; // Смещение по оси X
    private double yOffset = 0; // Смещение по оси Y

    @Override
    public void start(Stage stage) throws IOException {
        var fxmlLoader = new FXMLLoader(logins.class.getResource("logins.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Schedule RSVPU"); // Заголовок окна
        stage.setResizable(false); // Запрет изменения размеров окна
        stage.initStyle(StageStyle.UNDECORATED); // Удаление оформления окна (без рамок и кнопок закрытия и минимизации)
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

        Timer timer = new Timer();
        dataBaseHandler db = new dataBaseHandler();

        TimerTask parsingAll = new TimerTask() {
            @Override
            public void run() {
                db.parsingAll();
            }
        };

        TimerTask parsingDictionaries = new TimerTask() {
            @Override
            public void run() {
                db.parsingDictionaries();
            }
        };

        TimerTask parsingOnlySchedule = new TimerTask() {
            @Override
            public void run() {
                db.parsingOnlySchedule();
            }
        };


        // Запускаем задачу через 1 секунд
        timer.schedule(parsingAll, 1000);

        // Повторяем задачу каждые 30 минут, начиная с момента запуска
        timer.schedule(parsingOnlySchedule, 1800000, 1800000);

        // Повторяем задачу каждые сутки, начиная с момента запуска
        timer.schedule(parsingDictionaries, 86400000, 86400000);
    }

    public static void main(String[] args) {
        launch(); // Запуск приложения
    }
}
