package pt.unl.fct.di.adc.firstwebapp.data;




public class DeleteAccountResponse {

    private final String message;

    public DeleteAccountResponse() {
        this.message = "Account deleted successfully";
    }

	public String getUsername() {
		return message;
	}

}
