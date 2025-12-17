module com.example.cms {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.desktop;

    opens com.example.cms to javafx.fxml;
    exports com.example.cms;
    exports com.example.cms.controllers;
    opens com.example.cms.controllers to javafx.fxml;
    exports com.example.cms.models;
    opens com.example.cms.models to javafx.fxml;
    exports com.example.cms.database;
    opens com.example.cms.database to javafx.fxml;
}