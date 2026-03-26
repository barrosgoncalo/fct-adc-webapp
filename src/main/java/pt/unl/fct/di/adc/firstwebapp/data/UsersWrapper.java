package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.List;

public class UsersWrapper {

    private final List<UserSummary> users;

    public UsersWrapper(List<UserSummary> users) {
        this.users = users;
    }

    // getters
	public List<UserSummary> getUsers() {
		return users;
	}
}
