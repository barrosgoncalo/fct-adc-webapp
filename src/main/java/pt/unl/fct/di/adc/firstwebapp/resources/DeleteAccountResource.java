package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Transaction;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.data.DeleteAccountRequest;
import pt.unl.fct.di.adc.firstwebapp.data.DeleteAccountResponse;
import pt.unl.fct.di.adc.firstwebapp.data.UserRole;
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
public class DeleteAccountResource {


    private final String USER_ROLE = "role";


	private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();


    public DeleteAccountResource() {}

    @POST
    public Response doDeleteResource(AppRequest<DeleteAccountRequest> request) {

        DeleteAccountRequest data = request.input;

        if(!data.validDelete())
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
            try { requester = AuthUtils.validateToken(txn, request.token.getTokenId()); }
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
            String roleString = requester.getString(USER_ROLE);
            if(UserRole.isDefined(roleString)) {
                // TODO : LOG
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();
            }

            if(UserRole.ADMIN != UserRole.valueOf(roleString)) {
                // TODO: LOG
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }

            txn.delete(user.getKey());
            txn.commit();
            LOG.info("Deleted user " + data.getUsername() + " with token " + request.token.getTokenId());
            DeleteAccountResponse response = new DeleteAccountResponse();
            return new AppResponse<DeleteAccountResponse>( "success",  response ).toResponse();

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
