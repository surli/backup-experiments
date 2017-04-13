/*
 * Copyright (c) 2010-2016 Evolveum
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

package com.evolveum.midpoint.web.page.admin.configuration.component;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.AjaxSubmitButton;
import com.evolveum.midpoint.web.component.dialog.Popupable;
import com.evolveum.midpoint.web.component.form.DropDownFormGroup;
import com.evolveum.midpoint.web.component.input.ChoiceableChoiceRenderer;
import com.evolveum.midpoint.web.component.input.QNameChoiceRenderer;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.page.admin.configuration.dto.ObjectPolicyConfigurationTypeDto;
import com.evolveum.midpoint.web.page.admin.configuration.dto.ObjectPolicyDialogDto;
import com.evolveum.midpoint.web.page.admin.configuration.dto.ObjectTemplateConfigTypeReferenceDto;
import com.evolveum.midpoint.web.page.admin.configuration.dto.PropertyConstraintTypeDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectTemplateType;


/**
 * @author shood
 */
public class ObjectPolicyPanel extends BasePanel<ObjectPolicyDialogDto> implements Popupable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Trace LOGGER = TraceManager.getTrace(ObjectPolicyPanel.class);

	private static final String DOT_CLASS = ObjectPolicyPanel.class.getName() + ".";

	private static final String OPERATION_LOAD_ALL_OBJECT_TEMPLATES = DOT_CLASS + "loadObjectTemplates";

	private static final String ID_FORM = "mainForm";
	private static final String ID_TYPE = "type";
	private static final String ID_SUBTYPE = "subtype";
	private static final String ID_OBJECT_TEMPLATE = "objectTemplate";
	private static final String ID_BUTTON_SAVE = "saveButton";
	private static final String ID_BUTTON_CANCEL = "cancelButton";
	private static final String ID_OID_BOUND = "oidBound";
	private static final String ID_PROPERTY = "property";
	private static final String ID_REPEATER = "repeater";
	private static final String ID_TEXT_WRAPPER = "textWrapper";
	private static final String ID_BUTTON_GROUP = "buttonGroup";
	private static final String ID_BUTTON_REMOVE = "remove";
	private static final String ID_BUTTON_ADD = "add";

	private static final String ID_LABEL_SIZE = "col-md-4";
	private static final String ID_INPUT_SIZE = "col-md-8";

	private static final String CLASS_MULTI_VALUE = "multivalue-form";
	private static final String OFFSET_CLASS = "col-md-offset-4";

	private boolean initialized;
	private IModel<ObjectPolicyDialogDto> model;

	public ObjectPolicyPanel(String id, final ObjectPolicyConfigurationTypeDto config) {
		super(id);

		model = new LoadableModel<ObjectPolicyDialogDto>(false) {

			@Override
			protected ObjectPolicyDialogDto load() {
				return loadModel(config);
			}
		};
		
		initLayout();

		setOutputMarkupId(true);
//		setTitle(createStringResource("ObjectPolicyDialog.label"));
//		showUnloadConfirmation(false);
//		setCssClassName(ModalWindow.CSS_CLASS_GRAY);
//		setCookieName(ObjectPolicyPanel.class.getSimpleName() + ((int) (Math.random() * 100)));
//		setInitialWidth(625);
//		setInitialHeight(400);
//		setWidthUnit("px");
//
//		WebMarkupContainer content = new WebMarkupContainer(getContentId());
//		content.setOutputMarkupId(true);
//		setContent(content);
	}

	private ObjectPolicyDialogDto loadModel(ObjectPolicyConfigurationTypeDto config) {
		ObjectPolicyDialogDto dto;

		if (config == null) {
			dto = new ObjectPolicyDialogDto(new ObjectPolicyConfigurationTypeDto(), getPageBase());
		} else {
			dto = new ObjectPolicyDialogDto(config, getPageBase());
		}

		return dto;
	}

	public StringResourceModel createStringResource(String resourceKey, Object... objects) {
		return PageBase.createStringResourceStatic(this, resourceKey, objects);
		// return new StringResourceModel(resourceKey, this, null, resourceKey,
		// objects);
	}


//	public void updateModel(AjaxRequestTarget target, ObjectPolicyConfigurationTypeDto config) {
//		model.setObject(new ObjectPolicyDialogDto(config, getPageBase()));
//		target.add(getContent());
//	}

	public void initLayout() {
		Form form = new Form(ID_FORM);
		form.setOutputMarkupId(true);
		add(form);

		DropDownFormGroup type = new DropDownFormGroup<>(ID_TYPE,
				new PropertyModel<QName>(model, ObjectPolicyDialogDto.F_TYPE), createTypeChoiceList(),
				new QNameChoiceRenderer(), createStringResource("ObjectPolicyDialog.type"), ID_LABEL_SIZE,
				ID_INPUT_SIZE, false);
		form.add(type);
		type.getInput().setNullValid(false);
		type.getInput().setRequired(true);
		
		TextField<String> fieldSubtype = new TextField<>(ID_SUBTYPE, new PropertyModel<String>(model, ObjectPolicyDialogDto.F_SUBTYPE));
		form.add(fieldSubtype);
		form.add(fieldSubtype);

		DropDownFormGroup template = new DropDownFormGroup<>(ID_OBJECT_TEMPLATE,
				new PropertyModel<ObjectTemplateConfigTypeReferenceDto>(model, ObjectPolicyDialogDto.F_TEMPLATE_REF),
				createObjectTemplateList(), new ChoiceableChoiceRenderer<ObjectTemplateConfigTypeReferenceDto>(),
				createStringResource("ObjectPolicyDialog.template"), ID_LABEL_SIZE, ID_INPUT_SIZE, false);
		form.add(template);
		template.getInput().setNullValid(false);
		template.getInput().setRequired(true);

		ListView repeater = new ListView<PropertyConstraintTypeDto>(ID_REPEATER,
				new PropertyModel<List<PropertyConstraintTypeDto>>(model, ObjectPolicyDialogDto.F_PROPERTY_LIST)) {

			@Override
			protected void populateItem(final ListItem item) {
				WebMarkupContainer textWrapper = new WebMarkupContainer(ID_TEXT_WRAPPER);
				textWrapper.add(AttributeAppender.prepend("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (item.getIndex() > 0) {
							return OFFSET_CLASS + " " + CLASS_MULTI_VALUE;
						}

						return null;
					}
				}));
				item.add(textWrapper);

				TextField property = new TextField<>(ID_PROPERTY,
						new PropertyModel<String>(item.getModel(), PropertyConstraintTypeDto.F_PROPERTY_PATH));
				property.add(new AjaxFormComponentUpdatingBehavior("blur") {
					@Override
					protected void onUpdate(AjaxRequestTarget target) {
					}
				});
				property.add(AttributeAppender.replace("placeholder",
						createStringResource("ObjectPolicyDialog.property.placeholder")));
				textWrapper.add(property);

				CheckBox oidBound = new CheckBox(ID_OID_BOUND,
						new PropertyModel<Boolean>(item.getModel(), PropertyConstraintTypeDto.F_OID_BOUND));
				oidBound.add(AttributeModifier.replace("title",
						createStringResource("ObjectPolicyDialog.label.oidBound.help")));
				textWrapper.add(oidBound);

				WebMarkupContainer buttonGroup = new WebMarkupContainer(ID_BUTTON_GROUP);
				buttonGroup.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						if (item.getIndex() > 0) {
							return CLASS_MULTI_VALUE;
						}

						return null;
					}
				}));
				item.add(buttonGroup);
				initButtons(buttonGroup, item);
			}

		};
		form.add(repeater);

		AjaxSubmitButton cancel = new AjaxSubmitButton(ID_BUTTON_CANCEL,
				createStringResource("ObjectPolicyDialog.button.cancel")) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				cancelPerformed(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				cancelPerformed(target);
			}
		};
		form.add(cancel);

		AjaxSubmitButton save = new AjaxSubmitButton(ID_BUTTON_SAVE,
				createStringResource("ObjectPolicyDialog.button.save")) {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				savePerformed(target);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
		};
		form.add(save);
	}

	private void initButtons(WebMarkupContainer buttonGroup, final ListItem item) {
		AjaxLink add = new AjaxLink(ID_BUTTON_ADD) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				addPerformed(target);
			}
		};
		add.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				return isAddButtonVisible(item);
			}
		});
		buttonGroup.add(add);

		AjaxLink remove = new AjaxLink(ID_BUTTON_REMOVE) {

			@Override
			public void onClick(AjaxRequestTarget target) {
				removePerformed(target, item);
			}
		};
		remove.add(new VisibleEnableBehaviour() {

			@Override
			public boolean isVisible() {
				return isRemoveButtonVisible();
			}
		});
		buttonGroup.add(remove);
	}

	private void addPerformed(AjaxRequestTarget target) {
		List<PropertyConstraintTypeDto> list = model.getObject().getPropertyConstraintsList();
		list.add(new PropertyConstraintTypeDto(null));

//		target.add(getContent());
	}

	private void removePerformed(AjaxRequestTarget target, ListItem item) {
		List<PropertyConstraintTypeDto> list = model.getObject().getPropertyConstraintsList();
		Iterator<PropertyConstraintTypeDto> iterator = list.iterator();

		while (iterator.hasNext()) {
			PropertyConstraintTypeDto object = iterator.next();

			if (object.equals(item.getModelObject())) {
				iterator.remove();
				break;
			}
		}

		if (list.size() == 0) {
			list.add(new PropertyConstraintTypeDto(null));
		}

//		target.add(getContent());
	}

	protected boolean isAddButtonVisible(ListItem item) {
		int size = model.getObject().getPropertyConstraintsList().size();
		if (size <= 1) {
			return true;
		}
		if (item.getIndex() == size - 1) {
			return true;
		}

		return false;
	}

	protected boolean isRemoveButtonVisible() {
		int size = model.getObject().getPropertyConstraintsList().size();
		if (size > 0) {
			return true;
		}

		return false;
	}

	protected IModel<List<ObjectTemplateConfigTypeReferenceDto>> createObjectTemplateList() {
		return new AbstractReadOnlyModel<List<ObjectTemplateConfigTypeReferenceDto>>() {

			@Override
			public List<ObjectTemplateConfigTypeReferenceDto> getObject() {
				List<PrismObject<ObjectTemplateType>> templateList = null;
				List<ObjectTemplateConfigTypeReferenceDto> list = new ArrayList<>();
				OperationResult result = new OperationResult(OPERATION_LOAD_ALL_OBJECT_TEMPLATES);
				Task task = getPageBase().createSimpleTask(OPERATION_LOAD_ALL_OBJECT_TEMPLATES);

				try {
					templateList = getPageBase().getModelService().searchObjects(ObjectTemplateType.class,
							new ObjectQuery(), null, task, result);
					result.recomputeStatus();
				} catch (Exception e) {
					result.recordFatalError("Could not get list of object templates", e);
					LoggingUtils.logUnexpectedException(LOGGER, "Could not get list of object templates", e);
					// TODO - show this error in GUI
				}

				if (templateList != null) {
					ObjectTemplateType template;
					for (PrismObject<ObjectTemplateType> obj : templateList) {
						template = obj.asObjectable();
						list.add(new ObjectTemplateConfigTypeReferenceDto(template.getOid(),
								WebComponentUtil.getName(template)));
					}
				}
				return list;
			}
		};
	}

	// TODO - to what types can be ObjectTemplate bound?
	private IModel<List<QName>> createTypeChoiceList() {
		return new AbstractReadOnlyModel<List<QName>>() {

			@Override
			public List<QName> getObject() {
				return WebComponentUtil.createFocusTypeList();
			}
		};
	}

	private void cancelPerformed(AjaxRequestTarget target) {
		getPageBase().hideMainPopup(target);
	}

	protected void savePerformed(AjaxRequestTarget target) {
		getPageBase().hideMainPopup(target);
	}

//	private PageBase getPageBase() {
//		return (PageBase) getPage();
//	}

	public IModel<ObjectPolicyDialogDto> getModel() {
		return model;
	}

	@Override
	public int getWidth() {
		return 625;
	}

	@Override
	public int getHeight() {
		return 400;
	}

	@Override
	public StringResourceModel getTitle() {
		return createStringResource("ObjectPolicyDialog.label");
	}

	@Override
	public Component getComponent() {
		return this;
	}
}
