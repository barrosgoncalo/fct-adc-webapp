package pt.unl.fct.di.adc.firstwebapp.data;

public enum Role {
    USER, BOFFICER, ADMIN;

    public static boolean isDefined(String role) {
        if(role == null) return false;
        for(Role r : Role.values())
            if( role.equals(r.name()))
                return true;
        return false;
    }

    public static boolean isAdmin(Role role) {
        return role == ADMIN;
    }

    public static boolean isAdminOrBofficer(Role role) {
       return role == ADMIN ||  role == BOFFICER;
    }

}
