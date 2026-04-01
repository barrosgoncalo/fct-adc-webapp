package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
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
import pt.unl.fct.di.adc.firstwebapp.data.MessageWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.Constants;
import pt.unl.fct.di.adc.firstwebapp.data.UsernameWrapper;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UnauthenticTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.util.UserUtils;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

    private static final String SUCCESS = "Logout successful";

    private static final Logger LOG = Logger.getLogger(LogoutResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public LogoutResource () {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doLogout(AppRequest<UsernameWrapper> request) {

        UsernameWrapper data = request.getInput();

        if(!data.isValid())
            return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

        Transaction txn = null;

        try { 

            // Token validation
            Entity requester;
            try { requester = AuthUtils.validateToken(request.getToken().getTokenId() ); }
            catch(InvalidInputException | UnauthenticTokenException e) {
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();
            }
            catch(ExpiredTokenException e) {
                return new ErrorResponse(Status.OK, ErrorCode.TOKEN_EXPIRED).toResponse();
            }
            catch(UserNotFoundException e) {
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }


            // User Validation
            try{ UserUtils.validateUser(data.getUsername() ); }
            catch(InvalidInputException e) {
                return new ErrorResponse(Status.OK, ErrorCode.FORBIDDEN).toResponse();
            }
            catch(UserNotFoundException e) {
                return new ErrorResponse(Status.OK, ErrorCode.USER_NOT_FOUND).toResponse();
            }

            // Role Permissions enforce
            Role role = Role.valueOf(requester.getString(Constants.USER_ROLE));

            if( !(Role.isAdmin(role) || data.getUsername().equals(requester.getString(Constants.USER_NAME))) )
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            // Query Tokens
            String gqlQuery = 
                "SELECT __key__ FROM " + Constants.KIND_TOKEN + 
                " WHERE " + Constants.USER_NAME + " = @username";

            Query<Key> query = Query.newGqlQueryBuilder(Query.ResultType.KEY, gqlQuery)
                                .setBinding("username", data.getUsername())
                                .build();
            QueryResults<Key> results = datastore.run(query);

            List<Key> keysToRemove = new ArrayList<>();

            // Creation of array list of Keys, through iteration over the iterable results
            results.forEachRemaining(keysToRemove::add);

            txn = datastore.newTransaction();

            if(!keysToRemove.isEmpty())
                txn.delete( keysToRemove.toArray( new Key[0]) );

            txn.commit();

            return new AppResponse <MessageWrapper>("success", new MessageWrapper(SUCCESS)).toResponse();

        } catch (Exception e) {
            LOG.severe("Error lougout user: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        } finally {
            if(txn != null && txn.isActive())
                txn.rollback();
        }

    }
}
