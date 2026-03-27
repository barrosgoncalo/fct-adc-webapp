package pt.unl.fct.di.adc.firstwebapp.security;

import pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils;

public class SecurityConfig {

    private static final String MASTER_KEY = System.getenv("MASTER_KEY");
    private static final String ERROR_MESSAGE = "CRITICAL ERROR: MASTER_KEY is not set in environment.";

	public static String getMasterKey() {
        if(!ValidationUtils.nonEmptyOrBlankField(MASTER_KEY))
            throw new IllegalStateException(ERROR_MESSAGE);
        return MASTER_KEY;
	}

}
