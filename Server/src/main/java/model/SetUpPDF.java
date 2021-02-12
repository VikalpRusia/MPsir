package model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class SetUpPDF {
    private PDDocument document;
    private final String setTitle;
    private final String setAuthor;

    public SetUpPDF(@Value("${pdf.title}") String setTitle, @Value("${pdf.author}") String setAuthor) {
        this.setTitle = setTitle;
        this.setAuthor = setAuthor;
    }

    public void main(String password, String dob, String phoneNumber) throws IOException {
        this.document = new PDDocument();
        PDDocumentInformation info = document.getDocumentInformation();
        info.setTitle(setTitle);
        info.setAuthor(setAuthor);
        insertText(password);
        encrypt(dob, phoneNumber);
        save();
    }

    private void insertText(String password) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);
        PDPageContentStream contentStream = new PDPageContentStream(document, page);
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
        contentStream.setLeading(14.5f);
        contentStream.newLineAtOffset(50, 725);
        contentStream.showText(password);
        contentStream.endText();
        contentStream.close();
    }

    private void encrypt(String dob, String phoneNumber) throws IOException {
        AccessPermission ac = new AccessPermission();
        StandardProtectionPolicy sp = new StandardProtectionPolicy(dob + "@" + phoneNumber,
                dob + "@" + phoneNumber, ac);
        sp.setEncryptionKeyLength(128);
        document.protect(sp);
    }

    private void save() throws IOException {
        document.save("C:/Users/vikal/Desktop/reset-password.pdf");
        document.close();
    }
}
