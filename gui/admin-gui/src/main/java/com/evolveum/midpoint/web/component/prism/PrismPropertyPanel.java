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

package com.evolveum.midpoint.web.component.prism;

import com.evolveum.midpoint.common.refinery.RefinedAttributeDefinition;
import com.evolveum.midpoint.gui.api.model.LoadableModel;
import com.evolveum.midpoint.gui.api.page.PageBase;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ItemDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.DeltaConvertor;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import com.evolveum.midpoint.web.util.InfoTooltipBehavior;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ObjectDeltaType;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;

import java.util.List;

/**
 * @author lazyman
 */
public class PrismPropertyPanel<IW extends ItemWrapper> extends Panel {
	private static final long serialVersionUID = 1L;
	
	private static final Trace LOGGER = TraceManager.getTrace(PrismPropertyPanel.class);
    private static final String ID_HAS_PENDING_MODIFICATION = "hasPendingModification";
    private static final String ID_HELP = "help";
    private static final String ID_LABEL = "label";
    private static final String ID_LABEL_CONTAINER = "labelContainer";

    private PageBase pageBase;

    private boolean labelContainerVisible = true;

    public PrismPropertyPanel(String id, final IModel<IW> model, Form form, PageBase pageBase) {
        super(id);
        Validate.notNull(model, "no model");
        this.pageBase = pageBase;

        LOGGER.trace("Creating property panel for {}", model.getObject());
        
        setOutputMarkupId(true);
        add(new VisibleEnableBehaviour() {
			private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
            	IW propertyWrapper = model.getObject();
                boolean visible = propertyWrapper.isVisible();
                LOGGER.trace("isVisible: {}: {}", propertyWrapper, visible);
                return visible;
            }

            @Override
            public boolean isEnabled() {
                return !model.getObject().isReadonly();
            }
        });

        initLayout(model, form);
    }

    private void initLayout(final IModel<IW> model, final Form form) {
        WebMarkupContainer labelContainer = new WebMarkupContainer(ID_LABEL_CONTAINER);
        labelContainer.setOutputMarkupId(true);
        labelContainer.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;
        	
            @Override public boolean isVisible() {
                return labelContainerVisible;
            }
        });
        add(labelContainer);

        final IModel<String> label = createDisplayName(model);
        labelContainer.add(new Label(ID_LABEL, label));

        final IModel<String> helpText = new LoadableModel<String>(false) {
        	private static final long serialVersionUID = 1L;

            @Override
            protected String load() {
                return loadHelpText(model);
            }
        };
        Label help = new Label(ID_HELP);
        help.add(AttributeModifier.replace("title", helpText));
        help.add(new InfoTooltipBehavior());
        help.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return StringUtils.isNotEmpty(helpText.getObject());
            }
        });
        labelContainer.add(help);

        WebMarkupContainer required = new WebMarkupContainer("required");
        required.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                IW wrapper = model.getObject();
                Item property = wrapper.getItem();
                ItemDefinition def = property.getDefinition();

                if (ObjectType.F_NAME.equals(def.getName())) {
                    //fix for "name as required" MID-789
                    return true;
                }

                return def.isMandatory();
            }
        });
        labelContainer.add(required);

        WebMarkupContainer hasOutbound = new WebMarkupContainer("hasOutbound");
        hasOutbound.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return hasOutbound(model);
            }
        });
        labelContainer.add(hasOutbound);

        WebMarkupContainer hasPendingModification = new WebMarkupContainer(ID_HAS_PENDING_MODIFICATION);
        hasPendingModification.add(new VisibleEnableBehaviour() {
        	private static final long serialVersionUID = 1L;

            @Override
            public boolean isVisible() {
                return hasPendingModification(model);
            }
        });
        labelContainer.add(hasPendingModification);

        ListView<ValueWrapper> values = new ListView<ValueWrapper>("values",
                new PropertyModel<List<ValueWrapper>>(model, "values")) {
        	private static final long serialVersionUID = 1L;

            @Override
            protected void populateItem(final ListItem<ValueWrapper> item) {
                PrismValuePanel panel = new PrismValuePanel("value", item.getModel(), label, form, getValueCssClass(), getInputCssClass(), pageBase);
                item.add(panel);
                item.add(AttributeModifier.append("class", createStyleClassModel(item.getModel())));

                item.add(new VisibleEnableBehaviour() {
                	private static final long serialVersionUID = 1L;

                    @Override
                    public boolean isVisible() {
                        return isVisibleValue(item.getModel());
                    }
                });
            }
        };
        values.add(new AttributeModifier("class", getValuesClass()));
        values.setReuseItems(true);
        add(values);
    }

    protected String getInputCssClass() {
        return"col-xs-10";
    }

    protected String getValuesClass() {
        return "col-md-6";
    }

    protected String getValueCssClass() {
        return "row";
    }

    private String loadHelpText(IModel<IW> model) {
        Item property = (Item) model.getObject().getItem();
        ItemDefinition def = property.getDefinition();
        String doc = def.getHelp();
        if (StringUtils.isEmpty(doc)) {
            return null;
        }

        return PageBase.createStringResourceStatic(this, doc).getString();
//        return StringResourceModelMigration.of(doc, null, doc).getString();
    }

    protected IModel<String> createStyleClassModel(final IModel<ValueWrapper> value) {
        return new AbstractReadOnlyModel<String>() {
        	private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                if (getIndexOfValue(value.getObject()) > 0) {
                    return "col-md-offset-2 prism-value";
                }

                return null;
            }
        };
    }

    private int getIndexOfValue(ValueWrapper value) {
        ItemWrapper property = value.getItem();
        List<ValueWrapper> values = property.getValues();
        for (int i = 0; i < values.size(); i++) {
            if (values.get(i).equals(value)) {
                return i;
            }
        }

        return -1;
    }

    private boolean hasOutbound(IModel<IW> model) {
        ItemWrapper wrapper = model.getObject();
        Item property = wrapper.getItem();
        ItemDefinition def = property.getDefinition();
        if (!(def instanceof RefinedAttributeDefinition)) {
            return false;
        }

        RefinedAttributeDefinition refinedDef = (RefinedAttributeDefinition) def;
        return refinedDef.hasOutboundMapping();
    }

    private boolean hasPendingModification(IModel<IW> model) {
        ItemWrapper propertyWrapper = model.getObject();
        ContainerWrapper containerWrapper = propertyWrapper.getContainer();
        if (containerWrapper == null) {
            return false;           // TODO - ok?
        }
        ObjectWrapper objectWrapper = containerWrapper.getObject();

		if (objectWrapper == null) {
			return false;
		}
        PrismObject prismObject = objectWrapper.getObject();
        if (!ShadowType.class.isAssignableFrom(prismObject.getCompileTimeClass())) {
            return false;
        }

        PrismProperty objectChange = prismObject.findProperty(ShadowType.F_OBJECT_CHANGE);
        if (objectChange == null || objectChange.getValue() == null) {
            return false;
        }

        ItemPath path = propertyWrapper.getItem().getPath();
        ObjectDeltaType delta = (ObjectDeltaType) objectChange.getValue().getValue();
        try {
            for (ItemDeltaType itemDelta : delta.getItemDelta()) {
                ItemDelta iDelta = DeltaConvertor.createItemDelta(itemDelta, (Class<? extends Objectable>)
                        prismObject.getCompileTimeClass(), prismObject.getPrismContext());
                if (iDelta.getPath().equivalent(path)) {
                    return true;
                }
            }
        } catch (SchemaException ex) {
            LoggingUtils.logUnexpectedException(LOGGER, "Couldn't check if property has pending modification", ex);
        }

        return false;
    }

    private IModel<String> createDisplayName(final IModel<IW> model) {
        return new AbstractReadOnlyModel<String>() {
        	private static final long serialVersionUID = 1L;

            @Override
            public String getObject() {
                IW wrapper = model.getObject();
                String displayName = wrapper.getDisplayName();
                String displayNameValueByKey = PageBase.createStringResourceStatic(PrismPropertyPanel.this, displayName).getString();
                return StringUtils.isEmpty(displayNameValueByKey) ?
                        getString(displayName, null, displayName) : displayNameValueByKey;
            }
        };
    }

    private boolean isVisibleValue(IModel<ValueWrapper> model) {
        ValueWrapper value = model.getObject();
        return !ValueStatus.DELETED.equals(value.getStatus());
    }

    public boolean isLabelContainerVisible() {
        return labelContainerVisible;
    }

    public void setLabelContainerVisible(boolean labelContainerVisible) {
        this.labelContainerVisible = labelContainerVisible;
    }
}
