package pt.unl.fct.di.adc.firstwebapp.error;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;

public class ErrorResponse {

    public static Response build(Status httpStatus, ErrorCode code) {

        AppResponse<String> errorBody = new AppResponse<>(code.getCode(), code.getMessage());

        return Response.status(httpStatus).entity(errorBody).build();
    }

}
