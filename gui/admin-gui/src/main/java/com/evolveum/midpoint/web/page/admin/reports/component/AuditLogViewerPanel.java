package com.evolveum.midpoint.web.page.admin.reports.component;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.component.path.ItemPathDto;
import com.evolveum.midpoint.gui.api.component.path.ItemPathPanel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.prism.path.CanonicalItemPath;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ObjectTypeUtil;
import com.evolveum.midpoint.util.QNameUtil;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.component.data.BoxedTablePanel;
import com.evolveum.midpoint.web.component.data.column.LinkColumn;
import com.evolveum.midpoint.web.component.form.ValueChooseWrapperPanel;
import com.evolveum.midpoint.web.component.input.DatePanel;
import com.evolveum.midpoint.web.component.input.DropDownChoicePanel;
import com.evolveum.midpoint.web.component.input.QNameChoiceRenderer;
import com.evolveum.midpoint.web.component.input.TextPanel;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.admin.configuration.component.EmptyOnBlurAjaxFormUpdatingBehaviour;
import com.evolveum.midpoint.web.page.admin.configuration.component.EmptyOnChangeAjaxFormUpdatingBehavior;
import com.evolveum.midpoint.web.page.admin.reports.PageAuditLogDetails;
import com.evolveum.midpoint.web.page.admin.reports.dto.AuditEventRecordProvider;
import com.evolveum.midpoint.web.page.admin.reports.dto.AuditSearchDto;
import com.evolveum.midpoint.web.page.admin.users.PageUser;
import com.evolveum.midpoint.web.session.AuditLogStorage;
import com.evolveum.midpoint.web.session.UserProfileStorage;
import com.evolveum.midpoint.web.util.DateValidator;
import com.evolveum.midpoint.xml.ns._public.common.audit_3.AuditEventRecordType;
import com.evolveum.midpoint.xml.ns._public.common.audit_3.AuditEventStageType;
import com.evolveum.midpoint.xml.ns._public.common.audit_3.AuditEventTypeType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectReferenceType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.yui.calendar.DateTimeField;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by honchar.
 */
public class AuditLogViewerPanel extends BasePanel{
    private static final long serialVersionUID = 1L;
    private static final String ID_PARAMETERS_PANEL = "parametersPanel";
    private static final String ID_TABLE = "table";
    private static final String ID_FROM = "fromField";
    private static final String ID_TO = "toField";
    private static final String ID_INITIATOR_NAME = "initiatorNameField";
    private static final String ID_TARGET_NAME_FIELD = "targetNameField";
    private static final String ID_TARGET_NAME = "targetName";
    private static final String ID_TARGET_OWNER_NAME = "targetOwnerName";
    private static final String ID_TARGET_OWNER_NAME_FIELD = "targetOwnerNameField";
    private static final String ID_CHANNEL = "channelField";
    private static final String ID_HOST_IDENTIFIER = "hostIdentifierField";
    private static final String ID_EVENT_TYPE = "eventTypeField";
    private static final String ID_EVENT_STAGE_FIELD = "eventStageField";
    private static final String ID_EVENT_STAGE = "eventStage";
    private static final String ID_OUTCOME = "outcomeField";
    private static final String ID_CHANGED_ITEM = "changedItem";

    private static final String ID_MAIN_FORM = "mainForm";
    private static final String ID_SEARCH_BUTTON = "searchButton";
    private static final String ID_FEEDBACK = "feedback";

    public static final String TARGET_NAME_LABEL_VISIBILITY = "targetNameLabel";
    public static final String TARGET_NAME_FIELD_VISIBILITY = "targetFieldField";
    public static final String TARGET_OWNER_LABEL_VISIBILITY = "targetOwnerLabel";
    public static final String TARGET_OWNER_FIELD_VISIBILITY = "targetOwnerField";
    public static final String TARGET_COLUMN_VISIBILITY = "targetColumn";
    public static final String TARGET_OWNER_COLUMN_VISIBILITY = "targetOwnerColumn";
    public static final String EVENT_STAGE_COLUMN_VISIBILITY = "eventStageColumn";
    public static final String EVENT_STAGE_LABEL_VISIBILITY = "eventStageLabel";
    public static final String EVENT_STAGE_FIELD_VISIBILITY = "eventStageField";

    private static final String OPERATION_RESOLVE_REFENRENCE_NAME = AuditLogViewerPanel.class.getSimpleName()
            + ".resolveReferenceName()";

    private IModel<AuditSearchDto> auditSearchDto;
    private AuditSearchDto searchDto;
    private PageBase pageBase;
    private Map<String, Boolean> visibilityMap;
    private AuditLogStorage auditLogStorage;

    public AuditLogViewerPanel(String id, PageBase pageBase){
        this(id, pageBase, null);
    }

    public AuditLogViewerPanel(String id, PageBase pageBase, AuditSearchDto searchDto) {
        this(id, pageBase, searchDto, new HashMap<String, Boolean>());
    }

    public AuditLogViewerPanel(String id, PageBase pageBase, AuditSearchDto searchDto, Map<String, Boolean> visibilityMap){
        super(id);
        this.pageBase = pageBase;
        this.searchDto = searchDto;
        this.visibilityMap = visibilityMap;
        initAuditSearchModel();
        initLayout();
    }

    private void initAuditSearchModel(){
        if (pageBase instanceof PageUser){
            auditLogStorage = pageBase.getSessionStorage().getUserHistoryAuditLog();
        } else {
            auditLogStorage = pageBase.getSessionStorage().getAuditLog();
        }
        if (searchDto == null){
            searchDto = auditLogStorage.getSearchDto();
        }
        auditSearchDto = new Model<AuditSearchDto>(searchDto);

    }

    private void initLayout() {

        Form mainForm = new Form(ID_MAIN_FORM);
        mainForm.setOutputMarkupId(true);
        add(mainForm);

        FeedbackPanel feedback = new FeedbackPanel(ID_FEEDBACK);
        feedback.setOutputMarkupId(true);
        mainForm.add(feedback);

        initParametersPanel(mainForm);
        addOrReplaceTable(mainForm);
    }

    private void initParametersPanel(Form mainForm) {
        WebMarkupContainer parametersPanel = new WebMarkupContainer(ID_PARAMETERS_PANEL);
        parametersPanel.setOutputMarkupId(true);
        mainForm.add(parametersPanel);

        PropertyModel<XMLGregorianCalendar> fromModel = new PropertyModel<XMLGregorianCalendar>(
                auditSearchDto, AuditSearchDto.F_FROM);

        DatePanel from = new DatePanel(ID_FROM, fromModel);
        DateValidator dateFromValidator = WebComponentUtil.getRangeValidator(mainForm,
                new ItemPath(AuditSearchDto.F_FROM));
        dateFromValidator.setMessageKey("AuditLogViewerPanel.dateValidatorMessage");
        dateFromValidator.setDateFrom((DateTimeField) from.getBaseFormComponent());
        for (FormComponent<?> formComponent : from.getFormComponents()) {
            formComponent.add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        }

        from.setOutputMarkupId(true);
        parametersPanel.add(from);

        PropertyModel<XMLGregorianCalendar> toModel = new PropertyModel<XMLGregorianCalendar>(auditSearchDto,
                AuditSearchDto.F_TO);
        DatePanel to = new DatePanel(ID_TO, toModel);
        DateValidator dateToValidator = WebComponentUtil.getRangeValidator(mainForm,
                new ItemPath(AuditSearchDto.F_FROM));
        dateToValidator.setMessageKey("AuditLogViewerPanel.dateValidatorMessage");
        dateToValidator.setDateTo((DateTimeField) to.getBaseFormComponent());
        for (FormComponent<?> formComponent : to.getFormComponents()) {
            formComponent.add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        }

        to.setOutputMarkupId(true);
        parametersPanel.add(to);

        PropertyModel<ItemPathDto> changedItemModel = new PropertyModel<ItemPathDto>(auditSearchDto,
                AuditSearchDto.F_CHANGED_ITEM);
        
        ItemPathPanel changedItemPanel = new ItemPathPanel(ID_CHANGED_ITEM, changedItemModel, pageBase);
//        changedItemPanel.getBaseFormComponent().add(new EmptyOnChangeAjaxFormUpdatingBehavior());
//        changedItemPanel.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        changedItemPanel.setOutputMarkupId(true);
        parametersPanel.add(changedItemPanel);

        PropertyModel<String> hostIdentifierModel = new PropertyModel<String>(auditSearchDto,
                AuditSearchDto.F_HOST_IDENTIFIER);
        TextPanel<String> hostIdentifier = new TextPanel<String>(ID_HOST_IDENTIFIER, hostIdentifierModel);
        hostIdentifier.getBaseFormComponent().add(new EmptyOnChangeAjaxFormUpdatingBehavior());
        hostIdentifier.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        hostIdentifier.setOutputMarkupId(true);
        parametersPanel.add(hostIdentifier);

        ListModel<AuditEventTypeType> eventTypeListModel = new ListModel<AuditEventTypeType>(
                Arrays.asList(AuditEventTypeType.values()));
        PropertyModel<AuditEventTypeType> eventTypeModel = new PropertyModel<AuditEventTypeType>(
                auditSearchDto, AuditSearchDto.F_EVENT_TYPE);
        DropDownChoicePanel<AuditEventTypeType> eventType = new DropDownChoicePanel<AuditEventTypeType>(
                ID_EVENT_TYPE, eventTypeModel, eventTypeListModel,
                new EnumChoiceRenderer<AuditEventTypeType>(), true);
        eventType.getBaseFormComponent().add(new EmptyOnChangeAjaxFormUpdatingBehavior());
        eventType.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        eventType.setOutputMarkupId(true);
        parametersPanel.add(eventType);

        WebMarkupContainer eventStage = new WebMarkupContainer(ID_EVENT_STAGE);
        eventStage.add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;
            @Override
	        public boolean isVisible(){
	                return visibilityMap == null || visibilityMap.get(EVENT_STAGE_LABEL_VISIBILITY) == null ?
	                        true : visibilityMap.get(EVENT_STAGE_LABEL_VISIBILITY);
            }
        });
        eventStage.setOutputMarkupId(true);
        parametersPanel.add(eventStage);

        ListModel<AuditEventStageType> eventStageListModel = new ListModel<AuditEventStageType>(
                Arrays.asList(AuditEventStageType.values()));
        PropertyModel<AuditEventStageType> eventStageModel = new PropertyModel<AuditEventStageType>(
                auditSearchDto, AuditSearchDto.F_EVENT_STAGE);
        DropDownChoicePanel<AuditEventStageType> eventStageField = new DropDownChoicePanel<AuditEventStageType>(
                ID_EVENT_STAGE_FIELD, eventStageModel, eventStageListModel,
                new EnumChoiceRenderer<AuditEventStageType>(), true);
        eventStageField.add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;
			@Override
            public boolean isVisible() {
                return visibilityMap == null || visibilityMap.get(EVENT_STAGE_FIELD_VISIBILITY) == null ?
                        true : visibilityMap.get(EVENT_STAGE_FIELD_VISIBILITY);
            }
        });
        eventStageField.getBaseFormComponent().add(new EmptyOnChangeAjaxFormUpdatingBehavior());
        eventStageField.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        eventStageField.setOutputMarkupId(true);
        eventStage.add(eventStageField);

        ListModel<OperationResultStatusType> outcomeListModel = new ListModel<OperationResultStatusType>(
                Arrays.asList(OperationResultStatusType.values()));
        PropertyModel<OperationResultStatusType> outcomeModel = new PropertyModel<OperationResultStatusType>(
                auditSearchDto, AuditSearchDto.F_OUTCOME);
        DropDownChoicePanel<OperationResultStatusType> outcome = new DropDownChoicePanel<OperationResultStatusType>(
                ID_OUTCOME, outcomeModel, outcomeListModel,
                new EnumChoiceRenderer<OperationResultStatusType>(), true);
        outcome.getBaseFormComponent().add(new EmptyOnChangeAjaxFormUpdatingBehavior());
        outcome.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        outcome.setOutputMarkupId(true);
        parametersPanel.add(outcome);

        List<String> channelList = WebComponentUtil.getChannelList();
        List<QName> channelQnameList = new ArrayList<QName>();
        for (int i = 0; i < channelList.size(); i++) {
            String channel = channelList.get(i);
            if (channel != null) {
                QName channelQName = QNameUtil.uriToQName(channel);
                channelQnameList.add(channelQName);
            }
        }
        ListModel<QName> channelListModel = new ListModel<QName>(channelQnameList);
        PropertyModel<QName> channelModel = new PropertyModel<QName>(auditSearchDto,
                AuditSearchDto.F_CHANNEL);
        DropDownChoicePanel<QName> channel = new DropDownChoicePanel<QName>(ID_CHANNEL, channelModel,
                channelListModel, new QNameChoiceRenderer(), true);
        channel.getBaseFormComponent().add(new EmptyOnChangeAjaxFormUpdatingBehavior());
        channel.getBaseFormComponent().add(new EmptyOnBlurAjaxFormUpdatingBehaviour());
        channel.setOutputMarkupId(true);
        parametersPanel.add(channel);

        Collection<Class<? extends UserType>> allowedClasses = new ArrayList<>();
        allowedClasses.add(UserType.class);
        ValueChooseWrapperPanel<ObjectReferenceType, UserType> chooseInitiatorPanel = new ValueChooseWrapperPanel<ObjectReferenceType, UserType>(
                ID_INITIATOR_NAME,
                new PropertyModel<ObjectReferenceType>(auditSearchDto, AuditSearchDto.F_INITIATOR_NAME),
                allowedClasses) {
        	private static final long serialVersionUID = 1L;
            @Override
            protected void replaceIfEmpty(ObjectType object) {
                ObjectReferenceType ort = ObjectTypeUtil.createObjectRef(object);
                ort.setTargetName(object.getName());
                getModel().setObject(ort);
            }
        };
        parametersPanel.add(chooseInitiatorPanel);

        WebMarkupContainer targetOwnerName = new WebMarkupContainer(ID_TARGET_OWNER_NAME);
        targetOwnerName.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;
            @Override
            public boolean isVisible(){
                return visibilityMap == null || visibilityMap.get(TARGET_OWNER_LABEL_VISIBILITY) == null ?
                        true : visibilityMap.get(TARGET_OWNER_LABEL_VISIBILITY);
            }
        });
        parametersPanel.add(targetOwnerName);

        ValueChooseWrapperPanel<ObjectReferenceType, UserType> chooseTargerOwnerPanel = new ValueChooseWrapperPanel<ObjectReferenceType, UserType>(
                ID_TARGET_OWNER_NAME_FIELD,
                new PropertyModel<ObjectReferenceType>(auditSearchDto, AuditSearchDto.F_TARGET_OWNER_NAME),
                allowedClasses) {
        	private static final long serialVersionUID = 1L;
            @Override
            protected void replaceIfEmpty(ObjectType object) {
                ObjectReferenceType ort = ObjectTypeUtil.createObjectRef(object);
                ort.setTargetName(object.getName());
                getModel().setObject(ort);
            }
        };
        chooseTargerOwnerPanel.add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;
			@Override
            public boolean isVisible(){
                return visibilityMap == null || visibilityMap.get(TARGET_OWNER_FIELD_VISIBILITY) == null ?
                        true : visibilityMap.get(TARGET_OWNER_FIELD_VISIBILITY);
            }
        });
        targetOwnerName.add(chooseTargerOwnerPanel);
        
        WebMarkupContainer targetName = new WebMarkupContainer(ID_TARGET_NAME);
        targetName.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;
            @Override
            public boolean isVisible(){
                return visibilityMap == null || visibilityMap.get(TARGET_NAME_LABEL_VISIBILITY) == null ?
                        true : visibilityMap.get(TARGET_NAME_LABEL_VISIBILITY);
            }
        });
        parametersPanel.add(targetName);
        Collection<Class<? extends ObjectType>> allowedClassesAll = new ArrayList<>();
        allowedClassesAll.addAll(ObjectTypes.getAllObjectTypes());
        ValueChooseWrapperPanel<ObjectReferenceType, ObjectType> chooseTargetPanel = new ValueChooseWrapperPanel<ObjectReferenceType, ObjectType>(
                ID_TARGET_NAME_FIELD,
                new PropertyModel<ObjectReferenceType>(auditSearchDto, AuditSearchDto.F_TARGET_NAME),
                allowedClassesAll) {
        	private static final long serialVersionUID = 1L;
            @Override
            protected void replaceIfEmpty(ObjectType object) {
                ObjectReferenceType ort = ObjectTypeUtil.createObjectRef(object);
                ort.setTargetName(object.getName());
                getModel().setObject(ort);
            }
        };
        chooseTargetPanel.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;
            @Override
            public boolean isVisible(){
                return visibilityMap == null || visibilityMap.get(TARGET_NAME_FIELD_VISIBILITY) == null ?
                        true : visibilityMap.get(TARGET_NAME_FIELD_VISIBILITY);
            }
        });
        targetName.add(chooseTargetPanel);

        AjaxSubmitButton ajaxButton = new AjaxSubmitButton(ID_SEARCH_BUTTON,
                createStringResource("BasicSearchPanel.search")) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                auditLogStorage.setSearchDto(searchDto);
                auditLogStorage.setPageNumber(0);
                Form mainForm = (Form) getParent().getParent();
                addOrReplaceTable(mainForm);
                getFeedbackPanel().getFeedbackMessages().clear();
                target.add(getFeedbackPanel());
                target.add(mainForm);
            }

            @Override
        protected void onError(AjaxRequestTarget target, Form<?> form){
                target.add(getFeedbackPanel());
            }
        };
        ajaxButton.setOutputMarkupId(true);
        parametersPanel.add(ajaxButton);
    }

    private void addOrReplaceTable(Form mainForm) {
        AuditEventRecordProvider provider = new AuditEventRecordProvider(AuditLogViewerPanel.this) {
            private static final long serialVersionUID = 1L;

            public Map<String, Object> getParameters() {
                Map<String, Object> parameters = new HashMap<String, Object>();

                AuditSearchDto search = auditSearchDto.getObject();
                parameters.put("from", search.getFrom());
                parameters.put("to", search.getTo());

                if (search.getChannel() != null) {
                    parameters.put("channel", QNameUtil.qNameToUri(search.getChannel()));
                }
                parameters.put("hostIdentifier", search.getHostIdentifier());

                if (search.getInitiatorName() != null) {
                    parameters.put("initiatorName", search.getInitiatorName().getOid());
                }

                if (search.getTargetOwnerName() != null) {
                    parameters.put("targetOwnerName", search.getTargetOwnerName().getOid());
                }
                if (search.getTargetName() != null) {
                    parameters.put("targetName", search.getTargetName().getOid());
                }
                if (search.getChangedItem().toItemPath() != null) {
                	ItemPath itemPath = search.getChangedItem().toItemPath();
                	parameters.put("changedItem", CanonicalItemPath.create(itemPath).asString());
                }
                parameters.put("eventType", search.getEventType());
                parameters.put("eventStage", search.getEventStage());
                parameters.put("outcome", search.getOutcome());
                return parameters;
            }

            @Override
            protected void saveCurrentPage(long from, long count) {
                if (count != 0) {
                    auditLogStorage.setPageNumber(from / count);
                }
            }

        };
        BoxedTablePanel table = new BoxedTablePanel(ID_TABLE, provider, initColumns(),
                UserProfileStorage.TableId.PAGE_AUDIT_LOG_VIEWER,
                (int) pageBase.getItemsPerPage(UserProfileStorage.TableId.PAGE_AUDIT_LOG_VIEWER));
        table.setShowPaging(true);
        table.setCurrentPage(auditLogStorage.getPageNumber());
        table.setOutputMarkupId(true);
        mainForm.addOrReplace(table);
    }

    protected List<IColumn<AuditEventRecordType, String>> initColumns() {
        List<IColumn<AuditEventRecordType, String>> columns = new ArrayList<IColumn<AuditEventRecordType, String>>();
        IColumn<AuditEventRecordType, String> linkColumn = new LinkColumn<AuditEventRecordType>(
                createStringResource("AuditEventRecordType.timestamp"), "timestamp") {
            private static final long serialVersionUID = 1L;

            @Override
            protected IModel<String> createLinkModel(final IModel<AuditEventRecordType> rowModel){
                return new AbstractReadOnlyModel<String>() {

                    @Override
                    public String getObject() {
                        XMLGregorianCalendar time = rowModel.getObject().getTimestamp();
                        return WebComponentUtil.formatDate(time);
                    }
                };
            }
            @Override
            public void onClick(AjaxRequestTarget target, IModel<AuditEventRecordType> rowModel) {
                setResponsePage(new PageAuditLogDetails(rowModel.getObject()));
            }

        };
        columns.add(linkColumn);

        PropertyColumn<AuditEventRecordType, String> initiatorRefColumn = new PropertyColumn<AuditEventRecordType, String>(createStringResource("AuditEventRecordType.initiatorRef"),
                AuditEventRecordType.F_INITIATOR_REF.getLocalPart()) {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<AuditEventRecordType>> item, String componentId,
                                     IModel<AuditEventRecordType> rowModel) {
                AuditEventRecordType auditEventRecordType = (AuditEventRecordType) rowModel.getObject();
                createReferenceColumn(auditEventRecordType.getInitiatorRef(), item, componentId);
            }
        };
        columns.add(initiatorRefColumn);

        if (visibilityMap == null || visibilityMap.get(EVENT_STAGE_COLUMN_VISIBILITY) == null ||
                visibilityMap.get(EVENT_STAGE_COLUMN_VISIBILITY)) {
            IColumn<AuditEventRecordType, String> eventStageColumn = new PropertyColumn<AuditEventRecordType, String>(
                    createStringResource("PageAuditLogViewer.eventStageLabel"), "eventStage");
            columns.add(eventStageColumn);
        }
        IColumn<AuditEventRecordType, String> eventTypeColumn = new PropertyColumn<AuditEventRecordType, String>(
                createStringResource("PageAuditLogViewer.eventTypeLabel"), "eventType");
        columns.add(eventTypeColumn);

        if (visibilityMap == null || visibilityMap.get(TARGET_COLUMN_VISIBILITY) == null ||
                visibilityMap.get(TARGET_COLUMN_VISIBILITY)) {
            PropertyColumn<AuditEventRecordType, String> targetRefColumn = new PropertyColumn<AuditEventRecordType, String>(createStringResource("AuditEventRecordType.targetRef"),
                    AuditEventRecordType.F_TARGET_REF.getLocalPart()) {
                private static final long serialVersionUID = 1L;

                @Override
                public void populateItem(Item<ICellPopulator<AuditEventRecordType>> item, String componentId,
                                         IModel<AuditEventRecordType> rowModel) {
                    AuditEventRecordType auditEventRecordType = (AuditEventRecordType) rowModel.getObject();
                    createReferenceColumn(auditEventRecordType.getTargetRef(), item, componentId);
                }
            };
            columns.add(targetRefColumn);
        }

        if (visibilityMap == null || visibilityMap.get(TARGET_OWNER_COLUMN_VISIBILITY) == null ||
                visibilityMap.get(TARGET_OWNER_COLUMN_VISIBILITY)) {
            PropertyColumn<AuditEventRecordType, String> targetOwnerRefColumn = new PropertyColumn<AuditEventRecordType, String>(createStringResource("AuditEventRecordType.targetOwnerRef"),
                    AuditEventRecordType.F_TARGET_OWNER_REF.getLocalPart()) {
                private static final long serialVersionUID = 1L;

                @Override
                public void populateItem(Item<ICellPopulator<AuditEventRecordType>> item, String componentId,
                                         IModel<AuditEventRecordType> rowModel) {
                    AuditEventRecordType auditEventRecordType = (AuditEventRecordType) rowModel.getObject();
                    createReferenceColumn(auditEventRecordType.getTargetOwnerRef(), item, componentId);
                }
            };
            columns.add(targetOwnerRefColumn);
        }
        IColumn<AuditEventRecordType, String> channelColumn = new PropertyColumn<AuditEventRecordType, String>(
                createStringResource("AuditEventRecordType.channel"), "channel") {
            private static final long serialVersionUID = 1L;

            @Override
            public void populateItem(Item<ICellPopulator<AuditEventRecordType>> item, String componentId,
                                     IModel<AuditEventRecordType> rowModel) {
                AuditEventRecordType auditEventRecordType = (AuditEventRecordType) rowModel.getObject();
                String channel = auditEventRecordType.getChannel();
                if (channel != null) {
                    QName channelQName = QNameUtil.uriToQName(channel);
                    String return_ = channelQName.getLocalPart();
                    item.add(new Label(componentId, return_));
                } else {
                    item.add(new Label(componentId, ""));
                }
                item.add(new AttributeModifier("style", new Model<String>("width: 10%;")));
            }
        };
        columns.add(channelColumn);

        IColumn<AuditEventRecordType, String> outcomeColumn = new PropertyColumn<AuditEventRecordType, String>(
                createStringResource("PageAuditLogViewer.outcomeLabel"), "outcome");
        columns.add(outcomeColumn);

        return columns;
    }

//	private PropertyColumn<AuditEventRecordType, String> createReferenceColumn(String columnKey,
//			QName attributeName) {
//		return new PropertyColumn<AuditEventRecordType, String>(createStringResource(columnKey),
//				attributeName.getLocalPart()) {
//			private static final long serialVersionUID = 1L;
//
//			@Override
//			public void populateItem(Item<ICellPopulator<AuditEventRecordType>> item, String componentId,
//					IModel<AuditEventRecordType> rowModel) {
//				AuditEventRecordType auditEventRecordType = (AuditEventRecordType) rowModel.getObject();
//				createReferenceColumn(auditEventRecordType.getTargetRef(), item, componentId);
//			}
//		};
//	}

    private void createReferenceColumn(ObjectReferenceType ref, Item item, String componentId) {
        String name = WebModelServiceUtils.resolveReferenceName(ref, pageBase,
                pageBase.createSimpleTask(OPERATION_RESOLVE_REFENRENCE_NAME),
                new OperationResult(OPERATION_RESOLVE_REFENRENCE_NAME));
        item.add(new Label(componentId, name));
        item.add(new AttributeModifier("style", new Model<String>("width: 10%;")));
    }

    public WebMarkupContainer getFeedbackPanel() {
        return (FeedbackPanel) get(pageBase.createComponentPath(ID_MAIN_FORM, ID_FEEDBACK));
    }
}
