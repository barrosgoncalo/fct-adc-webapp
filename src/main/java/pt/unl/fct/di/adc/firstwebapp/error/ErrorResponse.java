package pt.unl.fct.di.adc.firstwebapp.error;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;

public class ErrorResponse extends AppResponse<String> {

    @JsonIgnore
    private final Status httpStatus;

    // Constructors
    public ErrorResponse(Status httpStatus, ErrorCode code) {
        super(code.getCode(), code.getMessage());
        this.httpStatus = httpStatus;
    }

    public ErrorResponse(ErrorCode code) {
        super(code.getCode(), code.getMessage());
        this.httpStatus = Status.OK;
    }

    @Override
    public Response toResponse() {
        return Response.status(this.httpStatus).entity(this).build();
    }

    public static Response build(ErrorCode code) {
        return new ErrorResponse(code).toResponse();
    }

    public static Response build(Status httpStatus, ErrorCode code) {
        return new ErrorResponse(httpStatus, code).toResponse();
    }
}
