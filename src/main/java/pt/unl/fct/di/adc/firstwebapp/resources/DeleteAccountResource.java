package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
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
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;


@Path("/deleteaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class DeleteAccountResource {


    private final String USER_ROLE = "role";
    private final String EXPIRES_AT = "expiresAt";

	private static final String LOG_DELETE_ACCOUNT_ATTEMP = "Delete account attempt by user: ";


	private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind("Token");


    public DeleteAccountResource() {}

    @POST
    public Response doDeleteResource(AppRequest<DeleteAccountRequest> request) {

        DeleteAccountRequest data = request.input;

		LOG.fine(LOG_DELETE_ACCOUNT_ATTEMP + data.getUsername());

        if(!data.validDelete())
            return new ErrorResponse(Status.OK, ErrorCode.FORBIDDEN).toResponse();


        Transaction txn = datastore.newTransaction();

        try {

            // User verification
            Key userKey = userKeyFactory.newKey(data.getUsername());
            Entity user = txn.get(userKey);

            if(user == null) {
                LOG.warning(LOG_DELETE_ACCOUNT_ATTEMP + data.getUsername());
                return new ErrorResponse(Status.OK, ErrorCode.USER_NOT_FOUND).toResponse();
            }

            // Token verification
            Key tokenKey = tokensKeyFactory.newKey(request.token.tokenId);
            Entity token = txn.get(tokenKey);

            if(token == null) {
                LOG.warning(LOG_DELETE_ACCOUNT_ATTEMP + request.token.tokenId);
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_TOKEN).toResponse();
            }

            if(System.currentTimeMillis() > token.getLong(EXPIRES_AT)) {
                // TODO: LOG 
                return new ErrorResponse(Status.OK, ErrorCode.TOKEN_EXPIRED).toResponse();
            }

            String roleString = user.getString(USER_ROLE);
            UserRole role;
            
            try { role = UserRole.valueOf(roleString); }
            catch(Exception e) {
                return new ErrorResponse(Status.OK, ErrorCode.FORBIDDEN).toResponse();
            }

            if(UserRole.ADMIN != role) {
                // TODO: LOG
                return new ErrorResponse(Status.OK, ErrorCode.TOKEN_EXPIRED).toResponse();
            }

            txn.delete(userKey);
            txn.commit();
            LOG.info("Deleted user " + data.getUsername() + " with token " + request.token.tokenId);
            DeleteAccountResponse response = new DeleteAccountResponse();
            return new AppResponse<DeleteAccountResponse>( "success",  response).toResponse();

        } catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
		} finally {
			if (txn.isActive()) {
				txn.rollback();
			}
		}

    }
}
