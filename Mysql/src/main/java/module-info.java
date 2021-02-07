module Mysql {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
    requires mysql.connector.java;
    requires org.slf4j;

    exports createdNodes to javafx.fxml;
    opens main;
    opens controller;
}