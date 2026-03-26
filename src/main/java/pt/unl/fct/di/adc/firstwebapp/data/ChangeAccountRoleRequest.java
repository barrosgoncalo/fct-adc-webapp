package pt.unl.fct.di.adc.firstwebapp.data;

public class ChangeAccountRoleRequest {

    private String username;
    private String newRole;

    public ChangeAccountRoleRequest() {}

    public ChangeAccountRoleRequest(String username, String newRole) {
        this.username = username;
        this.newRole = newRole;
    }

    // getters
	public String getUsername() {
		return username;
	}

    public boolean isValid() {
        return nonEmptyOrBlankField(username)
            && nonEmptyOrBlankField(newRole);
    }

	public String getNewRole() {
		return newRole;
	}

    // auxiliary
    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}
