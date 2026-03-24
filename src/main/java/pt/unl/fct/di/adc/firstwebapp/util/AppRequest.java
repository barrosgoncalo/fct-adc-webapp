package pt.unl.fct.di.adc.firstwebapp.util;

import pt.unl.fct.di.adc.firstwebapp.data.AuthToken;

public class AppRequest<T> {

    public T input;
    public AuthToken token;

    public AppRequest() {}

}
