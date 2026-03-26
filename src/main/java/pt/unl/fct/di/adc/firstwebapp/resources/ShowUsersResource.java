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
import pt.unl.fct.di.adc.firstwebapp.data.UserRole;
import pt.unl.fct.di.adc.firstwebapp.data.UserSummaryResponse;
import pt.unl.fct.di.adc.firstwebapp.data.UsersWrapper;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;

@Path("/showusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUsersResource {

    private static final String USER_KEY_NAME = "username";
    private static final String USER_ROLE = "role";

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    
    public ShowUsersResource() {}

    // TODO: not understanding ERROR cases
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowUsers(AppRequest<Void> request) {

        AuthToken token = request.getToken();

        if(token == null || !token.isValid())
            return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();

        try {

            // Token Validation
            Entity requester;
            try { requester = AuthUtils.validateToken(token.getTokenId()); }
            catch(InvalidInputException e) {
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();
            }
            catch(ExpiredTokenException e) {
                return new ErrorResponse(Status.OK, ErrorCode.TOKEN_EXPIRED).toResponse();
            }
            catch(UserNotFoundException e) {
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }

            // Role Verification
            String roleString = requester.getString(USER_ROLE);
            if(!UserRole.isDefined(roleString))
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

            UserRole role = UserRole.valueOf(roleString);
            if(!UserRole.isAdminOrBofficer(role))
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            // Query Users
            String kind = "User";
            String gqlQuery = "select * from " + kind;
            Query<Entity> query = Query.newGqlQueryBuilder(Query.ResultType.ENTITY, gqlQuery).build();
            QueryResults<Entity> results = datastore.run(query);
            List<UserSummaryResponse> summary = new ArrayList<>(); 

            while( results.hasNext() ) {
                Entity entity = results.next();
                String username = entity.getString(USER_KEY_NAME);
                String userRole = entity.getString(USER_ROLE);
                summary.add( new UserSummaryResponse(username, userRole) );
            }

            return new AppResponse <UsersWrapper>("success", new UsersWrapper( summary )).toResponse();

        } catch (Exception e) {
            LOG.severe("Error showing users: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        } finally {
            // TODO
        }
    }

}

