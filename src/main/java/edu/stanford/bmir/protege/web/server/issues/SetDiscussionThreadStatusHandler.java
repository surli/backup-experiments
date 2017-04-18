package edu.stanford.bmir.protege.web.server.issues;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractHasProjectActionHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.event.ProjectEvent;
import edu.stanford.bmir.protege.web.shared.events.EventList;
import edu.stanford.bmir.protege.web.shared.events.EventTag;
import edu.stanford.bmir.protege.web.shared.issues.*;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import java.util.Optional;

import static edu.stanford.bmir.protege.web.shared.access.BuiltInAction.SET_OBJECT_COMMENT_STATUS;

/**
 * Matthew Horridge
 * Stanford Center for Biomedical Informatics Research
 * 12 Oct 2016
 */
public class SetDiscussionThreadStatusHandler extends AbstractHasProjectActionHandler<SetDiscussionThreadStatusAction, SetDiscussionThreadStatusResult> {

    @Nonnull
    private final EntityDiscussionThreadRepository repository;

    @Inject
    public SetDiscussionThreadStatusHandler(@Nonnull ProjectManager projectManager,
                                            @Nonnull AccessManager accessManager,
                                            @Nonnull EntityDiscussionThreadRepository repository) {
        super(projectManager, accessManager);
        this.repository = repository;
    }

    @Override
    public Class<SetDiscussionThreadStatusAction> getActionClass() {
        return SetDiscussionThreadStatusAction.class;
    }

    @Nullable
    @Override
    protected BuiltInAction getRequiredExecutableBuiltInAction() {
        return SET_OBJECT_COMMENT_STATUS;
    }

    @Override
    protected SetDiscussionThreadStatusResult execute(SetDiscussionThreadStatusAction action,
                                                      Project project,
                                                      ExecutionContext executionContext) {
        EventTag fromTag = project.getEventManager().getCurrentTag();
        ThreadId threadId = action.getThreadId();
        Status status = action.getStatus();
        Optional<EntityDiscussionThread> thread = repository.setThreadStatus(threadId, status);
        int openComments = thread.map(t -> repository.getOpenCommentsCount(project.getProjectId(), t.getEntity())).orElse(-1);
        ProjectId projectId = action.getProjectId();
        project.getEventManager().postEvent(new DiscussionThreadStatusChangedEvent(projectId,
                                                                                   threadId,
                                                                                   thread.map(EntityDiscussionThread::getEntity),
                                                                                   openComments,
                                                                                   status));
        EventList<ProjectEvent<?>> eventList = project.getEventManager().getEventsFromTag(fromTag);
        return new SetDiscussionThreadStatusResult(threadId, status, eventList);
    }
}
