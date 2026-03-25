package pt.unl.fct.di.adc.firstwebapp.data;

public class UserResponse {

    public String username;
    public UserRole role;

    public UserResponse() {}

    public UserResponse(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

}
