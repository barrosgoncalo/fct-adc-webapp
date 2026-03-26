package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.List;

public class UsersWrapper {

    private final List<UserSummaryResponse> users;

    public UsersWrapper(List<UserSummaryResponse> users) {
        this.users = users;
    }

    // getters
	public List<UserSummaryResponse> getUsers() {
		return users;
	}
}
