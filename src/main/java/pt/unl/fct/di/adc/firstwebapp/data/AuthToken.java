package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*15; // 9h
	
	private String tokenId;
	private String username;
    private String role;
	private long issuedAt;
	private long expiresAt;
	
	public AuthToken() { }
	
	public AuthToken(String username, String role) {
		this.username = username;
		this.tokenId = UUID.randomUUID().toString();
        this.role = role;
		this.issuedAt = System.currentTimeMillis();
		this.expiresAt = this.issuedAt + EXPIRATION_TIME;
	}

    // getters
	public String getTokenId() {
		return tokenId;
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public long getIssuedAt() {
		return issuedAt;
	}

	public long getExpiresAt() {
		return expiresAt;
	}

    
    @JsonIgnore
    public boolean isValid() {
        return nonEmptyOrBlankField(username)
            && nonEmptyOrBlankField(tokenId)
            && Role.isDefined(role)
            && issuedAt != 0L
            && expiresAt != 0L;
    }

    // auxiliary
    private boolean nonEmptyOrBlankField(String field) {
        return field != null && !field.isBlank();
    }

}
