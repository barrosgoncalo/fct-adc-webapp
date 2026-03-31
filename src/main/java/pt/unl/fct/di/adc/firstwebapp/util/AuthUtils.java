package pt.unl.fct.di.adc.firstwebapp.util;

import java.security.MessageDigest;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.DatastoreReader;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;

import pt.unl.fct.di.adc.firstwebapp.data.Constants;
import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UnauthenticTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;
import pt.unl.fct.di.adc.firstwebapp.security.SecurityConfig;

public class AuthUtils {

    private static final String PROPERTY_USERNAME = "username";
    private static final String EXPIRES_AT = "expiresAt";
    private static final String KIND_TOKEN = "Token";
    private static final String KIND_USER = "User";

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind(KIND_TOKEN);
    private static final KeyFactory usersKeyFactory = datastore.newKeyFactory().setKind(KIND_USER);

    // Read-only
    public static Entity validateToken(String tokenId) 
            throws InvalidInputException, ExpiredTokenException, UserNotFoundException, UnauthenticTokenException {

            return validateToken(datastore, tokenId);
    }

    // Read-Modify-Write
    public static Entity validateToken(DatastoreReader txn, String tokenId) 
            throws InvalidInputException, ExpiredTokenException, UserNotFoundException, UnauthenticTokenException {

        if(!ValidationUtils.nonEmptyOrBlankField(tokenId))
            throw new InvalidInputException();

        Key tokenKey = tokensKeyFactory.newKey(tokenId);
        Entity token = txn.get(tokenKey);


        if(token == null)
            throw new InvalidInputException();

        if(!authenticate(token))
            throw new UnauthenticTokenException();

        if(System.currentTimeMillis() > token.getLong(EXPIRES_AT)) {
            throw new ExpiredTokenException();
        }

        String username = token.getString(PROPERTY_USERNAME);
        Key userKey = usersKeyFactory.newKey(username);
        Entity user = txn.get(userKey);

        if(user == null)
            throw new UserNotFoundException();

        return user;
    }

    private static boolean authenticate(Entity token) {

        String username = token.getString(Constants.USER_NAME);
        String role = token.getString(Constants.USER_ROLE);
        long issuedAt = token.getLong(Constants.ISSUED_AT);
        long expiresAt = token.getLong(Constants.EXPIRES_AT);
        String tokenId = token.getString(Constants.TOKEN_ID);
        String sessionKey  = computeSessionKey(SecurityConfig.getMasterKey(), tokenId);

        //hashed
        String tokenHash = token.getString(Constants.HASH);
        String computeHash = 
            DigestUtils.sha512Hex(
                    username
                    + role
                    + String.valueOf( issuedAt )
                    + String.valueOf( expiresAt )
                    + sessionKey
                );

        return MessageDigest.isEqual(tokenHash.getBytes(), computeHash.getBytes());
    }

    public static String computeSessionKey(String masterKey, String tokenId) {
        return DigestUtils.sha512Hex( masterKey + tokenId );
    }
}

