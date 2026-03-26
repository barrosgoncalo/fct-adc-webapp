package pt.unl.fct.di.adc.firstwebapp.data;

public enum UserRole {
    USER, BOFFICER, ADMIN;

    public static boolean isDefined(String role) {
        if(role == null) return false;
        for(UserRole r : UserRole.values())
            if( role.equals(r.name()))
                return true;
        return false;
    }

    public static boolean isAdmin(UserRole role) {
        return role == ADMIN;
    }
    public static boolean isAdminOrBofficer(UserRole role) {
       return role == ADMIN ||  role == BOFFICER;
    }

}
