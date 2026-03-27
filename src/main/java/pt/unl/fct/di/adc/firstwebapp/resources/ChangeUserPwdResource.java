package pt.unl.fct.di.adc.firstwebapp.resources;

import java.security.MessageDigest;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

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
import pt.unl.fct.di.adc.firstwebapp.data.ChangeUserPwdRequest;
import pt.unl.fct.di.adc.firstwebapp.data.MessageWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.Constants;
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

@Path("/changeuserpwd")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ChangeUserPwdResource {

    private static final String SUCCESS = "Password changed successfully";

    private static final Logger LOG = Logger.getLogger(ChangeUserPwdResource.class.getName());
    private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    public ChangeUserPwdResource () {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doChangeUserPwd(AppRequest<ChangeUserPwdRequest> request) {

        ChangeUserPwdRequest data = request.getInput();

        if(!data.isValid())
            return new ErrorResponse(Status.BAD_REQUEST, ErrorCode.INVALID_INPUT).toResponse();

        Transaction txn = datastore.newTransaction();

        try { 

            // Token Validation
            Entity requester;
            try { requester = AuthUtils.validateToken(txn, request.getToken().getTokenId()); }
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

            if( !requester.getString(Constants.USER_NAME).equals(data.getUsername()) ) {
                // TODO : LOG
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();
            }

            // Password Validation
            String oldPwd = DigestUtils.sha512Hex( data.getOldPassword() );
            String hashedPwd = user.getString(Constants.USER_PWD);

            if( !MessageDigest.isEqual(oldPwd.getBytes(), hashedPwd.getBytes()) )
                return new ErrorResponse(Status.OK, ErrorCode.INVALID_CREDENTIALS).toResponse();

            String newPwd = DigestUtils.sha512Hex(data.getNewPassword());

            Entity modUser = Entity.newBuilder(user)
                    .set(Constants.USER_PWD, newPwd)
                    .build();
                    
            txn.put(modUser);
            txn.commit();
            //TODO : LOG

            return new AppResponse <MessageWrapper>("success", new MessageWrapper(SUCCESS)).toResponse();

        } catch (Exception e) {
            LOG.severe("Error modifying user password: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        } finally {
            if(txn.isActive())
                txn.rollback();
        }
    }

}
