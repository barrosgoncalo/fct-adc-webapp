package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.regex.Pattern;

public class ValidationUtils {

        private static final String SETUP_CHECK = "^(?=.{1,64}@)";
        private static final String PLUS = "+";
        private static final String AT = "@";
        private static final String END_OF_STRING = "$";

        private static final String LOCAL_REGULAR = "[A-Za-z0-9_-]";
        private static final String LOCAL_SPECIAL = "(\\.[A-Za-z0-9_-]+)*";
        private static final String COMMON_LOCAL_PATTERN = LOCAL_REGULAR + PLUS + LOCAL_SPECIAL;

        private static final String DOMAIN_REGULAR = "[A-Za-z0-9-]";
        private static final String DOMAIN_SPECIAL = "(\\.[A-Za-z0-9-]+)*";
        private static final String COMMON_DOMAIN_PATTERN = DOMAIN_REGULAR + PLUS + DOMAIN_SPECIAL;

        private static final String INITAL_FORBBIDEN = "[^-]";
        private static final String TLD_PATTERN = "(\\.[A-Za-z]{2,})";

        private static final String REGEX_PATTERN =
            SETUP_CHECK + COMMON_LOCAL_PATTERN + AT + INITAL_FORBBIDEN + COMMON_DOMAIN_PATTERN + TLD_PATTERN + END_OF_STRING;

    public static boolean validEmail(String email) {
        return Pattern.compile(REGEX_PATTERN)
            .matcher(email)
            .matches();
    }

    public static boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }
}
