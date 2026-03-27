package pt.unl.fct.di.adc.firstwebapp.util;

public class ValidationUtils {

    public static boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }
}
