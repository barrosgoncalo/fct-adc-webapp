package pt.unl.fct.di.adc.firstwebapp.data;

public class UserRequest {

        private String username;
        private String password;
        private String confirmation;
        private String phone;
        private String address;
        private String role;

        public UserRequest() {}


        public UserRequest(String username, String password, String confirmation, String phone, String address, String role) {
            this.username = username;
            this.password = password;
            this.confirmation = confirmation;
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

		public String getPhone() {
			return phone;
		}


		public String getAddress() {
			return address;
		}


		public String getRole() {
			return role;
		}

        // TODO: Should unknown roles lead to INVALID_INPUT?? [NEXT POINT]
        public boolean isValid() {
            return nonEmptyOrBlankField(username) &&
                nonEmptyOrBlankField(password) &&
                nonEmptyOrBlankField(phone) &&
                nonEmptyOrBlankField(address) &&
                nonEmptyOrBlankField(role) &&
                username.contains("@") &&
                password.equals(confirmation);
        }

        // auxiliary
        private boolean nonEmptyOrBlankField(String field) {
            return field != null && !field.isBlank();
        }



}
