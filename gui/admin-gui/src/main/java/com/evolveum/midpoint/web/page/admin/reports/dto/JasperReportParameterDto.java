package com.evolveum.midpoint.web.page.admin.reports.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.evolveum.midpoint.web.component.util.Editable;
import com.evolveum.midpoint.web.component.util.Selectable;
import com.evolveum.midpoint.web.component.util.Validatable;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRPropertiesMap;

public class JasperReportParameterDto<T> extends Selectable implements Serializable, Editable, Validatable {

   private static final long serialVersionUID = 1L;
	private String name;
    private Class<T> type;
    private String typeAsString;
//	private ItemPath path;
    private String description;
    private Class nestedType;
    private boolean forPrompting = false;
    private JasperReportValueDto<T> value;

    private JRPropertiesMap properties;

    private boolean editing;

    public JasperReportParameterDto() {
        // TODO Auto-generated constructor stub
    }

    public void setNestedType(Class nestedType) {
        this.nestedType = nestedType;
    }

    public Class getNestedType() {
        return nestedType;
    }

    public JasperReportParameterDto(JRParameter param) {
        this.name = param.getName();
        this.typeAsString = param.getValueClassName();
        this.type = (Class<T>) param.getValueClass();
        this.forPrompting = param.isForPrompting();

        if (param.getDescription() != null){
		this.description = param.getDescription();
	}
	if (param.getNestedType() != null){
		this.nestedType = param.getNestedType();
	}

	this.value = new JasperReportValueDto<T>(param.getPropertiesMap());




    }


    public JasperReportValueDto<T> getValue() {
		return value;
	}

    public void setValue(JasperReportValueDto<T> value) {
		this.value = value;
	}


    public boolean isForPrompting() {
        return forPrompting;
    }

    public void setForPrompting(boolean forPrompting) {
        this.forPrompting = forPrompting;
    }

    public boolean getForPrompting() {
        return forPrompting;
    }

    public String getName() {
        return name;
    }

    public String getTypeAsString() {
        return typeAsString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setProperties(JRPropertiesMap properties) {
        this.properties = properties;
    }

    public JRPropertiesMap getProperties() {
        if (properties == null) {
            return null;
        }
       return this.value.getPropertiesMap();

    }


    public Class<T> getType() throws ClassNotFoundException {
        if (type == null) {
            if (StringUtils.isNotBlank(typeAsString)) {
                type = (Class<T>) Class.forName(typeAsString);
            } else {
                type = (Class<T>) Object.class;
            }
        }
        return type;
    }

    @Override
    public boolean isEditing() {
        return editing;
    }

    @Override
    public void setEditing(boolean editing) {
        this.editing = editing;
    }

    @Override
    public boolean isEmpty() {
        if (StringUtils.isBlank(name) && StringUtils.isBlank(typeAsString)) {
            return true;
        }
        return false;
    }
}
