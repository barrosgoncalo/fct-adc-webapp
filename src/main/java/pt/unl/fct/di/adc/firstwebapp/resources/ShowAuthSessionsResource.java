package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Query;
import com.google.cloud.datastore.QueryResults;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import pt.unl.fct.di.adc.firstwebapp.data.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.data.TokenSummary;
import pt.unl.fct.di.adc.firstwebapp.data.UserRole;
import pt.unl.fct.di.adc.firstwebapp.data.SessionsWrapper;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;

@Path("/showauthsessions")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowAuthSessionsResource {

    private static final String KIND_TOKEN = "Token";
    private static final String TOKEN_ID = "tokenId";
    private static final String USER_NAME = "username";
    private static final String USER_ROLE = "role";
    private static final String EXPIRES_AT = "expiresAt";

    public ShowAuthSessionsResource() {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowAuthSessionsResource(AppRequest<Void> request)
        throws InvalidInputException, ExpiredTokenException, UserNotFoundException {

        Logger LOG = Logger.getLogger(ShowAuthSessionsResource.class.getName());
        Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

        AuthToken token = request.getToken();

        try {
            // Token validation
            Entity requester;
            try { requester = AuthUtils.validateToken( token.getTokenId() ); }
            catch(InvalidInputException e) {
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();
            }
            catch(ExpiredTokenException e) {
                return new ErrorResponse(Status.OK, ErrorCode.TOKEN_EXPIRED).toResponse();
            }
            catch(UserNotFoundException e) {
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }
            

            // Role Validation
            UserRole role = UserRole.valueOf(requester.getString(USER_ROLE));
            if(role != UserRole.ADMIN)
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            // Query Tokens
            String kind = KIND_TOKEN;
            String gqlQuery = "select * from " + kind;
            Query<Entity> query = Query.newGqlQueryBuilder(Query.ResultType.ENTITY, gqlQuery).build();
            QueryResults<Entity> results = datastore.run(query);

            List<TokenSummary> summary = new ArrayList<>(); 
            while( results.hasNext() ) {
                Entity entity = results.next();
                String tokenId = entity.getString(TOKEN_ID);
                String username = entity.getString(USER_NAME);
                String roleString = entity.getString(USER_ROLE);
                long expiresAt = entity.getLong(EXPIRES_AT) / 1000;
                summary.add( new TokenSummary(tokenId, username, roleString, expiresAt) );
            }

            return new AppResponse <SessionsWrapper>("success", new SessionsWrapper( summary )).toResponse();

        } catch (Exception e) {
            LOG.severe("Error showing sessions: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        }

    }

}
