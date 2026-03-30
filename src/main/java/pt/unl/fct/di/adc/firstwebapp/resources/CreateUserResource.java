package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.DatastoreOptions;

import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.data.UserRequest;
import pt.unl.fct.di.adc.firstwebapp.data.UserResponse;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.Constants;

@Path("/createaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class CreateUserResource {


	private static final Logger LOG = Logger.getLogger(CreateUserResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public CreateUserResource() {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doCreateAccount(AppRequest<UserRequest> request) {

        UserRequest data = request.getInput();

        LOG.fine("Attempt to create account: " + data.getUsername());

        if( !data.isValid() )
            return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

        Transaction txn = datastore.newTransaction();

        try {

            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.getUsername());
            Entity user = txn.get(userKey);

            if(user != null) {
                txn.rollback();
                return new ErrorResponse(Status.OK, ErrorCode.USER_ALREADY_EXISTS).toResponse();
            } else {
                user = Entity.newBuilder(userKey)
                        .set( Constants.USER_NAME, data.getUsername() )
                        .set( Constants.USER_PWD , DigestUtils.sha512Hex(data.getPassword()) )
                        .set( Constants.USER_PHONE, data.getPhone() )
                        .set( Constants.USER_ADDRESS, data.getAddress())
                        .set( Constants.USER_ROLE, data.getRole())
                        .set( Constants.USER_CREATION_TIME, Timestamp.now())
                        .build();

                txn.put(user);
                txn.commit();
                LOG.info("User registered " + data.getUsername());

                return new AppResponse<>("success", new UserResponse( data.getUsername(), Role.valueOf(data.getRole()) )).toResponse();
            }
        } 
        catch (Exception e) {
            LOG.severe("Error registering user: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        }
        finally {
            // No need to rollback here, as we only have one transaction and it will be automatically rolled back if not committed.
        }
    }
}
