package pt.unl.fct.di.adc.firstwebapp.data;

import pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils;

public class UsernameWrapper {

    private String username;

    public UsernameWrapper() {}

    public UsernameWrapper(String username) {
        this.username = username;
    }

    // getters
	public String getUsername() {
		return username;
	}

    public boolean isValid() {
        return ValidationUtils.nonEmptyOrBlankField(username);
    }

}
