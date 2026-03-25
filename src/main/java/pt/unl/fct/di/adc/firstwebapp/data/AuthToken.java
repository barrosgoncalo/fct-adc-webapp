package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60*2; // 2h
	
	public String username;
	public String tokenID;
    public UserRole role;
	public long creationData;
	public long expirationData;
	
	public AuthToken() { }
	
	public AuthToken(String username, UserRole role) {
		this.username = username;
		this.tokenID = UUID.randomUUID().toString();
        this.role = role;
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + EXPIRATION_TIME;
	}
    
    public boolean isValid() {
        return nonEmptyOrBlankField(username)
            && nonEmptyOrBlankField(tokenID)
            && UserRole.isDefined(role)
            && creationData != 0L
            && expirationData != 0L;
    }

    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}
