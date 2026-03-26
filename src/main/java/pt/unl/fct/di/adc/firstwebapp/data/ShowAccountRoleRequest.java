package pt.unl.fct.di.adc.firstwebapp.data;

public class ShowAccountRoleRequest {

    private String username;

    public ShowAccountRoleRequest() {}

    public ShowAccountRoleRequest(String username) {
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
