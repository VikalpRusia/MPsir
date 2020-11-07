module Mysql {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;

    exports createdNodes to javafx.fxml;
    opens main;
    opens controller;
}