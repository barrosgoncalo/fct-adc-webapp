package pt.unl.fct.di.adc.firstwebapp.util;

public class AppResponse<T> {

    public String status;
    public T data;

    public AppResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

}
