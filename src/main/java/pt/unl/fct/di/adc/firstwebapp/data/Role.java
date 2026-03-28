package pt.unl.fct.di.adc.firstwebapp.data;

public enum Role {
    USER(1), BOFFICER(2), ADMIN(3);

    private int degree;

    private Role(int degree) {
        this.degree = degree;
    }

    public int getDegree() {
		return this.degree;
	}

    public boolean isHigherOrEqualDegree(Role role) {
        return this.getDegree() >= role.getDegree();
    }

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
