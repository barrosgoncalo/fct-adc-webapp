package pt.unl.fct.di.adc.firstwebapp.data;

public class ChangeUserPwdRequest {

    private String username;
    private String oldPassword;
    private String newPassword;

    public ChangeUserPwdRequest() {}

    public ChangeUserPwdRequest(String username, String newRole) {
        this.username = username;
        this.oldPassword = newRole;
        this.newPassword = newRole;
    }

    // getters
	public String getUsername() {
		return username;
	}

	public String getOldPassword() {
		return oldPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

    public boolean isValid() {
        return nonEmptyOrBlankField(username)
            && nonEmptyOrBlankField(oldPassword);
    }
    // auxiliary
    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}
