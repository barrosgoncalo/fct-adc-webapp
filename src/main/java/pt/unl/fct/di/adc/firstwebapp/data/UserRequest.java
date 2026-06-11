package pt.unl.fct.di.adc.firstwebapp.data;

import java.security.MessageDigest;

import static pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils.*;

public record UserRequest(
        String username,
        String password,
        String confirmation,
        String phone,
        String address,
        String role 
) {
        public boolean isValid() {
            return nonEmptyOrBlankField(username)
                && nonEmptyOrBlankField(password)
                && nonEmptyOrBlankField(phone)
                && nonEmptyOrBlankField(address)
                && nonEmptyOrBlankField(role) && Role.isDefined(role)
                && validEmail(username)
                && MessageDigest.isEqual(password.getBytes(), confirmation.getBytes());
        }

}
