package pt.unl.fct.di.adc.firstwebapp.data;

public class UserRequest {

        private String username;
        private String password;
        private String confirmation;
        private String email;
        private String phone;
        private String address;
        private UserRole role;

        public UserRequest() {}


        public UserRequest(String username, String password, String confirmation, String email, String phone, String address, UserRole role) {
            this.username = username;
            this.password = password;
            this.confirmation = confirmation;
            this.email = email;
            this.phone = phone;
            this.address = address;
            this.role = role;
        }

        // getters
		public String getUsername() {
			return username;
		}


		public String getPassword() {
			return password;
		}


		public String getConfirmation() {
			return confirmation;
		}


		public String getEmail() {
			return email;
		}


		public String getPhone() {
			return phone;
		}


		public String getAddress() {
			return address;
		}


		public UserRole getRole() {
			return role;
		}

        // TODO: Should unknown roles lead to INVALID_INPUT?? [NEXT POINT]
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

        // auxiliary
        private boolean nonEmptyOrBlankField(String field) {
            return field != null && !field.isBlank();
        }



}
