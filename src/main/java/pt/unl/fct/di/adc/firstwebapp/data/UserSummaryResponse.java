package pt.unl.fct.di.adc.firstwebapp.data;


public class UserSummaryResponse {

    private String username;
    private String role;

    public UserSummaryResponse() {}

    public UserSummaryResponse(String username, String role) {

        this.username = username;
        this.role = role;
    }

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setRole(String role) {
		this.role = role;
	}
}
