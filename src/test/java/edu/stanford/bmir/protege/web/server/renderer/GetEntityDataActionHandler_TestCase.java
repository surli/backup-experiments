package edu.stanford.bmir.protege.web.server.renderer;

import com.google.common.collect.ImmutableSet;
import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.entity.OWLEntityData;
import edu.stanford.bmir.protege.web.shared.renderer.GetEntityDataAction;
import edu.stanford.bmir.protege.web.shared.renderer.GetEntityDataResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.semanticweb.owlapi.model.OWLEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasEntry;
import static org.mockito.Mockito.when;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 22/02/15
 */
@RunWith(MockitoJUnitRunner.class)
public class GetEntityDataActionHandler_TestCase {

    private GetEntityDataActionHandler handler;

    @Mock
    private RenderingManager renderingManager;

    @Mock
    private Project project;

    private Map<OWLEntity, OWLEntityData> renderingMap;

    @Mock
    private OWLEntity entity;

    @Mock
    private OWLEntityData entityData;

    @Mock
    private GetEntityDataAction action;

    @Mock
    private ExecutionContext executionContext;

    @Mock
    private ProjectManager projectManager;

    @Mock
    private AccessManager accessManager;


    @Before
    public void setUp() throws Exception {
        handler = new GetEntityDataActionHandler(projectManager, accessManager);

        renderingMap = new HashMap<>();
        renderingMap.put(entity, entityData);
        when(project.getRenderingManager()).thenReturn(renderingManager);
        when(renderingManager.getRendering(Matchers.<Set<OWLEntity>>any())).thenReturn(renderingMap);

        when(action.getEntities()).thenReturn(ImmutableSet.of(entity));
    }

    @Test
    public void shouldGetRendering() {
        GetEntityDataResult result = handler.execute(action, project, executionContext);
        assertThat(result.getEntityDataMap(), hasEntry(entity, entityData));
    }
}
