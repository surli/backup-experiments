package edu.stanford.bmir.protege.web.client.mail;

import com.google.common.base.Optional;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.client.library.dlg.DialogButton;
import edu.stanford.bmir.protege.web.client.library.dlg.WebProtegeDialog;
import edu.stanford.bmir.protege.web.client.library.dlg.WebProtegeDialogButtonHandler;
import edu.stanford.bmir.protege.web.client.library.dlg.WebProtegeDialogCloser;
import edu.stanford.bmir.protege.web.client.library.msgbox.MessageBox;
import edu.stanford.bmir.protege.web.client.progress.ProgressMonitor;
import edu.stanford.bmir.protege.web.client.user.LoggedInUserProvider;
import edu.stanford.bmir.protege.web.shared.mail.GetEmailAddressAction;
import edu.stanford.bmir.protege.web.shared.mail.GetEmailAddressResult;
import edu.stanford.bmir.protege.web.shared.mail.SetEmailAddressAction;
import edu.stanford.bmir.protege.web.shared.mail.SetEmailAddressResult;
import edu.stanford.bmir.protege.web.shared.user.EmailAddress;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import javax.inject.Inject;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 06/11/2013
 */
public class ChangeEmailAddressPresenter {

    private final DispatchServiceManager dispatchServiceManager;

    private final LoggedInUserProvider loggedInUserProvider;

    @Inject
    public ChangeEmailAddressPresenter(DispatchServiceManager dispatchServiceManager, LoggedInUserProvider loggedInUserProvider) {
        this.dispatchServiceManager = dispatchServiceManager;
        this.loggedInUserProvider = loggedInUserProvider;
    }

    public void changeEmail() {
        final UserId userId = loggedInUserProvider.getCurrentUserId();
        if(userId.isGuest()) {
            MessageBox.showAlert("You must be logged in to change your email address");
            return;
        }
        ProgressMonitor.get().showProgressMonitor("Retrieving email address", "Please wait.");

        dispatchServiceManager.execute(new GetEmailAddressAction(userId), new DispatchServiceCallback<GetEmailAddressResult>() {
            @Override
            public void handleSuccess(GetEmailAddressResult result) {
                showDialog(result.getEmailAddress());
                ProgressMonitor.get().hideProgressMonitor();
            }

            @Override
            public void handleFinally() {
                ProgressMonitor.get().hideProgressMonitor();
            }
        });
    }

    private void showDialog(Optional<EmailAddress> emailAddress) {
        final UserId userId = loggedInUserProvider.getCurrentUserId();
        ChangeEmailAddressDialogController controller = new ChangeEmailAddressDialogController();
        if (emailAddress.isPresent()) {
            controller.setValue(emailAddress.get());
        }
        controller.setDialogButtonHandler(DialogButton.OK, new WebProtegeDialogButtonHandler<Optional<EmailAddress>>() {
            @Override
            public void handleHide(Optional<EmailAddress> data, final WebProtegeDialogCloser closer) {
                if(data.isPresent()) {
                    dispatchServiceManager.execute(new SetEmailAddressAction(userId, data.get().getEmailAddress()), new DispatchServiceCallback<SetEmailAddressResult>() {
                        @Override
                        public void handleSuccess(SetEmailAddressResult result) {
                            if (result.getResult() == SetEmailAddressResult.Result.ADDRESS_ALREADY_EXISTS) {
                                MessageBox.showMessage("Address already taken",
                                        "The email address that you have specified is taken by another user.  " +
                                                "Please specify a different email address.");
                            }
                            else {
                                closer.hide();
                            }
                        }
                    });
                }
                else {
                    MessageBox.showAlert("The specified email addresses do not match.");
                }
            }
        });
        WebProtegeDialog<Optional<EmailAddress>> dlg = new WebProtegeDialog<Optional<EmailAddress>>(controller);
        dlg.setVisible(true);
    }

}
