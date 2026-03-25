package pt.unl.fct.di.adc.firstwebapp.data;

public class DeleteAccountRequest {

    private String username;

    public DeleteAccountRequest() {}

    public DeleteAccountRequest(String username) {
        this.username = username;
    }

	public String getUsername() {
		return username;
	}

    public boolean validDelete() {
        return nonEmptyOrBlankField(username);
    }

    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }
}
