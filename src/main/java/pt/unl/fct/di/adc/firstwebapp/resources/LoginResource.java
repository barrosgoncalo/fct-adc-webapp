package pt.unl.fct.di.adc.firstwebapp.resources;

import java.security.MessageDigest;
import java.util.Objects;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response.Status;

import jakarta.servlet.http.HttpServletRequest;

import pt.unl.fct.di.adc.firstwebapp.data.AuthToken;
import pt.unl.fct.di.adc.firstwebapp.data.Role;
import pt.unl.fct.di.adc.firstwebapp.data.LoginRequest;
import pt.unl.fct.di.adc.firstwebapp.data.TokenWrapper;
import pt.unl.fct.di.adc.firstwebapp.data.UserConstants;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorCode;
import pt.unl.fct.di.adc.firstwebapp.error.ErrorResponse;

import pt.unl.fct.di.adc.firstwebapp.util.AppRequest;
import pt.unl.fct.di.adc.firstwebapp.util.AppResponse;

import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.DatastoreOptions;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LoginResource {

	private static final String MESSAGE_INVALID_CREDENTIALS = "Incorrect username or password.";
	private static final String MESSAGE_NEXT_PARAMETER_INVALID = "Request parameter 'next' must be greater or equal to 0.";


	private static final String LOG_MESSAGE_LOGIN_ATTEMP = "Login attempt by user: ";
	private static final String LOG_MESSAGE_LOGIN_SUCCESSFUL = "Login successful by user: ";
	private static final String LOG_MESSAGE_WRONG_PASSWORD = "Wrong password for: ";
	private static final String LOG_MESSAGE_UNKNOW_USER = "Failed login attempt for username: ";
	
	/** 
	 * Logger Object
	 */
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	private static final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	private static final KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind("Token");

	public LoginResource() {} // Nothing to be done here
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response doLogin(AppRequest<LoginRequest> request,
			@Context HttpServletRequest httpRequest,
			@Context HttpHeaders httpHeaders) {

            LoginRequest data = request.getInput();

		LOG.fine(LOG_MESSAGE_LOGIN_ATTEMP + data.getUsername());

        if(!data.isValid())
            return new ErrorResponse(Status.BAD_REQUEST, ErrorCode.INVALID_INPUT).toResponse();

		Key userKey = userKeyFactory.newKey(data.getUsername());

		Transaction txn = datastore.newTransaction();
		try {
			Entity user = txn.get(userKey);
			if (user == null) {
				// Username does not exist
				LOG.warning(LOG_MESSAGE_LOGIN_ATTEMP + data.getUsername());
				return new ErrorResponse(Status.OK, ErrorCode.USER_NOT_FOUND).toResponse();
			}

			String hashedPWD = (String) user.getString(UserConstants.USER_PWD);
			if ( MessageDigest.isEqual( hashedPWD.getBytes(), (DigestUtils.sha512Hex(data.getPassword())).getBytes() )) {
				// Login successful
                
                // Return token
                String role = user.getString(UserConstants.USER_ROLE);
                
                AuthToken token = new AuthToken(data.getUsername(), role);
                LOG.info(LOG_MESSAGE_LOGIN_SUCCESSFUL + data.getUsername());

                Key tokenKey = tokensKeyFactory.newKey(token.getTokenId());
                Entity newToken = txn.get(tokenKey);

                // TODO: VERIFY IF THE TOKEN BELONGS TO THE DATABSE

                newToken = Entity.newBuilder(tokenKey)
                    .set( UserConstants.TOKEN_ID, token.getTokenId() )
                    .set( UserConstants.USER_NAME, token.getUsername() )
                    .set( UserConstants.USER_ROLE, token.getRole() )
                    .set( UserConstants.ISSUED_AT, token.getIssuedAt() )
                    .set( UserConstants.EXPIRES_AT, token.getExpiresAt() )
                    .build();

                // Batch operation
                txn.put(newToken);
                txn.commit();
                LOG.info("Session started for user " + data.getUsername() + " with token " + token.getTokenId());

                return new AppResponse<TokenWrapper>( "success", new TokenWrapper(token) ).toResponse();
			} else {
				// Incorrect password
				LOG.warning(LOG_MESSAGE_WRONG_PASSWORD + data.getUsername());
				return new ErrorResponse(Status.FORBIDDEN, ErrorCode.INVALID_CREDENTIALS).toResponse();
			}
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
