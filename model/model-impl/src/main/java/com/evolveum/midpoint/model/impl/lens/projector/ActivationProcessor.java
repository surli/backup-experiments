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
package com.evolveum.midpoint.model.impl.lens.projector;

import com.evolveum.midpoint.model.api.context.SynchronizationPolicyDecision;
import com.evolveum.midpoint.model.common.expression.ExpressionUtil;
import com.evolveum.midpoint.model.common.expression.ItemDeltaItem;
import com.evolveum.midpoint.model.common.expression.Source;
import com.evolveum.midpoint.model.common.mapping.Mapping;
import com.evolveum.midpoint.model.impl.lens.LensContext;
import com.evolveum.midpoint.model.impl.lens.LensFocusContext;
import com.evolveum.midpoint.model.impl.lens.LensProjectionContext;
import com.evolveum.midpoint.model.impl.lens.LensUtil;
import com.evolveum.midpoint.model.impl.lens.SynchronizationIntent;
import com.evolveum.midpoint.prism.*;
import com.evolveum.midpoint.prism.delta.ObjectDelta;
import com.evolveum.midpoint.prism.delta.PrismValueDeltaSetTriple;
import com.evolveum.midpoint.prism.delta.PropertyDelta;
import com.evolveum.midpoint.prism.path.ItemPath;
import com.evolveum.midpoint.schema.CapabilityUtil;
import com.evolveum.midpoint.schema.constants.ExpressionConstants;
import com.evolveum.midpoint.schema.constants.SchemaConstants;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.schema.util.ResourceTypeUtil;
import com.evolveum.midpoint.task.api.Task;
import com.evolveum.midpoint.util.DOMUtil;
import com.evolveum.midpoint.util.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.util.exception.ObjectNotFoundException;
import com.evolveum.midpoint.util.exception.PolicyViolationException;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationStatusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ActivationType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.FocusType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.MappingType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceActivationDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceBidirectionalMappingType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceObjectTypeDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.ActivationCapabilityType;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.ActivationLockoutStatusCapabilityType;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.ActivationStatusCapabilityType;
import com.evolveum.midpoint.xml.ns._public.resource.capabilities_3.ActivationValidityCapabilityType;

import org.apache.commons.lang.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import java.util.Collection;
import java.util.List;

/**
 * The processor that takes care of user activation mapping to an account (outbound direction).
 * 
 * @author Radovan Semancik
 */
@Component
public class ActivationProcessor {

    private static final Trace LOGGER = TraceManager.getTrace(ActivationProcessor.class);

	private static final QName SHADOW_EXISTS_PROPERTY_NAME = new QName(SchemaConstants.NS_C, "shadowExists");
	private static final QName LEGAL_PROPERTY_NAME = new QName(SchemaConstants.NS_C, "legal");
    private static final QName ASSIGNED_PROPERTY_NAME = new QName(SchemaConstants.NS_C, "assigned");
	private static final QName FOCUS_EXISTS_PROPERTY_NAME = new QName(SchemaConstants.NS_C, "focusExists");
	
	private PrismObjectDefinition<UserType> userDefinition;
	private PrismContainerDefinition<ActivationType> activationDefinition;

    @Autowired
    private PrismContext prismContext;

    @Autowired
    private MappingEvaluator mappingHelper;

    public <O extends ObjectType, F extends FocusType> void processActivation(LensContext<O> context,
    		LensProjectionContext projectionContext, XMLGregorianCalendar now, Task task, OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException, PolicyViolationException {
    	
    	LensFocusContext<O> focusContext = context.getFocusContext();
    	if (focusContext != null && !FocusType.class.isAssignableFrom(focusContext.getObjectTypeClass())) {
    		// We can do this only for focal object.
    		return;
    	}
    	
    	processActivationFocal((LensContext<F>)context, projectionContext, now, task, result);
    }
    
    private <F extends FocusType> void processActivationFocal(LensContext<F> context, 
    		LensProjectionContext projectionContext, XMLGregorianCalendar now, Task task, OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException, PolicyViolationException {
    	LensFocusContext<F> focusContext = context.getFocusContext();
    	if (focusContext == null) {
    		processActivationMetadata(context, projectionContext, now, result);
    		return;
    	}
    	processActivationUserCurrent(context, projectionContext, now, task, result);
    	processActivationMetadata(context, projectionContext, now, result);
    	processActivationUserFuture(context, projectionContext, now, task, result);
    }

    public <F extends FocusType> void processActivationUserCurrent(LensContext<F> context, LensProjectionContext accCtx, 
    		XMLGregorianCalendar now, Task task, OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException, PolicyViolationException {

    	String accCtxDesc = accCtx.toHumanReadableString();
    	SynchronizationPolicyDecision decision = accCtx.getSynchronizationPolicyDecision();
    	SynchronizationIntent synchronizationIntent = accCtx.getSynchronizationIntent();
    	
    	if (decision == SynchronizationPolicyDecision.BROKEN) {
    		LOGGER.trace("Broken projection {}, skipping further activation processing", accCtxDesc);
    		return;
    	}
    	if (decision != null) {
    		throw new IllegalStateException("Decision "+decision+" already present for projection "+accCtxDesc);
    	}
    	
    	if (accCtx.isThombstone() || synchronizationIntent == SynchronizationIntent.UNLINK) {
    		accCtx.setSynchronizationPolicyDecision(SynchronizationPolicyDecision.UNLINK);
    		LOGGER.trace("Evaluated decision for {} to {}, skipping further activation processing", accCtxDesc, SynchronizationPolicyDecision.UNLINK);
    		return;
    	}
    	
    	if (synchronizationIntent == SynchronizationIntent.DELETE || accCtx.isDelete()) {
    		// TODO: is this OK?
    		accCtx.setSynchronizationPolicyDecision(SynchronizationPolicyDecision.DELETE);
    		LOGGER.trace("Evaluated decision for {} to {}, skipping further activation processing", accCtxDesc, SynchronizationPolicyDecision.DELETE);
    		return;
    	}
    	    	
    	boolean shadowShouldExist = evaluateExistenceMapping(context, accCtx, now, true, task, result);
    	
    	LOGGER.trace("Evaluated intended existence of projection {} to {}", accCtxDesc, shadowShouldExist);
    	
    	// Let's reconcile the existence intent (shadowShouldExist) and the synchronization intent in the context

    	LensProjectionContext lowerOrderContext = LensUtil.findLowerOrderContext(context, accCtx);
    	
    	if (synchronizationIntent == null || synchronizationIntent == SynchronizationIntent.SYNCHRONIZE) {
	    	if (shadowShouldExist) {
	    		accCtx.setActive(true);
	    		if (accCtx.isExists()) {
	    			if (lowerOrderContext != null && lowerOrderContext.isDelete()) {
    					// HACK HACK HACK
    					decision = SynchronizationPolicyDecision.DELETE;
    				} else {
    					decision = SynchronizationPolicyDecision.KEEP;
    				}
	    		} else {
	    			if (lowerOrderContext != null) {
	    				if (lowerOrderContext.isDelete()) {
	    					// HACK HACK HACK
	    					decision = SynchronizationPolicyDecision.DELETE;
	    				} else {
		    				// If there is a lower-order context then that one will be ADD
		    				// and this one is KEEP. When the execution comes to this context
		    				// then the projection already exists
		    				decision = SynchronizationPolicyDecision.KEEP;
	    				}
	    			} else {
	    				decision = SynchronizationPolicyDecision.ADD;
	    			}
	    		}
	    	} else {
	    		// Delete
	    		if (accCtx.isExists()) {
	    			decision = SynchronizationPolicyDecision.DELETE;
	    		} else {
	    			// we should delete the entire context, but then we will lost track of what
	    			// happened. So just ignore it.
	    			decision = SynchronizationPolicyDecision.IGNORE;
	    			// if there are any triggers then move them to focus. We may still need them.
	    			LensUtil.moveTriggers(accCtx, context.getFocusContext());
	    		}
	    	}
	    	
    	} else if (synchronizationIntent == SynchronizationIntent.ADD) {
    		if (shadowShouldExist) {
    			accCtx.setActive(true);
    			if (accCtx.isExists()) {
    				// Attempt to add something that is already there, but should be OK
	    			decision = SynchronizationPolicyDecision.KEEP;
	    		} else {
	    			decision = SynchronizationPolicyDecision.ADD;
	    		}
    		} else {
    			throw new PolicyViolationException("Request to add projection "+accCtxDesc+" but the activation policy decided that it should not exist");
    		}
    		
    	} else if (synchronizationIntent == SynchronizationIntent.KEEP) {
	    	if (shadowShouldExist) {
	    		accCtx.setActive(true);
	    		if (accCtx.isExists()) {
	    			decision = SynchronizationPolicyDecision.KEEP;
	    		} else {
	    			decision = SynchronizationPolicyDecision.ADD;
	    		}
	    	} else {
	    		throw new PolicyViolationException("Request to keep projection "+accCtxDesc+" but the activation policy decided that it should not exist");
	    	}
    		
    	} else {
    		throw new IllegalStateException("Unknown sync intent "+synchronizationIntent);
    	}
    	
    	LOGGER.trace("Evaluated decision for projection {} to {}", accCtxDesc, decision);
    	
    	accCtx.setSynchronizationPolicyDecision(decision);
    	
        PrismObject<F> focusNew = context.getFocusContext().getObjectNew();
        if (focusNew == null) {
            // This must be a user delete or something similar. No point in proceeding
            LOGGER.trace("focusNew is null, skipping activation processing of {}", accCtxDesc);
            return;
        }
    	
    	if (decision == SynchronizationPolicyDecision.UNLINK || decision == SynchronizationPolicyDecision.DELETE) {
    		LOGGER.trace("Decision is {}, skipping activation properties processing for {}", decision, accCtxDesc);
    		return;
    	}
    	
    	ResourceObjectTypeDefinitionType resourceAccountDefType = accCtx.getResourceObjectTypeDefinitionType();
        if (resourceAccountDefType == null) {
            LOGGER.trace("No refined object definition, therefore also no activation outbound definition, skipping activation processing for account " + accCtxDesc);
            return;
        }
        ResourceActivationDefinitionType activationType = resourceAccountDefType.getActivation();
        if (activationType == null) {
            LOGGER.trace("No activation definition in projection {}, skipping activation properties processing", accCtxDesc);
            return;
        }
        
        ActivationCapabilityType capActivation = ResourceTypeUtil.getEffectiveCapability(accCtx.getResource(), ActivationCapabilityType.class);
        if (capActivation == null) {
        	LOGGER.trace("Skipping activation status and validity processing because {} has no activation capability", accCtx.getResource());
        	return;
        }

        ActivationStatusCapabilityType capStatus = CapabilityUtil.getEffectiveActivationStatus(capActivation);
        ActivationValidityCapabilityType capValidFrom = CapabilityUtil.getEffectiveActivationValidFrom(capActivation);
        ActivationValidityCapabilityType capValidTo = CapabilityUtil.getEffectiveActivationValidTo(capActivation);
        ActivationLockoutStatusCapabilityType capLockoutStatus = CapabilityUtil.getEffectiveActivationLockoutStatus(capActivation);
        
        if (capStatus != null) {
	    	evaluateActivationMapping(context, accCtx,
	    			activationType.getAdministrativeStatus(),
	    			SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS, SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS, 
	    			capActivation, now, true, ActivationType.F_ADMINISTRATIVE_STATUS.getLocalPart(), task, result);
        } else {
        	LOGGER.trace("Skipping activation administrative status processing because {} does not have activation administrative status capability", accCtx.getResource());
        }

        ResourceBidirectionalMappingType validFromMappingType = activationType.getValidFrom();
        if (validFromMappingType == null || validFromMappingType.getOutbound() == null) {
        	LOGGER.trace("Skipping activation validFrom processing because {} does not have appropriate outbound mapping", accCtx.getResource());
        } else if (capValidFrom == null && !ExpressionUtil.hasExplicitTarget(validFromMappingType.getOutbound())) {
        	LOGGER.trace("Skipping activation validFrom processing because {} does not have activation validFrom capability nor outbound mapping with explicit target", accCtx.getResource());
        } else {
	    	evaluateActivationMapping(context, accCtx, activationType.getValidFrom(),
	    			SchemaConstants.PATH_ACTIVATION_VALID_FROM, SchemaConstants.PATH_ACTIVATION_VALID_FROM, 
	    			null, now, true, ActivationType.F_VALID_FROM.getLocalPart(), task, result);
        }
	
        ResourceBidirectionalMappingType validToMappingType = activationType.getValidTo();
        if (validToMappingType == null || validToMappingType.getOutbound() == null) {
        	LOGGER.trace("Skipping activation validTo processing because {} does not have appropriate outbound mapping", accCtx.getResource());
        } else if (capValidTo == null && !ExpressionUtil.hasExplicitTarget(validToMappingType.getOutbound())) {
        	LOGGER.trace("Skipping activation validTo processing because {} does not have activation validTo capability nor outbound mapping with explicit target", accCtx.getResource());
        } else {
	    	evaluateActivationMapping(context, accCtx, activationType.getValidTo(),
	    			SchemaConstants.PATH_ACTIVATION_VALID_TO, SchemaConstants.PATH_ACTIVATION_VALID_TO, 
	    			null, now, true, ActivationType.F_VALID_TO.getLocalPart(), task, result);
	    }
        
        if (capLockoutStatus != null) {
	    	evaluateActivationMapping(context, accCtx,
	    			activationType.getLockoutStatus(),
	    			SchemaConstants.PATH_ACTIVATION_LOCKOUT_STATUS, SchemaConstants.PATH_ACTIVATION_LOCKOUT_STATUS, 
	    			capActivation, now, true, ActivationType.F_LOCKOUT_STATUS.getLocalPart(), task, result);
        } else {
        	LOGGER.trace("Skipping activation lockout status processing because {} does not have activation lockout status capability", accCtx.getResource());
        }
    	
    }

    public <F extends FocusType> void processActivationMetadata(LensContext<F> context, LensProjectionContext accCtx, 
    		XMLGregorianCalendar now, OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException, PolicyViolationException {
    	ObjectDelta<ShadowType> projDelta = accCtx.getDelta();
    	if (projDelta == null) {
    		return;
    	}
    	
    	PropertyDelta<ActivationStatusType> statusDelta = projDelta.findPropertyDelta(SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS);
    	
    	if (statusDelta != null && !statusDelta.isDelete()) {

            // we have to determine if the status really changed
            PrismObject<ShadowType> oldShadow = accCtx.getObjectOld();
            ActivationStatusType statusOld = null;
            if (oldShadow != null && oldShadow.asObjectable().getActivation() != null) {
                statusOld = oldShadow.asObjectable().getActivation().getAdministrativeStatus();
            }

            PrismProperty<ActivationStatusType> statusPropNew = (PrismProperty<ActivationStatusType>) statusDelta.getItemNewMatchingPath(null);
            ActivationStatusType statusNew = statusPropNew.getRealValue();

            if (statusNew == statusOld) {
                LOGGER.trace("Administrative status not changed ({}), timestamp and/or reason will be recorded", statusNew);
            } else {
                // timestamps
                PropertyDelta<XMLGregorianCalendar> timestampDelta = LensUtil.createActivationTimestampDelta(statusNew,
                        now, getActivationDefinition(), OriginType.OUTBOUND);
                accCtx.swallowToSecondaryDelta(timestampDelta);

                // disableReason
                if (statusNew == ActivationStatusType.DISABLED) {
                    PropertyDelta<String> disableReasonDelta = projDelta.findPropertyDelta(SchemaConstants.PATH_ACTIVATION_DISABLE_REASON);
                    if (disableReasonDelta == null) {
                        String disableReason = null;
                        ObjectDelta<ShadowType> projPrimaryDelta = accCtx.getPrimaryDelta();
                        ObjectDelta<ShadowType> projSecondaryDelta = accCtx.getSecondaryDelta();
                        if (projPrimaryDelta != null
                                && projPrimaryDelta.findPropertyDelta(SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS) != null
                                && (projSecondaryDelta == null || projSecondaryDelta.findPropertyDelta(SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS) == null)) {
                            disableReason = SchemaConstants.MODEL_DISABLE_REASON_EXPLICIT;
                        } else if (accCtx.isLegal()) {
                            disableReason = SchemaConstants.MODEL_DISABLE_REASON_MAPPED;
                        } else {
                            disableReason = SchemaConstants.MODEL_DISABLE_REASON_DEPROVISION;
                        }

                        PrismPropertyDefinition<String> disableReasonDef = activationDefinition.findPropertyDefinition(ActivationType.F_DISABLE_REASON);
                        disableReasonDelta = disableReasonDef.createEmptyDelta(new ItemPath(FocusType.F_ACTIVATION, ActivationType.F_DISABLE_REASON));
                        disableReasonDelta.setValueToReplace(new PrismPropertyValue<String>(disableReason, OriginType.OUTBOUND, null));
                        accCtx.swallowToSecondaryDelta(disableReasonDelta);
                    }
                }
            }
    	}
    	
    }
    
    public <F extends FocusType> void processActivationUserFuture(LensContext<F> context, LensProjectionContext accCtx,
    		XMLGregorianCalendar now, Task task, OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException, PolicyViolationException {
    	String accCtxDesc = accCtx.toHumanReadableString();
    	SynchronizationPolicyDecision decision = accCtx.getSynchronizationPolicyDecision();
    	SynchronizationIntent synchronizationIntent = accCtx.getSynchronizationIntent();
    	
    	if (accCtx.isThombstone() || decision == SynchronizationPolicyDecision.BROKEN 
    			|| decision == SynchronizationPolicyDecision.IGNORE 
    			|| decision == SynchronizationPolicyDecision.UNLINK || decision == SynchronizationPolicyDecision.DELETE) {
    		return;
    	}
    	
    	accCtx.recompute();
    	
    	evaluateExistenceMapping(context, accCtx, now, false, task, result);
    	
        PrismObject<F> focusNew = context.getFocusContext().getObjectNew();
        if (focusNew == null) {
            // This must be a user delete or something similar. No point in proceeding
            LOGGER.trace("focusNew is null, skipping activation processing of {}", accCtxDesc);
            return;
        }
    	    	
    	ResourceObjectTypeDefinitionType resourceAccountDefType = accCtx.getResourceObjectTypeDefinitionType();
        if (resourceAccountDefType == null) {
            return;
        }
        ResourceActivationDefinitionType activationType = resourceAccountDefType.getActivation();
        if (activationType == null) {
            return;
        }
        
        ActivationCapabilityType capActivation = ResourceTypeUtil.getEffectiveCapability(accCtx.getResource(), ActivationCapabilityType.class);
        if (capActivation == null) {
        	return;
        }

        ActivationStatusCapabilityType capStatus = CapabilityUtil.getEffectiveActivationStatus(capActivation);
        ActivationValidityCapabilityType capValidFrom = CapabilityUtil.getEffectiveActivationValidFrom(capActivation);
        ActivationValidityCapabilityType capValidTo = CapabilityUtil.getEffectiveActivationValidTo(capActivation);
        
        if (capStatus != null) {
        	
	    	evaluateActivationMapping(context, accCtx, activationType.getAdministrativeStatus(),
	    			SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS, SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS, 
	    			capActivation, now, false, ActivationType.F_ADMINISTRATIVE_STATUS.getLocalPart(), task, result);	    	
        }

        if (capValidFrom != null) {
	    	evaluateActivationMapping(context, accCtx, activationType.getAdministrativeStatus(),
	    			SchemaConstants.PATH_ACTIVATION_VALID_FROM, SchemaConstants.PATH_ACTIVATION_VALID_FROM, 
	    			null, now, false, ActivationType.F_VALID_FROM.getLocalPart(), task, result);
        }
	
        if (capValidTo != null) {
	    	evaluateActivationMapping(context, accCtx, activationType.getAdministrativeStatus(),
	    			SchemaConstants.PATH_ACTIVATION_VALID_TO, SchemaConstants.PATH_ACTIVATION_VALID_TO, 
	    			null, now, false, ActivationType.F_VALID_FROM.getLocalPart(), task, result);
	    }
    	
    }

    
    private <F extends FocusType> boolean evaluateExistenceMapping(final LensContext<F> context,
    		final LensProjectionContext accCtx, final XMLGregorianCalendar now, final boolean current,
            Task task, final OperationResult result)
    				throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
    	final String accCtxDesc = accCtx.toHumanReadableString();
    	
    	final Boolean legal = accCtx.isLegal();
    	if (legal == null) {
    		throw new IllegalStateException("Null 'legal' for "+accCtxDesc);
    	}
    	
    	ResourceObjectTypeDefinitionType resourceAccountDefType = accCtx.getResourceObjectTypeDefinitionType();
        if (resourceAccountDefType == null) {
            return legal;
        }
        ResourceActivationDefinitionType activationType = resourceAccountDefType.getActivation();
        if (activationType == null) {
            return legal;
        }
        ResourceBidirectionalMappingType existenceType = activationType.getExistence();
        if (existenceType == null) {
            return legal;
        }
        List<MappingType> outbound = existenceType.getOutbound();
        if (outbound == null || outbound.isEmpty()) {
        	// "default mapping"
            return legal;
        }
        
        MappingInitializer<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> initializer = new MappingInitializer<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>>() {
			@Override
			public Mapping.Builder<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> initialize(Mapping.Builder<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> builder) throws SchemaException {
				// Source: legal
		        ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> legalSourceIdi = getLegalIdi(accCtx); 
		        Source<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> legalSource 
		        	= new Source<>(legalSourceIdi, ExpressionConstants.VAR_LEGAL);
				builder.setDefaultSource(legalSource);

                // Source: assigned
                ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> assignedIdi = getAssignedIdi(accCtx);
                Source<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> assignedSource = new Source<>(assignedIdi, ExpressionConstants.VAR_ASSIGNED);
				builder.addSource(assignedSource);

                // Source: focusExists
		        ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> focusExistsSourceIdi = getFocusExistsIdi(context.getFocusContext()); 
		        Source<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> focusExistsSource 
		        	= new Source<>(focusExistsSourceIdi, ExpressionConstants.VAR_FOCUS_EXISTS);
				builder.addSource(focusExistsSource);
				
				// Variable: focus
				builder.addVariableDefinition(ExpressionConstants.VAR_FOCUS, context.getFocusContext().getObjectDeltaObject());

		        // Variable: user (for convenience, same as "focus")
				builder.addVariableDefinition(ExpressionConstants.VAR_USER, context.getFocusContext().getObjectDeltaObject());

				// Variable: shadow
				builder.addVariableDefinition(ExpressionConstants.VAR_SHADOW, accCtx.getObjectDeltaObject());

				// Variable: resource
				builder.addVariableDefinition(ExpressionConstants.VAR_RESOURCE, accCtx.getResource());

				builder.setOriginType(OriginType.OUTBOUND);
				builder.setOriginObject(accCtx.getResource());
				return builder;
            }
        };
        
        final MutableBoolean output = new MutableBoolean(false);
        
        MappingOutputProcessor<PrismPropertyValue<Boolean>> processor = new MappingOutputProcessor<PrismPropertyValue<Boolean>>() {
			@Override
			public void process(ItemPath mappingOutputPath,
					PrismValueDeltaSetTriple<PrismPropertyValue<Boolean>> outputTriple) throws ExpressionEvaluationException {
				if (outputTriple == null) {
					// The "default existence mapping"
					output.setValue(legal);
					return;
				}
				        
				Collection<PrismPropertyValue<Boolean>> nonNegativeValues = outputTriple.getNonNegativeValues();
				
				// MID-3507: this is probably the bug. The processor is executed after every mapping. 
				// The processing will die on the error if one mapping returns a value and the other mapping returns null 
				// (e.g. because the condition is false). This should be fixed.
		        if (nonNegativeValues == null || nonNegativeValues.isEmpty()) {
		        	throw new ExpressionEvaluationException("Activation existence expression resulted in null or empty value for projection " + accCtxDesc);
		        }
		        if (nonNegativeValues.size() > 1) {
		        	throw new ExpressionEvaluationException("Activation existence expression resulted in too many values ("+nonNegativeValues.size()+") for projection " + accCtxDesc);
		        }
		    	
		        output.setValue(nonNegativeValues.iterator().next().getValue());
			}
		};
        
        MappingEvaluatorParams<PrismPropertyValue<Boolean>, PrismPropertyDefinition<Boolean>, ShadowType, F> params = new MappingEvaluatorParams<>();
        params.setMappingTypes(outbound);
        params.setMappingDesc("outbound existence mapping in projection " + accCtxDesc);
        params.setNow(now);
        params.setInitializer(initializer);
		params.setProcessor(processor);
        params.setAPrioriTargetObject(accCtx.getObjectOld());
        params.setEvaluateCurrent(current);
        params.setTargetContext(accCtx);
        params.setFixTarget(true);
        params.setContext(context);
        
        PrismPropertyDefinitionImpl<Boolean> shadowExistsDef = new PrismPropertyDefinitionImpl<>(
				SHADOW_EXISTS_PROPERTY_NAME,
				DOMUtil.XSD_BOOLEAN, prismContext);
        shadowExistsDef.setMinOccurs(1);
        shadowExistsDef.setMaxOccurs(1);
        params.setTargetItemDefinition(shadowExistsDef);
		mappingHelper.evaluateMappingSetProjection(params, task, result);
        
//		PrismValueDeltaSetTriple<PrismPropertyValue<Boolean>> outputTriple = mappingHelper.evaluateMappingSetProjection(
//				outbound, "outbound existence mapping in projection " + accCtxDesc,
//        		now, initializer, null, null, accCtx.getObjectOld(), current, null, context, accCtx, task, result);
    	
		return (boolean) output.getValue();

    }
    
	private <T, F extends FocusType> void evaluateActivationMapping(final LensContext<F> context, 
			final LensProjectionContext projCtx, ResourceBidirectionalMappingType bidirectionalMappingType, 
			final ItemPath focusPropertyPath, final ItemPath projectionPropertyPath,
   			final ActivationCapabilityType capActivation, XMLGregorianCalendar now, final boolean current, 
   			String desc, final Task task, final OperationResult result) throws ExpressionEvaluationException, ObjectNotFoundException, SchemaException {
        
   		String accCtxDesc = projCtx.toHumanReadableString();

        if (bidirectionalMappingType == null) {
            LOGGER.trace("No '{}' definition in activation in projection {}, skipping", desc, accCtxDesc);
            return;
        }
        List<MappingType> outbound = bidirectionalMappingType.getOutbound();
        if (outbound == null || outbound.isEmpty()) {
            LOGGER.trace("No outbound definition in '{}' definition in activation in projection {}, skipping", desc, accCtxDesc);
            return;
        }

        PrismObject<ShadowType> shadowNew = projCtx.getObjectNew();

        MappingInitializer<PrismPropertyValue<T>,PrismPropertyDefinition<T>> initializer = new MappingInitializer<PrismPropertyValue<T>,PrismPropertyDefinition<T>>() {
			@Override
			public Mapping.Builder<PrismPropertyValue<T>,PrismPropertyDefinition<T>> initialize(Mapping.Builder<PrismPropertyValue<T>,PrismPropertyDefinition<T>> builder) throws SchemaException {
				// Source: administrativeStatus, validFrom or validTo
		        ItemDeltaItem<PrismPropertyValue<T>,PrismPropertyDefinition<T>> sourceIdi = context.getFocusContext().getObjectDeltaObject().findIdi(focusPropertyPath);
		        
		        if (capActivation != null && focusPropertyPath.equals(SchemaConstants.PATH_ACTIVATION_ADMINISTRATIVE_STATUS)) {
			        ActivationValidityCapabilityType capValidFrom = CapabilityUtil.getEffectiveActivationValidFrom(capActivation);
			        ActivationValidityCapabilityType capValidTo = CapabilityUtil.getEffectiveActivationValidTo(capActivation);
			        
			        // Source: computedShadowStatus
			        ItemDeltaItem<PrismPropertyValue<ActivationStatusType>,PrismPropertyDefinition<ActivationStatusType>> computedIdi;
			        if (capValidFrom != null && capValidTo != null) {
			        	// "Native" validFrom and validTo, directly use administrativeStatus
			        	computedIdi = context.getFocusContext().getObjectDeltaObject().findIdi(focusPropertyPath);
			        	
			        } else {
			        	// Simulate validFrom and validTo using effectiveStatus
			        	computedIdi = context.getFocusContext().getObjectDeltaObject().findIdi(SchemaConstants.PATH_ACTIVATION_EFFECTIVE_STATUS);
			        	
			        }
			        
			        Source<PrismPropertyValue<ActivationStatusType>,PrismPropertyDefinition<ActivationStatusType>> computedSource = new Source<>(computedIdi, ExpressionConstants.VAR_INPUT);
			        
			        builder.setDefaultSource(computedSource);
			        
			        Source<PrismPropertyValue<T>,PrismPropertyDefinition<T>> source = new Source<>(sourceIdi, ExpressionConstants.VAR_ADMINISTRATIVE_STATUS);
					builder.addSource(source);
			        
		        } else {
		        	Source<PrismPropertyValue<T>,PrismPropertyDefinition<T>> source = new Source<>(sourceIdi, ExpressionConstants.VAR_INPUT);
					builder.setDefaultSource(source);
		        }
		        
				// Source: legal
		        ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> legalIdi = getLegalIdi(projCtx);
		        Source<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> legalSource = new Source<>(legalIdi, ExpressionConstants.VAR_LEGAL);
				builder.addSource(legalSource);

                // Source: assigned
                ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> assignedIdi = getAssignedIdi(projCtx);
                Source<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> assignedSource = new Source<>(assignedIdi, ExpressionConstants.VAR_ASSIGNED);
				builder.addSource(assignedSource);

                // Source: focusExists
		        ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> focusExistsSourceIdi = getFocusExistsIdi(context.getFocusContext()); 
		        Source<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> focusExistsSource 
		        	= new Source<>(focusExistsSourceIdi, ExpressionConstants.VAR_FOCUS_EXISTS);
		        builder.addSource(focusExistsSource);
		        
		        // Variable: focus
		        builder.addVariableDefinition(ExpressionConstants.VAR_FOCUS, context.getFocusContext().getObjectDeltaObject());

		        // Variable: user (for convenience, same as "focus")
		        builder.addVariableDefinition(ExpressionConstants.VAR_USER, context.getFocusContext().getObjectDeltaObject());
		        
		        builder.addVariableDefinition(ExpressionConstants.VAR_RESOURCE, projCtx.getResource());
				
		        builder.setOriginType(OriginType.OUTBOUND);
				builder.setOriginObject(projCtx.getResource());
				return builder;
			}

		};

		MappingEvaluatorParams<PrismPropertyValue<T>, PrismPropertyDefinition<T>, ShadowType, F> params = new MappingEvaluatorParams<>();
		params.setMappingTypes(outbound);
		params.setMappingDesc(desc + " outbound activation mapping in projection " + accCtxDesc);
		params.setNow(now);
		params.setInitializer(initializer);
		params.setProcessor(null);
		params.setAPrioriTargetObject(shadowNew);
		params.setAPrioriTargetDelta(LensUtil.findAPrioriDelta(context, projCtx));
		params.setTargetContext(projCtx);
		params.setDefaultTargetItemPath(projectionPropertyPath);
		params.setEvaluateCurrent(current);
		params.setContext(context);
		params.setHasFullTargetObject(projCtx.hasFullShadow());
		mappingHelper.evaluateMappingSetProjection(params, task, result);

    }

	private ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> getLegalIdi(LensProjectionContext accCtx) throws SchemaException {
		Boolean legal = accCtx.isLegal();
		Boolean legalOld = accCtx.isLegalOld();
		return createBooleanIdi(LEGAL_PROPERTY_NAME, legalOld, legal);
	}

	@NotNull
	private ItemDeltaItem<PrismPropertyValue<Boolean>, PrismPropertyDefinition<Boolean>> createBooleanIdi(
			QName propertyName, Boolean old, Boolean current) throws SchemaException {
		PrismPropertyDefinitionImpl<Boolean> definition = new PrismPropertyDefinitionImpl<>(propertyName, DOMUtil.XSD_BOOLEAN, prismContext);
		definition.setMinOccurs(1);
		definition.setMaxOccurs(1);
		PrismProperty<Boolean> property = definition.instantiate();
		property.add(new PrismPropertyValue<>(current));

		if (current == old) {
			return new ItemDeltaItem<>(property);
		} else {
			PrismProperty<Boolean> propertyOld = property.clone();
			propertyOld.setRealValue(old);
			PropertyDelta<Boolean> delta = propertyOld.createDelta();
			delta.setValuesToReplace(new PrismPropertyValue<>(current));
			return new ItemDeltaItem<>(propertyOld, delta, property);
		}
	}

	private ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> getAssignedIdi(LensProjectionContext accCtx) throws SchemaException {
        Boolean assigned = accCtx.isAssigned();
        Boolean assignedOld = accCtx.isAssignedOld();
		return createBooleanIdi(ASSIGNED_PROPERTY_NAME, assignedOld, assigned);
	}

    private <F extends ObjectType> ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>> getFocusExistsIdi(
			LensFocusContext<F> lensFocusContext) throws SchemaException {
		Boolean existsOld = null;
		Boolean existsNew = null;
		
		if (lensFocusContext != null) {
			if (lensFocusContext.isDelete()) {
				existsOld = true;
				existsNew = false;
			} else if (lensFocusContext.isAdd()) {
				existsOld = false;
				existsNew = true;
			} else {
				existsOld = true;
				existsNew = true;
			}
		}
		
		PrismPropertyDefinitionImpl<Boolean> existsDef = new PrismPropertyDefinitionImpl<>(FOCUS_EXISTS_PROPERTY_NAME,
				DOMUtil.XSD_BOOLEAN, prismContext);
		existsDef.setMinOccurs(1);
		existsDef.setMaxOccurs(1);
		PrismProperty<Boolean> existsProp = existsDef.instantiate();
		
		existsProp.add(new PrismPropertyValue<Boolean>(existsNew));
		
		if (existsOld == existsNew) {
			return new ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>>(existsProp);
		} else {
			PrismProperty<Boolean> existsPropOld = existsProp.clone();
			existsPropOld.setRealValue(existsOld);
			PropertyDelta<Boolean> existsDelta = existsPropOld.createDelta();
			existsDelta.setValuesToReplace(new PrismPropertyValue<Boolean>(existsNew));
			return new ItemDeltaItem<PrismPropertyValue<Boolean>,PrismPropertyDefinition<Boolean>>(existsPropOld, existsDelta, existsProp);
		}
	}

	private PrismObjectDefinition<UserType> getUserDefinition() {
		if (userDefinition == null) {
			userDefinition = prismContext.getSchemaRegistry()
				.findObjectDefinitionByCompileTimeClass(UserType.class);
		}
		return userDefinition;
	}
	
	private PrismContainerDefinition<ActivationType> getActivationDefinition() {
		if (activationDefinition == null) {
			PrismObjectDefinition<UserType> userDefinition = getUserDefinition();
			activationDefinition = userDefinition.findContainerDefinition(UserType.F_ACTIVATION);
		}
		return activationDefinition;
	}
}
