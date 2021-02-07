package controller;

import model.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ForgotPasswordController {

    private Database database;

    @Autowired
    public ForgotPasswordController(Database database) {
        this.database = database;
    }

    @RequestMapping(value = "/hello")
    public String sendMePassword() {
        return "index";
    }
}
