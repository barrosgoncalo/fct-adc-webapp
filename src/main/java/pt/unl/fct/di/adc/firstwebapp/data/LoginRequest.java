package pt.unl.fct.di.adc.firstwebapp.data;

import pt.unl.fct.di.adc.firstwebapp.util.ValidationUtils;

public class LoginRequest {

        private String username;
        private String password;

        public LoginRequest() { }

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        // getters
		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
        

        public boolean isValid() {
            return ValidationUtils.nonEmptyOrBlankField(username) 
                && ValidationUtils.nonEmptyOrBlankField(password);
        }

}
