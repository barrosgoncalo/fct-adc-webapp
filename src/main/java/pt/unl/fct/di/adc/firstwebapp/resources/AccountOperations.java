package pt.unl.fct.di.adc.firstwebapp.resources;

import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import pt.unl.fct.di.adc.firstwebapp.data.ChangeUserPwdRequest;
import pt.unl.fct.di.adc.firstwebapp.data.ChangeUserRoleRequest;
import pt.unl.fct.di.adc.firstwebapp.data.ModifyUserRequest;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.UserRequest;
import pt.unl.fct.di.adc.firstwebapp.data.UserResponse;
import pt.unl.fct.di.adc.firstwebapp.data.UsernameWrapper;

import static pt.unl.fct.di.adc.firstwebapp.data.Constants.*;

import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;
import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;
import pt.unl.fct.di.adc.firstwebapp.util.DB;
import pt.unl.fct.di.adc.firstwebapp.util.Result;

@Path("/accounts")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON)
public class AccountOperations {

	private static final Logger LOG = Logger.getLogger(AccountOperations.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/create")
    public Response doCreateAccount(AppRequest<UserRequest> request) {
        
        if (request == null || request.getInput() == null) {
            return ErrorResponse.build(ErrorCode.INVALID_INPUT);
        }

        UserRequest data = request.getInput();

        if (!data.isValid()) {
            return ErrorResponse.build(ErrorCode.INVALID_INPUT);
        }

        LOG.fine("Attempt to create account: " + data.username());

        return createUser(data)
            .<Response>fold(
                (UserResponse userRes) -> {
                    LOG.info("User registered successfully: " + data.username());
                    return AppResponse.buildSuccess("success", userRes);
                },
                (ErrorCode errorCode) -> {
                    LOG.warning("Failed to create account for: " + data.username() + " - Reason: " + errorCode);
                    return ErrorResponse.build(errorCode);
                }
            );
    }

    @POST
    @Path("/modify")
    public Response modifyAccount(AppRequest<ModifyUserRequest> request) {

        ModifyUserRequest data = request.getInput();

        LOG.fine("Attempt to modify account: " + data.getUsername());

        if(!data.isValid()) {
            return new ErrorResponse(Status.OK, ErrorCode.INVALID_INPUT).toResponse();
        }

        // Return Result chain checking permissions, applying modifications, saving to DB
        return Response.ok().build();
    }

    @POST
    @Path("/delete")
    public Response deleteAccount(AppRequest<UsernameWrapper> request) {
        // ...
        return Response.ok().build();
    }

    @POST
    @Path("/password")
    public Response changePassword(AppRequest<ChangeUserPwdRequest> request) {
        // ...
        return Response.ok().build();
    }

    @POST
    @Path("/role")
    public Response changeRole(AppRequest<ChangeUserRoleRequest> request) {
        // ...
        return Response.ok().build();
    }
    
    @POST
    @Path("/list")
    public Response listUsers(AppRequest<Void> request) {
        // Equivalent to ShowUsersResource
        return Response.ok().build();
    }

    // --- Functional Helper Methods ---

    private Result<UserResponse> createUser(UserRequest data) {
        return DB.executeInTransaction(datastore, txn -> {

            Key userKey = datastore.newKeyFactory().setKind(KIND_USER).newKey(data.username());
            Entity user = txn.get(userKey);

            if (user != null) {
                return Result.failure(ErrorCode.USER_ALREADY_EXISTS);
            }

            Entity newUser = Entity.newBuilder(userKey)
                .set(USER_NAME, data.username())
                .set(USER_PWD, DigestUtils.sha512Hex(data.password()))
                .set(USER_PHONE, data.phone())
                .set(USER_ADDRESS, data.address())
                .set(USER_ROLE, data.role())
                .set(USER_CREATION_TIME, Timestamp.now())
                .build();

            txn.put(newUser);

            return Result.success(new UserResponse(data.username(), Role.valueOf(data.role())));
        });
    }

}
