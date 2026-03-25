package pt.unl.fct.di.adc.firstwebapp.util;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;

public class AuthUtils {

    private static final String PROPERTY_USERNAME = "username";
    private static final String EXPIRES_AT = "expiresAt";
    private static final String KIND_TOKEN = "Token";
    private static final String KIND_USER = "User";

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final KeyFactory tokensKeyFactory = datastore.newKeyFactory().setKind(KIND_TOKEN);
    private static final KeyFactory usersKeyFactory = datastore.newKeyFactory().setKind(KIND_USER);

    public static Entity validateToken(Transaction txn, String tokenId) 
            throws InvalidInputException, ExpiredTokenException, UserNotFoundException {

        if(!nonEmptyOrBlankField(tokenId))
            throw new InvalidInputException();

        Key tokenKey = tokensKeyFactory.newKey(tokenId);
        Entity token = txn.get(tokenKey);


        if(token == null)
            throw new InvalidInputException();

        if(System.currentTimeMillis() > token.getLong(EXPIRES_AT)) {
            // TODO: LOG 
            throw new ExpiredTokenException();
        }

        String username = token.getString(PROPERTY_USERNAME);
        Key userKey = usersKeyFactory.newKey(username);
        Entity user = txn.get(userKey);

        if(user == null)
            throw new UserNotFoundException();

        return user;
    }


    private static boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}

