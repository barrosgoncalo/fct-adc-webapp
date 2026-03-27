package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

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
import pt.unl.fct.di.adc.firstwebapp.data.Constants;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.SessionsWrapper;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UnauthenticTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;

@Path("/showauthsessions")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowAuthSessionsResource {

    private static final Logger LOG = Logger.getLogger(ShowAuthSessionsResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public ShowAuthSessionsResource() {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowAuthSessions(AppRequest<Void> request)
        throws InvalidInputException, ExpiredTokenException, UserNotFoundException {

        AuthToken token = request.getToken();

        try {
            // Token validation
            Entity requester;
            try { requester = AuthUtils.validateToken( token.getTokenId() ); }
            catch(InvalidInputException | UnauthenticTokenException e) {
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();
            }
            catch(ExpiredTokenException e) {
                return new ErrorResponse(Status.OK, ErrorCode.TOKEN_EXPIRED).toResponse();
            }
            catch(UserNotFoundException e) {
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }
            

            // Role Validation
            Role role = Role.valueOf(requester.getString(Constants.USER_ROLE));
            if(role != Role.ADMIN)
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            // Query Tokens
            String kind = Constants.KIND_TOKEN;
            String gqlQuery = "select * from " + kind;
            Query<Entity> query = Query.newGqlQueryBuilder(Query.ResultType.ENTITY, gqlQuery).build();
            QueryResults<Entity> results = datastore.run(query);

            List<TokenSummary> summary = new ArrayList<>(); 
            while( results.hasNext() ) {
                Entity entity = results.next();
                String tokenId = entity.getString(Constants.TOKEN_ID);
                String username = entity.getString(Constants.USER_NAME);
                String roleString = entity.getString(Constants.USER_ROLE);
                long expiresAt = entity.getLong(Constants.EXPIRES_AT) / 1000;
                summary.add( new TokenSummary(tokenId, username, roleString, expiresAt) );
            }

            return new AppResponse <SessionsWrapper>("success", new SessionsWrapper( summary )).toResponse();

        } catch (Exception e) {
            LOG.severe("Error showing sessions: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        }

    }

}
