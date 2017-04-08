/**
 * Copyright (c) 2015-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.evolveum.midpoint.web.page.admin.users.component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.namespace.QName;

import com.evolveum.midpoint.prism.query.builder.QueryBuilder;
import com.evolveum.midpoint.prism.query.builder.S_AtomicFilterEntry;
import com.evolveum.midpoint.prism.query.builder.S_FilterEntryOrEmpty;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import org.apache.commons.lang.Validate;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.EnumChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.evolveum.midpoint.gui.api.component.MainObjectListPanel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.gui.api.util.WebModelServiceUtils;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.PrismReferenceValue;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.prism.query.AndFilter;
import com.evolveum.midpoint.prism.query.InOidFilter;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.prism.query.OrgFilter;
import com.evolveum.midpoint.prism.query.RefFilter;
import com.evolveum.midpoint.prism.query.TypeFilter;
import com.evolveum.midpoint.prism.query.OrgFilter.Scope;
import com.evolveum.midpoint.schema.GetOperationOptions;
import com.evolveum.midpoint.schema.RetrieveOption;
import com.evolveum.midpoint.schema.SelectorOptions;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.task.api.TaskCategory;
import com.evolveum.midpoint.util.exception.CommunicationException;
import com.evolveum.midpoint.util.exception.ConfigurationException;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.PolicyViolationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.exception.SecurityViolationException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.FocusSummaryPanel;
import com.evolveum.midpoint.web.component.dialog.ConfirmationPanel;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenu;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.prism.ContainerStatus;
import com.evolveum.midpoint.web.component.prism.ObjectWrapper;
import com.evolveum.midpoint.web.component.util.ObjectWrapperUtil;
import com.evolveum.midpoint.web.page.admin.configuration.component.HeaderMenuAction;
import com.evolveum.midpoint.web.page.admin.roles.component.RoleSummaryPanel;
import com.evolveum.midpoint.web.session.UserProfileStorage.TableId;
import com.evolveum.midpoint.web.util.StringResourceChoiceRenderer;

public class OrgMemberPanel extends AbstractRoleMemberPanel<OrgType> {

	private static final Trace LOGGER = TraceManager.getTrace(OrgMemberPanel.class);

	protected static final String ID_SEARCH_SCOPE = "searchScope";
	protected static final String ID_SEARCH_BY_TYPE = "searchByType";

	protected static final String ID_MANAGER_MENU = "managerMenu";
	protected static final String ID_MANAGER_MENU_BODY = "managerMenuBody";

	protected static final String SEARCH_SCOPE_SUBTREE = "subtree";
	protected static final String SEARCH_SCOPE_ONE = "one";

	protected static final ObjectTypes OBJECT_TYPES_DEFAULT = ObjectTypes.OBJECT;

	protected static final List<String> SEARCH_SCOPE_VALUES = Arrays.asList(SEARCH_SCOPE_SUBTREE,
			SEARCH_SCOPE_ONE);

	protected static final String DOT_CLASS = OrgMemberPanel.class.getName() + ".";
	protected static final String OPERATION_SEARCH_MANAGERS = DOT_CLASS + "searchManagers";
	private static final String ID_MANAGER_SUMMARY = "managerSummary";
	private static final String ID_REMOVE_MANAGER = "removeManager";
	private static final String ID_DELETE_MANAGER = "deleteManager";
	private static final String ID_EDIT_MANAGER = "editManager";

	private static final long serialVersionUID = 1L;

	public OrgMemberPanel(String id, IModel<OrgType> model, PageBase parentPage) {
		super(id, TableId.ORG_MEMEBER_PANEL, model, parentPage);
		setOutputMarkupId(true);
	}

	@Override
	protected void initSearch(Form form) {

		/// TODO: move to utils class??
		List<ObjectTypes> objectTypes = Arrays.asList(ObjectTypes.values());
		Collections.sort(objectTypes, new Comparator<ObjectTypes>() {

			@Override
			public int compare(ObjectTypes o1, ObjectTypes o2) {
				Validate.notNull(o1);
				Validate.notNull(o2);

				String type1 = o1.getValue();
				String type2 = o2.getValue();

				return String.CASE_INSENSITIVE_ORDER.compare(type1, type2);

			}
		});
		////////////

		DropDownChoice<ObjectTypes> objectType = new DropDownChoice<ObjectTypes>(ID_SEARCH_BY_TYPE,
				Model.of(OBJECT_TYPES_DEFAULT), objectTypes, new EnumChoiceRenderer<ObjectTypes>());
		objectType.add(new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				refreshTable(target);

			}
		});
		objectType.setOutputMarkupId(true);
		form.add(objectType);

		DropDownChoice<String> seachScrope = new DropDownChoice<String>(ID_SEARCH_SCOPE,
				Model.of(SEARCH_SCOPE_ONE), SEARCH_SCOPE_VALUES,
				new StringResourceChoiceRenderer("TreeTablePanel.search.scope"));
		seachScrope.add(new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				refreshTable(target);
			}
		});
		seachScrope.setOutputMarkupId(true);
		form.add(seachScrope);

	}

	@Override
	protected void initCustomLayout(Form form) {
		WebMarkupContainer managerContainer = createManagerContainer();
		form.addOrReplace(managerContainer);
	}

	private WebMarkupContainer createManagerContainer() {
		WebMarkupContainer managerContainer = new WebMarkupContainer(ID_CONTAINER_MANAGER);
		managerContainer.setOutputMarkupId(true);
		managerContainer.setOutputMarkupPlaceholderTag(true);

		RepeatingView view = new RepeatingView(ID_MANAGER_TABLE);
		view.setOutputMarkupId(true);
		ObjectQuery managersQuery = createManagerQuery();

		OperationResult searchManagersResult = new OperationResult(OPERATION_SEARCH_MANAGERS);
		Collection<SelectorOptions<GetOperationOptions>> options = SelectorOptions.createCollection(
				FocusType.F_JPEG_PHOTO, GetOperationOptions.createRetrieve(RetrieveOption.INCLUDE));
		List<PrismObject<FocusType>> managers = WebModelServiceUtils.searchObjects(FocusType.class,
				managersQuery, options, searchManagersResult, getPageBase());
		for (PrismObject<FocusType> manager : managers) {
			ObjectWrapper<FocusType> managerWrapper = ObjectWrapperUtil.createObjectWrapper(
					WebComponentUtil.getEffectiveName(manager, RoleType.F_DISPLAY_NAME), "", manager,
					ContainerStatus.MODIFYING, getPageBase());
			WebMarkupContainer managerMarkup = new WebMarkupContainer(view.newChildId());

			AjaxLink<String> link = new AjaxLink<String>(ID_EDIT_MANAGER) {
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target) {
					FocusSummaryPanel<FocusType> summary = (FocusSummaryPanel<FocusType>) getParent()
							.get(ID_MANAGER_SUMMARY);
					detailsPerformed(target, summary.getModelObject());

				}
			};
			if (manager.getCompileTimeClass().equals(UserType.class)) {
				managerMarkup.add(new UserSummaryPanel(ID_MANAGER_SUMMARY,
						new Model<ObjectWrapper<UserType>>((ObjectWrapper) managerWrapper)));
			} else if (manager.getCompileTimeClass().equals(RoleType.class)) {
				managerMarkup.add(new RoleSummaryPanel(ID_MANAGER_SUMMARY,
						new Model<ObjectWrapper<RoleType>>((ObjectWrapper) managerWrapper)));
			} else if (manager.getCompileTimeClass().equals(OrgType.class)) {
				managerMarkup.add(new OrgSummaryPanel(ID_MANAGER_SUMMARY,
						new Model<ObjectWrapper<OrgType>>((ObjectWrapper) managerWrapper)));
			} else if (manager.getCompileTimeClass().equals(ServiceType.class)) {
				managerMarkup.add(new ServiceSummaryPanel(ID_MANAGER_SUMMARY,
						new Model<ObjectWrapper<ServiceType>>((ObjectWrapper) managerWrapper)));
			}
			link.setOutputMarkupId(true);
			managerMarkup.setOutputMarkupId(true);
			managerMarkup.add(link);
			view.add(managerMarkup);

			AjaxButton removeManager = new AjaxButton(ID_REMOVE_MANAGER) {

				@Override
				public void onClick(AjaxRequestTarget target) {
					FocusSummaryPanel<FocusType> summary = (FocusSummaryPanel<FocusType>) getParent()
							.get(ID_MANAGER_SUMMARY);
					removeManagerPerformed(summary.getModelObject(), target);
					getParent().setVisible(false);
					target.add(OrgMemberPanel.this);

				}
			};
			removeManager.setOutputMarkupId(true);
			managerMarkup.add(removeManager);
			
			AjaxButton deleteManager = new AjaxButton(ID_DELETE_MANAGER) {

				@Override
				public void onClick(AjaxRequestTarget target) {
					FocusSummaryPanel<FocusType> summary = (FocusSummaryPanel<FocusType>) getParent()
							.get(ID_MANAGER_SUMMARY);
					deleteManagerPerformed(summary.getModelObject(), this, target);
				}
			};
			deleteManager.setOutputMarkupId(true);
			managerMarkup.add(deleteManager);
		}

		managerContainer.add(view);

		InlineMenu menupanel = new InlineMenu(ID_MANAGER_MENU,
				new Model<Serializable>((Serializable) createManagersHeaderInlineMenu()));

		add(menupanel);
		menupanel.setOutputMarkupId(true);
		managerContainer.add(menupanel);

		return managerContainer;
	}

	private void removeManagerPerformed(FocusType manager, AjaxRequestTarget target) {
		OperationResult parentResult = new OperationResult("Remove manager");
		Task task = getPageBase().createSimpleTask("Remove manager");
		try {

			ObjectDelta delta = ObjectDelta.createModificationDeleteContainer(
					manager.asPrismObject().getCompileTimeClass(), manager.getOid(), FocusType.F_ASSIGNMENT,
					getPageBase().getPrismContext(), createAssignmentToModify(SchemaConstants.ORG_MANAGER));

			getPageBase().getModelService().executeChanges(WebComponentUtil.createDeltaCollection(delta),
					null, task, parentResult);
			parentResult.computeStatus();
		} catch (SchemaException | ObjectAlreadyExistsException | ObjectNotFoundException
				| ExpressionEvaluationException | CommunicationException | ConfigurationException
				| PolicyViolationException | SecurityViolationException e) {

			parentResult.recordFatalError("Failed to remove manager " + e.getMessage(), e);
			LoggingUtils.logUnexpectedException(LOGGER, "Failed to remove manager", e);
			getPageBase().showResult(parentResult);
		}
		target.add(getPageBase().getFeedbackPanel());

	}
	
	private void deleteManagerConfirmPerformed(FocusType manager, AjaxRequestTarget target) {
		getPageBase().hideMainPopup(target);
		OperationResult parentResult = new OperationResult("Remove manager");
		Task task = getPageBase().createSimpleTask("Remove manager");
		try {

			ObjectDelta delta = ObjectDelta.createDeleteDelta(manager.asPrismObject().getCompileTimeClass(), manager.getOid(), getPageBase().getPrismContext());
			getPageBase().getModelService().executeChanges(WebComponentUtil.createDeltaCollection(delta),
					null, task, parentResult);
			parentResult.computeStatus();
		} catch (SchemaException | ObjectAlreadyExistsException | ObjectNotFoundException
				| ExpressionEvaluationException | CommunicationException | ConfigurationException
				| PolicyViolationException | SecurityViolationException e) {

			parentResult.recordFatalError("Failed to remove manager " + e.getMessage(), e);
			LoggingUtils.logUnexpectedException(LOGGER, "Failed to remove manager", e);
			getPageBase().showResult(parentResult);
		}
		target.add(getPageBase().getFeedbackPanel());

	}
	
	private void deleteManagerPerformed(final FocusType manager, final Component summary, AjaxRequestTarget target) {
		ConfirmationPanel confirmDelete = new ConfirmationPanel(getPageBase().getMainPopupBodyId(), createStringResource("TreeTablePanel.menu.deleteManager.confirm")) {
			@Override
			public void yesPerformed(AjaxRequestTarget target) {
				OrgMemberPanel.this.deleteManagerConfirmPerformed(manager, target);
				summary.getParent().setVisible(false);
				target.add(OrgMemberPanel.this);
			}
		};
		
		getPageBase().showMainPopup(confirmDelete, target);
	}
	
	
	@Override
	protected List<InlineMenuItem> createMembersHeaderInlineMenu() {
		List<InlineMenuItem> headerMenuItems = super.createMembersHeaderInlineMenu();
		
		headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.deleteMember"),
				false, new HeaderMenuAction(this) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						deleteMemberPerformed(QueryScope.SELECTED, null, target, "TreeTablePanel.menu.deleteMember.confirm");
					}
				}));
		
		headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.deleteAllMembers"),
				false, new HeaderMenuAction(this) {

					@Override
					public void onClick(AjaxRequestTarget target) {
						deleteMemberPerformed(QueryScope.ALL, null, target, "TreeTablePanel.menu.deleteAllMembers.confirm");
					}
				}));
		return headerMenuItems;
	}
	
	private void deleteMemberPerformed(final QueryScope scope, final QName relation, final AjaxRequestTarget target, String confirmMessageKey) {
		ConfirmationPanel confirmDelete = new ConfirmationPanel(getPageBase().getMainPopupBodyId(), createStringResource(confirmMessageKey)) {
			@Override
			public void yesPerformed(AjaxRequestTarget target) {
				OrgMemberPanel.this.deleteMemberConfirmPerformed(scope, relation, target);
			}
		};
		
		getPageBase().showMainPopup(confirmDelete, target);
	}

	private void deleteMemberConfirmPerformed(QueryScope scope, QName relation, AjaxRequestTarget target) {
		getPageBase().hideMainPopup(target);
		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Delete", scope, false));
		ObjectDelta delta = ObjectDelta.createDeleteDelta(FocusType.class, "fakeOid", getPageBase().getPrismContext());
		if (delta == null) {
			return;
		}
		executeMemberOperation(operationalTask, FocusType.COMPLEX_TYPE, createQueryForMemberAction(scope, relation, true), delta, TaskCategory.EXECUTE_CHANGES, target);
		
	}
	
	private List<InlineMenuItem> createManagersHeaderInlineMenu() {
		List<InlineMenuItem> headerMenuItems = new ArrayList<>();

		headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.createManager"),
				false, new HeaderMenuAction(this) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						OrgMemberPanel.this.createFocusMemberPerformed(SchemaConstants.ORG_MANAGER, target);
					}
				}));
		headerMenuItems.add(new InlineMenuItem());

		headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.addManagers"), false,
				new HeaderMenuAction(this) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						OrgMemberPanel.this.addMembers(SchemaConstants.ORG_MANAGER, target);
					}
				}));
		headerMenuItems.add(new InlineMenuItem());

		headerMenuItems.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.removeManagersAll"),
				false, new HeaderMenuAction(this) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						removeManagersPerformed(QueryScope.ALL, target);
					}
				}));

		headerMenuItems
				.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.recomputeManagersAll"),
						false, new HeaderMenuAction(this) {
							private static final long serialVersionUID = 1L;

							@Override
							public void onClick(AjaxRequestTarget target) {
								recomputeManagersPerformed(QueryScope.ALL, target);
							}
						}));
		
		headerMenuItems
		.add(new InlineMenuItem(createStringResource("TreeTablePanel.menu.deleteManagersAll"),
				false, new HeaderMenuAction(this) {
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target) {
						OrgMemberPanel.this.deleteMemberPerformed(QueryScope.ALL, SchemaConstants.ORG_MANAGER, target, "TreeTablePanel.menu.deleteManagersAll.confirm");
					}
				}));

		return headerMenuItems;
	}

	protected void refreshTable(AjaxRequestTarget target) {
		DropDownChoice<ObjectTypes> typeChoice = (DropDownChoice<ObjectTypes>) get(
				createComponentPath(ID_FORM, ID_SEARCH_BY_TYPE));
		ObjectTypes type = typeChoice.getModelObject();
		target.add(get(createComponentPath(ID_FORM, ID_SEARCH_SCOPE)));
		getMemberTable().clearCache();
		getMemberTable().refreshTable(WebComponentUtil
				.qnameToClass(getPageBase().getPrismContext(), type.getTypeQName(), ObjectType.class), target);
	}

	private MainObjectListPanel<ObjectType> getMemberTable() {
		return (MainObjectListPanel<ObjectType>) get(
				createComponentPath(ID_FORM, ID_CONTAINER_MEMBER, ID_MEMBER_TABLE));
	}

	private ObjectTypes getSearchType() {
		DropDownChoice<ObjectTypes> searchByTypeChoice = (DropDownChoice<ObjectTypes>) get(
				createComponentPath(ID_FORM, ID_SEARCH_BY_TYPE));
		return searchByTypeChoice.getModelObject();
	}

	private ObjectQuery createManagerQuery() {
		String oid = getModelObject().getOid();
		PrismReferenceValue referenceFilter = new PrismReferenceValue();
		referenceFilter.setOid(oid);
		referenceFilter.setRelation(SchemaConstants.ORG_MANAGER);
		ObjectQuery query = QueryBuilder.queryFor(FocusType.class, getPageBase().getPrismContext())
				.item(FocusType.F_PARENT_ORG_REF).ref(referenceFilter)
				.build();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Searching members of org {} with query:\n{}", oid, query.debugDump());
		}
		return query;
	}

	private ObjectDelta prepareDelta(MemberOperation operaton, QName type, QName relation,
			OperationResult result, AjaxRequestTarget target) {
		ObjectDelta delta = null;
		try {
			delta = createMemberDelta(operaton, type, relation);
		} catch (SchemaException e) {
			result.recordFatalError("Failed to prepare delta for add members");
			getPageBase().showResult(result);
			target.add(getPageBase().getFeedbackPanel());
		}
		return delta;
	}

	@Override
	protected void addMembersPerformed(QName type, QName relation, List selected, AjaxRequestTarget target) {
		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Add", null, false));
		ObjectDelta delta = prepareDelta(MemberOperation.ADD, type, relation, operationalTask.getResult(),
				target);
		if (delta == null) {
			return;
		}
		executeMemberOperation(operationalTask, type, createQueryForAdd(selected), delta,
				TaskCategory.EXECUTE_CHANGES, target);

	}

	protected void addManagersPerformed(QName type, List selected, AjaxRequestTarget target) {
		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Add", null, true));
		ObjectDelta delta = prepareDelta(MemberOperation.ADD, type, SchemaConstants.ORG_MANAGER,
				operationalTask.getResult(), target);
		if (delta == null) {
			return;
		}
		executeMemberOperation(operationalTask, type, createQueryForAdd(selected), delta,
				TaskCategory.EXECUTE_CHANGES, target);

	}

	protected void removeManagersPerformed(QueryScope scope, AjaxRequestTarget target) {

		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Remove", scope, true));
		ObjectDelta delta = prepareDelta(MemberOperation.REMOVE, FocusType.COMPLEX_TYPE,
				SchemaConstants.ORG_MANAGER, operationalTask.getResult(), target);
		if (delta == null) {
			return;
		}
		executeMemberOperation(operationalTask, FocusType.COMPLEX_TYPE,
				createQueryForMemberAction(scope, SchemaConstants.ORG_MANAGER, true), delta,
				TaskCategory.EXECUTE_CHANGES, target);

	}

	@Override
	protected void removeMembersPerformed(QueryScope scope, AjaxRequestTarget target) {

		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Remove", scope, false));

		ObjectDelta delta = prepareDelta(MemberOperation.REMOVE, FocusType.COMPLEX_TYPE, null,
				operationalTask.getResult(), target);
		if (delta != null) {
			executeMemberOperation(operationalTask, FocusType.COMPLEX_TYPE,
					createQueryForMemberAction(scope, null, true), delta, TaskCategory.EXECUTE_CHANGES,
					target);
		}

		delta = prepareDelta(MemberOperation.REMOVE, ObjectType.COMPLEX_TYPE, null,
				operationalTask.getResult(), target);
		if (delta == null) {
			return;
		}
		executeMemberOperation(operationalTask, ObjectType.COMPLEX_TYPE,
				createQueryForMemberAction(scope, null, false), delta, TaskCategory.EXECUTE_CHANGES, target);

	}

	@Override
	protected void recomputeMembersPerformed(QueryScope scope, AjaxRequestTarget target) {
		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Recompute", scope, false));
		executeMemberOperation(operationalTask, FocusType.COMPLEX_TYPE,
				createQueryForMemberAction(scope, null, true), null, TaskCategory.RECOMPUTATION, target);
	}

	protected void recomputeManagersPerformed(QueryScope scope, AjaxRequestTarget target) {
		Task operationalTask = getPageBase().createSimpleTask(getTaskName("Recompute", scope, true));
		executeMemberOperation(operationalTask, FocusType.COMPLEX_TYPE,
				createQueryForMemberAction(scope, SchemaConstants.ORG_MANAGER, true), null,
				TaskCategory.RECOMPUTATION, target);
	}

	@Override
	protected ObjectQuery createMemberQuery() {
		String oid = getModelObject().getOid();

		DropDownChoice<String> searchScopeChoice = (DropDownChoice<String>) get(
				createComponentPath(ID_FORM, ID_SEARCH_SCOPE));
		String scope = searchScopeChoice.getModelObject();

		ObjectTypes searchType = getSearchType();
		S_FilterEntryOrEmpty q = QueryBuilder.queryFor(ObjectType.class, getPageBase().getPrismContext());
		if (!searchType.equals(ObjectTypes.OBJECT)) {
			q = q.type(searchType.getClassDefinition());
		}
		ObjectQuery query;
		if (SEARCH_SCOPE_ONE.equals(scope)) {
			query = q.isDirectChildOf(oid).build();
		} else {
			query = q.isChildOf(oid).build();
		}
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Searching members of org {} with query:\n{}", oid, query.debugDump());
		}
		return query;
	}

	private ObjectQuery createQueryForMemberAction(QueryScope scope, QName orgRelation, boolean isFocus) {

		ObjectQuery query = null;
		switch (scope) {
			case SELECTED:
				List<ObjectType> objects = getMemberTable().getSelectedObjects();
				List<String> oids = new ArrayList<>();
				for (ObjectType object : objects) {
					if (satisfyConstraints(isFocus, object.getClass())) {
						oids.add(object.getOid());
					}
				}

				query = ObjectQuery.createObjectQuery(InOidFilter.createInOid(oids));
				break;
			case ALL_DIRECT:
			case ALL: 
				query = createQueryForAll(scope, isFocus, orgRelation);
				break;
			default:
				break;
		}
		return query;

	}

	private boolean satisfyConstraints(boolean isFocus, Class<? extends ObjectType> type) {
		if (isFocus && FocusType.class.isAssignableFrom(type)) {
			return true;
		}

		if (!isFocus && !FocusType.class.isAssignableFrom(type)) {
			return true;
		}

		return false;
	}

	private ObjectQuery createQueryForAll(QueryScope scope, boolean isFocus, QName relation) {
		OrgType org = getModelObject();
		return QueryBuilder.queryFor(ObjectType.class, getPageBase().getPrismContext())
				.isInScopeOf(org.getOid(), getScope(scope))
				.build();
	}
	
	private Scope getScope(QueryScope queryScope) {
		return QueryScope.ALL == queryScope ? Scope.SUBTREE : Scope.ONE_LEVEL;
	}


	protected ObjectDelta createMemberDelta(MemberOperation operation, QName type, QName relation)
			throws SchemaException {
		Class classType = WebComponentUtil.qnameToClass(getPageBase().getPrismContext(), type);
		ObjectDelta delta = null;
		switch (operation) {
			case ADD:

				if (isFocus(type)) {

					delta = ObjectDelta.createModificationAddContainer(classType, "fakeOid",
							FocusType.F_ASSIGNMENT, getPageBase().getPrismContext(),
							createAssignmentToModify(relation));
				} else {
					delta = ObjectDelta.createModificationAddReference(classType, "fakeOid",
							ObjectType.F_PARENT_ORG_REF, getPageBase().getPrismContext(),
							createReference(relation).asReferenceValue());
				}
				break;

			case REMOVE:
				if (isFocus(type)) {
					delta = ObjectDelta.createModificationDeleteContainer(classType, "fakeOid",
							FocusType.F_ASSIGNMENT, getPageBase().getPrismContext(),
							createAssignmentToModify(relation));
				} else {
					delta = ObjectDelta.createModificationDeleteReference(classType, "fakeOid",
							ObjectType.F_PARENT_ORG_REF, getPageBase().getPrismContext(),
							createReference(relation).asReferenceValue());
				}
				break;
			default:
				break;
		}
		return delta;

	}

	private boolean isFocus(QName type) {
		return FocusType.COMPLEX_TYPE.equals(type) || UserType.COMPLEX_TYPE.equals(type)
				|| RoleType.COMPLEX_TYPE.equals(type) || OrgType.COMPLEX_TYPE.equals(type)
				|| ServiceType.COMPLEX_TYPE.equals(type);
	}

}
