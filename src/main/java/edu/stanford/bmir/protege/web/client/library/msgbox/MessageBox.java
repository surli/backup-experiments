package edu.stanford.bmir.protege.web.client.library.msgbox;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Widget;
import edu.stanford.bmir.protege.web.client.library.dlg.*;

import javax.annotation.Nonnull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 22/07/2013
 */
public class MessageBox {

    private static final Void RETURN = null;

    private static final String DLG_TITLE = "";

    private static final String DEFAULT_SUB_MESSAGE = "";


    /**
     * Shows a {@link MessageBox} that displays a single message.
     * The message box will have a "message" icon.
     * @param mainMessage The message to be displayed.
     */
    public static void showMessage(@Nonnull String mainMessage) {
        showMessage(mainMessage, DEFAULT_SUB_MESSAGE);
    }

    /**
     * Shows a {@link MessageBox} that displays a main message (like a title) along with a sub-message.
     * The message box will have a "message" icon.
     * @param mainMessage The main message.
     * @param subMessage The sub-message.
     */
    public static void showMessage(@Nonnull String mainMessage,
                                   @Nonnull String subMessage) {
        showMessageBox(MessageStyle.MESSAGE, mainMessage, subMessage, () -> {});
    }

    /**
     * Shows a {@link MessageBox} that displays a main message (like a title) along with a sub-message.
     * The message box will have a "message" icon.
     * @param mainMessage The main message.
     * @param subMessage The sub-message.
     * @param closedCallback A callback that will be run when the user dismisses the message box.
     */
    public static void showMessage(@Nonnull String mainMessage,
                                   @Nonnull String subMessage,
                                   @Nonnull Runnable closedCallback) {
        showMessageBox(MessageStyle.MESSAGE, mainMessage, subMessage, closedCallback);
    }

    /**
     * Shows a {@link MessageBox} that displays a single message.
     * The message box will have an "alert" icon.
     * @param mainMessage The message to be displayed.
     */
    public static void showAlert(@Nonnull String mainMessage) {
        showAlert(mainMessage, "");
    }

    /**
     * Shows a {@link MessageBox} that displays a main message (like a title) along with a sub-message.
     * The message box will have an "alert" icon.
     * @param mainMessage The main message.
     * @param subMessage The sub-message.
     */
    public static void showAlert(@Nonnull String mainMessage,
                                 @Nonnull String subMessage) {
        showMessageBox(MessageStyle.ALERT, mainMessage, subMessage, () -> {});
    }

    /**
     * Shows a {@link MessageBox} that displays a main message (like a title) along with a sub-message.
     * The message box will have an "alert" icon.
     * @param mainMessage The main message.
     * @param subMessage The sub-message.
     * @param closedCallback A callback that will be run when the user dismisses the message box.
     */
    public static void showAlert(@Nonnull String mainMessage,
                                 @Nonnull String subMessage,
                                 @Nonnull Runnable closedCallback) {
        showMessageBox(MessageStyle.ALERT, mainMessage, subMessage, closedCallback);
    }

    public static void showErrorMessage(String mainMessage, Throwable throwable) {
        SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendEscaped("Details: ");
        sb.appendEscaped(throwable.getMessage());
        showAlert(mainMessage, sb.toSafeHtml().toString());
    }


    public static void showOKCancelConfirmBox(String mainMessage, String subMessage, final OKCancelHandler handler) {
        final MessageBoxView messageBoxView = createMessageBox(MessageStyle.QUESTION, mainMessage, subMessage);
        final WebProtegeOKCancelDialogController<Void> controller = new WebProtegeOKCancelDialogController<Void>(DLG_TITLE) {
            @Override
            public Widget getWidget() {
                return messageBoxView.asWidget();
            }

            @Nonnull
            @Override
            public java.util.Optional<HasRequestFocus> getInitialFocusable() {
                return java.util.Optional.empty();
            }

            @Override
            public Void getData() {
                return RETURN;
            }
        };
        controller.setDialogButtonHandler(DialogButton.OK, (data, closer) -> {
            closer.hide();
            handler.handleOK();
        });
        controller.setDialogButtonHandler(DialogButton.CANCEL, (data, closer) -> {
            closer.hide();
            handler.handleCancel();
        });
        WebProtegeDialog<Void> dlg = createDialog(controller);
        dlg.setVisible(true);
        scheduleCentering(dlg);
    }

    public static void showYesNoConfirmBox(String mainMessage, String subMessage, final YesNoHandler handler) {
        final MessageBoxView messageBoxView = createMessageBox(MessageStyle.QUESTION, mainMessage, subMessage);
        final WebProtegeYesNoDialogController<Void> controller = new WebProtegeYesNoDialogController<Void>(DLG_TITLE) {
            @Override
            public Widget getWidget() {
                return messageBoxView.asWidget();
            }

            @Nonnull
            @Override
            public java.util.Optional<HasRequestFocus> getInitialFocusable() {
                return java.util.Optional.empty();
            }

            @Override
            public Void getData() {
                return RETURN;
            }
        };
        controller.setDialogButtonHandler(DialogButton.YES, (data, closer) -> {
            closer.hide();
            handler.handleYes();
        });
        controller.setDialogButtonHandler(DialogButton.NO, (data, closer) -> {
            closer.hide();
            handler.handleNo();

        });
        WebProtegeDialog<Void> dlg = createDialog(controller);
        dlg.show();
        scheduleCentering(dlg);
    }

    public static void showYesNoConfirmBox(String mainMessage, String subMessage, final Runnable yesHandler) {
        showYesNoConfirmBox(mainMessage, subMessage, new YesNoHandler() {
            @Override
            public void handleYes() {
                yesHandler.run();
            }

            @Override
            public void handleNo() {
                // Ignore
            }
        });
    }


    private static void showMessageBox(MessageStyle messageStyle, String mainMessage, String subMessage, Runnable callback) {
        final MessageBoxView messageBoxView = createMessageBox(messageStyle, mainMessage, subMessage);
        final WebProtegeOKDialogController<Void> controller = new WebProtegeOKDialogController<Void>(DLG_TITLE) {
            @Override
            public Widget getWidget() {
                return messageBoxView.asWidget();
            }

            @Nonnull
            @Override
            public java.util.Optional<HasRequestFocus> getInitialFocusable() {
                return java.util.Optional.empty();
            }

            @Override
            public Void getData() {
                return RETURN;
            }
        };
        final WebProtegeDialog<Void> dlg = createDialog(controller);
        dlg.setVisible(true);
        dlg.addCloseHandler(event -> callback.run());
        scheduleCentering(dlg);
    }

    private static void scheduleCentering(final WebProtegeDialog<Void> dlg) {
        Scheduler.get().scheduleDeferred(() -> {
            int left = (Window.getClientWidth() - dlg.getOffsetWidth()) / 2;
            int top = (Window.getClientHeight() - dlg.getOffsetHeight()) / 2;
            dlg.setPopupPosition(left, top);
            dlg.setVisible(true);
        });
    }

    private static MessageBoxView createMessageBox(MessageStyle messageStyle, String mainMessage, String subMessage) {
        final MessageBoxView messageBoxView = new MessageBoxViewImpl();
        messageBoxView.setMainMessage(mainMessage);
        messageBoxView.setSubMessage(subMessage);
        messageBoxView.setMessageStyle(messageStyle);
        return messageBoxView;
    }

    private static WebProtegeDialog<Void> createDialog(WebProtegeDialogController<Void> controller) {
        WebProtegeDialog<Void> dlg = new WebProtegeDialog<Void>(controller);
        dlg.setGlassEnabled(true);
        dlg.setGlassStyleName("glass");
        dlg.addStyleName("glass-popup-shadow");
        return dlg;
    }


}
