package controller;

import model.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.SQLException;

@Controller
public class ForgotPasswordController {

    private final Database database;

    @Autowired
    public ForgotPasswordController(Database database) {
        this.database = database;
    }

    @ResponseBody
    @RequestMapping(value = "/hello")
    public String sendMePassword() throws SQLException {
        return String.valueOf(database.search("9644919131"));
    }
}