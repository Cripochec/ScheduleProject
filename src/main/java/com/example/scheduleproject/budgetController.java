package com.example.scheduleproject;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;

public class budgetController {

    @FXML
    private TextField defenseLir;

    @FXML
    private TextField sciencePercent;

    @FXML
    private Label budgetGap;

    @FXML
    private TextField culturePercent;

    @FXML
    private TextField scienceLir;

    @FXML
    private TextField socialPoliticsLir;

    @FXML
    private TextField educationPercent;

    @FXML
    private TextField socialPoliticsPercent;

    @FXML
    private TextField annualBudget;

    @FXML
    private TextField educationLir;

    @FXML
    private TextField healthcarePercent;

    @FXML
    private TextField cultureLir;

    @FXML
    private TextField healthcareLir;

    @FXML
    private TextField defensePercent;

    @FXML
    private Label info;
    public double Budget;

    @FXML
    void initialize() {
        annualBudget.setOnKeyReleased(event -> {
            dataUpdate();
        });

        defensePercent.setOnKeyReleased(event -> {
            try {
                defenseLir.clear();
                if (!defensePercent.getText().equals("")){
                    defenseLir.setText(String.valueOf((int) (Budget/100 * Integer.parseInt(defensePercent.getText()))));
                    dataUpdate();
                }
            } catch (Exception e){
                showNotification(info, "Неправильное значение процентов");
            }
        });




    }

    public void dataUpdate() {
        try {
            Budget = Double.parseDouble(annualBudget.getText());
        } catch (Exception e){
            showNotification(info, "Неправильное значение бюджета");
        }

        try {
            Budget -= Double.parseDouble(defenseLir.getText());
        } catch (NumberFormatException ignored){}
        try {
            Budget -= Double.parseDouble(scienceLir.getText());
        } catch (NumberFormatException ignored){}
        try {
            Budget -= Double.parseDouble(cultureLir.getText());
        } catch (NumberFormatException ignored){}
        try {
            Budget -= Double.parseDouble(socialPoliticsLir.getText());
        } catch (NumberFormatException ignored){}
        try {
            Budget -= Double.parseDouble(educationLir.getText());
        } catch (NumberFormatException ignored){}
        try {
            Budget -= Double.parseDouble(healthcareLir.getText());
        } catch (NumberFormatException ignored){}

        budgetGap.setText(Budget + " лир.");
    }

    private void showNotification(Label notificationLabel, String message) {
        notificationLabel.setText(message);
        notificationLabel.setVisible(true);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), evt -> notificationLabel.setVisible(false)));
        timeline.play();
    }
}

