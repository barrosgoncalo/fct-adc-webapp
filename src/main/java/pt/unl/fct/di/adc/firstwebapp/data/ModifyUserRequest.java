package pt.unl.fct.di.adc.firstwebapp.data;


public class ModifyUserRequest {

    private String username;
    private AttributesData attributes;

    public ModifyUserRequest() {}

    public ModifyUserRequest(String username, AttributesData attributes) {
        this.username = username;
        this.attributes = attributes;
    }

    public String getUsername() {
        return username;
    }

    public AttributesData getAttributes() {
        return attributes;
    }


    public static class AttributesData {

        private String phone;
        private String address;
        private String username;

        public AttributesData() {}

        public AttributesData(String phone, String address) {
            this.phone = phone;
            this. address = address;
        }

        public String getPhone() {
            return phone;
        }

        public String getAddress() {
            return address;
        }

        public String getUsername() {
            return username;
        }

    }

}
