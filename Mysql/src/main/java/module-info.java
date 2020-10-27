module Mysql {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;

    opens main;
    opens controller;
}