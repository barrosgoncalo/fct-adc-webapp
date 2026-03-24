package pt.unl.fct.di.adc.firstwebapp.data;

public enum UserRole {
    USER, BOFFICER, ADMIN;

    public static boolean isDefined(UserRole role) {
        return role != null;
    }
}
