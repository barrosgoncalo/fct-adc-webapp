package pt.unl.fct.di.adc.firstwebapp.data;

public class LoginRequest {

        public String username;
        public String password;

        public LoginRequest() { }

        public LoginRequest(String username, String password) {
            this.username = username;
            this.password = password;
        }

        private boolean nonEmptyOrBlankField(String field) {
            return field != null && !field.isBlank();
        }


        public boolean validRegistration() {
            return nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(password);
        }

}
