module Mysql {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;
    requires mysql.connector.java;
    requires org.slf4j;
    requires org.apache.httpcomponents.httpclient;
    requires org.apache.httpcomponents.httpcore;
    requires org.controlsfx.controls;

    exports createdNodes to javafx.fxml;
    opens main;
    opens controller;
}