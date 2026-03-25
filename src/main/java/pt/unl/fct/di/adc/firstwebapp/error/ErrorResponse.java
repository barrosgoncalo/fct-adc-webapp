package pt.unl.fct.di.adc.firstwebapp.error;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;

public class ErrorResponse extends AppResponse<String> {

    @JsonIgnore
    private final Status httpStatus;

    public ErrorResponse(Status httpStatus, ErrorCode code) {
        super( code.getCode(), code.getMessage() );
        this.httpStatus = httpStatus;
    }

    @Override
    public Response toResponse() {
        return Response.status(this.httpStatus).entity(this).build();
    }

}
