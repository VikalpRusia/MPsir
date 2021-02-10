package controller;

import model.Database;
import model.Mailing;
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
    private final Mailing mailing;

    @Autowired
    public ForgotPasswordController(Database database, Mailing mailing) {
        this.database = database;
        this.mailing = mailing;
    }

    @ResponseBody
    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public String validateUser(@RequestParam(value = "search", defaultValue = "") String toBeSearched) throws SQLException {
        String result = database.search(toBeSearched);
        if (result != null) {
            return "true\n" + result;
        } else {
            return "false";
        }
    }

    @ResponseBody
    @RequestMapping(value = "/showPassword", method = RequestMethod.POST)
    public String sendMePassword(@RequestParam(value = "search", defaultValue = "") String toBeSearched) {
        mailing.setRecipient("vikalprusia@gmail.com");
        mailing.sendMail();
        return toBeSearched;
    }
}