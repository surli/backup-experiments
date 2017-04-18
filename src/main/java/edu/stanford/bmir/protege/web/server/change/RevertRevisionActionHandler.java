package edu.stanford.bmir.protege.web.server.change;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectChangeHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.change.RevertRevisionAction;
import edu.stanford.bmir.protege.web.shared.change.RevertRevisionResult;
import edu.stanford.bmir.protege.web.shared.event.ProjectEvent;
import edu.stanford.bmir.protege.web.shared.events.EventList;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import org.semanticweb.owlapi.model.OWLEntity;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 19/03/15
 */
public class RevertRevisionActionHandler extends AbstractProjectChangeHandler<OWLEntity, RevertRevisionAction, RevertRevisionResult> {

    private final Provider<OWLOntologyChangeDataReverter> reverterProvider;

    @Inject
    public RevertRevisionActionHandler(ProjectManager projectManager, Provider<OWLOntologyChangeDataReverter> reverterProvider, AccessManager accessManager) {
        super(projectManager, accessManager);
        this.reverterProvider = reverterProvider;
    }

    @Override
    public Class<RevertRevisionAction> getActionClass() {
        return RevertRevisionAction.class;
    }

    @Override
    protected ChangeListGenerator<OWLEntity> getChangeListGenerator(RevertRevisionAction action, Project project, ExecutionContext executionContext) {
        RevisionNumber revisionNumber = action.getRevisionNumber();
        return new RevisionReverterChangeListGenerator(revisionNumber, reverterProvider.get());
    }

    @Override
    protected RevertRevisionResult createActionResult(ChangeApplicationResult<OWLEntity> changeApplicationResult, RevertRevisionAction action, Project project, ExecutionContext executionContext, EventList<ProjectEvent<?>> eventList) {
        return new RevertRevisionResult(project.getProjectId(), eventList);
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction() {
        return BuiltInAction.REVERT_CHANGES;
    }

    @Override
    protected ChangeDescriptionGenerator<OWLEntity> getChangeDescription(RevertRevisionAction action, Project project, ExecutionContext executionContext) {
        return new FixedMessageChangeDescriptionGenerator<>("Reverted the changes in Revision " + action.getRevisionNumber().getValue());
    }
}
