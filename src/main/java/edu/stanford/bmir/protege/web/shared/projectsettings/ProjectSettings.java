package edu.stanford.bmir.protege.web.shared.projectsettings;

import com.google.common.base.Objects;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

import java.io.Serializable;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 09/07/2012
 */
public class ProjectSettings implements Serializable {

    private ProjectId projectId;

    private String projectDisplayName;
    
    private String projectDescription;


    /**
     * For serialization purposes only
     */
    private ProjectSettings() {}

    /**
     * Constructs a ProjectSettingsData object.
     * @param projectId The projectId.  Not {@code null}.
     * @param projectDescription The project description. Not {@code null}.
     * @throws java.lang.NullPointerException if any parameters are {@code null}.
     */
    public ProjectSettings(ProjectId projectId, String projectDisplayName, String projectDescription) {
        this.projectId = checkNotNull(projectId);
        this.projectDisplayName = checkNotNull(projectDisplayName);
        this.projectDescription = checkNotNull(projectDescription);
    }

    /**
     * Gets the projectId.
     * @return The projectId.  Not {@code null}.
     */
    public ProjectId getProjectId() {
        return projectId;
    }

    /**
     * Gets the project display name.
     * @return The project display name.  Not {@code null}.
     */
    public String getProjectDisplayName() {
        return projectDisplayName;
    }

    /**
     * Gets the project description.
     * @return The project description as a string.  May be empty. Not {@code null}.
     */
    public String getProjectDescription() {
        return projectDescription;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(projectId, projectDisplayName, projectDescription);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof ProjectSettings)) {
            return false;
        }
        ProjectSettings other = (ProjectSettings) obj;
        return this.projectDisplayName.equals(other.projectDisplayName)
                && this.projectDescription.equals(other.projectDescription)
                && this.projectId.equals(other.projectId);
    }


    @Override
    public String toString() {
        return Objects.toStringHelper("ProjectSettings")
                .addValue(projectId)
                .add("displayName", projectDisplayName)
                .add("description", projectDescription)
                .toString();
    }
}
