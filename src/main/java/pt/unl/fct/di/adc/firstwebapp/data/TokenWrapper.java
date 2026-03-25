package pt.unl.fct.di.adc.firstwebapp.data;


public class TokenWrapper {

    private final AuthToken token;

    public TokenWrapper(AuthToken token) {
        this.token = token;
    }

    // getters
	public AuthToken getToken() {
		return token;
	}
}
