package controller;

import model.AdminMail;
import model.Database;
import model.NonAdminMail;
import model.SetUpPDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;

@Controller
public class ForgotPasswordController {

    private final Database database;
    private final AdminMail adminMail;
    private final NonAdminMail nonAdminMail;
    private final SetUpPDF setUpPDF;

    @Autowired
    public ForgotPasswordController(Database database, AdminMail adminMail, SetUpPDF setUpPDF,NonAdminMail nonAdminMail) {
        this.database = database;
        this.adminMail = adminMail;
        this.setUpPDF = setUpPDF;
        this.nonAdminMail = nonAdminMail;
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
    public String sendMePassword(@RequestParam(value = "search", defaultValue = "") String toBeSearched) throws SQLException, IOException {
        String[] details = database.sendMail_Passcode_DOB_Phone(toBeSearched);
        Path path = Files.createTempFile("", ".pdf");
        setUpPDF.main(details[1], details[2], details[3], path.toAbsolutePath().toString());
        adminMail.setRecipient(details[0]);
        adminMail.setFilePath(path.toAbsolutePath().toString());
        adminMail.sendMail();
        return toBeSearched;
    }

    @ResponseBody
    @RequestMapping(value = "/non-admin", method = RequestMethod.POST)
    public String notifyAdmin(@RequestParam(value = "name", defaultValue = "") String name,
                              @RequestParam(value = "UUID", defaultValue = "") String UUID) throws SQLException {
        nonAdminMail.setUserName(name);
        nonAdminMail.setRecipient(database.getMail(UUID));
        nonAdminMail.sendMail();
        return "succesfully sent";
    }
}