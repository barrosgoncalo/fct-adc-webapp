package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*15; // 9h
	
	public String tokenId;
	public String username;
    public UserRole role;
	public long issuedAt;
	public long expiresAt;
	
	public AuthToken() { }
	
	public AuthToken(String username, UserRole role) {
		this.username = username;
		this.tokenId = UUID.randomUUID().toString();
        this.role = role;
		this.issuedAt = System.currentTimeMillis();
		this.expiresAt = this.issuedAt + EXPIRATION_TIME;
	}
    
    @JsonIgnore
    public boolean isValid() {
        return nonEmptyOrBlankField(username)
            && nonEmptyOrBlankField(tokenId)
            && UserRole.isDefined(role)
            && issuedAt != 0L
            && expiresAt != 0L;
    }

    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}
