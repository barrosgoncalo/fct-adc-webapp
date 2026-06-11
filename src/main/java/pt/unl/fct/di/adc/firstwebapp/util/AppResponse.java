package pt.unl.fct.di.adc.firstwebapp.util;

import jakarta.ws.rs.core.Response;

public class AppResponse<T> {

    private String status;
    private T data;

    public AppResponse(String status, T data) {
        this.status = status;
        this.data = data;
    }

    public Response toResponse() {
        return Response.ok(this).build();
    }

	public String getStatus() {
		return status;
	}

	public T getData() {
		return data;
	}

    public static <T> Response buildSuccess(String status, T data) {
        return new AppResponse<>(status, data).toResponse();
    }


}
