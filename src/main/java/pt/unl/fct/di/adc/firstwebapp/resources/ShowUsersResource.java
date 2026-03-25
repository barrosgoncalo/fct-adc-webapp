package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.unl.fct.di.adc.firstwebapp.data.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.data.UserRole;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;

@Path("/")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUsersResource {

    private final String CREATION_DATA = "cretionData";
    private final String EXPIRATION_DATA = "expirationData";
    private final String USER_ROLE = "role";

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind("Token");

    public ShowUsersResource() {}

    // TODO: not understanding ERROR cases
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowUsers(AppRequest<String> request) {

        AuthToken token = request.token;

        if(token == null || !token.isValid())
            return new ErrorResponse(Status.BAD_REQUEST, ErrorCode.INVALID_TOKEN).toResponse();

        Key tokenKey = tokensKeyFactory.newKey(token.tokenID);

        Entity tokenEntity = datastore.get(tokenKey);

        if(tokenEntity == null)
            return new ErrorResponse(Status.UNAUTHORIZED, ErrorCode.UNAUTHORIZED).toResponse();
        long expiration = tokenEntity.getLong(EXPIRATION_DATA);
        if(System.currentTimeMillis() > expiration)
            return new ErrorResponse(Status.FORBIDDEN, ErrorCode.TOKEN_EXPIRED).toResponse();

        String roleString = tokenEntity.getString(USER_ROLE);
        UserRole role = UserRole.valueOf(roleString);

        if(role != UserRole.ADMIN)
            return new ErrorResponse(Status.UNAUTHORIZED, ErrorCode.UNAUTHORIZED).toResponse();

    }

}

