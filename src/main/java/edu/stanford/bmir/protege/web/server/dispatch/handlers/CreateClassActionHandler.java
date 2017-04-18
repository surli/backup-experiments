package edu.stanford.bmir.protege.web.server.dispatch.handlers;

import edu.stanford.bmir.protege.web.client.dispatch.actions.CreateClassAction;
import edu.stanford.bmir.protege.web.client.dispatch.actions.CreateClassResult;
import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.change.*;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectChangeHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.logging.WebProtegeLogger;
import edu.stanford.bmir.protege.web.server.msg.OWLMessageFormatter;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.ObjectPath;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.event.ProjectEvent;
import edu.stanford.bmir.protege.web.shared.events.EventList;
import org.semanticweb.owlapi.model.OWLClass;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/02/2013
 */
public class CreateClassActionHandler extends AbstractProjectChangeHandler<OWLClass, CreateClassAction, CreateClassResult> {

    private final WebProtegeLogger logger;

    @Inject
    public CreateClassActionHandler(ProjectManager projectManager,
                                    AccessManager accessManager,
                                    WebProtegeLogger logger) {
        super(projectManager, accessManager);
        this.logger = logger;
    }

    @Override
    public Class<CreateClassAction> getActionClass() {
        return CreateClassAction.class;
    }

    @Nonnull
    @Override
    protected Iterable<BuiltInAction> getRequiredExecutableBuiltInActions() {
        return Arrays.asList(BuiltInAction.CREATE_CLASS, BuiltInAction.EDIT_ONTOLOGY);
    }

    @Override
    protected ChangeListGenerator<OWLClass> getChangeListGenerator(final CreateClassAction action, Project project, ExecutionContext executionContext) {
        return new CreateClassChangeGenerator(action.getBrowserText(), action.getSuperClass());
    }

    @Override
    protected ChangeDescriptionGenerator<OWLClass> getChangeDescription(CreateClassAction action, Project project, ExecutionContext executionContext) {
        return new FixedMessageChangeDescriptionGenerator<>(OWLMessageFormatter.formatMessage("Created {0} as a subclass of {1}", project, action.getBrowserText(), action.getSuperClass()));
    }

    @Override
    protected CreateClassResult createActionResult(ChangeApplicationResult<OWLClass> changeApplicationResult, CreateClassAction action, Project project, ExecutionContext executionContext, EventList<ProjectEvent<?>> eventList) {
        final OWLClass subclass = changeApplicationResult.getSubject().get();
        final ObjectPath<OWLClass> pathToRoot = getPathToRoot(project, subclass, action.getSuperClass());
        return new CreateClassResult(project.getRenderingManager().getRendering(subclass),
                                     pathToRoot,
                                     eventList);
    }

    private ObjectPath<OWLClass> getPathToRoot(Project project, OWLClass subClass, OWLClass superClass) {
        Set<List<OWLClass>> paths = project.getClassHierarchyProvider().getPathsToRoot(subClass);
        if(paths.isEmpty()) {
            logger.info("[WARNING] Path to root not found for SubClass: %s and SuperClass: %", superClass);
            return new ObjectPath<>();
        }
        ObjectPath<OWLClass> pathToRoot = null;
        for(List<OWLClass> path : paths) {
            if(path.size() > 1 && path.get(path.size() - 2).equals(superClass)) {
                pathToRoot = new ObjectPath<>(path);
                break;
            }
        }
        if(pathToRoot == null) {
            pathToRoot = new ObjectPath<>();
        }
        return pathToRoot;
    }


}
