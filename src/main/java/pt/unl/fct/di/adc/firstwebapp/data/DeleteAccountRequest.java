package pt.unl.fct.di.adc.firstwebapp.data;

import pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils;

public class DeleteAccountRequest {

    private String username;

    public DeleteAccountRequest() {}

    public DeleteAccountRequest(String username) {
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
