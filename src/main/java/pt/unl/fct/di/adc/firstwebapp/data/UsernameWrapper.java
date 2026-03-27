package pt.unl.fct.di.adc.firstwebapp.data;

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
        return nonEmptyOrBlankField(username);
    }

    // auxiliary
    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }
}
