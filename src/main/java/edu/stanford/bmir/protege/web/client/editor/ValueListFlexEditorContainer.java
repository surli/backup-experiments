package edu.stanford.bmir.protege.web.client.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.SimplePanel;
import edu.stanford.bmir.protege.web.client.library.button.DeleteButton;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 10/04/16
 */
public class ValueListFlexEditorContainer<O> extends Composite implements HasEnabled {

    public static final String FLEX_GROW = "flexGrow";

    private boolean deleteButtonEnabled;

    public void setDeleteButtonEnabled(boolean deleteButtonEnabled) {
        this.deleteButtonEnabled = deleteButtonEnabled;
    }

    interface ValueListInlineEditorContainerUiBinder extends UiBinder<HTMLPanel, ValueListFlexEditorContainer> {

    }

    private static ValueListInlineEditorContainerUiBinder ourUiBinder = GWT.create(ValueListInlineEditorContainerUiBinder.class);

    private final ValueEditor<O> editor;

    @UiField
    SimplePanel editorHolder;

    @UiField
    DeleteButton deleteButton;

    private boolean enabled = true;

    public ValueListFlexEditorContainer(ValueEditor<O> editor) {
        initWidget(ourUiBinder.createAndBindUi(this));
        this.editor = editor;
        editorHolder.setWidget(editor);
    }

    public void setDeleteButtonVisible(boolean visible) {
        if (visible) {
            deleteButton.getElement().getStyle().setVisibility(Style.Visibility.VISIBLE);
        }
        else {
            deleteButton.getElement().getStyle().setVisibility(Style.Visibility.HIDDEN);
        }
    }

    public boolean isDeleteButtonVisible() {
        return deleteButton.isVisible();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean b) {
        enabled = b;
        deleteButton.setEnabled(enabled);
        if(editor instanceof HasEnabled) {
            ((HasEnabled) editor).setEnabled(enabled);
        }
    }


    public void setDirection(ValueListFlexEditorDirection direction) {
        String flexGrow;
        if(direction == ValueListFlexEditorDirection.COLUMN) {
            flexGrow = "1";
        }
        else {
            flexGrow = "0";
            getElement().getStyle().setMargin(5, Style.Unit.PX);
        }
        editorHolder.getElement().getStyle().setProperty(FLEX_GROW, flexGrow);
        getElement().getStyle().setProperty(FLEX_GROW, flexGrow);

    }

}