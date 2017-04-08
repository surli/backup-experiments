/*
 * Copyright (c) 2010-2015 Evolveum
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

package com.evolveum.midpoint.web.page.admin.roles;

import com.evolveum.midpoint.gui.api.GuiStyleConstants;
import com.evolveum.midpoint.gui.api.component.MainObjectListPanel;
import com.evolveum.midpoint.security.api.AuthorizationConstants;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.application.AuthorizationAction;
import com.evolveum.midpoint.web.application.PageDescriptor;
import com.evolveum.midpoint.web.component.data.column.InlineMenuHeaderColumn;
import com.evolveum.midpoint.web.component.menu.cog.InlineMenuItem;
import com.evolveum.midpoint.web.component.search.Search;
import com.evolveum.midpoint.web.component.util.FocusListComponent;
import com.evolveum.midpoint.web.component.util.FocusListInlineMenuHelper;
import com.evolveum.midpoint.web.component.util.SelectableBean;
import com.evolveum.midpoint.web.session.UserProfileStorage.TableId;
import com.evolveum.midpoint.web.util.OnePageParameterEncoder;
import com.evolveum.midpoint.xml.ns._public.common.common_3.RoleType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lazyman
 */
@PageDescriptor(url = "/admin/roles", action = {
        @AuthorizationAction(actionUri = PageAdminRoles.AUTH_ROLE_ALL,
                label = PageAdminRoles.AUTH_ROLE_ALL_LABEL,
                description = PageAdminRoles.AUTH_ROLE_ALL_DESCRIPTION),
        @AuthorizationAction(actionUri = AuthorizationConstants.AUTZ_UI_ROLES_URL,
                label = "PageRoles.auth.roles.label",
                description = "PageRoles.auth.roles.description")})
public class PageRoles extends PageAdminRoles implements FocusListComponent {

    private static final Trace LOGGER = TraceManager.getTrace(PageRoles.class);
    private static final String DOT_CLASS = PageRoles.class.getName() + ".";

    private static final String ID_TABLE = "table";
    private static final String ID_MAIN_FORM = "mainForm";

    private IModel<Search> searchModel;

    public PageRoles() {
        this(false);
    }

    public PageRoles(boolean clearPagingInSession) {
        initLayout();
    }

	private final FocusListInlineMenuHelper<RoleType> listInlineMenuHelper = new FocusListInlineMenuHelper<>(RoleType.class, this, this);

    private void initLayout() {
        Form mainForm = new Form(ID_MAIN_FORM);
        add(mainForm);
        
        MainObjectListPanel<RoleType> roleListPanel = new MainObjectListPanel<RoleType>(ID_TABLE, RoleType.class, TableId.TABLE_ROLES, null, this) {

			@Override
			protected List<InlineMenuItem> createInlineMenu() {
				return listInlineMenuHelper.createRowActions();
			}

			@Override
			protected List<IColumn<SelectableBean<RoleType>, String>> createColumns() {
				return PageRoles.this.initColumns();
			}
			
			@Override
			protected void objectDetailsPerformed(AjaxRequestTarget target, RoleType object) {
				PageRoles.this.roleDetailsPerformed(target, object.getOid());;
			}
			
			@Override
			protected void newObjectPerformed(AjaxRequestTarget target) {
				setResponsePage(PageRole.class);
			}
		};
		roleListPanel.setOutputMarkupId(true);
		roleListPanel.setAdditionalBoxCssClasses(GuiStyleConstants.CLASS_OBJECT_ROLE_BOX_CSS_CLASSES);
		mainForm.add(roleListPanel);
    }

    private List<IColumn<SelectableBean<RoleType>, String>> initColumns() {
        List<IColumn<SelectableBean<RoleType>, String>> columns = new ArrayList<>();

        IColumn column = new PropertyColumn(createStringResource("OrgType.displayName"), "value.displayName");
        columns.add(column);

        column = new PropertyColumn(createStringResource("OrgType.identifier"), "value.identifier");
        columns.add(column);

        column = new PropertyColumn(createStringResource("ObjectType.description"), "value.description");
        columns.add(column);

        column = new InlineMenuHeaderColumn(listInlineMenuHelper.initInlineMenu());
        columns.add(column);

        return columns;
    }

    private MainObjectListPanel<RoleType> getRoleTable() {
        return (MainObjectListPanel<RoleType>) get(createComponentPath(ID_MAIN_FORM, ID_TABLE));
    }

    private void roleDetailsPerformed(AjaxRequestTarget target, String oid) {
        PageParameters parameters = new PageParameters();
        parameters.add(OnePageParameterEncoder.PARAMETER, oid);
        setResponsePage(PageRole.class, parameters);
    }

	@Override
	public MainObjectListPanel<RoleType> getObjectListPanel() {
		return (MainObjectListPanel<RoleType>) get(createComponentPath(ID_MAIN_FORM, ID_TABLE));
	}

}
