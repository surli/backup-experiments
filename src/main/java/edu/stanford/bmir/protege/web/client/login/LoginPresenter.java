package edu.stanford.bmir.protege.web.client.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.web.bindery.event.shared.EventBus;
import edu.stanford.bmir.protege.web.client.app.Presenter;
import edu.stanford.bmir.protege.web.client.chgpwd.ResetPasswordPresenter;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.place.SignUpPlace;
import edu.stanford.bmir.protege.web.client.user.LoggedInUserManager;
import edu.stanford.bmir.protege.web.shared.auth.AuthenticatedActionExecutor;
import edu.stanford.bmir.protege.web.shared.auth.AuthenticationResponse;
import edu.stanford.bmir.protege.web.shared.auth.PerformLoginActionFactory;
import edu.stanford.bmir.protege.web.shared.auth.SignInDetails;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import edu.stanford.bmir.protege.web.shared.user.UserDetails;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.shared.access.BuiltInAction.CREATE_ACCOUNT;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12/02/16
 */
@ApplicationSingleton
public class LoginPresenter implements Presenter {

    private final LoginView view;

    private final AuthenticatedActionExecutor loginExecutor;

    private final LoggedInUserManager loggedInUserManager;

    private final PlaceController placeController;

    private final ResetPasswordPresenter resetPasswordPresenter;

    private Optional<Place> nextPlace = Optional.empty();




    @Inject
    public LoginPresenter(@Nonnull LoginView view,
                          @Nonnull AuthenticatedActionExecutor loginExecutor,
                          @Nonnull LoggedInUserManager loggedInUserManager,
                          @Nonnull PlaceController placeController,
                          @Nonnull ResetPasswordPresenter resetPasswordPresenter) {
        this.view = checkNotNull(view);
        this.loginExecutor = checkNotNull(loginExecutor);
        this.loggedInUserManager = checkNotNull(loggedInUserManager);
        this.placeController = checkNotNull(placeController);
        this.resetPasswordPresenter = checkNotNull(resetPasswordPresenter);
        view.setSignInHandler(this::handleSignIn);
        view.setForgotPasswordHandler(this::handleResetPassword);
        view.setSignUpForAccountHandler(this::handleSignUpForAccout);
    }

    @Override
    public void start(@Nonnull AcceptsOneWidget container, @Nonnull EventBus eventBus) {
        view.clearView();
        view.hideErrorMessages();
        boolean canCreateUser = loggedInUserManager.isAllowedApplicationAction(CREATE_ACCOUNT);
        view.setSignUpForAccountVisible(canCreateUser);
        container.setWidget(view);
    }

    public void setNextPlace(Place nextPlace) {
        this.nextPlace = Optional.of(nextPlace);
    }

    private void handleSignUpForAccout() {
        placeController.goTo(new SignUpPlace());
    }

    private void handleSignIn() {
        view.hideErrorMessages();
        String userName = view.getUserName();
        if(userName.isEmpty()) {
            view.showUserNameRequiredErrorMessage();
            return;
        }
        String password = view.getPassword();
        if(password.isEmpty()) {
            view.showPasswordRequiredErrorMessage();
            return;
        }
        SignInDetails signInDetails = new SignInDetails(userName, password);
        handleSignIn(signInDetails);
    }

    private void handleResetPassword() {
        resetPasswordPresenter.resetPassword();
    }

    private void handleSignIn(SignInDetails signInDetails) {
        final UserId userId = UserId.getUserId(signInDetails.getUserName());
        loginExecutor.execute(userId, signInDetails.getClearTextPassword(),
                new PerformLoginActionFactory(),
                new DispatchServiceCallback<AuthenticationResponse>() {
                    @Override
                    public void handleSuccess(AuthenticationResponse response) {
                        handleAuthenticationResponse(userId, response);
                    }
                });
    }

    private void handleAuthenticationResponse(UserId userId, AuthenticationResponse response) {
        if(response == AuthenticationResponse.SUCCESS) {
            loggedInUserManager.setLoggedInUser(userId, new AsyncCallback<UserDetails>() {
                @Override
                public void onFailure(Throwable caught) {
                    GWT.log("[LoginPresenter] Switching user failed");
                }

                @Override
                public void onSuccess(UserDetails result) {
                    if(nextPlace.isPresent()) {
                        placeController.goTo(nextPlace.get());
                    }
                }
            });
        }
        else {
            view.showLoginFailedErrorMessage();
        }
    }


    @Override
    public String toString() {
        return toStringHelper("LoginPresenter")
                .toString();
    }
}
