/*
 * Copyright (c) 2010-2013 Evolveum
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
package com.evolveum.midpoint.model.api;


import com.evolveum.midpoint.xml.ns._public.common.common_3.ModelExecuteOptionsType;

import java.io.Serializable;
import java.util.List;

/**
 * @author semancik
 *
 */
public class ModelExecuteOptions implements Serializable, Cloneable {
	
	/**
	 * Force the operation even if it would otherwise fail due to external failure. E.g. attempt to delete an account
	 * that no longer exists on resource may fail without a FORCE option. If FORCE option is used then the operation is
	 * finished even if the account does not exist (e.g. at least shadow is removed from midPoint repository).
	 */
	Boolean force;
	
	/**
	 * Avoid any smart processing of the data except for schema application. Do not synchronize the data, do not apply
	 * any expressions, etc.
	 */
	Boolean raw;
	
	/**
	 * Encrypt any cleartext data on write, decrypt any encrypted data on read. Applies only to the encrypted
	 * data formats (ProtectedString, ProtectedByteArray).
	 */
	Boolean noCrypt;
	
	/**
	 * Option to reconcile focus while executing changes.
	 */
	Boolean reconcile;

    /**
     * Option to reconcile affected objects after executing changes.
     * Typical use: after a role is changed, all users that have been assigned this role
     * would be reconciled.
     *
     * Because it is difficult to determine all affected objects (e.g. users that have
     * indirectly assigned a role), midPoint does a reasonable attempt to determine
     * and reconcile them. E.g. it may be limited to a direct assignees.
     *
     * Also, because of time complexity, the reconciliation may be executed in
     * a separate background task.
     */
    Boolean reconcileAffected;

    /**
     * Option to execute changes as soon as they are approved. (For the primary stage approvals, the default behavior
     * is to wait until all changes are approved/rejected and then execute the operation as a whole.)
     */
    Boolean executeImmediatelyAfterApproval;

    /**
     * Option to user overwrite flag. It can be used from web service, if we want to re-import some object
     */
    Boolean overwrite;
    
    Boolean isImport;

	/**
	 * Causes reevaluation of search filters (producing partial errors on failure).
	 */
	Boolean reevaluateSearchFilters;
    
    /**
     * Option to limit propagation only for the source resource
     */
    Boolean limitPropagation;

	/**
	 * Is this operation already authorized, i.e. should it be executed without any further authorization checks?
	 * EXPERIMENTAL. Currently supported only for raw executions.
	 */
	Boolean preAuthorized;

    public Boolean getForce() {
		return force;
	}

	public void setForce(Boolean force) {
		this.force = force;
	}
	
	public static boolean isForce(ModelExecuteOptions options) {
		if (options == null) {
			return false;
		}
		if (options.force == null) {
			return false;
		}
		return options.force;
	}
	
	public static ModelExecuteOptions createForce() {
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setForce(true);
		return opts;
	}

	public ModelExecuteOptions setForce() {
		setForce(true);
		return this;
	}

	public Boolean getRaw() {
		return raw;
	}

	public void setRaw(Boolean raw) {
		this.raw = raw;
	}
	
	public static boolean isRaw(ModelExecuteOptions options) {
		if (options == null) {
			return false;
		}
		if (options.raw == null) {
			return false;
		}
		return options.raw;
	}
	
	public static ModelExecuteOptions createRaw() {
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setRaw(true);
		return opts;
	}

	public ModelExecuteOptions setRaw() {
		setRaw(true);
		return this;
	}

	public Boolean getNoCrypt() {
		return noCrypt;
	}

	public void setNoCrypt(Boolean noCrypt) {
		this.noCrypt = noCrypt;
	}
	
	public static boolean isNoCrypt(ModelExecuteOptions options) {
		if (options == null) {
			return false;
		}
		if (options.noCrypt == null) {
			return false;
		}
		return options.noCrypt;
	}

	public static ModelExecuteOptions createNoCrypt() {
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setNoCrypt(true);
		return opts;
	}

	public ModelExecuteOptions setNoCrypt() {
		setNoCrypt(true);
		return this;
	}

	public Boolean getReconcile() {
		return reconcile;
	}
	
	public void setReconcile(Boolean reconcile) {
		this.reconcile = reconcile;
	}
	
	public static boolean isReconcile(ModelExecuteOptions options){
		if (options == null){
			return false;
		}
		if (options.reconcile == null){
			return false;
		}
		return options.reconcile;
	}
	
	public static ModelExecuteOptions createReconcile(){
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setReconcile(true);
		return opts;
	}

	public ModelExecuteOptions setReconcile(){
		setReconcile(true);
		return this;
	}

    public Boolean getReconcileAffected() {
        return reconcileAffected;
    }

    public void setReconcileAffected(Boolean reconcile) {
        this.reconcileAffected = reconcile;
    }

    public static boolean isReconcileAffected(ModelExecuteOptions options){
        if (options == null){
            return false;
        }
        if (options.reconcileAffected == null){
            return false;
        }
        return options.reconcileAffected;
    }

    public static ModelExecuteOptions createReconcileAffected(){
        ModelExecuteOptions opts = new ModelExecuteOptions();
        opts.setReconcileAffected(true);
        return opts;
    }

	public ModelExecuteOptions setReconcileAffected() {
		setReconcileAffected(true);
		return this;
	}

    public Boolean getOverwrite() {
		return overwrite;
	}
	
	public void setOverwrite(Boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	public static boolean isOverwrite(ModelExecuteOptions options){
		if (options == null){
			return false;
		}
		if (options.overwrite == null){
			return false;
		}
		return options.overwrite;
	}
	
	public static ModelExecuteOptions createOverwrite(){
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setOverwrite(true);
		return opts;
	}

	public ModelExecuteOptions setOverwrite() {
		setOverwrite(true);
		return this;
	}

	public Boolean getIsImport() {
		return isImport;
	}
	
	public void setIsImport(Boolean isImport) {
		this.isImport = isImport;
	}
	
	public static boolean isIsImport(ModelExecuteOptions options){
		if (options == null){
			return false;
		}
		if (options.isImport == null){
			return false;
		}
		return options.isImport;
	}
	
	public static ModelExecuteOptions createIsImport(){
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setIsImport(true);
		return opts;
	}

	public ModelExecuteOptions setIsImport(){
		setIsImport(true);
		return this;
	}
	
    public void setExecuteImmediatelyAfterApproval(Boolean executeImmediatelyAfterApproval) {
        this.executeImmediatelyAfterApproval = executeImmediatelyAfterApproval;
    }

    public static boolean isExecuteImmediatelyAfterApproval(ModelExecuteOptions options){
        if (options == null){
            return false;
        }
        if (options.executeImmediatelyAfterApproval == null) {
            return false;
        }
        return options.executeImmediatelyAfterApproval;
    }

	public static ModelExecuteOptions createExecuteImmediatelyAfterApproval(){
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setExecuteImmediatelyAfterApproval(true);
		return opts;
	}

	public void setLimitPropagation(Boolean limitPropagation) {
		this.limitPropagation = limitPropagation;
	}
    
    public static boolean isLimitPropagation(ModelExecuteOptions options){
    	if (options == null){
    		return false;
    	}
    	if (options.limitPropagation == null){
    		return false;
    	}
    	return options.limitPropagation;
    }

	public static ModelExecuteOptions createLimitPropagation() {
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setLimitPropagation(true);
		return opts;
	}

	public ModelExecuteOptions setLimitPropagation() {
		setLimitPropagation(true);
		return this;
	}


	public Boolean getReevaluateSearchFilters() {
		return reevaluateSearchFilters;
	}

	public void setReevaluateSearchFilters(Boolean reevaluateSearchFilters) {
		this.reevaluateSearchFilters = reevaluateSearchFilters;
	}

	public static boolean isReevaluateSearchFilters(ModelExecuteOptions options){
		if (options == null){
			return false;
		}
		if (options.reevaluateSearchFilters == null){
			return false;
		}
		return options.reevaluateSearchFilters;
	}

	public static ModelExecuteOptions createReevaluateSearchFilters(){
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setReevaluateSearchFilters(true);
		return opts;
	}

	public ModelExecuteOptions setReevaluateSearchFilters(){
		setReevaluateSearchFilters(true);
		return this;
	}

	public Boolean getPreAuthorized() {
		return preAuthorized;
	}

	public void setPreAuthorized(Boolean value) {
		this.preAuthorized = value;
	}

	public static boolean isPreAuthorized(ModelExecuteOptions options) {
		if (options == null) {
			return false;
		}
		if (options.preAuthorized == null){
			return false;
		}
		return options.preAuthorized;
	}

	public static ModelExecuteOptions createPreAuthorized() {
		ModelExecuteOptions opts = new ModelExecuteOptions();
		opts.setPreAuthorized(true);
		return opts;
	}

	public ModelExecuteOptions setPreAuthorized() {
		setPreAuthorized(true);
		return this;
	}

	public ModelExecuteOptionsType toModelExecutionOptionsType() {
        ModelExecuteOptionsType retval = new ModelExecuteOptionsType();
        retval.setForce(force);
        retval.setRaw(raw);
        retval.setNoCrypt(noCrypt);
        retval.setReconcile(reconcile);
        retval.setExecuteImmediatelyAfterApproval(executeImmediatelyAfterApproval);
        retval.setOverwrite(overwrite);
        retval.setIsImport(isImport);
        retval.setLimitPropagation(limitPropagation);
		retval.setReevaluateSearchFilters(reevaluateSearchFilters);
		// preAuthorized is purposefully omitted (security reasons)
        return retval;
    }

    public static ModelExecuteOptions fromModelExecutionOptionsType(ModelExecuteOptionsType type) {
        if (type == null) {
            return null;
        }
        ModelExecuteOptions retval = new ModelExecuteOptions();
        retval.setForce(type.isForce());
        retval.setRaw(type.isRaw());
        retval.setNoCrypt(type.isNoCrypt());
        retval.setReconcile(type.isReconcile());
        retval.setExecuteImmediatelyAfterApproval(type.isExecuteImmediatelyAfterApproval());
        retval.setOverwrite(type.isOverwrite());
        retval.setIsImport(type.isIsImport());
        retval.setLimitPropagation(type.isLimitPropagation());
		retval.setReevaluateSearchFilters(type.isReevaluateSearchFilters());
		// preAuthorized is purposefully omitted (security reasons)
        return retval;
    }
    
    public static ModelExecuteOptions fromRestOptions(List<String> options){
    	if (options == null || options.isEmpty()){
    		return null;
    	}
    	
    	ModelExecuteOptions retVal = new ModelExecuteOptions();
    	for (String option : options){
    		if (ModelExecuteOptionsType.F_RAW.getLocalPart().equals(option)){
    			retVal.setRaw(true);
    		}
    		if (ModelExecuteOptionsType.F_EXECUTE_IMMEDIATELY_AFTER_APPROVAL.getLocalPart().equals(option)){
    			retVal.setExecuteImmediatelyAfterApproval(true);
    		}
    		if (ModelExecuteOptionsType.F_FORCE.getLocalPart().equals(option)){
    			retVal.setForce(true);
    		}
    		if (ModelExecuteOptionsType.F_NO_CRYPT.getLocalPart().equals(option)){
    			retVal.setNoCrypt(true);
    		}
    		if (ModelExecuteOptionsType.F_OVERWRITE.getLocalPart().equals(option)){
    			retVal.setOverwrite(true);
    		}
    		if (ModelExecuteOptionsType.F_RECONCILE.getLocalPart().equals(option)){
    			retVal.setReconcile(true);
    		}
    		if (ModelExecuteOptionsType.F_IS_IMPORT.getLocalPart().equals(option)){
    			retVal.setIsImport(true);
    		}
    		if (ModelExecuteOptionsType.F_LIMIT_PROPAGATION.getLocalPart().equals(option)){
    			retVal.setIsImport(true);
    		}
			if (ModelExecuteOptionsType.F_REEVALUATE_SEARCH_FILTERS.getLocalPart().equals(option)){
				retVal.setReevaluateSearchFilters(true);
			}
			// preAuthorized is purposefully omitted (security reasons)
    	}
    	
    	return retVal;
    }
    
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder("ModelExecuteOptions(");
    	appendFlag(sb, "executeImmediatelyAfterApproval", executeImmediatelyAfterApproval);
    	appendFlag(sb, "force", force);
    	appendFlag(sb, "isImport", isImport);
    	appendFlag(sb, "limitPropagation", limitPropagation);
    	appendFlag(sb, "noCrypt", noCrypt);
    	appendFlag(sb, "overwrite", overwrite);
    	appendFlag(sb, "preAuthorized", preAuthorized);
    	appendFlag(sb, "raw", raw);
    	appendFlag(sb, "reconcile", reconcile);
    	appendFlag(sb, "reevaluateSearchFilters", reevaluateSearchFilters);
    	appendFlag(sb, "reconcileAffected", reconcileAffected);
    	if (sb.charAt(sb.length() - 1) == ',') {
			sb.deleteCharAt(sb.length() - 1);
		}
		sb.append(")");
		return sb.toString();
    }
    
    private void appendFlag(StringBuilder sb, String name, Boolean val) {
		if (val == null) {
			return;
		} else if (val) {
			sb.append(name);
			sb.append(",");
		} else {
			sb.append(name);
			sb.append("=false,");
		}
	}
	
	private void appendVal(StringBuilder sb, String name, Object val) {
		if (val == null) {
			return;
		} else {
			sb.append(name);
			sb.append("=");
			sb.append(val);
			sb.append(",");
		}
	}

    public ModelExecuteOptions clone() {
        // not much efficient, but...
        ModelExecuteOptions clone = fromModelExecutionOptionsType(toModelExecutionOptionsType());
		clone.setPreAuthorized(this.preAuthorized);
		return clone;
    }

}
