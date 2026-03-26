package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Transaction;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.data.ChangeAccountRoleRequest;
import pt.unl.fct.di.adc.firstwebapp.data.MessageWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.UserRole;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.util.UserUtils;

@Path("/changeuserpwd")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeUserPasswordResource {

    private static final String USER_ROLE = "role";
    private static final String SUCCESS = "Role updated successfully";

    Logger LOG = Logger.getLogger(ChangeUserPasswordResource.class.getName());
    Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public ChangeUserPasswordResource () {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doChangeUserRoleResource(AppRequest<ChangeAccountRoleRequest> request) {

        ChangeAccountRoleRequest data = request.getInput();

        if(!data.isValid())
            return new ErrorResponse(Status.BAD_REQUEST, ErrorCode.INVALID_INPUT).toResponse();

        Transaction txn = datastore.newTransaction();

        try { 

            // Token validation
            Entity requester;
            try { requester = AuthUtils.validateToken(txn, request.getToken().getTokenId()); }
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
            if( !UserRole.isAdmin(role) )
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            Entity user;
            try{ user = UserUtils.validateUser( data.getUsername()); }
            catch(InvalidInputException e) {
                // TODO : LOG
                return new ErrorResponse(Status.OK, ErrorCode.FORBIDDEN).toResponse();
            }
            catch(UserNotFoundException e) {
                // TODO : LOG
                return new ErrorResponse(Status.OK, ErrorCode.USER_NOT_FOUND).toResponse();
            }

            Entity modUser = Entity.newBuilder(user)
                    .set(USER_ROLE, data.getNewRole())
                    .build();
                    
            txn.put(modUser);
            txn.commit();
            //TODO : LOG

            return new AppResponse <MessageWrapper>("success", new MessageWrapper(SUCCESS)).toResponse();

        } catch (Exception e) {
            LOG.severe("Error modifying user: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        } finally {
            if(txn.isActive())
                txn.rollback();
        }
    }

}
