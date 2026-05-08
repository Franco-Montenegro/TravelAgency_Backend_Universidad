package mingeso.first.travelAgencyBackend.utils;

import java.util.regex.Pattern;

public class ValidationUtils {

    // Patrón RFC 5322 simplificado para validar el formato de email [cite: 63]
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }
}