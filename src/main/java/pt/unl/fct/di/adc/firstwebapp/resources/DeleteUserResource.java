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

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.data.UsernameWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.MessageWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.UserConstants;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.util.UserUtils;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;


@Path("/deleteaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteUserResource {


    private static final String SUCCESS = "Account deleted successfully";


	private static final Logger LOG = Logger.getLogger(CreateUserResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    public DeleteUserResource() {}

    @POST
    public Response doDeleteUser(AppRequest<UsernameWrapper> request) {

        UsernameWrapper data = request.getInput();

        if(!data.isValid())
            return new ErrorResponse(Status.OK, ErrorCode.FORBIDDEN).toResponse();


        Transaction txn = datastore.newTransaction();

        try {

            // User verification
            Entity user;
            try{ user = UserUtils.validateUser(txn, data.getUsername()); }
            catch(InvalidInputException e) {
                // TODO : LOG
                return new ErrorResponse(Status.OK, ErrorCode.FORBIDDEN).toResponse();
            }
            catch(UserNotFoundException e) {
                // TODO : LOG
                return new ErrorResponse(Status.OK, ErrorCode.USER_NOT_FOUND).toResponse();
            }

            // Token verification
            Entity requester;
            try { requester = AuthUtils.validateToken(txn, request.getToken().getTokenId()); }
            catch(InvalidInputException e) {
                // TODO: LOG
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();
            }
            // TODO : Dubious Error to show, if the requester doesn't exist in DB
            catch(UserNotFoundException e) {
                // TODO: LOG
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }

            // Verify authorization
            String role = requester.getString(UserConstants.USER_ROLE);
            if(Role.ADMIN != Role.valueOf(role)) {
                // TODO: LOG
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }

            txn.delete(user.getKey());

            // TODO : FURTHER TEST TOKEN REMOVAL UPPON ACCOUNT DELETION
            // Query Tokens
            String gqlQuery = 
                "SELECT __key__ FROM " + UserConstants.KIND_TOKEN + 
                " WHERE " + UserConstants.USER_NAME + " = @username";

            Query<Key> query = Query.newGqlQueryBuilder(Query.ResultType.KEY, gqlQuery)
                                .setBinding("username", data.getUsername())
                                .build();
            QueryResults<Key> results = datastore.run(query);

            List<Key> keysToRemove = new ArrayList<>();

            results.forEachRemaining(keysToRemove::add);

            if(!keysToRemove.isEmpty())
                txn.delete( keysToRemove.toArray(new Key[0]) );

            txn.commit();

            LOG.info("Deleted user " + data.getUsername() + " with token " + request.getToken().getTokenId());

            return new AppResponse<MessageWrapper>( "success",  new MessageWrapper(SUCCESS) ).toResponse();

        } catch (Exception e) {
			LOG.severe(e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}

    }
}
