package edu.stanford.bmir.protege.web.client.dispatch.actions;

import edu.stanford.bmir.protege.web.shared.HasProjectId;
import edu.stanford.bmir.protege.web.shared.HasSubject;
import edu.stanford.bmir.protege.web.shared.dispatch.Action;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import org.semanticweb.owlapi.model.OWLNamedIndividual;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 20/02/2013
 */
public class GetNamedIndividualFrameAction implements Action<GetNamedIndividualFrameResult>, HasProjectId, HasSubject<OWLNamedIndividual> {

    private ProjectId projectId;

    private OWLNamedIndividual subject;

    /**
     * For serialization purposes only
     */
    private GetNamedIndividualFrameAction() {
        super();
    }


    public GetNamedIndividualFrameAction(ProjectId projectId, OWLNamedIndividual subject) {
        this.projectId = projectId;
        this.subject = subject;
    }

    /**
     * Get the {@link edu.stanford.bmir.protege.web.shared.project.ProjectId}.
     *
     * @return The {@link edu.stanford.bmir.protege.web.shared.project.ProjectId}.  Not {@code null}.
     */
    @Override
    public ProjectId getProjectId() {
        return projectId;
    }

    /**
     * Gets the subject of this object.
     *
     * @return The subject.  Not {@code null}.
     */
    @Override
    public OWLNamedIndividual getSubject() {
        return subject;
    }

    @Override
    public int hashCode() {
        return "GetNamedIndividualFrameAction".hashCode() + super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof GetNamedIndividualFrameAction)) {
            return false;
        }
        GetNamedIndividualFrameAction other = (GetNamedIndividualFrameAction) obj;
        return super.equals(other);
    }
}
