package pt.unl.fct.di.adc.firstwebapp.resources;

import static pt.unl.fct.di.adc.firstwebapp.data.Constants.SUCCESS;

import com.google.cloud.datastore.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import pt.unl.fct.di.adc.firstwebapp.data.*;
import pt.unl.fct.di.adc.firstwebapp.error.*;
import pt.unl.fct.di.adc.firstwebapp.exceptions.*;
import pt.unl.fct.di.adc.firstwebapp.security.SecurityConfig;
import pt.unl.fct.di.adc.firstwebapp.util.*;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.codec.digest.DigestUtils;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
@Consumes(MediaType.APPLICATION_JSON)
public class AuthOperations {

    private static final Logger LOG = Logger.getLogger(AuthOperations.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    @POST
    @Path("/login")
    public Response login(AppRequest<LoginRequest> request) {
        LoginRequest data = request.getInput();

        if (!data.isValid()) {
            return ErrorResponse.build(ErrorCode.INVALID_INPUT);
        }

        LOG.fine("Login attempt by user: " + data.getUsername());

        return verifyCredentials(data)
            .flatMap((Entity user) -> createAndSaveToken(user, data))
            .fold( 
                (AuthToken token) -> {
                    LOG.info("Session started for user " + data.getUsername());
                    return AppResponse.buildSuccess(SUCCESS, new TokenWrapper(token));
                },
                (ErrorCode errorCode) -> {
                    LOG.warning("Login failed for: " + data.getUsername());
                    return ErrorResponse.build(errorCode);
                }
             );
    }

    @POST
    @Path("/logout")
    public Response logout(AppRequest<UsernameWrapper> request) {
        UsernameWrapper data = request.getInput();

        if (!data.isValid()) {
            LOG.warning("Logout attempt failed: Invalid input payload received.");
            return ErrorResponse.build(ErrorCode.INVALID_INPUT);
        }

        LOG.fine("Processing logout request for target user: " + data.getUsername());

        return validateToken(request.getToken().getTokenId())
            .flatMap((Entity requester) -> verifyLogoutPermissions(requester, data.getUsername()))
            .flatMap((String validUsername) -> deleteUserTokens(validUsername))
            .fold(
                (String successMsg) -> {
                    LOG.info("User successfully logged out. Tokens invalidated for: " + data.getUsername());
                    return AppResponse.buildSuccess(SUCCESS, new MessageWrapper(successMsg));
                },
                (ErrorCode errorCode) -> {
                    LOG.warning("Logout failed for target '" + data.getUsername() + "'. Reason: " + errorCode);
                    return ErrorResponse.build(errorCode);
                }
            );
    }

    @POST
    @Path("/sessions")
    public Response showSessions(AppRequest<Void> request) {
        LOG.fine("Request received to show all auth sessions.");

        return validateToken(request.getToken().getTokenId())
            .flatMap((Entity requester) -> verifyAdminRole(requester))
            .flatMap((Entity validAdmin) -> fetchAllSessions())
            .fold(
                    (SessionsWrapper sessions) -> {
                        LOG.info("Successfully fetched all auth sessions.");
                        return AppResponse.buildSuccess(SUCCESS, sessions);
                    },
                    (ErrorCode errorCode) -> {
                        LOG.warning("Failed to fetch sessions. Reason: " + errorCode);
                        return ErrorResponse.build(errorCode);
                    }
                 );
    }

    // --- Functional Helper Methods ---
    
    private Result<Entity> verifyCredentials(LoginRequest data) {
        Entity user;
        try {
            user = UserUtils.validateUser(data.getUsername());
        } catch (InvalidInputException e) {
            return Result.failure(ErrorCode.INVALID_CREDENTIALS);
        } catch (UserNotFoundException e) {
            return Result.failure(ErrorCode.USER_NOT_FOUND);
        }

        // Check password match
        String hashedPWD = user.getString(Constants.USER_PWD);
        String providedPwd = DigestUtils.sha512Hex(data.getPassword());

        if (!MessageDigest.isEqual(hashedPWD.getBytes(), providedPwd.getBytes())) {
            return Result.failure(ErrorCode.INVALID_CREDENTIALS);
        }

        return Result.success(user);
    }

    private Result<AuthToken> createAndSaveToken(Entity user, LoginRequest data) {
        // Generate the token
        String masterKey = SecurityConfig.getMasterKey();
        String role = user.getString(Constants.USER_ROLE);
        AuthToken token = new AuthToken(data.getUsername(), role, masterKey);

        // Save to Datastore using our functional utility
        return DB.executeInTransaction(datastore, txn -> {
            KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind(Constants.KIND_TOKEN);
            Key tokenKey = tokensKeyFactory.newKey(token.getTokenId());
            
            Entity newToken = Entity.newBuilder(tokenKey)
                .set(Constants.TOKEN_ID, token.getTokenId())
                .set(Constants.USER_NAME, token.getUsername())
                .set(Constants.USER_ROLE, token.getRole())
                .set(Constants.ISSUED_AT, token.getIssuedAt())
                .set(Constants.EXPIRES_AT, token.getExpiresAt())
                .set(Constants.HASH, token.getHash())
                .build();
                
            txn.put(newToken);
            
            // Return the token payload on success
            return Result.success(token); 
        });
    }

    private Result<Entity> validateToken(String tokenId) {
        try {
            Entity requester = AuthUtils.validateToken(tokenId);
            return Result.success(requester);
        } catch (InvalidInputException | UnauthenticTokenException e) {
            return Result.failure(ErrorCode.INVALID_TOKEN);
        } catch (ExpiredTokenException e) {
            return Result.failure(ErrorCode.TOKEN_EXPIRED);
        } catch (UserNotFoundException e) {
            return Result.failure(ErrorCode.UNAUTHORIZED);
        }
    }

    private Result<String> verifyLogoutPermissions(Entity requester, String targetUsername) {
        try {
            UserUtils.validateUser(targetUsername);
        } catch (InvalidInputException e) {
            return Result.failure(ErrorCode.FORBIDDEN);
        } catch (UserNotFoundException e) {
            return Result.failure(ErrorCode.USER_NOT_FOUND);
        }

        Role role = Role.valueOf(requester.getString(Constants.USER_ROLE));
        if (Role.isAdmin(role) || targetUsername.equals(requester.getString(Constants.USER_NAME))) {
            return Result.success(targetUsername);
        }
        return Result.failure(ErrorCode.UNAUTHORIZED);
    }

    private Result<String> deleteUserTokens(String username) {
        return DB.executeInTransaction(datastore, txn -> {
            try {
                String gqlQuery = 
                    "SELECT __key__ FROM " + Constants.KIND_TOKEN + 
                    " WHERE " + Constants.USER_NAME + " = @username";

                Query<Key> query = Query.newGqlQueryBuilder(Query.ResultType.KEY, gqlQuery)
                                    .setBinding("username", username)
                                    .build();
                QueryResults<Key> results = datastore.run(query);

                List<Key> keysToRemove = new ArrayList<>();
                results.forEachRemaining(keysToRemove::add);

                if (!keysToRemove.isEmpty()) {
                    txn.delete(keysToRemove.toArray(new Key[0]));
                }

                return Result.success("Logout successful");
            } catch (Exception e) {
                LOG.severe("Error deleting tokens: " + e.getMessage());
                return Result.failure(ErrorCode.FORBIDDEN);
            }
        });
    }

    // --- Helper Methods for Show Sessions ---

    private Result<Entity> verifyAdminRole(Entity requester) {
        String roleString = requester.getString(Constants.USER_ROLE);
        
        if (!Role.isDefined(roleString)) {
            return Result.failure(ErrorCode.INVALID_INPUT);
        }
        
        Role role = Role.valueOf(roleString);
        if (role != Role.ADMIN) {
            return Result.failure(ErrorCode.UNAUTHORIZED);
        }
        
        // Return the requester entity so the functional chain can continue
        return Result.success(requester); 
    }

    private Result<SessionsWrapper> fetchAllSessions() {
        try {
            String gqlQuery = "SELECT * FROM " + Constants.KIND_TOKEN;
            Query<Entity> query = Query.newGqlQueryBuilder(Query.ResultType.ENTITY, gqlQuery).build();
            QueryResults<Entity> results = datastore.run(query);

            List<TokenSummary> summary = new ArrayList<>(); 

            // Clean functional iteration to build the list
            results.forEachRemaining(entity -> {
                String tokenId = entity.getString(Constants.TOKEN_ID);
                String username = entity.getString(Constants.USER_NAME);
                String tRoleString = entity.getString(Constants.USER_ROLE);
                long expiresAt = entity.getLong(Constants.EXPIRES_AT) / 1000;
                
                summary.add(new TokenSummary(tokenId, username, tRoleString, expiresAt));
            });

            return Result.success(new SessionsWrapper(summary));

        } catch (Exception e) {
            LOG.severe("Database error showing sessions: " + e.getMessage());
            return Result.failure(ErrorCode.FORBIDDEN); 
        }
    }
}
