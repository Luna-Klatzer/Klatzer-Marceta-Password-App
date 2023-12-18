package at.htlleonding.password;

public class MailAndSMSSimulator {
    public static void sendMail(String to, String subject, String text) {
        System.out.println("Sending mail to " + to + " with subject " + subject + " and text " + text);
    }

    public static void sendSMS(String to, String text) {
        System.out.println("Sending SMS to " + to + " with text " + text);
    }
}
