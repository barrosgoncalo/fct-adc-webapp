package pt.unl.fct.di.adc.firstwebapp.data;

public class CreateAccountResponse {

    public String username;
    public UserRole role;

    public CreateAccountResponse() {

    }

    public CreateAccountResponse(String username, UserRole role) {
        this.username = username;
        this.role = role;
    }

}
