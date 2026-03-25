package pt.unl.fct.di.adc.firstwebapp.util;

import jakarta.ws.rs.core.Response;

public class AppResponse<T> {

    public String status;
    public T data;

    public AppResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public Response toResponse() {
        return Response.ok(this).build();
    }

}
