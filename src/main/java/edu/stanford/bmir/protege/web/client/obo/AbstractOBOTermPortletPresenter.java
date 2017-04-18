package edu.stanford.bmir.protege.web.client.obo;

import com.google.gwt.core.client.GWT;
import edu.stanford.bmir.protege.web.client.portlet.AbstractWebProtegePortletPresenter;
import edu.stanford.bmir.protege.web.client.rpc.OBOTextEditorService;
import edu.stanford.bmir.protege.web.client.rpc.OBOTextEditorServiceAsync;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.selection.SelectionModel;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.Optional;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/05/2012
 */
public abstract class AbstractOBOTermPortletPresenter extends AbstractWebProtegePortletPresenter {

    private static final OBOTextEditorServiceAsync SERVICE = GWT.create(OBOTextEditorService.class);

    protected AbstractOBOTermPortletPresenter(SelectionModel selectionModel,
                                              ProjectId projectId) {
        super(selectionModel, projectId);
    }

    public OBOTextEditorServiceAsync getService() {
        return SERVICE;
    }

    @Override
    protected void handleBeforeSetEntity(Optional<? extends OWLEntity> existingEntity) {
        if(existingEntity.isPresent() && isDirty()) {
            commitChangesForEntity(existingEntity.get());
        }
    }

    @Override
    protected void handleAfterSetEntity(Optional<OWLEntity> entity) {
        if(entity.isPresent()) {
            displayEntity(entity.get());
        }
        else {
//            clearDisplay();
        }
        updateTitle();
    }

    /**
     * Called to determined whether or not any edits have been made to the current entity via the UI
     * @return <code>true</code> if edits have been made, otherwise <code>false</code>.
     */
    protected abstract boolean isDirty();

    /**
     * Called to commit any changes that have been made.  This method will only be called by the system if the
     * {@link #isDirty()} method returns <code>true</code>.
     * @param entity The entity being edited not <code>null</code>.
     */
    protected abstract void commitChangesForEntity(OWLEntity entity);

    /**
     * Called to update the display so that it displays a given entity.
     * @param entity The entity to be displayed. Not <code>null</code>.
     */
    protected abstract void displayEntity(OWLEntity entity);

    /**
     * Called to clear the display if no entity is should be displayed.
     */
    protected abstract void clearDisplay();

    protected abstract String getTitlePrefix();

    /**
     * Called to update the title.  Implementations should compute a title and then set it using the {@link #setTitle(String)}
     * method.
     */
    final protected void updateTitle() {
//        setTitle(getTitlePrefix());
    }

}
