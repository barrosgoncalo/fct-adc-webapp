package pt.unl.fct.di.adc.firstwebapp.data;

public class UserResponse {

    private String username;
    private UserRole role;

    public UserResponse() {}

    public UserResponse(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

    // getters
	public String getUsername() {
		return username;
	}

	public UserRole getRole() {
		return role;
	}

}
