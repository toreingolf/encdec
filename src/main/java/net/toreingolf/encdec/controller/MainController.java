package net.toreingolf.encdec.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@RestController
@RequestMapping("/")
public class MainController {

    private final static String ENCODE = "encode";
    private final static String DECODE = "decode";

    @GetMapping
    public String frontend() {
        StringBuilder page = pageOpen();
        displayForm(page, "", ENCODE, true);
        displayLinkToExample(page);
        return pageClose(page);
    }

    @PostMapping("/process")
    public String process(@RequestParam String input,
                          @RequestParam String mode,
                          @RequestParam(defaultValue = "0") String zip) {
        var doZip = zip != null && zip.equals("1");

        StringBuilder page = pageOpen();
        displayForm(page, input, mode, doZip);

        if (input != null && input.length() > 0) {
            try {
                var result = mode.equals(DECODE)
                        ? (doZip ? decodeAndUnzip(input) : new String(decodeFromString(input)))
                        : (doZip ? zipAndEncode(input) : new String(encodeBytes(input.getBytes())));
                displayTextArea(page, "Result", result);
            } catch (Exception e) {
                page.append(formatError(e));
            }
        }

        displayLinkToExample(page);

        return pageClose(page);
    }

    @GetMapping("/example")
    public String example() throws IOException {

        String originalString =
                "H4sIAAAAAAAA/+1WTY/aMBC9V+p/iHIndpzwKeNVd5dKdAmlgFDbmxVciEoclDiw/PudfDmBdrccuqfd"
                        + "EzPPb+aNxw8EvXkMd8ZBxEkQyaFpW9g0hPSjdSA3QzNVv1o984Z9/EDvRrf3t7vI/21AgUwGj0kwNLdK"
                        + "7QcIHY9H6+hYUbxBBGMbffcmC38rQt4KZKK49IWpq9b/rgJyEgxkNOWhSPbcFwU8iXyu8in1LBb0M2E6"
                        + "w6Ajb5EHEN7NxvdlDNl0NWefxxRlnxpcPXjsy8PXHxRlkYZHqynruzYmuGdjB2OKMqQ+9hbQ2QY0D0o5"
                        + "1NCjy/mMzZZtj6IsKkG42arYMIMFU9TI89lRNTz1BE/SWIRCqrFUIj7wXdVkoXisGMGEtLDTIvbSdgZO"
                        + "e4DxT4qKs5I4kusLmlvSspNC8Dmd/Jn1HiGGm/X77a4Ltyyy6p5BKEA13D8zUX1+WfAt5btAnZhNug1a"
                        + "hVbSWy6l2NWrLwEYgMAoOtHny5j7mTkWp0SJkGF4pAuofkUp4s1J54B8At5BNJBMMJJJGoo1w9mL6eyM"
                        + "MxcbAc24qmhNoNEf/SEApfy1Rf8iQc+Wf77z3IfN1eg9V3n1BawLJpCrdC1YB1vEdnpgEw3VpEhuCoi4"
                        + "Vp+4PXibGtO0F0bL+A3pwosv+rV9rV9d/O7Xt+vX7lV+Ja/v187Vfn3/fX27fu1e5ddO73/5tQjyf1rs"
                        + "CTCbf1wpCgAA";

        var decodedString = decodeAndUnzip(originalString);
        var encodedString = zipAndEncode(decodedString);

        StringBuilder page = pageOpen();
        displayTextArea(page, "Original input string", originalString);
        displayTextArea(page, "Decoded and unzipped output", decodedString);
        displayTextArea(page,"Re-zipped and encoded output", encodedString);

        displayLink(page, "/", "Return to main page");

        return pageClose(page);
    }

    private String formatError(Exception e) {
        return "<span style=\"color: Red; font-weight: Bold\">Error: " + e + "</span>";
    }

    private String decodeAndUnzip(String input) throws IOException {
        var is = new ByteArrayInputStream(decodeFromString(input));
        var decodedByteArray = new GZIPInputStream(is).readAllBytes();
        return new String(decodedByteArray);
    }

    private String zipAndEncode(String input) throws IOException {
        return new String(encodeBytes(zipBytes(input.getBytes())));
    }

    private StringBuilder pageOpen() {
        return new StringBuilder()
                .append("<!DOCTYPE html><html>")
                .append("<head><title>ENC/DEC</title>")
                .append("<style type=\"text/css\">")
                .append("body { font-family:Arial,Helvetica; font-size:10pt }")
                .append("h1 { font-size:14pt; font-weight:Bold }")
                .append("h2 { font-size:12pt; font-weight:Bold }")
                .append("</style>")
                .append("</head><body><h1>The Encode/Decode App</h1>")
                ;
    }

    private String pageClose(StringBuilder sb) {
        return sb.append("</body></html>").toString();
    }

    private void displayForm(StringBuilder sb, String input, String mode, boolean zip) {
        sb.append("<form method=\"post\" action=\"process\">");
        displayTextArea(sb, "Input", input);
        sb.append("<table cellpadding=10><tr><td>")
                .append(modeRadioButton("Encode", mode))
                .append(modeRadioButton("Decode", mode))
                .append("</td><td valign=\"top\">")
                .append(zipCheckBox(zip))
                .append("</td><td valign=\"top\">")
                .append("<input type=\"submit\" value=\"Go\">")
                .append("</table></form>")
                .append("<script language=\"JavaScript\">document.forms[0].elements[0].focus();</script>")
        ;
    }

    private void displayTextArea(StringBuilder sb, String label, String value) {
        sb.append(heading(label))
                .append("<textarea name=\"")
                .append(label.toLowerCase())
                .append("\" cols=120 rows=10>")
                .append(value)
                .append("</textarea><br>")
        ;
    }

    private String heading(String text) {
        return "<h2>" + text + "</h2>";
    }

    private String modeRadioButton(String label, String currentMode) {
        var id = label.toLowerCase();

        return "<input type=\"radio\" id=\"" + id + "\" name=\"mode\" value=\"" + id + "\""
                + (id.equals(currentMode) ? " checked=\"checked\"" : "")
                + ">" + fieldLabel(label, id)
                + "<br>";
    }

    private String zipCheckBox(boolean zip) {
        return "<input type=\"checkbox\" id=\"zip\" name=\"zip\" value=\"1\""
                + (zip ? " checked" : "")
                + ">" + fieldLabel("Zip?", "zip")
                ;
    }

    private String fieldLabel(String label, String fieldId) {
        return "<label for=\"" + fieldId + "\">" + label + "</label>";
    }

    private void displayLink(StringBuilder sb, String path, String label) {
        sb.append("<p><a href=\"")
                .append(path)
                .append("\">")
                .append(label)
                .append("</a>")
        ;
    }

    private void displayLinkToExample(StringBuilder sb) {
        displayLink(sb, "/example", "Example");
    }

    private byte[] decodeFromString(String input) {
        return Base64.getDecoder().decode(input);
    }

    private byte[] encodeBytes(byte[] input) {
        return Base64.getEncoder().encode(input);
    }

    private byte[] zipBytes(byte[] input) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(input.length);
        try (baos) {
            try (GZIPOutputStream gos = new GZIPOutputStream(baos)) {
                gos.write(input);
                gos.finish();
                gos.flush();
                baos.flush();
            }
        }
        return baos.toByteArray();
    }
}
