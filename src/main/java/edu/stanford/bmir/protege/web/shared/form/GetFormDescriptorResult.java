package edu.stanford.bmir.protege.web.shared.form;

import edu.stanford.bmir.protege.web.shared.dispatch.Result;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import org.semanticweb.owlapi.model.OWLEntity;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 30/03/16
 */
public class GetFormDescriptorResult implements Result {

    private ProjectId projectId;

    private OWLEntity entity;

    private FormDescriptor formDescriptor;

    private FormData formData;

    private GetFormDescriptorResult() {
    }

    public GetFormDescriptorResult(ProjectId projectId,
                                   OWLEntity entity,
                                   FormDescriptor formDescriptor,
                                   FormData formData) {
        this.projectId = projectId;
        this.entity = entity;
        this.formDescriptor = formDescriptor;
        this.formData = formData;
    }

    public FormData getFormData() {
        return formData;
    }

    public ProjectId getProjectId() {
        return projectId;
    }

    public OWLEntity getEntity() {
        return entity;
    }

    public FormDescriptor getFormDescriptor() {
        return formDescriptor;
    }
}
