package controller;

import model.Database;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String sendMePassword(@RequestParam(value = "search", defaultValue = "") String toBeSearched) throws SQLException {
        String result = database.search(toBeSearched);
        if (result !=null) {
            return "true\n" + result;
        } else {
            return "false";
        }
    }
}