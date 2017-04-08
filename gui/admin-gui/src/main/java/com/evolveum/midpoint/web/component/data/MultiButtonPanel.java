/*
 * Copyright (c) 2010-2016 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.component.data;

import com.evolveum.midpoint.gui.api.component.BasePanel;
import com.evolveum.midpoint.web.component.AjaxButton;
import com.evolveum.midpoint.web.component.data.column.DoubleButtonColumn;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;

/**
 * @author shood
 * @author mederly
 */
public class MultiButtonPanel<T> extends BasePanel<T> {

    private static final String ID_BUTTONS = "buttons";

    int numberOfButtons;

    public MultiButtonPanel(String id, int numberOfButtons, IModel<T> model) {
        super(id, model);
        this.numberOfButtons = numberOfButtons;
        initLayout();
    }

    private void initLayout() {
        RepeatingView buttons = new RepeatingView(ID_BUTTONS);
        add(buttons);
        for (int id = 0; id < numberOfButtons; id++) {
            final int finalId = id;
            AjaxButton button = new AjaxButton(String.valueOf(finalId), createStringResource(getCaption(finalId))) {
                @Override
                public void onClick(AjaxRequestTarget target) {
                    clickPerformed(finalId, target, MultiButtonPanel.this.getModel());
                }
                @Override
                public boolean isEnabled(){
                    return MultiButtonPanel.this.isButtonEnabled(finalId, MultiButtonPanel.this.getModel());
                }
                @Override
                public boolean isVisible(){
                    return MultiButtonPanel.this.isButtonVisible(finalId, MultiButtonPanel.this.getModel());
                }
            };
            button.add(new AttributeAppender("class", getButtonCssClass(finalId)));
            button.add(new AttributeAppender("title", getButtonTitle(finalId)));
            buttons.add(button);
            buttons.add(new Label("label"+finalId, " "));
        }
    }

    public String getCaption(int id) {
        return String.valueOf(id);
    }

    public boolean isButtonEnabled(int id, IModel<T> model) {
        return true;
    }

    public boolean isButtonVisible(int id, IModel<T> model) {
        return true;
    }


    protected String getButtonCssClass(int id) {
        StringBuilder sb = new StringBuilder();
        sb.append(DoubleButtonColumn.BUTTON_BASE_CLASS).append(" ");
        sb.append(getButtonColorCssClass(id)).append(" ").append(getButtonSizeCssClass(id));
        return sb.toString();
    }

    public String getButtonSizeCssClass(int id) {
        return DoubleButtonColumn.BUTTON_SIZE_CLASS.DEFAULT.toString();
    }

    public String getButtonTitle(int id) {
        return "";
    }

    public String getButtonColorCssClass(int id) {
        return DoubleButtonColumn.BUTTON_COLOR_CLASS.DEFAULT.toString();
    }

    public void clickPerformed(int id, AjaxRequestTarget target, IModel<T> model) {
    }

    public AjaxButton getButton(int id){
        return (AjaxButton) get(ID_BUTTONS).get(String.valueOf(id));
    }

}
