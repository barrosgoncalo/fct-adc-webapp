package pt.unl.fct.di.adc.firstwebapp.data;

import com.google.cloud.Timestamp;

public class TokenSummary {

    private String tokenId;
    private String username;
    private String role;
    private Timestamp expiresAt;

    public TokenSummary() {}

    public TokenSummary(String tokenId, String username, String role, Timestamp expiresAt) {
        this.tokenId = tokenId;
        this.username = username;
        this.role = role;
        this.expiresAt = expiresAt;
    }

	public String getTokenId() {
		return tokenId;
	}

	public String getUsername() {
		return username;
	}

	public String getRole() {
		return role;
	}

	public Timestamp getExpiresAt() {
		return expiresAt;
	}


}
