package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.Map;
import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.data.MessageWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.ModifyUserRequest;
import pt.unl.fct.di.adc.firstwebapp.data.ModifyUserRequest.AttributesData;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.Constants;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UnauthenticTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.util.UserUtils;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;

@Path("/modaccount")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ModifyAccountResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public ModifyAccountResource() {} // default constructor
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doModifyAccount(AppRequest<ModifyUserRequest> request) {

        ModifyUserRequest data = request.getInput();

        LOG.fine("Attempt to modify account: " + data.getUsername());

        if(!data.isValid())
            return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

        Transaction txn = datastore.newTransaction();

        try {

            // Token Validation
            String tokenId = request.getToken().getTokenId();

            Entity requester;
            try { requester = AuthUtils.validateToken(txn, tokenId); }
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
            String roleString = requester.getString(Constants.USER_ROLE);
            if(!Role.isDefined(roleString))
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

            Role role = Role.valueOf(roleString);

            // User Validation
            String username = data.getUsername();
            Entity user;
            try { user = UserUtils.validateUser(txn, username); }
            catch(InvalidInputException e) {
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();
            }
            catch(UserNotFoundException e) {
                return new ErrorResponse(Status.OK, ErrorCode.USER_NOT_FOUND).toResponse();
            }

            // Role permissions enforce
            if( !(Role.isAdmin(role) || username.equals(requester.getString(Constants.USER_NAME))) )
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            AttributesData attributes = data.getAttributes();

            if(attributes.getUsername() != null)
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

            Entity modUser = Entity.newBuilder(user)
                    .set(Constants.USER_PHONE, attributes.getPhone())
                    .set(Constants.USER_ADDRESS, attributes.getAddress())
                    .build();

            txn.put(modUser);
            txn.commit();

            return new AppResponse<MessageWrapper>(
                    "success",
                    new MessageWrapper("Updated successfully")
                ).toResponse();

        } catch (Exception e) {
            LOG.severe("Error modifying user: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        } finally {
            if(txn != null && txn.isActive())
                txn.rollback();
        }
    }

}
