module com.example.scheduleproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.json;
    requires java.sql;
    requires org.postgresql.jdbc;


    opens com.example.scheduleproject to javafx.fxml;
    exports com.example.scheduleproject;
}