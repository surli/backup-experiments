package edu.stanford.bmir.protege.web.server.frame;

import edu.stanford.bmir.protege.web.client.frame.LabelledFrame;
import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.dispatch.Result;
import edu.stanford.bmir.protege.web.shared.dispatch.UpdateObjectResult;
import edu.stanford.bmir.protege.web.shared.entity.OWLDataPropertyData;
import edu.stanford.bmir.protege.web.shared.event.ProjectEvent;
import edu.stanford.bmir.protege.web.shared.events.EventList;
import edu.stanford.bmir.protege.web.shared.frame.DataPropertyFrame;
import edu.stanford.bmir.protege.web.shared.frame.UpdateDataPropertyFrameAction;
import org.semanticweb.owlapi.model.OWLDataProperty;

import javax.inject.Inject;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 23/04/2013
 */
public class UpdateDataPropertyFrameHandler extends AbstractUpdateFrameHandler<UpdateDataPropertyFrameAction, DataPropertyFrame, OWLDataPropertyData> {

    @Inject
    public UpdateDataPropertyFrameHandler(ProjectManager projectManager,
                                          AccessManager accessManager) {
        super(projectManager, accessManager);
    }

    @Override
    protected Result createResponse(LabelledFrame<DataPropertyFrame> to, EventList<ProjectEvent<?>> events) {
        return new UpdateObjectResult(events);
    }

    @Override
    protected FrameTranslator<DataPropertyFrame, OWLDataPropertyData> createTranslator() {
        return new DataPropertyFrameTranslator();
    }

    @Override
    protected String getChangeDescription(LabelledFrame<DataPropertyFrame> from, LabelledFrame<DataPropertyFrame> to) {
        return "Edited data property";
    }

    @Override
    public Class<UpdateDataPropertyFrameAction> getActionClass() {
        return UpdateDataPropertyFrameAction.class;
    }
}
