package pt.unl.fct.di.adc.firstwebapp.data;

public class UserResponse {

    private String username;
    private Role role;

    public UserResponse() {}

    public UserResponse(String username, Role role) {
        this.username = username;
        this.role = role;
    }

    // getters
	public String getUsername() {
		return username;
	}

	public Role getRole() {
		return role;
	}

}
