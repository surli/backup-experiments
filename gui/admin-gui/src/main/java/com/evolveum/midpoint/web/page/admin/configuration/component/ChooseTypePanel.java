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

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.gui.api.component.ObjectBrowserPanel;
import com.evolveum.midpoint.gui.api.util.WebComponentUtil;
import com.evolveum.midpoint.prism.query.ObjectQuery;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.page.admin.dto.ObjectViewDto;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 *  @author shood
 *
 *  TODO use a better name (ChooseObjectPanel ? ObjectChoosePanel ?)
 *  Distinguish between chooser panels that reside on "main page" and
 *  the one that resides in the popup window (ObjectSelectionPanel).
 */
public class ChooseTypePanel<T extends ObjectType> extends BasePanel<ObjectViewDto<T>> {
	private static final long serialVersionUID = 1L;

	private static final Trace LOGGER = TraceManager.getTrace(ChooseTypePanel.class);

    private static final String ID_OBJECT_NAME = "name";
    private static final String ID_LINK_CHOOSE = "choose";
    private static final String ID_LINK_REMOVE = "remove";

    public ChooseTypePanel(String id, IModel<ObjectViewDto<T>> model){
        super(id, model);
        initLayout();
    }

    protected void initLayout() {

        final Label name = new Label(ID_OBJECT_NAME, new AbstractReadOnlyModel<String>(){
        	private static final long serialVersionUID = 1L;
        	
            @Override
            public String getObject() {
                ObjectViewDto<T> dto = getModel().getObject();
                if (dto != null) {
                    if (dto.getName() != null)
                        return getModel().getObject().getName();
                    else if (ObjectViewDto.BAD_OID.equals(dto.getOid())) {
                        return createStringResource("chooseTypePanel.ObjectNameValue.badOid").getString();
                    } else {
                        return createStringResource("chooseTypePanel.ObjectNameValue.null").getString();
                    }
                }
                return "";
            }
        });
        name.setOutputMarkupId(true);

        AjaxLink<String> choose = new AjaxLink<String>(ID_LINK_CHOOSE) {
        	private static final long serialVersionUID = 1L;
        	
            @Override
            public void onClick(AjaxRequestTarget target) {
                 changeOptionPerformed(target);
            }
        };

        AjaxLink<String> remove = new AjaxLink<String>(ID_LINK_REMOVE) {
        	private static final long serialVersionUID = 1L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                setToDefault();
                target.add(name);
            }
        };

        add(choose);
        add(remove);
        add(name);

    }

    protected  boolean isSearchEnabled(){
        return false;
    }

    protected QName getSearchProperty(){
        return null;
    }

    protected ObjectQuery getChooseQuery(){
        return null;
    }

    private void choosePerformed(AjaxRequestTarget target, T object){
    	getPageBase().hideMainPopup(target);
        ObjectViewDto<T> o = getModel().getObject();

        o.setName(WebComponentUtil.getName(object));
        o.setOid(object.getOid());

        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("Choose operation performed: {} ({})", o.getName(), o.getOid());
        }

        target.add(get(ID_OBJECT_NAME));
    }

    private void changeOptionPerformed(AjaxRequestTarget target){
    	Class<T> type = getObjectTypeClass();
    	List<QName> supportedTypes = new ArrayList<>();
    	supportedTypes.add(WebComponentUtil.classToQName(getPageBase().getPrismContext(), type));
    	ObjectBrowserPanel<T> objectBrowserPanel = new ObjectBrowserPanel<T>(getPageBase().getMainPopupBodyId(),
                type, supportedTypes, false, getPageBase(), getChooseQuery() != null ? getChooseQuery().getFilter() : null){
    		private static final long serialVersionUID = 1L;

			@Override
    		protected void onSelectPerformed(AjaxRequestTarget target, T focus) {
    			choosePerformed(target, focus);
    		}
    	};
    	objectBrowserPanel.setOutputMarkupId(true);
    	
    	getPageBase().showMainPopup(objectBrowserPanel, target);
    }

    private void setToDefault(){
        ObjectViewDto<T> dto = new ObjectViewDto<T>();
        dto.setType(getObjectTypeClass());
        getModel().setObject(dto);
    }

    public Class<T> getObjectTypeClass(){
        return ChooseTypePanel.this.getModelObject().getType();
    }

    public void setPanelEnabled(boolean isEnabled){
        get(ID_LINK_CHOOSE).setEnabled(isEnabled);
        get(ID_LINK_REMOVE).setEnabled(isEnabled);
    }
}
