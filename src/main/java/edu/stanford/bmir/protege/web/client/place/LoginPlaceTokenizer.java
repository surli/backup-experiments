package edu.stanford.bmir.protege.web.client.place;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.PlaceTokenizer;
import edu.stanford.bmir.protege.web.client.login.LoginPlace;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12/02/16
 */
public class LoginPlaceTokenizer implements WebProtegePlaceTokenizer<LoginPlace> {

    private static final String LOGIN = "login";

    @Override
    public boolean matches(String token) {
        return LOGIN.equals(token);
    }

    @Override
    public Class<LoginPlace> getPlaceClass() {
        return LoginPlace.class;
    }

    public LoginPlace getPlace(String token) {
        GWT.log("[LoginPlaceTokenizer] getPlace Token: " + token);
        return new LoginPlace();
    }

    public String getToken(LoginPlace place) {
        return LOGIN;
    }

}
