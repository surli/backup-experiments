package edu.stanford.bmir.protege.web.client.primitive;

import com.google.common.base.Optional;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import edu.stanford.bmir.protege.web.client.library.common.EventStrategy;
import edu.stanford.bmir.protege.web.client.library.suggest.EntitySuggestion;
import edu.stanford.bmir.protege.web.client.place.*;
import edu.stanford.bmir.protege.web.shared.DataFactory;
import edu.stanford.bmir.protege.web.shared.DirtyChangedEvent;
import edu.stanford.bmir.protege.web.shared.DirtyChangedHandler;
import edu.stanford.bmir.protege.web.shared.PrimitiveType;
import edu.stanford.bmir.protege.web.shared.entity.*;
import edu.stanford.bmir.protege.web.shared.perspective.EntityTypePerspectiveMapper;
import edu.stanford.bmir.protege.web.shared.perspective.PerspectiveId;
import edu.stanford.bmir.protege.web.shared.place.ProjectViewPlace;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

import javax.inject.Inject;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static edu.stanford.bmir.protege.web.resources.WebProtegeClientBundle.BUNDLE;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 03/12/2012
 *
 * An view for {@link OWLPrimitiveData} objects.  The view supports auto-completion
 * and has an option to allow the creation of new primitives.
 *
 * This is really a presenter for the editor view.
 */
public class PrimitiveDataEditorImpl extends Composite implements PrimitiveDataEditor, HasEnabled {

    public static final double PLACEHOLDER_ICON_OPACITY = 0.3;

    private final PrimitiveDataEditorSuggestOracle entitySuggestOracle;

    private final LanguageEditor languageEditor;

    private final PrimitiveDataParser primitiveDataParser;

    private FreshEntitiesHandler freshEntitiesHandler;

    private Optional<OWLPrimitiveData> currentData = Optional.absent();

    private final Set<PrimitiveType> allowedTypes = Sets.newLinkedHashSet();

    private boolean dirty = false;

    private boolean enabled;

    private PrimitiveDataEditorView view;

    private String textPlaceholder = "";

    private Optional<OWLPrimitiveData> primitiveDataPlaceholder = Optional.absent();

    private java.util.Optional<EntitySuggestion> selectedSuggestion = java.util.Optional.empty();

    private final PlaceController placeController;

    private final EntityTypePerspectiveMapper typePerspectiveMapper = new EntityTypePerspectiveMapper();

    @Inject
    public PrimitiveDataEditorImpl(PrimitiveDataEditorView editorView,
                                   LanguageEditor languageEditor,
                                   PrimitiveDataEditorSuggestOracle suggestOracle,
                                   PrimitiveDataParser parser,
                                   FreshEntitiesHandler freshEntitiesHandler,
                                   PlaceController placeController) {
        this.languageEditor = languageEditor;
        this.freshEntitiesHandler = freshEntitiesHandler;
        this.primitiveDataParser = parser;
        entitySuggestOracle = suggestOracle;
        this.placeController = placeController;
        view = editorView;
        view.asWidget().addStyleName("web-protege-form-layout-editor-input");
        view.setMode(PrimitiveDataEditorView.Mode.SINGLE_LINE);
        view.setSuggestOracle(entitySuggestOracle);
        view.addSelectionHandler(event -> {
            EntitySuggestion suggestion = event.getSelectedItem();
            selectedSuggestion = java.util.Optional.of(suggestion);
            setCurrentData(Optional.of(suggestion.getEntity()), EventStrategy.FIRE_EVENTS);
        });
        view.addValueChangeHandler(event -> handleValueChanged());
        languageEditor.addValueChangeHandler(event -> handleLanguageChanged());
        view.setAnchorVisible(false);
        view.addAnchorClickedHandler(event -> handleAnchorClick());
        initWidget(view.asWidget());
    }

    @Override
    public void setPlaceholder(String placeholder) {
        this.textPlaceholder = placeholder;
        view.setPlaceholder(placeholder);
    }

    @Override
    public String getPlaceholder() {
        return view.getPlaceholder();
    }

    @Override
    public void setPrimitiveDataPlaceholder(Optional<OWLPrimitiveData> placeholder) {
        primitiveDataPlaceholder = placeholder;
        if(!primitiveDataPlaceholder.isPresent()) {
            view.setPlaceholder(textPlaceholder);
        }
        else {
            view.setPlaceholder(primitiveDataPlaceholder.get().getBrowserText());
            setIconInsetStyleNameForEntityData(primitiveDataPlaceholder, PLACEHOLDER_ICON_OPACITY);
        }
    }

    @Override
    public Optional<OWLPrimitiveData> getPrimitiveDataPlaceholder() {
        return primitiveDataPlaceholder;
    }

    @Override
    public void setFreshEntitiesSuggestStrategy(FreshEntitySuggestStrategy suggestStrategy) {
        entitySuggestOracle.setFreshEntityStrategy(suggestStrategy);
    }

    @Override
    public java.util.Optional<EntitySuggestion> getSelectedSuggestion() {
        return selectedSuggestion;
    }

    @Override
    public void setFreshEntitiesHandler(FreshEntitiesHandler handler) {
        checkNotNull(handler);
        this.freshEntitiesHandler = handler;
    }

    @Override
    public void setAutoSelectSuggestions(boolean autoSelectSuggestions) {
        this.view.setAutoSelectSuggestions(autoSelectSuggestions);
    }

    @Override
    public void setMode(PrimitiveDataEditorView.Mode mode) {
        checkNotNull(mode);
        view.setMode(mode);
    }

    @Override
    public void setEntityLinkMode(EntityLinkMode entityLinkMode) {
//        this.entityLinkMode = entityLinkMode;
    }

    @Override
    public void setShowLinksForEntities(boolean showLinksForEntities) {
        setEntityLinkMode(showLinksForEntities ? EntityLinkMode.SHOW_LINKS_FOR_ENTITIES : EntityLinkMode.DO_NOT_SHOW_LINKS_FOR_ENTITIES);
    }

    /**
     * Returns true if the widget is enabled, false if not.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this widget is enabled.
     *
     * @param enabled <code>true</code> to enable the widget, <code>false</code>
     *                to disable it
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        view.setEnabled(enabled);
        languageEditor.setEnabled(enabled);
    }

    @Override
    public LanguageEditor getLanguageEditor() {
        return languageEditor;
    }

    /**
     * Gets this object's text.
     *
     * @return the object's text
     */
//    @Override
    public String getText() {
        return getTrimmedText();
    }

    /**
     * Adds a {@link FocusEvent} handler.
     *
     * @param handler the focus handler
     * @return {@link HandlerRegistration} used to remove this handler
     */
    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return view.addFocusHandler(handler);
    }

    /**
     * Adds a {@link KeyUpEvent} handler.
     *
     * @param handler the key up handler
     * @return {@link HandlerRegistration} used to remove this handler
     */
    @Override
    public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
        return view.addKeyUpHandler(handler);
    }

    @Override
    public boolean isAnnotationPropertiesAllowed() {
        return allowedTypes.contains(PrimitiveType.ANNOTATION_PROPERTY);
    }

    @Override
    public void setAnnotationPropertiesAllowed(boolean annotationPropertiesAllowed) {
        setAllowedType(PrimitiveType.ANNOTATION_PROPERTY, annotationPropertiesAllowed);
    }

    @Override
    public boolean isDataPropertiesAllowed() {
        return allowedTypes.contains(PrimitiveType.DATA_PROPERTY);
    }

    @Override
    public void setDataPropertiesAllowed(boolean dataPropertiesAllowed) {
        setAllowedType(PrimitiveType.DATA_PROPERTY, dataPropertiesAllowed);
    }

    @Override
    public boolean isObjectPropertiesAllowed() {
        return allowedTypes.contains(PrimitiveType.OBJECT_PROPERTY);
    }

    @Override
    public void setObjectPropertiesAllowed(boolean objectPropertiesAllowed) {
        setAllowedType(PrimitiveType.OBJECT_PROPERTY, objectPropertiesAllowed);
    }

    @Override
    public boolean isClassesAllowed() {
        return allowedTypes.contains(PrimitiveType.CLASS);
    }

    @Override
    public void setClassesAllowed(boolean classesAllowed) {
        setAllowedType(PrimitiveType.CLASS, classesAllowed);
    }

    @Override
    public boolean isDatatypesAllowed() {
        return allowedTypes.contains(PrimitiveType.DATA_TYPE);
    }

    @Override
    public void setDatatypesAllowed(boolean datatypesAllowed) {
        setAllowedType(PrimitiveType.DATA_TYPE, datatypesAllowed);
    }

    @Override
    public boolean isNamedIndividualsAllowed() {
        return allowedTypes.contains(PrimitiveType.NAMED_INDIVIDUAL);
    }

    @Override
    public void setNamedIndividualsAllowed(boolean namedIndividualsAllowed) {
        setAllowedType(PrimitiveType.NAMED_INDIVIDUAL, namedIndividualsAllowed);
    }

    @Override
    public boolean isLiteralAllowed() {
        return allowedTypes.contains(PrimitiveType.LITERAL);
    }

    @Override
    public void setLiteralAllowed(boolean literalAllowed) {
        setAllowedType(PrimitiveType.LITERAL, literalAllowed);
    }

    @Override
    public boolean isIRIAllowed() {
        return allowedTypes.contains(PrimitiveType.IRI);
    }

    @Override
    public void setIRIAllowed(boolean iriAllowed) {
        setAllowedType(PrimitiveType.IRI, iriAllowed);
    }

    @Override
    public void setAllowedType(PrimitiveType type, boolean allowed) {
        boolean revalidate;
        if (allowed) {
            revalidate = allowedTypes.add(type);
        } else {
            revalidate = allowedTypes.remove(type);
        }
        if (revalidate) {
            if (type.getEntityType() != null) {
                updateOracle();
            }
            validateCurrentEntityTypeAgainstAllowedTypes();
        }
    }

    @Override
    public void coerceToEntityType(EntityType<?> entityType) {
        if(currentData.isPresent() && entityType.equals(currentData.get().getType().getEntityType())) {
            return;
        }
        String text = getTrimmedText();
        OWLEntity entity = freshEntitiesHandler.getFreshEntity(text, entityType);
        OWLPrimitiveData coercedData = DataFactory.getOWLEntityData(entity, text);
        setCurrentData(Optional.of(coercedData), EventStrategy.FIRE_EVENTS);
        updateDisplayForCurrentData();
    }

    /**
     * Adds a {@link ValueChangeEvent} handler.
     *
     * @param handler the handler
     * @return the registration for the event
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Optional<OWLPrimitiveData>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public Optional<OWLPrimitiveData> getValue() {
        return currentData;
    }

    @Override
    public void setValue(OWLPrimitiveData object) {
        checkNotNull(object);
        dirty = false;
        setCurrentData(Optional.of(object), EventStrategy.DO_NOT_FIRE_EVENTS);
    }

    @Override
    public void clearValue() {
        view.setText("");
        view.setPrimitiveDataStyleName(Optional.absent());
        languageEditor.setValue("");
        dirty = false;
        setCurrentData(Optional.absent(), EventStrategy.DO_NOT_FIRE_EVENTS);
    }

    @Override
    public boolean isWellFormed() {
        return getTrimmedText().isEmpty() || currentData.isPresent();
    }

    /**
     * Determines if this object is dirty.
     *
     * @return {@code true} if the object is dirty, otherwise {@code false}.
     */
    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void setAllowedTypes(Collection<PrimitiveType> primitiveTypes) {
        if (primitiveTypes.equals(this.allowedTypes)) {
            return;
        }
        this.allowedTypes.clear();
        this.allowedTypes.addAll(primitiveTypes);
        if (!allowedTypes.contains(PrimitiveType.LITERAL)) {
            setMode(PrimitiveDataEditorView.Mode.SINGLE_LINE);
        } else {
            setMode(PrimitiveDataEditorView.Mode.MULTI_LINE);
        }
        updateOracle();
        if (!isCurrentEntityTypeAllowed()) {
            reparsePrimitiveData();
        }
    }

    @Override
    public HandlerRegistration addDirtyChangedHandler(DirtyChangedHandler handler) {
        return addHandler(handler, DirtyChangedEvent.TYPE);
    }

    private void handleValueChanged() {
        GWT.log("[PrimitiveDataEditorImpl] Handling changed value");
        reparsePrimitiveData();
        dirty = true;
    }

    private void handleAnchorClick() {
        if (!currentData.isPresent()) {
            return;
        }
        currentData.get().accept(new OWLPrimitiveDataVisitorAdapter<Void, RuntimeException>() {
            @Override
            public Void visit(IRIData data) throws RuntimeException {
                Window.open(data.getObject().toString(), data.getBrowserText(), "");
                return null;
            }

            @Override
            public Void visit(OWLClassData data) throws RuntimeException {
                navigateTo(new OWLClassItem(data.getEntity()), data);
                return null;
            }

            private void navigateTo(Item item, OWLEntityData entityData) {
                PerspectiveId perspectiveId = typePerspectiveMapper
                        .getPerspectiveId(entityData.getEntity().getEntityType());

                Place place = placeController.getWhere();
                if(place instanceof ProjectViewPlace) {
                    ProjectViewPlace nextPlace = ((ProjectViewPlace) place).builder()
                                                                           .withPerspectiveId(perspectiveId)
                                                                           .clearSelection()
                                                                           .withSelectedItem(item)
                                                                           .build();
                    placeController.goTo(nextPlace);
                }
            }

            @Override
            public Void visit(OWLObjectPropertyData data) throws RuntimeException {
                navigateTo(new OWLObjectPropertyItem(data.getEntity()), data);
                return null;
            }

            @Override
            public Void visit(OWLDataPropertyData data) throws RuntimeException {
                navigateTo(new OWLDataPropertyItem(data.getEntity()), data);
                return null;
            }

            @Override
            public Void visit(OWLAnnotationPropertyData data) throws RuntimeException {
                navigateTo(new OWLAnnotationPropertyItem(data.getEntity()), data);
                return null;
            }

            @Override
            public Void visit(OWLNamedIndividualData data) throws RuntimeException {
                navigateTo(new OWLNamedIndividualItem(data.getEntity()), data);
                return null;
            }
        });
    }

    private void showErrorLabel() {
        String errorMessage = freshEntitiesHandler.getErrorMessage(view.getText());
        SafeHtml errorMessageHTML = new SafeHtmlBuilder().appendHtmlConstant(errorMessage).toSafeHtml();
        Set<EntityType<?>> suggestTypes;
        if(freshEntitiesHandler.getFreshEntitiesPolicy() == FreshEntitiesPolicy.ALLOWED) {
            suggestTypes = getMatchTypes();
        }
        else {
            suggestTypes = Collections.emptySet();
        }
        view.showErrorMessage(errorMessageHTML, suggestTypes);
    }

    private void hideErrorLabel() {
        view.clearErrorMessage();
    }

    private void reparsePrimitiveData() {
        if (isCurrentDataRendered()) {
            return;
        }
        final String trimmedText = getTrimmedText();
        primitiveDataParser.parsePrimitiveData(trimmedText, languageEditor.getValue(), allowedTypes, new PrimitiveDataParserCallback() {
            @Override
            public void parsingFailure() {
                setCurrentData(Optional.absent(), EventStrategy.FIRE_EVENTS);
                showErrorLabel();
            }

            @Override
            public void onSuccess(Optional<OWLPrimitiveData> result) {
                hideErrorLabel();
                setCurrentData(result, EventStrategy.FIRE_EVENTS);
            }
        });
    }

    private String getTrimmedText() {
        return view.getText().trim();
    }

    private boolean isCurrentDataRendered() {
        if (!currentData.isPresent()) {
            return getTrimmedText().isEmpty() && !languageEditor.getValue().isPresent();
        }
        OWLPrimitiveData data = currentData.get();
        String currentBrowserText = data.getBrowserText();
        if (!currentBrowserText.equals(getTrimmedText())) {
            return false;
        }
        if (!isCurrentEntityTypeAllowed()) {
            return false;
        }
        if (data instanceof OWLLiteralData) {
            final OWLLiteral literal = ((OWLLiteralData) data).getLiteral();
            Optional<String> lang;
            if (literal.hasLang()) {
                lang = Optional.of(literal.getLang());
            } else {
                lang = Optional.absent();
            }
            if (!lang.equals(languageEditor.getValue())) {
                return false;
            }
        }
        return true;
    }

    private void updateDisplayForCurrentData() {
        if(currentData.isPresent()) {
            setIconInsetStyleNameForEntityData(currentData, 1.0);
            validateCurrentEntityTypeAgainstAllowedTypes();
            if (isExternalIRI()) {
                view.setAnchorTitle("Open link in new window");
                view.setAnchorVisible(true);
            } else {
                OWLPrimitiveData primitiveData = currentData.get();
                if(primitiveData.isOWLEntity()) {
                    view.setAnchorTitle("Go to entity");
                    view.setAnchorVisible(true);
                }
                else {
                    view.setAnchorVisible(false);
                }
            }
        }
        else {
            setIconInsetStyleNameForEntityData(primitiveDataPlaceholder, PLACEHOLDER_ICON_OPACITY);
        }

    }

    private boolean isExternalIRI() {
        if (!currentData.isPresent()) {
            return false;
        }
        OWLPrimitiveData data = currentData.get();
        if (!(data instanceof IRIData)) {
            return false;
        }
        IRI iri = (IRI) data.getObject();
        if (!iri.isAbsolute()) {
            return false;
        }
        return "http".equalsIgnoreCase(iri.getScheme());
    }

    private void setIconInsetStyleName(Optional<String> name) {
        view.setPrimitiveDataStyleName(name);
    }
    ////////////////////////////////////////////////////////////////////////////////////////



    private void setIconInsetStyleNameForEntityData(final Optional<OWLPrimitiveData> data, double iconOpacity) {
        if (!data.isPresent()) {
            view.setTitle("");
            hideErrorLabel();
            setIconInsetStyleName(Optional.of("empty-icon-inset"));
            return;
        }
        final OWLPrimitiveData entityData = data.get();
        String styleName = entityData.accept(new OWLPrimitiveDataVisitorAdapter<String, RuntimeException>() {
            @Override
            protected String getDefaultReturnValue() {
                view.setTitle("");
                return "empty-icon-inset";
            }

            @Override
            public String visit(OWLClassData data) throws RuntimeException {
                setTooltip(data, "owl:Class");
                return BUNDLE.style().classIconInset();
            }

            @Override
            public String visit(OWLObjectPropertyData data) throws RuntimeException {
                setTooltip(data, "owl:ObjectProperty");
                return BUNDLE.style().objectPropertyIconInset();
            }

            @Override
            public String visit(OWLDataPropertyData data) throws RuntimeException {
                setTooltip(data, "owl:DataProperty");
                return BUNDLE.style().dataPropertyIconInset();
            }

            @Override
            public String visit(OWLAnnotationPropertyData data) throws RuntimeException {
                setTooltip(data, "owl:AnnotationProperty");
                return BUNDLE.style().annotationPropertyIconInset();
            }

            @Override
            public String visit(OWLNamedIndividualData data) throws RuntimeException {
                setTooltip(data, "owl:NamedIndividual");
                return BUNDLE.style().individualIconInset();
            }

            @Override
            public String visit(OWLDatatypeData data) throws RuntimeException {
                setTooltip(data, "owl:Datatype");
                return BUNDLE.style().datatypeIconInset();
            }

            private void setTooltip(OWLEntityData data, String typeName) {
                StringBuilder sb = new StringBuilder();
                sb.append("\"");
                sb.append(entityData.getBrowserText());
                sb.append("\" is an ");
                sb.append(typeName);
                if (!DataFactory.isFreshEntity(data.getEntity())) {
                    sb.append("\n");
                    sb.append("<");
                    IRI iri = data.getEntity().getIRI();
                    sb.append(iri);
                    sb.append(">");
                }
                view.setTitle(sb.toString());
            }

            @Override
            public String visit(OWLLiteralData data) throws RuntimeException {
                String styleName = BUNDLE.style().literalIconInset();
                OWLDatatype datatype = data.getLiteral().getDatatype();
                if (datatype.isBuiltIn()) {
                    OWL2Datatype owl2Datatype = datatype.getBuiltInDatatype();
                    if (owl2Datatype.isNumeric()) {
                        styleName = BUNDLE.style().numberIconInset();
                    } else if (owl2Datatype.equals(OWL2Datatype.XSD_DATE_TIME)) {
                        styleName = BUNDLE.style().dateTimeIconInset();
                    }
                }
                String datatypeName = datatype.getIRI().getFragment();
                if (datatypeName == null) {
                    datatypeName = datatype.getIRI().toString();
                }
                StringBuilder tooltip = new StringBuilder();
                tooltip.append(entityData.getBrowserText());
                char c = datatypeName.charAt(0);
                if (c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u') {
                    tooltip.append(" is an ");
                } else {
                    tooltip.append(" is a ");
                }
                tooltip.append(datatypeName);
                view.setTitle(tooltip.toString());
                return styleName;
            }

            @Override
            public String visit(IRIData data) throws RuntimeException {
                if (data.isHTTPLink()) {
                    return BUNDLE.style().linkIconInset();
                } else {
                    return BUNDLE.style().iriIconInset();
                }
            }
        });
        setIconInsetStyleName(Optional.of(styleName));
    }

    /**
     * Checks that the current entity type is one of the allowed types.
     */
    private void validateCurrentEntityTypeAgainstAllowedTypes() {
        if (!currentData.isPresent()) {
            hideErrorLabel();
            // Allowed to be empty
            return;
        }
        if (isCurrentEntityTypeAllowed()) {
            hideErrorLabel();
            return;
        }
        showErrorLabel();
    }

    private boolean isCurrentEntityTypeAllowed() {
        return !currentData.isPresent() || allowedTypes.contains(currentData.get().getType());
    }

    private void handleLanguageChanged() {
        reparsePrimitiveData();
    }

    private void updateOracle() {
        entitySuggestOracle.setAllowedPrimitiveTypes(allowedTypes);
    }

    private Set<EntityType<?>> getMatchTypes() {
        Set<EntityType<?>> types = new LinkedHashSet<EntityType<?>>();
        for (PrimitiveType primitiveType : allowedTypes) {
            EntityType<?> entityType = primitiveType.getEntityType();
            if (entityType != null) {
                types.add(entityType);
            }
        }
        return types;
    }

    private void fireValueChangedEvent() {
        ValueChangeEvent.fire(this, getValue());
    }

    private void setCurrentData(Optional<OWLPrimitiveData> nextCurrentData, EventStrategy eventStrategy) {
        checkNotNull(nextCurrentData);
//        dirty = false;
        if (currentData.equals(nextCurrentData)) {
            return;
        }
        currentData = nextCurrentData;
        if (nextCurrentData.isPresent()) {
            OWLPrimitiveData data = nextCurrentData.get();
            view.setText(data.getBrowserText());
            if (data instanceof OWLLiteralData) {
                String lang = ((OWLLiteralData) data).getLiteral().getLang();
                languageEditor.setValue(lang);
            }
            if(selectedSuggestion.isPresent()) {
                if(!selectedSuggestion.get().getReplacementString().equals(data.getBrowserText())) {
                    selectedSuggestion = java.util.Optional.empty();
                }
            }
        }
        else {
            selectedSuggestion = java.util.Optional.empty();
        }
        updateDisplayForCurrentData();
        if (eventStrategy == EventStrategy.FIRE_EVENTS) {
            fireValueChangedEvent();
        }
    }
}
