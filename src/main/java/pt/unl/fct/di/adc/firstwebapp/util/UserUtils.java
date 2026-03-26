package pt.unl.fct.di.adc.firstwebapp.util;

import java.util.logging.Logger;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.DatastoreReader;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.adc.firstwebapp.exceptions.ExpiredTokenException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.InvalidInputException;
import pt.unl.fct.di.adc.firstwebapp.exceptions.UserNotFoundException;

public class UserUtils {

    private static final String KIND_USER = "User";

	private static final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();

    private static final KeyFactory usersKeyFactory = datastore.newKeyFactory().setKind(KIND_USER);

    public static Entity validateUser(String username)
            throws InvalidInputException, ExpiredTokenException, UserNotFoundException {
        return validateUser(datastore, username);
    }

    public static Entity validateUser(DatastoreReader txn, String username) 
            throws InvalidInputException, ExpiredTokenException, UserNotFoundException {

        if(!nonEmptyOrBlankField(username))
            throw new InvalidInputException();

        Key userKey = usersKeyFactory.newKey(username);
        Entity user = txn.get(userKey);

        if(user == null) {
            throw new UserNotFoundException();
        }

        return user;
    }


    private static boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}
