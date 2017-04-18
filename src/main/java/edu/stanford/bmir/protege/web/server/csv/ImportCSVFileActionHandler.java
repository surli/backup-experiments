package edu.stanford.bmir.protege.web.server.csv;

import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.change.ChangeApplicationResult;
import edu.stanford.bmir.protege.web.server.change.ChangeDescriptionGenerator;
import edu.stanford.bmir.protege.web.server.change.ChangeListGenerator;
import edu.stanford.bmir.protege.web.server.change.FixedMessageChangeDescriptionGenerator;
import edu.stanford.bmir.protege.web.server.dispatch.AbstractProjectChangeHandler;
import edu.stanford.bmir.protege.web.server.dispatch.ExecutionContext;
import edu.stanford.bmir.protege.web.server.inject.UploadsDirectory;
import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.project.ProjectManager;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.csv.CSVGrid;
import edu.stanford.bmir.protege.web.shared.csv.ImportCSVFileAction;
import edu.stanford.bmir.protege.web.shared.csv.ImportCSVFileResult;
import edu.stanford.bmir.protege.web.shared.event.ProjectEvent;
import edu.stanford.bmir.protege.web.shared.events.EventList;
import edu.stanford.bmir.protege.web.shared.events.EventTag;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.shared.access.BuiltInAction.EDIT_ONTOLOGY;
import static java.util.Arrays.asList;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 31/05/2013
 */
public class ImportCSVFileActionHandler extends AbstractProjectChangeHandler<Integer,ImportCSVFileAction, ImportCSVFileResult> {

    private final File uploadsDirectory;

    @Inject
    public ImportCSVFileActionHandler(@UploadsDirectory File uploadsDirectory,
                                      ProjectManager projectManager,
                                      AccessManager accessManager) {
        super(projectManager, accessManager);
        this.uploadsDirectory = checkNotNull(uploadsDirectory);
    }

    @Override
    public Class<ImportCSVFileAction> getActionClass() {
        return ImportCSVFileAction.class;
    }

    @Override
    protected ChangeListGenerator<Integer> getChangeListGenerator(ImportCSVFileAction action, Project project, ExecutionContext executionContext) {
        CSVGrid csvGrid = parseCSVGrid(action);
        return new ImportCSVFileChangeListGenerator(action.getImportRootClass(), csvGrid, action.getDescriptor());
    }

    private CSVGrid parseCSVGrid(ImportCSVFileAction action) {
        try {
            CSVGridParser parser = new CSVGridParser();
            final File file = new File(uploadsDirectory, action.getDocumentId().getDocumentId());
            final BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "utf-8"));
            CSVGrid grid = parser.readAll(reader);
            reader.close();
            return grid;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ChangeDescriptionGenerator<Integer> getChangeDescription(ImportCSVFileAction action, Project project, ExecutionContext executionContext) {
        return new FixedMessageChangeDescriptionGenerator<>("Imported CSV File");
    }

    @Override
    protected ImportCSVFileResult createActionResult(ChangeApplicationResult<Integer> changeApplicationResult, ImportCSVFileAction action, Project project, ExecutionContext executionContext, EventList<ProjectEvent<?>> eventList) {
        return new ImportCSVFileResult(new EventList<ProjectEvent<?>>(EventTag.get(0), EventTag.get(1)), changeApplicationResult.getSubject().get());
    }

    @Nonnull
    @Override
    protected Iterable<BuiltInAction> getRequiredExecutableBuiltInActions() {
        return asList(EDIT_ONTOLOGY);
    }
}
