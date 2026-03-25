package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;
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
import pt.unl.fct.di.adc.firstwebapp.data.UserSummaryResponse;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;

@Path("/showusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUsersResource {

    private final String EXPIRES_AT = "expiresAt";
    private final String USER_KEY_NAME = "username";
    private final String USER_EMAIL = "email";
    private final String USER_ROLE = "role";

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind("Token");

    public ShowUsersResource() {}

    // TODO: not understanding ERROR cases
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowUsers(AppRequest<Void> request) {

        AuthToken token = request.token;

        if(token == null || !token.isValid())
            return new ErrorResponse(Status.BAD_REQUEST, ErrorCode.INVALID_TOKEN).toResponse();

        try {

            Key tokenKey = tokensKeyFactory.newKey(token.tokenId);

            Entity tokenEntity = datastore.get(tokenKey);

            if(tokenEntity == null)
                return new ErrorResponse(Status.UNAUTHORIZED, ErrorCode.UNAUTHORIZED).toResponse();
            long expiration = tokenEntity.getLong(EXPIRES_AT);
            if(System.currentTimeMillis() > expiration)
                return new ErrorResponse(Status.FORBIDDEN, ErrorCode.TOKEN_EXPIRED).toResponse();

            String roleString = tokenEntity.getString(USER_ROLE);
            UserRole role = UserRole.valueOf(roleString);

            if(role != UserRole.ADMIN)
                return new ErrorResponse(Status.UNAUTHORIZED, ErrorCode.UNAUTHORIZED).toResponse();
            String kind = "User";
            String gqlQuery = "select * from " + kind;
            Query<Entity> query = Query.newGqlQueryBuilder(Query.ResultType.ENTITY, gqlQuery).build();
            QueryResults<Entity> results = datastore.run(query);
            List<UserSummaryResponse> summary = new ArrayList<>(); 
            while( results.hasNext() ) {
                Entity entity = results.next();
                String keyName = entity.getString(USER_KEY_NAME);
                UserSummaryResponse user = new UserSummaryResponse(
                        keyName,
                        entity.getString(USER_EMAIL),
                        entity.getString(USER_ROLE));

                summary.add(user);
            }
            return new AppResponse<List<UserSummaryResponse>>("success", summary).toResponse();
        } catch (Exception e) {
            LOG.severe("Error showing users: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.IE_SHOWING_USERS).toResponse();
        } finally {
            // TODO
        }
    }

}

