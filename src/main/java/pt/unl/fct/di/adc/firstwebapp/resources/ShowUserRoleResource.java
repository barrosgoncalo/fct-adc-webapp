package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.Constants;
import pt.unl.fct.di.adc.firstwebapp.data.UserSummary;
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

@Path("/showuserrole")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ShowUserRoleResource {

    private static final Logger LOG = Logger.getLogger(ShowUserRoleResource.class.getName());

    public ShowUserRoleResource () {}

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response doShowAccountRole(AppRequest<UsernameWrapper> request) {

        UsernameWrapper data = request.getInput();

        if(!data.isValid())
            return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();

        try { 

            // Token validation
            Entity requester;
            try { requester = AuthUtils.validateToken(request.getToken().getTokenId()); }
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
            Role requesterRole = Role.valueOf(requester.getString(Constants.USER_ROLE));

            if( !Role.isAdminOrBofficer(requesterRole) )
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

            Role userRole = Role.valueOf(user.getString(Constants.USER_ROLE));

            if( !requesterRole.isHigherOrEqualDegree(userRole) )
                return new ErrorResponse(Status.OK, ErrorCode.UNAUTHORIZED).toResponse();

            UserSummary summary = new UserSummary(
                        user.getString(Constants.USER_NAME),
                        user.getString(Constants.USER_ROLE)
                        );

            return new AppResponse <UserSummary>("success", summary).toResponse();

        } catch (Exception e) {
            LOG.severe("Error modifying user: " + e.getMessage());
            return new ErrorResponse(Status.INTERNAL_SERVER_ERROR, ErrorCode.FORBIDDEN).toResponse();
        }
    }

}
