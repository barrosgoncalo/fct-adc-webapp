package pt.unl.fct.di.adc.firstwebapp.data;

import java.util.List;

public class SessionsWrapper {

    private List<TokenSummary> sessions;

    public SessionsWrapper() {}

    public SessionsWrapper(List<TokenSummary> sessions) {
        this.sessions = sessions;
    }

	public List<TokenSummary> getSessions() {
		return sessions;
	}


}
