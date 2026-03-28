package pt.unl.fct.di.adc.firstwebapp.data;

import pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils;

public class ChangeUserRoleRequest {

    private String username;
    private String newRole;

    public ChangeUserRoleRequest() {}

    public ChangeUserRoleRequest(String username, String newRole) {
        this.username = username;
        this.newRole = newRole;
    }

    // getters
	public String getUsername() {
		return username;
	}

    public boolean isValid() {
        return ValidationUtils.nonEmptyOrBlankField(username)
            && ValidationUtils.nonEmptyOrBlankField(newRole)
            && Role.isDefined(newRole);
    }

	public String getNewRole() {
		return newRole;
	}

}
