package at.htlleonding.password;

public class Validator {
    public static boolean validateUsername(String username) {
        // Username is an email address
        return username.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean validatePhoneNumber(String phoneNumber) {
        // Phone number is a number with 6 or more digits (with optional country code and optional spaces)
        return phoneNumber.matches("^(\\+\\d{1,3} )?\\d{6,}$");
    }

    public static boolean validatePassword(String password) {
        // Password is at least 8 characters long and contains at least one digit, one lowercase letter, one uppercase letter and one special character
        return password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=?!_.,\\-*}{\\[\\]])(?=\\S+$).{8,}$");
    }
}
