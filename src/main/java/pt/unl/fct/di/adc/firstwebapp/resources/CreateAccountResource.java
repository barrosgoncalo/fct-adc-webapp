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

import com.google.gson.Gson;
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
import pt.unl.fct.di.adc.firstwebapp.data.CreateAccountRequest;
import pt.unl.fct.di.adc.firstwebapp.data.CreateAccountResponse;

@Path("/CreateAccount")
public class CreateAccountResource {

	private static final Logger LOG = Logger.getLogger(CreateAccountResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

	public CreateAccountResource() {}	// Default constructor, nothing to do

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response CreateAccount(AppRequest<CreateAccountRequest> request) {

        CreateAccountRequest data = request.input;

        LOG.fine("Attempt to register user: " + data.username);

        if(!data.validRegistration())
            return ErrorResponse.build(Status.BAD_REQUEST, ErrorCode.INVALID_INPUT);

        try {
            Transaction txn = datastore.newTransaction();
            Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
            Entity user = txn.get(userKey);

            if(user != null) {
                txn.rollback();
                return ErrorResponse.build(Status.CONFLICT, ErrorCode.USER_ALREADY_EXISTS);
            }            
            else {
                user = Entity.newBuilder(userKey)
                        .set("user_name", data.username)
                        .set("user_pwd", DigestUtils.sha512Hex(data.password))
                        .set("user_email", data.email)
                        .set("user_phone", data.phone)
                        .set("user_address", data.address)
                        .set("user_role", data.role.name())
                        .set("user_creation_time", Timestamp.now())
                        .build();
                txn.put(user);
                txn.commit();
                LOG.info("User registered " + data.username);
                return Response.ok( new AppResponse<CreateAccountResponse>("success", new CreateAccountResponse(data.username, data.role) )).build();
            }
        } catch (Exception e) {
            LOG.severe("Error registering user: " + e.getMessage());
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error registering user.").build();
        }
        finally {
            // No need to rollback here, as we only have one transaction and it will be automatically rolled back if not committed.
        }
    }
}
