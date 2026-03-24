package pt.unl.fct.di.adc.firstwebapp.data;

public class CreateAccountRequest {

        public String username;
        public String password;
        public String confirmation;
        public String email;
        public String phone;
        public String address;
        public UserRole role;

        public CreateAccountRequest() {}


        public CreateAccountRequest(String username, String password, String confirmation, String email, String phone, String address, UserRole role) {
            this.username = username;
            this.password = password;
            this.confirmation = confirmation;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.role = role;
        }

        private boolean nonEmptyOrBlankField(String field) {
            return field != null && !field.isBlank();
        }


        public boolean validRegistration() {

            return nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(password) &&
                nonEmptyOrBlankField(email) &&
                nonEmptyOrBlankField(phone) &&
                nonEmptyOrBlankField(address) &&
                UserRole.isDefined(role) &&
                email.contains("@") &&
                password.equals(confirmation);
        }
}
