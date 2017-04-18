package edu.stanford.bmir.protege.web.server.frame;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractHasProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.server.shortform.EscapingShortFormProvider;
import edu.stanford.bmir.protege.web.shared.frame.GetManchesterSyntaxFrameAction;
import edu.stanford.bmir.protege.web.shared.frame.GetManchesterSyntaxFrameResult;
import org.semanticweb.owlapi.manchestersyntax.renderer.ManchesterOWLSyntaxFrameRenderer;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.inject.Inject;
import java.io.StringWriter;

/**
 * @author Matthew Horridge, Stanford University, Bio-Medical Informatics Research Group, Date: 18/03/2014
 */
public class GetManchesterSyntaxFrameActionHandler extends AbstractHasProjectActionHandler<GetManchesterSyntaxFrameAction, GetManchesterSyntaxFrameResult> {

    @Inject
    public GetManchesterSyntaxFrameActionHandler(ProjectManager projectManager, AccessManager accessManager) {
        super(projectManager, accessManager);
    }

    @Override
    protected GetManchesterSyntaxFrameResult execute(GetManchesterSyntaxFrameAction action, final Project project, ExecutionContext executionContext) {
        StringWriter writer = new StringWriter();
        final OWLOntology rootOntology = project.getRootOntology();
        EscapingShortFormProvider entityShortFormProvider = new EscapingShortFormProvider(project.getRenderingManager().getShortFormProvider());
        final ManchesterOWLSyntaxFrameRenderer frameRenderer = new ManchesterOWLSyntaxFrameRenderer(rootOntology.getImportsClosure(), writer, entityShortFormProvider);
        frameRenderer.setOntologyIRIShortFormProvider(project.getRenderingManager().getOntologyIRIShortFormProvider());
        frameRenderer.setRenderExtensions(true);
//        frameRenderer.setRenderOntologyLists(true);
//        frameRenderer.setUseTabbing(true);
//        frameRenderer.setUseWrapping(true);
        frameRenderer.writeFrame(action.getSubject());
//        frameRenderer.writeEntityNaryAxioms(action.getSubject());
//        frameRenderer.writeRulesContainingPredicate(action.getSubject());
        return new GetManchesterSyntaxFrameResult(writer.getBuffer().toString());
    }

    @Override
    public Class<GetManchesterSyntaxFrameAction> getActionClass() {
        return GetManchesterSyntaxFrameAction.class;
    }
}
