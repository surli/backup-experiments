package edu.stanford.bmir.protege.web.server.dispatch.handlers;

import com.google.common.collect.ImmutableList;
import edu.stanford.bmir.protege.web.client.dispatch.actions.GetOntologyAnnotationsAction;
import edu.stanford.bmir.protege.web.client.dispatch.actions.GetOntologyAnnotationsResult;
import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractHasProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import org.semanticweb.owlapi.model.OWLAnnotation;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static edu.stanford.bmir.protege.web.shared.access.BuiltInAction.VIEW_PROJECT;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/02/2013
 */
public class GetOntologyAnnotationsActionHandler extends AbstractHasProjectActionHandler<GetOntologyAnnotationsAction, GetOntologyAnnotationsResult> {

    @Inject
    public GetOntologyAnnotationsActionHandler(ProjectManager projectManager,
                                               AccessManager accessManager) {
        super(projectManager, accessManager);
    }

    @Override
    public Class<GetOntologyAnnotationsAction> getActionClass() {
        return GetOntologyAnnotationsAction.class;
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction() {
        return VIEW_PROJECT;
    }

    @Override
    protected GetOntologyAnnotationsResult execute(GetOntologyAnnotationsAction action, Project project, ExecutionContext executionContext) {
        List<OWLAnnotation> result = new ArrayList<>(project.getRootOntology().getAnnotations());
        return new GetOntologyAnnotationsResult(ImmutableList.of());
    }

}
