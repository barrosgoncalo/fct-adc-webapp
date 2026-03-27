package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.UUID;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

import pt.unl.fct.di.adc.firstwebapp.util.AuthUtils;
import pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*15; // 9h
	
	private String tokenId;
	private String username;
    private String role;
	private long issuedAt;
	private long expiresAt;
    private String hash;
	
	public AuthToken() { }
	
	public AuthToken(String username, String role, String masterKey) {
		this.username = username;
		this.tokenId = UUID.randomUUID().toString();
        this.role = role;
		this.issuedAt = System.currentTimeMillis();
		this.expiresAt = this.issuedAt + EXPIRATION_TIME;

        this.hash = 
            DigestUtils.sha512Hex(
                    username 
                    + role 
                    + issuedAt
                    + expiresAt
                    + AuthUtils.computeSessionKey(masterKey, tokenId)
                );
}

    // getters
	public String getTokenId() {
		return this.tokenId;
	}

	public String getUsername() {
		return this.username;
	}

	public String getRole() {
		return this.role;
	}

	public long getIssuedAt() {
		return this.issuedAt;
	}

	public long getExpiresAt() {
		return this.expiresAt;
	}

    public String getHash() {
        return this.hash;
    }

    
    @JsonIgnore
    public boolean isValid() {
        return ValidationUtils.nonEmptyOrBlankField(username)
            && ValidationUtils.nonEmptyOrBlankField(tokenId)
            && Role.isDefined(role)
            && issuedAt != 0L
            && expiresAt != 0L;
    }

}
