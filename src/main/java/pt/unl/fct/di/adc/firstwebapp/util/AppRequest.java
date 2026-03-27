package pt.unl.fct.di.adc.firstwebapp.util;

import pt.unl.fct.di.adc.firstwebapp.data.AuthToken;

public class AppRequest<T> {

    private T input;
    private AuthToken token;

    public AppRequest() {}

	public T getInput() {
		return input;
	}

	public AuthToken getToken() {
		return token;
	}

}
