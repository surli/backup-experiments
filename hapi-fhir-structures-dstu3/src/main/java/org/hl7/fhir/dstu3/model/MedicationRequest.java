package org.hl7.fhir.dstu3.model;

/*
  Copyright (c) 2011+, HL7, Inc.
  All rights reserved.
  
  Redistribution and use in source and binary forms, with or without modification, 
  are permitted provided that the following conditions are met:
  
   * Redistributions of source code must retain the above copyright notice, this 
     list of conditions and the following disclaimer.
   * Redistributions in binary form must reproduce the above copyright notice, 
     this list of conditions and the following disclaimer in the documentation 
     and/or other materials provided with the distribution.
   * Neither the name of HL7 nor the names of its contributors may be used to 
     endorse or promote products derived from this software without specific 
     prior written permission.
  
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  POSSIBILITY OF SUCH DAMAGE.
  
*/

// Generated on Tue, Dec 6, 2016 09:42-0500 for FHIR v1.8.0

import java.util.*;

import org.hl7.fhir.utilities.Utilities;
import ca.uhn.fhir.model.api.annotation.ResourceDef;
import ca.uhn.fhir.model.api.annotation.SearchParamDefinition;
import ca.uhn.fhir.model.api.annotation.Child;
import ca.uhn.fhir.model.api.annotation.ChildOrder;
import ca.uhn.fhir.model.api.annotation.Description;
import ca.uhn.fhir.model.api.annotation.Block;
import org.hl7.fhir.instance.model.api.*;
import org.hl7.fhir.exceptions.FHIRException;
/**
 * An order for both supply of the medication and the instructions for administration of the medication to a patient. The resource is called "MedicationRequest" rather than "MedicationPrescription" or "MedicationOrder" to generalize the use across inpatient and outpatient settings as well as for care plans, etc and to harmonize with workflow patterns.
 */
@ResourceDef(name="MedicationRequest", profile="http://hl7.org/fhir/Profile/MedicationRequest")
public class MedicationRequest extends DomainResource {

    public enum MedicationRequestStatus {
        /**
         * The prescription is 'actionable', but not all actions that are implied by it have occurred yet.
         */
        ACTIVE, 
        /**
         * Actions implied by the prescription are to be temporarily halted, but are expected to continue later.  May also be called "suspended".
         */
        ONHOLD, 
        /**
         * The prescription has been withdrawn.
         */
        CANCELLED, 
        /**
         * All actions that are implied by the prescription have occurred.
         */
        COMPLETED, 
        /**
         * The prescription was entered in error.
         */
        ENTEREDINERROR, 
        /**
         * Actions implied by the prescription are to be permanently halted, before all of them occurred.
         */
        STOPPED, 
        /**
         * The prescription is not yet 'actionable', i.e. it is a work in progress, requires sign-off or verification, and needs to be run through decision support process.
         */
        DRAFT, 
        /**
         * added to help the parsers with the generic types
         */
        NULL;
        public static MedicationRequestStatus fromCode(String codeString) throws FHIRException {
            if (codeString == null || "".equals(codeString))
                return null;
        if ("active".equals(codeString))
          return ACTIVE;
        if ("on-hold".equals(codeString))
          return ONHOLD;
        if ("cancelled".equals(codeString))
          return CANCELLED;
        if ("completed".equals(codeString))
          return COMPLETED;
        if ("entered-in-error".equals(codeString))
          return ENTEREDINERROR;
        if ("stopped".equals(codeString))
          return STOPPED;
        if ("draft".equals(codeString))
          return DRAFT;
        if (Configuration.isAcceptInvalidEnums())
          return null;
        else
          throw new FHIRException("Unknown MedicationRequestStatus code '"+codeString+"'");
        }
        public String toCode() {
          switch (this) {
            case ACTIVE: return "active";
            case ONHOLD: return "on-hold";
            case CANCELLED: return "cancelled";
            case COMPLETED: return "completed";
            case ENTEREDINERROR: return "entered-in-error";
            case STOPPED: return "stopped";
            case DRAFT: return "draft";
            default: return "?";
          }
        }
        public String getSystem() {
          switch (this) {
            case ACTIVE: return "http://hl7.org/fhir/medication-request-status";
            case ONHOLD: return "http://hl7.org/fhir/medication-request-status";
            case CANCELLED: return "http://hl7.org/fhir/medication-request-status";
            case COMPLETED: return "http://hl7.org/fhir/medication-request-status";
            case ENTEREDINERROR: return "http://hl7.org/fhir/medication-request-status";
            case STOPPED: return "http://hl7.org/fhir/medication-request-status";
            case DRAFT: return "http://hl7.org/fhir/medication-request-status";
            default: return "?";
          }
        }
        public String getDefinition() {
          switch (this) {
            case ACTIVE: return "The prescription is 'actionable', but not all actions that are implied by it have occurred yet.";
            case ONHOLD: return "Actions implied by the prescription are to be temporarily halted, but are expected to continue later.  May also be called \"suspended\".";
            case CANCELLED: return "The prescription has been withdrawn.";
            case COMPLETED: return "All actions that are implied by the prescription have occurred.";
            case ENTEREDINERROR: return "The prescription was entered in error.";
            case STOPPED: return "Actions implied by the prescription are to be permanently halted, before all of them occurred.";
            case DRAFT: return "The prescription is not yet 'actionable', i.e. it is a work in progress, requires sign-off or verification, and needs to be run through decision support process.";
            default: return "?";
          }
        }
        public String getDisplay() {
          switch (this) {
            case ACTIVE: return "Active";
            case ONHOLD: return "On Hold";
            case CANCELLED: return "Cancelled";
            case COMPLETED: return "Completed";
            case ENTEREDINERROR: return "Entered In Error";
            case STOPPED: return "Stopped";
            case DRAFT: return "Draft";
            default: return "?";
          }
        }
    }

  public static class MedicationRequestStatusEnumFactory implements EnumFactory<MedicationRequestStatus> {
    public MedicationRequestStatus fromCode(String codeString) throws IllegalArgumentException {
      if (codeString == null || "".equals(codeString))
            if (codeString == null || "".equals(codeString))
                return null;
        if ("active".equals(codeString))
          return MedicationRequestStatus.ACTIVE;
        if ("on-hold".equals(codeString))
          return MedicationRequestStatus.ONHOLD;
        if ("cancelled".equals(codeString))
          return MedicationRequestStatus.CANCELLED;
        if ("completed".equals(codeString))
          return MedicationRequestStatus.COMPLETED;
        if ("entered-in-error".equals(codeString))
          return MedicationRequestStatus.ENTEREDINERROR;
        if ("stopped".equals(codeString))
          return MedicationRequestStatus.STOPPED;
        if ("draft".equals(codeString))
          return MedicationRequestStatus.DRAFT;
        throw new IllegalArgumentException("Unknown MedicationRequestStatus code '"+codeString+"'");
        }
        public Enumeration<MedicationRequestStatus> fromType(Base code) throws FHIRException {
          if (code == null || code.isEmpty())
            return null;
          String codeString = ((PrimitiveType) code).asStringValue();
          if (codeString == null || "".equals(codeString))
            return null;
        if ("active".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.ACTIVE);
        if ("on-hold".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.ONHOLD);
        if ("cancelled".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.CANCELLED);
        if ("completed".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.COMPLETED);
        if ("entered-in-error".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.ENTEREDINERROR);
        if ("stopped".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.STOPPED);
        if ("draft".equals(codeString))
          return new Enumeration<MedicationRequestStatus>(this, MedicationRequestStatus.DRAFT);
        throw new FHIRException("Unknown MedicationRequestStatus code '"+codeString+"'");
        }
    public String toCode(MedicationRequestStatus code) {
      if (code == MedicationRequestStatus.ACTIVE)
        return "active";
      if (code == MedicationRequestStatus.ONHOLD)
        return "on-hold";
      if (code == MedicationRequestStatus.CANCELLED)
        return "cancelled";
      if (code == MedicationRequestStatus.COMPLETED)
        return "completed";
      if (code == MedicationRequestStatus.ENTEREDINERROR)
        return "entered-in-error";
      if (code == MedicationRequestStatus.STOPPED)
        return "stopped";
      if (code == MedicationRequestStatus.DRAFT)
        return "draft";
      return "?";
      }
    public String toSystem(MedicationRequestStatus code) {
      return code.getSystem();
      }
    }

    @Block()
    public static class MedicationRequestDispenseRequestComponent extends BackboneElement implements IBaseBackboneElement {
        /**
         * This indicates the validity period of a prescription (stale dating the Prescription).
         */
        @Child(name = "validityPeriod", type = {Period.class}, order=1, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Time period supply is authorized for", formalDefinition="This indicates the validity period of a prescription (stale dating the Prescription)." )
        protected Period validityPeriod;

        /**
         * An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus "3 repeats", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets.
         */
        @Child(name = "numberOfRepeatsAllowed", type = {PositiveIntType.class}, order=2, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Number of refills authorized", formalDefinition="An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus \"3 repeats\", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets." )
        protected PositiveIntType numberOfRepeatsAllowed;

        /**
         * The amount that is to be dispensed for one fill.
         */
        @Child(name = "quantity", type = {SimpleQuantity.class}, order=3, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Amount of medication to supply per dispense", formalDefinition="The amount that is to be dispensed for one fill." )
        protected SimpleQuantity quantity;

        /**
         * Identifies the period time over which the supplied product is expected to be used, or the length of time the dispense is expected to last.
         */
        @Child(name = "expectedSupplyDuration", type = {Duration.class}, order=4, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Number of days supply per dispense", formalDefinition="Identifies the period time over which the supplied product is expected to be used, or the length of time the dispense is expected to last." )
        protected Duration expectedSupplyDuration;

        /**
         * Indicates the intended dispensing Organization specified by the prescriber.
         */
        @Child(name = "performer", type = {Organization.class}, order=5, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Intended dispenser", formalDefinition="Indicates the intended dispensing Organization specified by the prescriber." )
        protected Reference performer;

        /**
         * The actual object that is the target of the reference (Indicates the intended dispensing Organization specified by the prescriber.)
         */
        protected Organization performerTarget;

        private static final long serialVersionUID = 280197622L;

    /**
     * Constructor
     */
      public MedicationRequestDispenseRequestComponent() {
        super();
      }

        /**
         * @return {@link #validityPeriod} (This indicates the validity period of a prescription (stale dating the Prescription).)
         */
        public Period getValidityPeriod() { 
          if (this.validityPeriod == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestDispenseRequestComponent.validityPeriod");
            else if (Configuration.doAutoCreate())
              this.validityPeriod = new Period(); // cc
          return this.validityPeriod;
        }

        public boolean hasValidityPeriod() { 
          return this.validityPeriod != null && !this.validityPeriod.isEmpty();
        }

        /**
         * @param value {@link #validityPeriod} (This indicates the validity period of a prescription (stale dating the Prescription).)
         */
        public MedicationRequestDispenseRequestComponent setValidityPeriod(Period value) { 
          this.validityPeriod = value;
          return this;
        }

        /**
         * @return {@link #numberOfRepeatsAllowed} (An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus "3 repeats", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets.). This is the underlying object with id, value and extensions. The accessor "getNumberOfRepeatsAllowed" gives direct access to the value
         */
        public PositiveIntType getNumberOfRepeatsAllowedElement() { 
          if (this.numberOfRepeatsAllowed == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestDispenseRequestComponent.numberOfRepeatsAllowed");
            else if (Configuration.doAutoCreate())
              this.numberOfRepeatsAllowed = new PositiveIntType(); // bb
          return this.numberOfRepeatsAllowed;
        }

        public boolean hasNumberOfRepeatsAllowedElement() { 
          return this.numberOfRepeatsAllowed != null && !this.numberOfRepeatsAllowed.isEmpty();
        }

        public boolean hasNumberOfRepeatsAllowed() { 
          return this.numberOfRepeatsAllowed != null && !this.numberOfRepeatsAllowed.isEmpty();
        }

        /**
         * @param value {@link #numberOfRepeatsAllowed} (An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus "3 repeats", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets.). This is the underlying object with id, value and extensions. The accessor "getNumberOfRepeatsAllowed" gives direct access to the value
         */
        public MedicationRequestDispenseRequestComponent setNumberOfRepeatsAllowedElement(PositiveIntType value) { 
          this.numberOfRepeatsAllowed = value;
          return this;
        }

        /**
         * @return An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus "3 repeats", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets.
         */
        public int getNumberOfRepeatsAllowed() { 
          return this.numberOfRepeatsAllowed == null || this.numberOfRepeatsAllowed.isEmpty() ? 0 : this.numberOfRepeatsAllowed.getValue();
        }

        /**
         * @param value An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus "3 repeats", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets.
         */
        public MedicationRequestDispenseRequestComponent setNumberOfRepeatsAllowed(int value) { 
            if (this.numberOfRepeatsAllowed == null)
              this.numberOfRepeatsAllowed = new PositiveIntType();
            this.numberOfRepeatsAllowed.setValue(value);
          return this;
        }

        /**
         * @return {@link #quantity} (The amount that is to be dispensed for one fill.)
         */
        public SimpleQuantity getQuantity() { 
          if (this.quantity == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestDispenseRequestComponent.quantity");
            else if (Configuration.doAutoCreate())
              this.quantity = new SimpleQuantity(); // cc
          return this.quantity;
        }

        public boolean hasQuantity() { 
          return this.quantity != null && !this.quantity.isEmpty();
        }

        /**
         * @param value {@link #quantity} (The amount that is to be dispensed for one fill.)
         */
        public MedicationRequestDispenseRequestComponent setQuantity(SimpleQuantity value) { 
          this.quantity = value;
          return this;
        }

        /**
         * @return {@link #expectedSupplyDuration} (Identifies the period time over which the supplied product is expected to be used, or the length of time the dispense is expected to last.)
         */
        public Duration getExpectedSupplyDuration() { 
          if (this.expectedSupplyDuration == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestDispenseRequestComponent.expectedSupplyDuration");
            else if (Configuration.doAutoCreate())
              this.expectedSupplyDuration = new Duration(); // cc
          return this.expectedSupplyDuration;
        }

        public boolean hasExpectedSupplyDuration() { 
          return this.expectedSupplyDuration != null && !this.expectedSupplyDuration.isEmpty();
        }

        /**
         * @param value {@link #expectedSupplyDuration} (Identifies the period time over which the supplied product is expected to be used, or the length of time the dispense is expected to last.)
         */
        public MedicationRequestDispenseRequestComponent setExpectedSupplyDuration(Duration value) { 
          this.expectedSupplyDuration = value;
          return this;
        }

        /**
         * @return {@link #performer} (Indicates the intended dispensing Organization specified by the prescriber.)
         */
        public Reference getPerformer() { 
          if (this.performer == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestDispenseRequestComponent.performer");
            else if (Configuration.doAutoCreate())
              this.performer = new Reference(); // cc
          return this.performer;
        }

        public boolean hasPerformer() { 
          return this.performer != null && !this.performer.isEmpty();
        }

        /**
         * @param value {@link #performer} (Indicates the intended dispensing Organization specified by the prescriber.)
         */
        public MedicationRequestDispenseRequestComponent setPerformer(Reference value) { 
          this.performer = value;
          return this;
        }

        /**
         * @return {@link #performer} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (Indicates the intended dispensing Organization specified by the prescriber.)
         */
        public Organization getPerformerTarget() { 
          if (this.performerTarget == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestDispenseRequestComponent.performer");
            else if (Configuration.doAutoCreate())
              this.performerTarget = new Organization(); // aa
          return this.performerTarget;
        }

        /**
         * @param value {@link #performer} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (Indicates the intended dispensing Organization specified by the prescriber.)
         */
        public MedicationRequestDispenseRequestComponent setPerformerTarget(Organization value) { 
          this.performerTarget = value;
          return this;
        }

        protected void listChildren(List<Property> childrenList) {
          super.listChildren(childrenList);
          childrenList.add(new Property("validityPeriod", "Period", "This indicates the validity period of a prescription (stale dating the Prescription).", 0, java.lang.Integer.MAX_VALUE, validityPeriod));
          childrenList.add(new Property("numberOfRepeatsAllowed", "positiveInt", "An integer indicating the number of times, in addition to the original dispense, (aka refills or repeats) that the patient can receive the prescribed medication. Usage Notes: This integer does NOT include the original order dispense. This means that if an order indicates dispense 30 tablets plus \"3 repeats\", then the order can be dispensed a total of 4 times and the patient can receive a total of 120 tablets.", 0, java.lang.Integer.MAX_VALUE, numberOfRepeatsAllowed));
          childrenList.add(new Property("quantity", "SimpleQuantity", "The amount that is to be dispensed for one fill.", 0, java.lang.Integer.MAX_VALUE, quantity));
          childrenList.add(new Property("expectedSupplyDuration", "Duration", "Identifies the period time over which the supplied product is expected to be used, or the length of time the dispense is expected to last.", 0, java.lang.Integer.MAX_VALUE, expectedSupplyDuration));
          childrenList.add(new Property("performer", "Reference(Organization)", "Indicates the intended dispensing Organization specified by the prescriber.", 0, java.lang.Integer.MAX_VALUE, performer));
        }

      @Override
      public Base[] getProperty(int hash, String name, boolean checkValid) throws FHIRException {
        switch (hash) {
        case -1434195053: /*validityPeriod*/ return this.validityPeriod == null ? new Base[0] : new Base[] {this.validityPeriod}; // Period
        case -239736976: /*numberOfRepeatsAllowed*/ return this.numberOfRepeatsAllowed == null ? new Base[0] : new Base[] {this.numberOfRepeatsAllowed}; // PositiveIntType
        case -1285004149: /*quantity*/ return this.quantity == null ? new Base[0] : new Base[] {this.quantity}; // SimpleQuantity
        case -1910182789: /*expectedSupplyDuration*/ return this.expectedSupplyDuration == null ? new Base[0] : new Base[] {this.expectedSupplyDuration}; // Duration
        case 481140686: /*performer*/ return this.performer == null ? new Base[0] : new Base[] {this.performer}; // Reference
        default: return super.getProperty(hash, name, checkValid);
        }

      }

      @Override
      public void setProperty(int hash, String name, Base value) throws FHIRException {
        switch (hash) {
        case -1434195053: // validityPeriod
          this.validityPeriod = castToPeriod(value); // Period
          break;
        case -239736976: // numberOfRepeatsAllowed
          this.numberOfRepeatsAllowed = castToPositiveInt(value); // PositiveIntType
          break;
        case -1285004149: // quantity
          this.quantity = castToSimpleQuantity(value); // SimpleQuantity
          break;
        case -1910182789: // expectedSupplyDuration
          this.expectedSupplyDuration = castToDuration(value); // Duration
          break;
        case 481140686: // performer
          this.performer = castToReference(value); // Reference
          break;
        default: super.setProperty(hash, name, value);
        }

      }

      @Override
      public void setProperty(String name, Base value) throws FHIRException {
        if (name.equals("validityPeriod"))
          this.validityPeriod = castToPeriod(value); // Period
        else if (name.equals("numberOfRepeatsAllowed"))
          this.numberOfRepeatsAllowed = castToPositiveInt(value); // PositiveIntType
        else if (name.equals("quantity"))
          this.quantity = castToSimpleQuantity(value); // SimpleQuantity
        else if (name.equals("expectedSupplyDuration"))
          this.expectedSupplyDuration = castToDuration(value); // Duration
        else if (name.equals("performer"))
          this.performer = castToReference(value); // Reference
        else
          super.setProperty(name, value);
      }

      @Override
      public Base makeProperty(int hash, String name) throws FHIRException {
        switch (hash) {
        case -1434195053:  return getValidityPeriod(); // Period
        case -239736976: throw new FHIRException("Cannot make property numberOfRepeatsAllowed as it is not a complex type"); // PositiveIntType
        case -1285004149:  return getQuantity(); // SimpleQuantity
        case -1910182789:  return getExpectedSupplyDuration(); // Duration
        case 481140686:  return getPerformer(); // Reference
        default: return super.makeProperty(hash, name);
        }

      }

      @Override
      public Base addChild(String name) throws FHIRException {
        if (name.equals("validityPeriod")) {
          this.validityPeriod = new Period();
          return this.validityPeriod;
        }
        else if (name.equals("numberOfRepeatsAllowed")) {
          throw new FHIRException("Cannot call addChild on a primitive type MedicationRequest.numberOfRepeatsAllowed");
        }
        else if (name.equals("quantity")) {
          this.quantity = new SimpleQuantity();
          return this.quantity;
        }
        else if (name.equals("expectedSupplyDuration")) {
          this.expectedSupplyDuration = new Duration();
          return this.expectedSupplyDuration;
        }
        else if (name.equals("performer")) {
          this.performer = new Reference();
          return this.performer;
        }
        else
          return super.addChild(name);
      }

      public MedicationRequestDispenseRequestComponent copy() {
        MedicationRequestDispenseRequestComponent dst = new MedicationRequestDispenseRequestComponent();
        copyValues(dst);
        dst.validityPeriod = validityPeriod == null ? null : validityPeriod.copy();
        dst.numberOfRepeatsAllowed = numberOfRepeatsAllowed == null ? null : numberOfRepeatsAllowed.copy();
        dst.quantity = quantity == null ? null : quantity.copy();
        dst.expectedSupplyDuration = expectedSupplyDuration == null ? null : expectedSupplyDuration.copy();
        dst.performer = performer == null ? null : performer.copy();
        return dst;
      }

      @Override
      public boolean equalsDeep(Base other) {
        if (!super.equalsDeep(other))
          return false;
        if (!(other instanceof MedicationRequestDispenseRequestComponent))
          return false;
        MedicationRequestDispenseRequestComponent o = (MedicationRequestDispenseRequestComponent) other;
        return compareDeep(validityPeriod, o.validityPeriod, true) && compareDeep(numberOfRepeatsAllowed, o.numberOfRepeatsAllowed, true)
           && compareDeep(quantity, o.quantity, true) && compareDeep(expectedSupplyDuration, o.expectedSupplyDuration, true)
           && compareDeep(performer, o.performer, true);
      }

      @Override
      public boolean equalsShallow(Base other) {
        if (!super.equalsShallow(other))
          return false;
        if (!(other instanceof MedicationRequestDispenseRequestComponent))
          return false;
        MedicationRequestDispenseRequestComponent o = (MedicationRequestDispenseRequestComponent) other;
        return compareValues(numberOfRepeatsAllowed, o.numberOfRepeatsAllowed, true);
      }

      public boolean isEmpty() {
        return super.isEmpty() && ca.uhn.fhir.util.ElementUtil.isEmpty(validityPeriod, numberOfRepeatsAllowed
          , quantity, expectedSupplyDuration, performer);
      }

  public String fhirType() {
    return "MedicationRequest.dispenseRequest";

  }

  }

    @Block()
    public static class MedicationRequestSubstitutionComponent extends BackboneElement implements IBaseBackboneElement {
        /**
         * True if the prescriber allows a different drug to be dispensed from what was prescribed.
         */
        @Child(name = "allowed", type = {BooleanType.class}, order=1, min=1, max=1, modifier=true, summary=false)
        @Description(shortDefinition="Whether substitution is allowed or not", formalDefinition="True if the prescriber allows a different drug to be dispensed from what was prescribed." )
        protected BooleanType allowed;

        /**
         * Indicates the reason for the substitution, or why substitution must or must not be performed.
         */
        @Child(name = "reason", type = {CodeableConcept.class}, order=2, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Why should (not) substitution be made", formalDefinition="Indicates the reason for the substitution, or why substitution must or must not be performed." )
        @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/v3-SubstanceAdminSubstitutionReason")
        protected CodeableConcept reason;

        private static final long serialVersionUID = -141547037L;

    /**
     * Constructor
     */
      public MedicationRequestSubstitutionComponent() {
        super();
      }

    /**
     * Constructor
     */
      public MedicationRequestSubstitutionComponent(BooleanType allowed) {
        super();
        this.allowed = allowed;
      }

        /**
         * @return {@link #allowed} (True if the prescriber allows a different drug to be dispensed from what was prescribed.). This is the underlying object with id, value and extensions. The accessor "getAllowed" gives direct access to the value
         */
        public BooleanType getAllowedElement() { 
          if (this.allowed == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestSubstitutionComponent.allowed");
            else if (Configuration.doAutoCreate())
              this.allowed = new BooleanType(); // bb
          return this.allowed;
        }

        public boolean hasAllowedElement() { 
          return this.allowed != null && !this.allowed.isEmpty();
        }

        public boolean hasAllowed() { 
          return this.allowed != null && !this.allowed.isEmpty();
        }

        /**
         * @param value {@link #allowed} (True if the prescriber allows a different drug to be dispensed from what was prescribed.). This is the underlying object with id, value and extensions. The accessor "getAllowed" gives direct access to the value
         */
        public MedicationRequestSubstitutionComponent setAllowedElement(BooleanType value) { 
          this.allowed = value;
          return this;
        }

        /**
         * @return True if the prescriber allows a different drug to be dispensed from what was prescribed.
         */
        public boolean getAllowed() { 
          return this.allowed == null || this.allowed.isEmpty() ? false : this.allowed.getValue();
        }

        /**
         * @param value True if the prescriber allows a different drug to be dispensed from what was prescribed.
         */
        public MedicationRequestSubstitutionComponent setAllowed(boolean value) { 
            if (this.allowed == null)
              this.allowed = new BooleanType();
            this.allowed.setValue(value);
          return this;
        }

        /**
         * @return {@link #reason} (Indicates the reason for the substitution, or why substitution must or must not be performed.)
         */
        public CodeableConcept getReason() { 
          if (this.reason == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create MedicationRequestSubstitutionComponent.reason");
            else if (Configuration.doAutoCreate())
              this.reason = new CodeableConcept(); // cc
          return this.reason;
        }

        public boolean hasReason() { 
          return this.reason != null && !this.reason.isEmpty();
        }

        /**
         * @param value {@link #reason} (Indicates the reason for the substitution, or why substitution must or must not be performed.)
         */
        public MedicationRequestSubstitutionComponent setReason(CodeableConcept value) { 
          this.reason = value;
          return this;
        }

        protected void listChildren(List<Property> childrenList) {
          super.listChildren(childrenList);
          childrenList.add(new Property("allowed", "boolean", "True if the prescriber allows a different drug to be dispensed from what was prescribed.", 0, java.lang.Integer.MAX_VALUE, allowed));
          childrenList.add(new Property("reason", "CodeableConcept", "Indicates the reason for the substitution, or why substitution must or must not be performed.", 0, java.lang.Integer.MAX_VALUE, reason));
        }

      @Override
      public Base[] getProperty(int hash, String name, boolean checkValid) throws FHIRException {
        switch (hash) {
        case -911343192: /*allowed*/ return this.allowed == null ? new Base[0] : new Base[] {this.allowed}; // BooleanType
        case -934964668: /*reason*/ return this.reason == null ? new Base[0] : new Base[] {this.reason}; // CodeableConcept
        default: return super.getProperty(hash, name, checkValid);
        }

      }

      @Override
      public void setProperty(int hash, String name, Base value) throws FHIRException {
        switch (hash) {
        case -911343192: // allowed
          this.allowed = castToBoolean(value); // BooleanType
          break;
        case -934964668: // reason
          this.reason = castToCodeableConcept(value); // CodeableConcept
          break;
        default: super.setProperty(hash, name, value);
        }

      }

      @Override
      public void setProperty(String name, Base value) throws FHIRException {
        if (name.equals("allowed"))
          this.allowed = castToBoolean(value); // BooleanType
        else if (name.equals("reason"))
          this.reason = castToCodeableConcept(value); // CodeableConcept
        else
          super.setProperty(name, value);
      }

      @Override
      public Base makeProperty(int hash, String name) throws FHIRException {
        switch (hash) {
        case -911343192: throw new FHIRException("Cannot make property allowed as it is not a complex type"); // BooleanType
        case -934964668:  return getReason(); // CodeableConcept
        default: return super.makeProperty(hash, name);
        }

      }

      @Override
      public Base addChild(String name) throws FHIRException {
        if (name.equals("allowed")) {
          throw new FHIRException("Cannot call addChild on a primitive type MedicationRequest.allowed");
        }
        else if (name.equals("reason")) {
          this.reason = new CodeableConcept();
          return this.reason;
        }
        else
          return super.addChild(name);
      }

      public MedicationRequestSubstitutionComponent copy() {
        MedicationRequestSubstitutionComponent dst = new MedicationRequestSubstitutionComponent();
        copyValues(dst);
        dst.allowed = allowed == null ? null : allowed.copy();
        dst.reason = reason == null ? null : reason.copy();
        return dst;
      }

      @Override
      public boolean equalsDeep(Base other) {
        if (!super.equalsDeep(other))
          return false;
        if (!(other instanceof MedicationRequestSubstitutionComponent))
          return false;
        MedicationRequestSubstitutionComponent o = (MedicationRequestSubstitutionComponent) other;
        return compareDeep(allowed, o.allowed, true) && compareDeep(reason, o.reason, true);
      }

      @Override
      public boolean equalsShallow(Base other) {
        if (!super.equalsShallow(other))
          return false;
        if (!(other instanceof MedicationRequestSubstitutionComponent))
          return false;
        MedicationRequestSubstitutionComponent o = (MedicationRequestSubstitutionComponent) other;
        return compareValues(allowed, o.allowed, true);
      }

      public boolean isEmpty() {
        return super.isEmpty() && ca.uhn.fhir.util.ElementUtil.isEmpty(allowed, reason);
      }

  public String fhirType() {
    return "MedicationRequest.substitution";

  }

  }

    /**
     * External identifier - one that would be used by another non-FHIR system - for example a re-imbursement system might issue its own id for each prescription that is created.  This is particularly important where FHIR only provides part of an entire workflow process where records have to be tracked through an entire system.
     */
    @Child(name = "identifier", type = {Identifier.class}, order=0, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="External identifier", formalDefinition="External identifier - one that would be used by another non-FHIR system - for example a re-imbursement system might issue its own id for each prescription that is created.  This is particularly important where FHIR only provides part of an entire workflow process where records have to be tracked through an entire system." )
    protected List<Identifier> identifier;

    /**
     * Protocol or definition followed by this request.
     */
    @Child(name = "definition", type = {ActivityDefinition.class, PlanDefinition.class}, order=1, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=true)
    @Description(shortDefinition="Protocol or definition", formalDefinition="Protocol or definition followed by this request." )
    protected List<Reference> definition;
    /**
     * The actual objects that are the target of the reference (Protocol or definition followed by this request.)
     */
    protected List<Resource> definitionTarget;


    /**
     * Plan/proposal/order fulfilled by this request.
     */
    @Child(name = "basedOn", type = {CarePlan.class, DiagnosticRequest.class, MedicationRequest.class, ProcedureRequest.class, ReferralRequest.class}, order=2, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=true)
    @Description(shortDefinition="What request fulfills", formalDefinition="Plan/proposal/order fulfilled by this request." )
    protected List<Reference> basedOn;
    /**
     * The actual objects that are the target of the reference (Plan/proposal/order fulfilled by this request.)
     */
    protected List<Resource> basedOnTarget;


    /**
     * Composite request this is part of.
     */
    @Child(name = "requisition", type = {Identifier.class}, order=3, min=0, max=1, modifier=false, summary=true)
    @Description(shortDefinition="Identifier of composite", formalDefinition="Composite request this is part of." )
    protected Identifier requisition;

    /**
     * A code specifying the state of the order.  Generally this will be active or completed state.
     */
    @Child(name = "status", type = {CodeType.class}, order=4, min=0, max=1, modifier=true, summary=true)
    @Description(shortDefinition="active | on-hold | cancelled | completed | entered-in-error | stopped | draft", formalDefinition="A code specifying the state of the order.  Generally this will be active or completed state." )
    @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/medication-request-status")
    protected Enumeration<MedicationRequestStatus> status;

    /**
     * Whether the request is a proposal, plan, or an original order.
     */
    @Child(name = "stage", type = {CodeableConcept.class}, order=5, min=1, max=1, modifier=true, summary=true)
    @Description(shortDefinition="proposal | plan | original-order", formalDefinition="Whether the request is a proposal, plan, or an original order." )
    @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/medication-request-stage")
    protected CodeableConcept stage;

    /**
     * Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications.
     */
    @Child(name = "medication", type = {CodeableConcept.class, Medication.class}, order=6, min=1, max=1, modifier=false, summary=true)
    @Description(shortDefinition="Medication to be taken", formalDefinition="Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications." )
    @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/medication-codes")
    protected Type medication;

    /**
     * A link to a resource representing the person to whom the medication will be given.
     */
    @Child(name = "patient", type = {Patient.class}, order=7, min=1, max=1, modifier=false, summary=true)
    @Description(shortDefinition="Who prescription is for", formalDefinition="A link to a resource representing the person to whom the medication will be given." )
    protected Reference patient;

    /**
     * The actual object that is the target of the reference (A link to a resource representing the person to whom the medication will be given.)
     */
    protected Patient patientTarget;

    /**
     * A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.
     */
    @Child(name = "context", type = {Encounter.class, EpisodeOfCare.class}, order=8, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Created during encounter/admission/stay", formalDefinition="A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider." )
    protected Reference context;

    /**
     * The actual object that is the target of the reference (A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.)
     */
    protected Resource contextTarget;

    /**
     * Include additional information (for example, patient height and weight) that supports the ordering of the medication.
     */
    @Child(name = "supportingInformation", type = {Reference.class}, order=9, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="Information to support ordering of the medication", formalDefinition="Include additional information (for example, patient height and weight) that supports the ordering of the medication." )
    protected List<Reference> supportingInformation;
    /**
     * The actual objects that are the target of the reference (Include additional information (for example, patient height and weight) that supports the ordering of the medication.)
     */
    protected List<Resource> supportingInformationTarget;


    /**
     * The date (and perhaps time) when the prescription was initially written.
     */
    @Child(name = "dateWritten", type = {DateTimeType.class}, order=10, min=0, max=1, modifier=false, summary=true)
    @Description(shortDefinition="When prescription was initially authorized", formalDefinition="The date (and perhaps time) when the prescription was initially written." )
    protected DateTimeType dateWritten;

    /**
     * The healthcare professional responsible for authorizing the initial prescription.
     */
    @Child(name = "requester", type = {Practitioner.class, Organization.class, Patient.class, RelatedPerson.class, Device.class}, order=11, min=0, max=1, modifier=false, summary=true)
    @Description(shortDefinition="Who ordered the initial medication(s)", formalDefinition="The healthcare professional responsible for authorizing the initial prescription." )
    protected Reference requester;

    /**
     * The actual object that is the target of the reference (The healthcare professional responsible for authorizing the initial prescription.)
     */
    protected Resource requesterTarget;

    /**
     * Can be the reason or the indication for writing the prescription.
     */
    @Child(name = "reasonCode", type = {CodeableConcept.class}, order=12, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="Reason or indication for writing the prescription", formalDefinition="Can be the reason or the indication for writing the prescription." )
    @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/condition-code")
    protected List<CodeableConcept> reasonCode;

    /**
     * Condition or observation that supports why the prescription is being written.
     */
    @Child(name = "reasonReference", type = {Condition.class, Observation.class}, order=13, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="Condition or Observation that supports why the prescription is being written", formalDefinition="Condition or observation that supports why the prescription is being written." )
    protected List<Reference> reasonReference;
    /**
     * The actual objects that are the target of the reference (Condition or observation that supports why the prescription is being written.)
     */
    protected List<Resource> reasonReferenceTarget;


    /**
     * Extra information about the prescription that could not be conveyed by the other attributes.
     */
    @Child(name = "note", type = {Annotation.class}, order=14, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="Information about the prescription", formalDefinition="Extra information about the prescription that could not be conveyed by the other attributes." )
    protected List<Annotation> note;

    /**
     * Indicates where type of medication order and where the medication is expected to be consumed or administered.
     */
    @Child(name = "category", type = {CodeableConcept.class}, order=15, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Type of medication usage", formalDefinition="Indicates where type of medication order and where the medication is expected to be consumed or administered." )
    @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/medication-request-category")
    protected CodeableConcept category;

    /**
     * Indicates how the medication is to be used by the patient.
     */
    @Child(name = "dosageInstruction", type = {DosageInstruction.class}, order=16, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="How the medication should be taken", formalDefinition="Indicates how the medication is to be used by the patient." )
    protected List<DosageInstruction> dosageInstruction;

    /**
     * Indicates the specific details for the dispense or medication supply part of a medication order (also known as a Medication Prescription).  Note that this information is NOT always sent with the order.  There may be in some settings (e.g. hospitals) institutional or system support for completing the dispense details in the pharmacy department.
     */
    @Child(name = "dispenseRequest", type = {}, order=17, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Medication supply authorization", formalDefinition="Indicates the specific details for the dispense or medication supply part of a medication order (also known as a Medication Prescription).  Note that this information is NOT always sent with the order.  There may be in some settings (e.g. hospitals) institutional or system support for completing the dispense details in the pharmacy department." )
    protected MedicationRequestDispenseRequestComponent dispenseRequest;

    /**
     * Indicates whether or not substitution can or should be part of the dispense. In some cases substitution must happen, in other cases substitution must not happen, and in others it does not matter. This block explains the prescriber's intent. If nothing is specified substitution may be done.
     */
    @Child(name = "substitution", type = {}, order=18, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Any restrictions on medication substitution", formalDefinition="Indicates whether or not substitution can or should be part of the dispense. In some cases substitution must happen, in other cases substitution must not happen, and in others it does not matter. This block explains the prescriber's intent. If nothing is specified substitution may be done." )
    protected MedicationRequestSubstitutionComponent substitution;

    /**
     * A link to a resource representing an earlier order related order or prescription.
     */
    @Child(name = "priorPrescription", type = {MedicationRequest.class}, order=19, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="An order/prescription that this supersedes", formalDefinition="A link to a resource representing an earlier order related order or prescription." )
    protected Reference priorPrescription;

    /**
     * The actual object that is the target of the reference (A link to a resource representing an earlier order related order or prescription.)
     */
    protected MedicationRequest priorPrescriptionTarget;

    /**
     * A summary of the events of interest that have occurred as the request is processed; e.g. when the order was verified or when it was completed.
     */
    @Child(name = "eventHistory", type = {Provenance.class}, order=20, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="A list of events of interest in the lifecycle", formalDefinition="A summary of the events of interest that have occurred as the request is processed; e.g. when the order was verified or when it was completed." )
    protected List<Reference> eventHistory;
    /**
     * The actual objects that are the target of the reference (A summary of the events of interest that have occurred as the request is processed; e.g. when the order was verified or when it was completed.)
     */
    protected List<Provenance> eventHistoryTarget;


    private static final long serialVersionUID = -478447012L;

  /**
   * Constructor
   */
    public MedicationRequest() {
      super();
    }

  /**
   * Constructor
   */
    public MedicationRequest(CodeableConcept stage, Type medication, Reference patient) {
      super();
      this.stage = stage;
      this.medication = medication;
      this.patient = patient;
    }

    /**
     * @return {@link #identifier} (External identifier - one that would be used by another non-FHIR system - for example a re-imbursement system might issue its own id for each prescription that is created.  This is particularly important where FHIR only provides part of an entire workflow process where records have to be tracked through an entire system.)
     */
    public List<Identifier> getIdentifier() { 
      if (this.identifier == null)
        this.identifier = new ArrayList<Identifier>();
      return this.identifier;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setIdentifier(List<Identifier> theIdentifier) { 
      this.identifier = theIdentifier;
      return this;
    }

    public boolean hasIdentifier() { 
      if (this.identifier == null)
        return false;
      for (Identifier item : this.identifier)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Identifier addIdentifier() { //3
      Identifier t = new Identifier();
      if (this.identifier == null)
        this.identifier = new ArrayList<Identifier>();
      this.identifier.add(t);
      return t;
    }

    public MedicationRequest addIdentifier(Identifier t) { //3
      if (t == null)
        return this;
      if (this.identifier == null)
        this.identifier = new ArrayList<Identifier>();
      this.identifier.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #identifier}, creating it if it does not already exist
     */
    public Identifier getIdentifierFirstRep() { 
      if (getIdentifier().isEmpty()) {
        addIdentifier();
      }
      return getIdentifier().get(0);
    }

    /**
     * @return {@link #definition} (Protocol or definition followed by this request.)
     */
    public List<Reference> getDefinition() { 
      if (this.definition == null)
        this.definition = new ArrayList<Reference>();
      return this.definition;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setDefinition(List<Reference> theDefinition) { 
      this.definition = theDefinition;
      return this;
    }

    public boolean hasDefinition() { 
      if (this.definition == null)
        return false;
      for (Reference item : this.definition)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Reference addDefinition() { //3
      Reference t = new Reference();
      if (this.definition == null)
        this.definition = new ArrayList<Reference>();
      this.definition.add(t);
      return t;
    }

    public MedicationRequest addDefinition(Reference t) { //3
      if (t == null)
        return this;
      if (this.definition == null)
        this.definition = new ArrayList<Reference>();
      this.definition.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #definition}, creating it if it does not already exist
     */
    public Reference getDefinitionFirstRep() { 
      if (getDefinition().isEmpty()) {
        addDefinition();
      }
      return getDefinition().get(0);
    }

    /**
     * @deprecated Use Reference#setResource(IBaseResource) instead
     */
    @Deprecated
    public List<Resource> getDefinitionTarget() { 
      if (this.definitionTarget == null)
        this.definitionTarget = new ArrayList<Resource>();
      return this.definitionTarget;
    }

    /**
     * @return {@link #basedOn} (Plan/proposal/order fulfilled by this request.)
     */
    public List<Reference> getBasedOn() { 
      if (this.basedOn == null)
        this.basedOn = new ArrayList<Reference>();
      return this.basedOn;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setBasedOn(List<Reference> theBasedOn) { 
      this.basedOn = theBasedOn;
      return this;
    }

    public boolean hasBasedOn() { 
      if (this.basedOn == null)
        return false;
      for (Reference item : this.basedOn)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Reference addBasedOn() { //3
      Reference t = new Reference();
      if (this.basedOn == null)
        this.basedOn = new ArrayList<Reference>();
      this.basedOn.add(t);
      return t;
    }

    public MedicationRequest addBasedOn(Reference t) { //3
      if (t == null)
        return this;
      if (this.basedOn == null)
        this.basedOn = new ArrayList<Reference>();
      this.basedOn.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #basedOn}, creating it if it does not already exist
     */
    public Reference getBasedOnFirstRep() { 
      if (getBasedOn().isEmpty()) {
        addBasedOn();
      }
      return getBasedOn().get(0);
    }

    /**
     * @deprecated Use Reference#setResource(IBaseResource) instead
     */
    @Deprecated
    public List<Resource> getBasedOnTarget() { 
      if (this.basedOnTarget == null)
        this.basedOnTarget = new ArrayList<Resource>();
      return this.basedOnTarget;
    }

    /**
     * @return {@link #requisition} (Composite request this is part of.)
     */
    public Identifier getRequisition() { 
      if (this.requisition == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.requisition");
        else if (Configuration.doAutoCreate())
          this.requisition = new Identifier(); // cc
      return this.requisition;
    }

    public boolean hasRequisition() { 
      return this.requisition != null && !this.requisition.isEmpty();
    }

    /**
     * @param value {@link #requisition} (Composite request this is part of.)
     */
    public MedicationRequest setRequisition(Identifier value) { 
      this.requisition = value;
      return this;
    }

    /**
     * @return {@link #status} (A code specifying the state of the order.  Generally this will be active or completed state.). This is the underlying object with id, value and extensions. The accessor "getStatus" gives direct access to the value
     */
    public Enumeration<MedicationRequestStatus> getStatusElement() { 
      if (this.status == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.status");
        else if (Configuration.doAutoCreate())
          this.status = new Enumeration<MedicationRequestStatus>(new MedicationRequestStatusEnumFactory()); // bb
      return this.status;
    }

    public boolean hasStatusElement() { 
      return this.status != null && !this.status.isEmpty();
    }

    public boolean hasStatus() { 
      return this.status != null && !this.status.isEmpty();
    }

    /**
     * @param value {@link #status} (A code specifying the state of the order.  Generally this will be active or completed state.). This is the underlying object with id, value and extensions. The accessor "getStatus" gives direct access to the value
     */
    public MedicationRequest setStatusElement(Enumeration<MedicationRequestStatus> value) { 
      this.status = value;
      return this;
    }

    /**
     * @return A code specifying the state of the order.  Generally this will be active or completed state.
     */
    public MedicationRequestStatus getStatus() { 
      return this.status == null ? null : this.status.getValue();
    }

    /**
     * @param value A code specifying the state of the order.  Generally this will be active or completed state.
     */
    public MedicationRequest setStatus(MedicationRequestStatus value) { 
      if (value == null)
        this.status = null;
      else {
        if (this.status == null)
          this.status = new Enumeration<MedicationRequestStatus>(new MedicationRequestStatusEnumFactory());
        this.status.setValue(value);
      }
      return this;
    }

    /**
     * @return {@link #stage} (Whether the request is a proposal, plan, or an original order.)
     */
    public CodeableConcept getStage() { 
      if (this.stage == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.stage");
        else if (Configuration.doAutoCreate())
          this.stage = new CodeableConcept(); // cc
      return this.stage;
    }

    public boolean hasStage() { 
      return this.stage != null && !this.stage.isEmpty();
    }

    /**
     * @param value {@link #stage} (Whether the request is a proposal, plan, or an original order.)
     */
    public MedicationRequest setStage(CodeableConcept value) { 
      this.stage = value;
      return this;
    }

    /**
     * @return {@link #medication} (Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications.)
     */
    public Type getMedication() { 
      return this.medication;
    }

    /**
     * @return {@link #medication} (Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications.)
     */
    public CodeableConcept getMedicationCodeableConcept() throws FHIRException { 
      if (!(this.medication instanceof CodeableConcept))
        throw new FHIRException("Type mismatch: the type CodeableConcept was expected, but "+this.medication.getClass().getName()+" was encountered");
      return (CodeableConcept) this.medication;
    }

    public boolean hasMedicationCodeableConcept() { 
      return this.medication instanceof CodeableConcept;
    }

    /**
     * @return {@link #medication} (Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications.)
     */
    public Reference getMedicationReference() throws FHIRException { 
      if (!(this.medication instanceof Reference))
        throw new FHIRException("Type mismatch: the type Reference was expected, but "+this.medication.getClass().getName()+" was encountered");
      return (Reference) this.medication;
    }

    public boolean hasMedicationReference() { 
      return this.medication instanceof Reference;
    }

    public boolean hasMedication() { 
      return this.medication != null && !this.medication.isEmpty();
    }

    /**
     * @param value {@link #medication} (Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications.)
     */
    public MedicationRequest setMedication(Type value) { 
      this.medication = value;
      return this;
    }

    /**
     * @return {@link #patient} (A link to a resource representing the person to whom the medication will be given.)
     */
    public Reference getPatient() { 
      if (this.patient == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.patient");
        else if (Configuration.doAutoCreate())
          this.patient = new Reference(); // cc
      return this.patient;
    }

    public boolean hasPatient() { 
      return this.patient != null && !this.patient.isEmpty();
    }

    /**
     * @param value {@link #patient} (A link to a resource representing the person to whom the medication will be given.)
     */
    public MedicationRequest setPatient(Reference value) { 
      this.patient = value;
      return this;
    }

    /**
     * @return {@link #patient} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (A link to a resource representing the person to whom the medication will be given.)
     */
    public Patient getPatientTarget() { 
      if (this.patientTarget == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.patient");
        else if (Configuration.doAutoCreate())
          this.patientTarget = new Patient(); // aa
      return this.patientTarget;
    }

    /**
     * @param value {@link #patient} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (A link to a resource representing the person to whom the medication will be given.)
     */
    public MedicationRequest setPatientTarget(Patient value) { 
      this.patientTarget = value;
      return this;
    }

    /**
     * @return {@link #context} (A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.)
     */
    public Reference getContext() { 
      if (this.context == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.context");
        else if (Configuration.doAutoCreate())
          this.context = new Reference(); // cc
      return this.context;
    }

    public boolean hasContext() { 
      return this.context != null && !this.context.isEmpty();
    }

    /**
     * @param value {@link #context} (A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.)
     */
    public MedicationRequest setContext(Reference value) { 
      this.context = value;
      return this;
    }

    /**
     * @return {@link #context} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.)
     */
    public Resource getContextTarget() { 
      return this.contextTarget;
    }

    /**
     * @param value {@link #context} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.)
     */
    public MedicationRequest setContextTarget(Resource value) { 
      this.contextTarget = value;
      return this;
    }

    /**
     * @return {@link #supportingInformation} (Include additional information (for example, patient height and weight) that supports the ordering of the medication.)
     */
    public List<Reference> getSupportingInformation() { 
      if (this.supportingInformation == null)
        this.supportingInformation = new ArrayList<Reference>();
      return this.supportingInformation;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setSupportingInformation(List<Reference> theSupportingInformation) { 
      this.supportingInformation = theSupportingInformation;
      return this;
    }

    public boolean hasSupportingInformation() { 
      if (this.supportingInformation == null)
        return false;
      for (Reference item : this.supportingInformation)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Reference addSupportingInformation() { //3
      Reference t = new Reference();
      if (this.supportingInformation == null)
        this.supportingInformation = new ArrayList<Reference>();
      this.supportingInformation.add(t);
      return t;
    }

    public MedicationRequest addSupportingInformation(Reference t) { //3
      if (t == null)
        return this;
      if (this.supportingInformation == null)
        this.supportingInformation = new ArrayList<Reference>();
      this.supportingInformation.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #supportingInformation}, creating it if it does not already exist
     */
    public Reference getSupportingInformationFirstRep() { 
      if (getSupportingInformation().isEmpty()) {
        addSupportingInformation();
      }
      return getSupportingInformation().get(0);
    }

    /**
     * @deprecated Use Reference#setResource(IBaseResource) instead
     */
    @Deprecated
    public List<Resource> getSupportingInformationTarget() { 
      if (this.supportingInformationTarget == null)
        this.supportingInformationTarget = new ArrayList<Resource>();
      return this.supportingInformationTarget;
    }

    /**
     * @return {@link #dateWritten} (The date (and perhaps time) when the prescription was initially written.). This is the underlying object with id, value and extensions. The accessor "getDateWritten" gives direct access to the value
     */
    public DateTimeType getDateWrittenElement() { 
      if (this.dateWritten == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.dateWritten");
        else if (Configuration.doAutoCreate())
          this.dateWritten = new DateTimeType(); // bb
      return this.dateWritten;
    }

    public boolean hasDateWrittenElement() { 
      return this.dateWritten != null && !this.dateWritten.isEmpty();
    }

    public boolean hasDateWritten() { 
      return this.dateWritten != null && !this.dateWritten.isEmpty();
    }

    /**
     * @param value {@link #dateWritten} (The date (and perhaps time) when the prescription was initially written.). This is the underlying object with id, value and extensions. The accessor "getDateWritten" gives direct access to the value
     */
    public MedicationRequest setDateWrittenElement(DateTimeType value) { 
      this.dateWritten = value;
      return this;
    }

    /**
     * @return The date (and perhaps time) when the prescription was initially written.
     */
    public Date getDateWritten() { 
      return this.dateWritten == null ? null : this.dateWritten.getValue();
    }

    /**
     * @param value The date (and perhaps time) when the prescription was initially written.
     */
    public MedicationRequest setDateWritten(Date value) { 
      if (value == null)
        this.dateWritten = null;
      else {
        if (this.dateWritten == null)
          this.dateWritten = new DateTimeType();
        this.dateWritten.setValue(value);
      }
      return this;
    }

    /**
     * @return {@link #requester} (The healthcare professional responsible for authorizing the initial prescription.)
     */
    public Reference getRequester() { 
      if (this.requester == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.requester");
        else if (Configuration.doAutoCreate())
          this.requester = new Reference(); // cc
      return this.requester;
    }

    public boolean hasRequester() { 
      return this.requester != null && !this.requester.isEmpty();
    }

    /**
     * @param value {@link #requester} (The healthcare professional responsible for authorizing the initial prescription.)
     */
    public MedicationRequest setRequester(Reference value) { 
      this.requester = value;
      return this;
    }

    /**
     * @return {@link #requester} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (The healthcare professional responsible for authorizing the initial prescription.)
     */
    public Resource getRequesterTarget() { 
      return this.requesterTarget;
    }

    /**
     * @param value {@link #requester} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (The healthcare professional responsible for authorizing the initial prescription.)
     */
    public MedicationRequest setRequesterTarget(Resource value) { 
      this.requesterTarget = value;
      return this;
    }

    /**
     * @return {@link #reasonCode} (Can be the reason or the indication for writing the prescription.)
     */
    public List<CodeableConcept> getReasonCode() { 
      if (this.reasonCode == null)
        this.reasonCode = new ArrayList<CodeableConcept>();
      return this.reasonCode;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setReasonCode(List<CodeableConcept> theReasonCode) { 
      this.reasonCode = theReasonCode;
      return this;
    }

    public boolean hasReasonCode() { 
      if (this.reasonCode == null)
        return false;
      for (CodeableConcept item : this.reasonCode)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public CodeableConcept addReasonCode() { //3
      CodeableConcept t = new CodeableConcept();
      if (this.reasonCode == null)
        this.reasonCode = new ArrayList<CodeableConcept>();
      this.reasonCode.add(t);
      return t;
    }

    public MedicationRequest addReasonCode(CodeableConcept t) { //3
      if (t == null)
        return this;
      if (this.reasonCode == null)
        this.reasonCode = new ArrayList<CodeableConcept>();
      this.reasonCode.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #reasonCode}, creating it if it does not already exist
     */
    public CodeableConcept getReasonCodeFirstRep() { 
      if (getReasonCode().isEmpty()) {
        addReasonCode();
      }
      return getReasonCode().get(0);
    }

    /**
     * @return {@link #reasonReference} (Condition or observation that supports why the prescription is being written.)
     */
    public List<Reference> getReasonReference() { 
      if (this.reasonReference == null)
        this.reasonReference = new ArrayList<Reference>();
      return this.reasonReference;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setReasonReference(List<Reference> theReasonReference) { 
      this.reasonReference = theReasonReference;
      return this;
    }

    public boolean hasReasonReference() { 
      if (this.reasonReference == null)
        return false;
      for (Reference item : this.reasonReference)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Reference addReasonReference() { //3
      Reference t = new Reference();
      if (this.reasonReference == null)
        this.reasonReference = new ArrayList<Reference>();
      this.reasonReference.add(t);
      return t;
    }

    public MedicationRequest addReasonReference(Reference t) { //3
      if (t == null)
        return this;
      if (this.reasonReference == null)
        this.reasonReference = new ArrayList<Reference>();
      this.reasonReference.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #reasonReference}, creating it if it does not already exist
     */
    public Reference getReasonReferenceFirstRep() { 
      if (getReasonReference().isEmpty()) {
        addReasonReference();
      }
      return getReasonReference().get(0);
    }

    /**
     * @deprecated Use Reference#setResource(IBaseResource) instead
     */
    @Deprecated
    public List<Resource> getReasonReferenceTarget() { 
      if (this.reasonReferenceTarget == null)
        this.reasonReferenceTarget = new ArrayList<Resource>();
      return this.reasonReferenceTarget;
    }

    /**
     * @return {@link #note} (Extra information about the prescription that could not be conveyed by the other attributes.)
     */
    public List<Annotation> getNote() { 
      if (this.note == null)
        this.note = new ArrayList<Annotation>();
      return this.note;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setNote(List<Annotation> theNote) { 
      this.note = theNote;
      return this;
    }

    public boolean hasNote() { 
      if (this.note == null)
        return false;
      for (Annotation item : this.note)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Annotation addNote() { //3
      Annotation t = new Annotation();
      if (this.note == null)
        this.note = new ArrayList<Annotation>();
      this.note.add(t);
      return t;
    }

    public MedicationRequest addNote(Annotation t) { //3
      if (t == null)
        return this;
      if (this.note == null)
        this.note = new ArrayList<Annotation>();
      this.note.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #note}, creating it if it does not already exist
     */
    public Annotation getNoteFirstRep() { 
      if (getNote().isEmpty()) {
        addNote();
      }
      return getNote().get(0);
    }

    /**
     * @return {@link #category} (Indicates where type of medication order and where the medication is expected to be consumed or administered.)
     */
    public CodeableConcept getCategory() { 
      if (this.category == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.category");
        else if (Configuration.doAutoCreate())
          this.category = new CodeableConcept(); // cc
      return this.category;
    }

    public boolean hasCategory() { 
      return this.category != null && !this.category.isEmpty();
    }

    /**
     * @param value {@link #category} (Indicates where type of medication order and where the medication is expected to be consumed or administered.)
     */
    public MedicationRequest setCategory(CodeableConcept value) { 
      this.category = value;
      return this;
    }

    /**
     * @return {@link #dosageInstruction} (Indicates how the medication is to be used by the patient.)
     */
    public List<DosageInstruction> getDosageInstruction() { 
      if (this.dosageInstruction == null)
        this.dosageInstruction = new ArrayList<DosageInstruction>();
      return this.dosageInstruction;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setDosageInstruction(List<DosageInstruction> theDosageInstruction) { 
      this.dosageInstruction = theDosageInstruction;
      return this;
    }

    public boolean hasDosageInstruction() { 
      if (this.dosageInstruction == null)
        return false;
      for (DosageInstruction item : this.dosageInstruction)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public DosageInstruction addDosageInstruction() { //3
      DosageInstruction t = new DosageInstruction();
      if (this.dosageInstruction == null)
        this.dosageInstruction = new ArrayList<DosageInstruction>();
      this.dosageInstruction.add(t);
      return t;
    }

    public MedicationRequest addDosageInstruction(DosageInstruction t) { //3
      if (t == null)
        return this;
      if (this.dosageInstruction == null)
        this.dosageInstruction = new ArrayList<DosageInstruction>();
      this.dosageInstruction.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #dosageInstruction}, creating it if it does not already exist
     */
    public DosageInstruction getDosageInstructionFirstRep() { 
      if (getDosageInstruction().isEmpty()) {
        addDosageInstruction();
      }
      return getDosageInstruction().get(0);
    }

    /**
     * @return {@link #dispenseRequest} (Indicates the specific details for the dispense or medication supply part of a medication order (also known as a Medication Prescription).  Note that this information is NOT always sent with the order.  There may be in some settings (e.g. hospitals) institutional or system support for completing the dispense details in the pharmacy department.)
     */
    public MedicationRequestDispenseRequestComponent getDispenseRequest() { 
      if (this.dispenseRequest == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.dispenseRequest");
        else if (Configuration.doAutoCreate())
          this.dispenseRequest = new MedicationRequestDispenseRequestComponent(); // cc
      return this.dispenseRequest;
    }

    public boolean hasDispenseRequest() { 
      return this.dispenseRequest != null && !this.dispenseRequest.isEmpty();
    }

    /**
     * @param value {@link #dispenseRequest} (Indicates the specific details for the dispense or medication supply part of a medication order (also known as a Medication Prescription).  Note that this information is NOT always sent with the order.  There may be in some settings (e.g. hospitals) institutional or system support for completing the dispense details in the pharmacy department.)
     */
    public MedicationRequest setDispenseRequest(MedicationRequestDispenseRequestComponent value) { 
      this.dispenseRequest = value;
      return this;
    }

    /**
     * @return {@link #substitution} (Indicates whether or not substitution can or should be part of the dispense. In some cases substitution must happen, in other cases substitution must not happen, and in others it does not matter. This block explains the prescriber's intent. If nothing is specified substitution may be done.)
     */
    public MedicationRequestSubstitutionComponent getSubstitution() { 
      if (this.substitution == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.substitution");
        else if (Configuration.doAutoCreate())
          this.substitution = new MedicationRequestSubstitutionComponent(); // cc
      return this.substitution;
    }

    public boolean hasSubstitution() { 
      return this.substitution != null && !this.substitution.isEmpty();
    }

    /**
     * @param value {@link #substitution} (Indicates whether or not substitution can or should be part of the dispense. In some cases substitution must happen, in other cases substitution must not happen, and in others it does not matter. This block explains the prescriber's intent. If nothing is specified substitution may be done.)
     */
    public MedicationRequest setSubstitution(MedicationRequestSubstitutionComponent value) { 
      this.substitution = value;
      return this;
    }

    /**
     * @return {@link #priorPrescription} (A link to a resource representing an earlier order related order or prescription.)
     */
    public Reference getPriorPrescription() { 
      if (this.priorPrescription == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.priorPrescription");
        else if (Configuration.doAutoCreate())
          this.priorPrescription = new Reference(); // cc
      return this.priorPrescription;
    }

    public boolean hasPriorPrescription() { 
      return this.priorPrescription != null && !this.priorPrescription.isEmpty();
    }

    /**
     * @param value {@link #priorPrescription} (A link to a resource representing an earlier order related order or prescription.)
     */
    public MedicationRequest setPriorPrescription(Reference value) { 
      this.priorPrescription = value;
      return this;
    }

    /**
     * @return {@link #priorPrescription} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (A link to a resource representing an earlier order related order or prescription.)
     */
    public MedicationRequest getPriorPrescriptionTarget() { 
      if (this.priorPrescriptionTarget == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create MedicationRequest.priorPrescription");
        else if (Configuration.doAutoCreate())
          this.priorPrescriptionTarget = new MedicationRequest(); // aa
      return this.priorPrescriptionTarget;
    }

    /**
     * @param value {@link #priorPrescription} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (A link to a resource representing an earlier order related order or prescription.)
     */
    public MedicationRequest setPriorPrescriptionTarget(MedicationRequest value) { 
      this.priorPrescriptionTarget = value;
      return this;
    }

    /**
     * @return {@link #eventHistory} (A summary of the events of interest that have occurred as the request is processed; e.g. when the order was verified or when it was completed.)
     */
    public List<Reference> getEventHistory() { 
      if (this.eventHistory == null)
        this.eventHistory = new ArrayList<Reference>();
      return this.eventHistory;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public MedicationRequest setEventHistory(List<Reference> theEventHistory) { 
      this.eventHistory = theEventHistory;
      return this;
    }

    public boolean hasEventHistory() { 
      if (this.eventHistory == null)
        return false;
      for (Reference item : this.eventHistory)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public Reference addEventHistory() { //3
      Reference t = new Reference();
      if (this.eventHistory == null)
        this.eventHistory = new ArrayList<Reference>();
      this.eventHistory.add(t);
      return t;
    }

    public MedicationRequest addEventHistory(Reference t) { //3
      if (t == null)
        return this;
      if (this.eventHistory == null)
        this.eventHistory = new ArrayList<Reference>();
      this.eventHistory.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #eventHistory}, creating it if it does not already exist
     */
    public Reference getEventHistoryFirstRep() { 
      if (getEventHistory().isEmpty()) {
        addEventHistory();
      }
      return getEventHistory().get(0);
    }

    /**
     * @deprecated Use Reference#setResource(IBaseResource) instead
     */
    @Deprecated
    public List<Provenance> getEventHistoryTarget() { 
      if (this.eventHistoryTarget == null)
        this.eventHistoryTarget = new ArrayList<Provenance>();
      return this.eventHistoryTarget;
    }

    /**
     * @deprecated Use Reference#setResource(IBaseResource) instead
     */
    @Deprecated
    public Provenance addEventHistoryTarget() { 
      Provenance r = new Provenance();
      if (this.eventHistoryTarget == null)
        this.eventHistoryTarget = new ArrayList<Provenance>();
      this.eventHistoryTarget.add(r);
      return r;
    }

      protected void listChildren(List<Property> childrenList) {
        super.listChildren(childrenList);
        childrenList.add(new Property("identifier", "Identifier", "External identifier - one that would be used by another non-FHIR system - for example a re-imbursement system might issue its own id for each prescription that is created.  This is particularly important where FHIR only provides part of an entire workflow process where records have to be tracked through an entire system.", 0, java.lang.Integer.MAX_VALUE, identifier));
        childrenList.add(new Property("definition", "Reference(ActivityDefinition|PlanDefinition)", "Protocol or definition followed by this request.", 0, java.lang.Integer.MAX_VALUE, definition));
        childrenList.add(new Property("basedOn", "Reference(CarePlan|DiagnosticRequest|MedicationRequest|ProcedureRequest|ReferralRequest)", "Plan/proposal/order fulfilled by this request.", 0, java.lang.Integer.MAX_VALUE, basedOn));
        childrenList.add(new Property("requisition", "Identifier", "Composite request this is part of.", 0, java.lang.Integer.MAX_VALUE, requisition));
        childrenList.add(new Property("status", "code", "A code specifying the state of the order.  Generally this will be active or completed state.", 0, java.lang.Integer.MAX_VALUE, status));
        childrenList.add(new Property("stage", "CodeableConcept", "Whether the request is a proposal, plan, or an original order.", 0, java.lang.Integer.MAX_VALUE, stage));
        childrenList.add(new Property("medication[x]", "CodeableConcept|Reference(Medication)", "Identifies the medication being administered. This is a link to a resource that represents the medication which may be the details of the medication or simply an attribute carrying a code that identifies the medication from a known list of medications.", 0, java.lang.Integer.MAX_VALUE, medication));
        childrenList.add(new Property("patient", "Reference(Patient)", "A link to a resource representing the person to whom the medication will be given.", 0, java.lang.Integer.MAX_VALUE, patient));
        childrenList.add(new Property("context", "Reference(Encounter|EpisodeOfCare)", "A link to a resource that identifies the particular occurrence or set oc occurences of contact between patient and health care provider.", 0, java.lang.Integer.MAX_VALUE, context));
        childrenList.add(new Property("supportingInformation", "Reference(Any)", "Include additional information (for example, patient height and weight) that supports the ordering of the medication.", 0, java.lang.Integer.MAX_VALUE, supportingInformation));
        childrenList.add(new Property("dateWritten", "dateTime", "The date (and perhaps time) when the prescription was initially written.", 0, java.lang.Integer.MAX_VALUE, dateWritten));
        childrenList.add(new Property("requester", "Reference(Practitioner|Organization|Patient|RelatedPerson|Device)", "The healthcare professional responsible for authorizing the initial prescription.", 0, java.lang.Integer.MAX_VALUE, requester));
        childrenList.add(new Property("reasonCode", "CodeableConcept", "Can be the reason or the indication for writing the prescription.", 0, java.lang.Integer.MAX_VALUE, reasonCode));
        childrenList.add(new Property("reasonReference", "Reference(Condition|Observation)", "Condition or observation that supports why the prescription is being written.", 0, java.lang.Integer.MAX_VALUE, reasonReference));
        childrenList.add(new Property("note", "Annotation", "Extra information about the prescription that could not be conveyed by the other attributes.", 0, java.lang.Integer.MAX_VALUE, note));
        childrenList.add(new Property("category", "CodeableConcept", "Indicates where type of medication order and where the medication is expected to be consumed or administered.", 0, java.lang.Integer.MAX_VALUE, category));
        childrenList.add(new Property("dosageInstruction", "DosageInstruction", "Indicates how the medication is to be used by the patient.", 0, java.lang.Integer.MAX_VALUE, dosageInstruction));
        childrenList.add(new Property("dispenseRequest", "", "Indicates the specific details for the dispense or medication supply part of a medication order (also known as a Medication Prescription).  Note that this information is NOT always sent with the order.  There may be in some settings (e.g. hospitals) institutional or system support for completing the dispense details in the pharmacy department.", 0, java.lang.Integer.MAX_VALUE, dispenseRequest));
        childrenList.add(new Property("substitution", "", "Indicates whether or not substitution can or should be part of the dispense. In some cases substitution must happen, in other cases substitution must not happen, and in others it does not matter. This block explains the prescriber's intent. If nothing is specified substitution may be done.", 0, java.lang.Integer.MAX_VALUE, substitution));
        childrenList.add(new Property("priorPrescription", "Reference(MedicationRequest)", "A link to a resource representing an earlier order related order or prescription.", 0, java.lang.Integer.MAX_VALUE, priorPrescription));
        childrenList.add(new Property("eventHistory", "Reference(Provenance)", "A summary of the events of interest that have occurred as the request is processed; e.g. when the order was verified or when it was completed.", 0, java.lang.Integer.MAX_VALUE, eventHistory));
      }

      @Override
      public Base[] getProperty(int hash, String name, boolean checkValid) throws FHIRException {
        switch (hash) {
        case -1618432855: /*identifier*/ return this.identifier == null ? new Base[0] : this.identifier.toArray(new Base[this.identifier.size()]); // Identifier
        case -1014418093: /*definition*/ return this.definition == null ? new Base[0] : this.definition.toArray(new Base[this.definition.size()]); // Reference
        case -332612366: /*basedOn*/ return this.basedOn == null ? new Base[0] : this.basedOn.toArray(new Base[this.basedOn.size()]); // Reference
        case 395923612: /*requisition*/ return this.requisition == null ? new Base[0] : new Base[] {this.requisition}; // Identifier
        case -892481550: /*status*/ return this.status == null ? new Base[0] : new Base[] {this.status}; // Enumeration<MedicationRequestStatus>
        case 109757182: /*stage*/ return this.stage == null ? new Base[0] : new Base[] {this.stage}; // CodeableConcept
        case 1998965455: /*medication*/ return this.medication == null ? new Base[0] : new Base[] {this.medication}; // Type
        case -791418107: /*patient*/ return this.patient == null ? new Base[0] : new Base[] {this.patient}; // Reference
        case 951530927: /*context*/ return this.context == null ? new Base[0] : new Base[] {this.context}; // Reference
        case -1248768647: /*supportingInformation*/ return this.supportingInformation == null ? new Base[0] : this.supportingInformation.toArray(new Base[this.supportingInformation.size()]); // Reference
        case -1496880759: /*dateWritten*/ return this.dateWritten == null ? new Base[0] : new Base[] {this.dateWritten}; // DateTimeType
        case 693933948: /*requester*/ return this.requester == null ? new Base[0] : new Base[] {this.requester}; // Reference
        case 722137681: /*reasonCode*/ return this.reasonCode == null ? new Base[0] : this.reasonCode.toArray(new Base[this.reasonCode.size()]); // CodeableConcept
        case -1146218137: /*reasonReference*/ return this.reasonReference == null ? new Base[0] : this.reasonReference.toArray(new Base[this.reasonReference.size()]); // Reference
        case 3387378: /*note*/ return this.note == null ? new Base[0] : this.note.toArray(new Base[this.note.size()]); // Annotation
        case 50511102: /*category*/ return this.category == null ? new Base[0] : new Base[] {this.category}; // CodeableConcept
        case -1201373865: /*dosageInstruction*/ return this.dosageInstruction == null ? new Base[0] : this.dosageInstruction.toArray(new Base[this.dosageInstruction.size()]); // DosageInstruction
        case 824620658: /*dispenseRequest*/ return this.dispenseRequest == null ? new Base[0] : new Base[] {this.dispenseRequest}; // MedicationRequestDispenseRequestComponent
        case 826147581: /*substitution*/ return this.substitution == null ? new Base[0] : new Base[] {this.substitution}; // MedicationRequestSubstitutionComponent
        case -486355964: /*priorPrescription*/ return this.priorPrescription == null ? new Base[0] : new Base[] {this.priorPrescription}; // Reference
        case 1835190426: /*eventHistory*/ return this.eventHistory == null ? new Base[0] : this.eventHistory.toArray(new Base[this.eventHistory.size()]); // Reference
        default: return super.getProperty(hash, name, checkValid);
        }

      }

      @Override
      public void setProperty(int hash, String name, Base value) throws FHIRException {
        switch (hash) {
        case -1618432855: // identifier
          this.getIdentifier().add(castToIdentifier(value)); // Identifier
          break;
        case -1014418093: // definition
          this.getDefinition().add(castToReference(value)); // Reference
          break;
        case -332612366: // basedOn
          this.getBasedOn().add(castToReference(value)); // Reference
          break;
        case 395923612: // requisition
          this.requisition = castToIdentifier(value); // Identifier
          break;
        case -892481550: // status
          this.status = new MedicationRequestStatusEnumFactory().fromType(value); // Enumeration<MedicationRequestStatus>
          break;
        case 109757182: // stage
          this.stage = castToCodeableConcept(value); // CodeableConcept
          break;
        case 1998965455: // medication
          this.medication = castToType(value); // Type
          break;
        case -791418107: // patient
          this.patient = castToReference(value); // Reference
          break;
        case 951530927: // context
          this.context = castToReference(value); // Reference
          break;
        case -1248768647: // supportingInformation
          this.getSupportingInformation().add(castToReference(value)); // Reference
          break;
        case -1496880759: // dateWritten
          this.dateWritten = castToDateTime(value); // DateTimeType
          break;
        case 693933948: // requester
          this.requester = castToReference(value); // Reference
          break;
        case 722137681: // reasonCode
          this.getReasonCode().add(castToCodeableConcept(value)); // CodeableConcept
          break;
        case -1146218137: // reasonReference
          this.getReasonReference().add(castToReference(value)); // Reference
          break;
        case 3387378: // note
          this.getNote().add(castToAnnotation(value)); // Annotation
          break;
        case 50511102: // category
          this.category = castToCodeableConcept(value); // CodeableConcept
          break;
        case -1201373865: // dosageInstruction
          this.getDosageInstruction().add(castToDosageInstruction(value)); // DosageInstruction
          break;
        case 824620658: // dispenseRequest
          this.dispenseRequest = (MedicationRequestDispenseRequestComponent) value; // MedicationRequestDispenseRequestComponent
          break;
        case 826147581: // substitution
          this.substitution = (MedicationRequestSubstitutionComponent) value; // MedicationRequestSubstitutionComponent
          break;
        case -486355964: // priorPrescription
          this.priorPrescription = castToReference(value); // Reference
          break;
        case 1835190426: // eventHistory
          this.getEventHistory().add(castToReference(value)); // Reference
          break;
        default: super.setProperty(hash, name, value);
        }

      }

      @Override
      public void setProperty(String name, Base value) throws FHIRException {
        if (name.equals("identifier"))
          this.getIdentifier().add(castToIdentifier(value));
        else if (name.equals("definition"))
          this.getDefinition().add(castToReference(value));
        else if (name.equals("basedOn"))
          this.getBasedOn().add(castToReference(value));
        else if (name.equals("requisition"))
          this.requisition = castToIdentifier(value); // Identifier
        else if (name.equals("status"))
          this.status = new MedicationRequestStatusEnumFactory().fromType(value); // Enumeration<MedicationRequestStatus>
        else if (name.equals("stage"))
          this.stage = castToCodeableConcept(value); // CodeableConcept
        else if (name.equals("medication[x]"))
          this.medication = castToType(value); // Type
        else if (name.equals("patient"))
          this.patient = castToReference(value); // Reference
        else if (name.equals("context"))
          this.context = castToReference(value); // Reference
        else if (name.equals("supportingInformation"))
          this.getSupportingInformation().add(castToReference(value));
        else if (name.equals("dateWritten"))
          this.dateWritten = castToDateTime(value); // DateTimeType
        else if (name.equals("requester"))
          this.requester = castToReference(value); // Reference
        else if (name.equals("reasonCode"))
          this.getReasonCode().add(castToCodeableConcept(value));
        else if (name.equals("reasonReference"))
          this.getReasonReference().add(castToReference(value));
        else if (name.equals("note"))
          this.getNote().add(castToAnnotation(value));
        else if (name.equals("category"))
          this.category = castToCodeableConcept(value); // CodeableConcept
        else if (name.equals("dosageInstruction"))
          this.getDosageInstruction().add(castToDosageInstruction(value));
        else if (name.equals("dispenseRequest"))
          this.dispenseRequest = (MedicationRequestDispenseRequestComponent) value; // MedicationRequestDispenseRequestComponent
        else if (name.equals("substitution"))
          this.substitution = (MedicationRequestSubstitutionComponent) value; // MedicationRequestSubstitutionComponent
        else if (name.equals("priorPrescription"))
          this.priorPrescription = castToReference(value); // Reference
        else if (name.equals("eventHistory"))
          this.getEventHistory().add(castToReference(value));
        else
          super.setProperty(name, value);
      }

      @Override
      public Base makeProperty(int hash, String name) throws FHIRException {
        switch (hash) {
        case -1618432855:  return addIdentifier(); // Identifier
        case -1014418093:  return addDefinition(); // Reference
        case -332612366:  return addBasedOn(); // Reference
        case 395923612:  return getRequisition(); // Identifier
        case -892481550: throw new FHIRException("Cannot make property status as it is not a complex type"); // Enumeration<MedicationRequestStatus>
        case 109757182:  return getStage(); // CodeableConcept
        case 1458402129:  return getMedication(); // Type
        case -791418107:  return getPatient(); // Reference
        case 951530927:  return getContext(); // Reference
        case -1248768647:  return addSupportingInformation(); // Reference
        case -1496880759: throw new FHIRException("Cannot make property dateWritten as it is not a complex type"); // DateTimeType
        case 693933948:  return getRequester(); // Reference
        case 722137681:  return addReasonCode(); // CodeableConcept
        case -1146218137:  return addReasonReference(); // Reference
        case 3387378:  return addNote(); // Annotation
        case 50511102:  return getCategory(); // CodeableConcept
        case -1201373865:  return addDosageInstruction(); // DosageInstruction
        case 824620658:  return getDispenseRequest(); // MedicationRequestDispenseRequestComponent
        case 826147581:  return getSubstitution(); // MedicationRequestSubstitutionComponent
        case -486355964:  return getPriorPrescription(); // Reference
        case 1835190426:  return addEventHistory(); // Reference
        default: return super.makeProperty(hash, name);
        }

      }

      @Override
      public Base addChild(String name) throws FHIRException {
        if (name.equals("identifier")) {
          return addIdentifier();
        }
        else if (name.equals("definition")) {
          return addDefinition();
        }
        else if (name.equals("basedOn")) {
          return addBasedOn();
        }
        else if (name.equals("requisition")) {
          this.requisition = new Identifier();
          return this.requisition;
        }
        else if (name.equals("status")) {
          throw new FHIRException("Cannot call addChild on a primitive type MedicationRequest.status");
        }
        else if (name.equals("stage")) {
          this.stage = new CodeableConcept();
          return this.stage;
        }
        else if (name.equals("medicationCodeableConcept")) {
          this.medication = new CodeableConcept();
          return this.medication;
        }
        else if (name.equals("medicationReference")) {
          this.medication = new Reference();
          return this.medication;
        }
        else if (name.equals("patient")) {
          this.patient = new Reference();
          return this.patient;
        }
        else if (name.equals("context")) {
          this.context = new Reference();
          return this.context;
        }
        else if (name.equals("supportingInformation")) {
          return addSupportingInformation();
        }
        else if (name.equals("dateWritten")) {
          throw new FHIRException("Cannot call addChild on a primitive type MedicationRequest.dateWritten");
        }
        else if (name.equals("requester")) {
          this.requester = new Reference();
          return this.requester;
        }
        else if (name.equals("reasonCode")) {
          return addReasonCode();
        }
        else if (name.equals("reasonReference")) {
          return addReasonReference();
        }
        else if (name.equals("note")) {
          return addNote();
        }
        else if (name.equals("category")) {
          this.category = new CodeableConcept();
          return this.category;
        }
        else if (name.equals("dosageInstruction")) {
          return addDosageInstruction();
        }
        else if (name.equals("dispenseRequest")) {
          this.dispenseRequest = new MedicationRequestDispenseRequestComponent();
          return this.dispenseRequest;
        }
        else if (name.equals("substitution")) {
          this.substitution = new MedicationRequestSubstitutionComponent();
          return this.substitution;
        }
        else if (name.equals("priorPrescription")) {
          this.priorPrescription = new Reference();
          return this.priorPrescription;
        }
        else if (name.equals("eventHistory")) {
          return addEventHistory();
        }
        else
          return super.addChild(name);
      }

  public String fhirType() {
    return "MedicationRequest";

  }

      public MedicationRequest copy() {
        MedicationRequest dst = new MedicationRequest();
        copyValues(dst);
        if (identifier != null) {
          dst.identifier = new ArrayList<Identifier>();
          for (Identifier i : identifier)
            dst.identifier.add(i.copy());
        };
        if (definition != null) {
          dst.definition = new ArrayList<Reference>();
          for (Reference i : definition)
            dst.definition.add(i.copy());
        };
        if (basedOn != null) {
          dst.basedOn = new ArrayList<Reference>();
          for (Reference i : basedOn)
            dst.basedOn.add(i.copy());
        };
        dst.requisition = requisition == null ? null : requisition.copy();
        dst.status = status == null ? null : status.copy();
        dst.stage = stage == null ? null : stage.copy();
        dst.medication = medication == null ? null : medication.copy();
        dst.patient = patient == null ? null : patient.copy();
        dst.context = context == null ? null : context.copy();
        if (supportingInformation != null) {
          dst.supportingInformation = new ArrayList<Reference>();
          for (Reference i : supportingInformation)
            dst.supportingInformation.add(i.copy());
        };
        dst.dateWritten = dateWritten == null ? null : dateWritten.copy();
        dst.requester = requester == null ? null : requester.copy();
        if (reasonCode != null) {
          dst.reasonCode = new ArrayList<CodeableConcept>();
          for (CodeableConcept i : reasonCode)
            dst.reasonCode.add(i.copy());
        };
        if (reasonReference != null) {
          dst.reasonReference = new ArrayList<Reference>();
          for (Reference i : reasonReference)
            dst.reasonReference.add(i.copy());
        };
        if (note != null) {
          dst.note = new ArrayList<Annotation>();
          for (Annotation i : note)
            dst.note.add(i.copy());
        };
        dst.category = category == null ? null : category.copy();
        if (dosageInstruction != null) {
          dst.dosageInstruction = new ArrayList<DosageInstruction>();
          for (DosageInstruction i : dosageInstruction)
            dst.dosageInstruction.add(i.copy());
        };
        dst.dispenseRequest = dispenseRequest == null ? null : dispenseRequest.copy();
        dst.substitution = substitution == null ? null : substitution.copy();
        dst.priorPrescription = priorPrescription == null ? null : priorPrescription.copy();
        if (eventHistory != null) {
          dst.eventHistory = new ArrayList<Reference>();
          for (Reference i : eventHistory)
            dst.eventHistory.add(i.copy());
        };
        return dst;
      }

      protected MedicationRequest typedCopy() {
        return copy();
      }

      @Override
      public boolean equalsDeep(Base other) {
        if (!super.equalsDeep(other))
          return false;
        if (!(other instanceof MedicationRequest))
          return false;
        MedicationRequest o = (MedicationRequest) other;
        return compareDeep(identifier, o.identifier, true) && compareDeep(definition, o.definition, true)
           && compareDeep(basedOn, o.basedOn, true) && compareDeep(requisition, o.requisition, true) && compareDeep(status, o.status, true)
           && compareDeep(stage, o.stage, true) && compareDeep(medication, o.medication, true) && compareDeep(patient, o.patient, true)
           && compareDeep(context, o.context, true) && compareDeep(supportingInformation, o.supportingInformation, true)
           && compareDeep(dateWritten, o.dateWritten, true) && compareDeep(requester, o.requester, true) && compareDeep(reasonCode, o.reasonCode, true)
           && compareDeep(reasonReference, o.reasonReference, true) && compareDeep(note, o.note, true) && compareDeep(category, o.category, true)
           && compareDeep(dosageInstruction, o.dosageInstruction, true) && compareDeep(dispenseRequest, o.dispenseRequest, true)
           && compareDeep(substitution, o.substitution, true) && compareDeep(priorPrescription, o.priorPrescription, true)
           && compareDeep(eventHistory, o.eventHistory, true);
      }

      @Override
      public boolean equalsShallow(Base other) {
        if (!super.equalsShallow(other))
          return false;
        if (!(other instanceof MedicationRequest))
          return false;
        MedicationRequest o = (MedicationRequest) other;
        return compareValues(status, o.status, true) && compareValues(dateWritten, o.dateWritten, true);
      }

      public boolean isEmpty() {
        return super.isEmpty() && ca.uhn.fhir.util.ElementUtil.isEmpty(identifier, definition, basedOn
          , requisition, status, stage, medication, patient, context, supportingInformation
          , dateWritten, requester, reasonCode, reasonReference, note, category, dosageInstruction
          , dispenseRequest, substitution, priorPrescription, eventHistory);
      }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.MedicationRequest;
   }

 /**
   * Search parameter: <b>requester</b>
   * <p>
   * Description: <b>Returns prescriptions prescribed by this prescriber</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.requester</b><br>
   * </p>
   */
  @SearchParamDefinition(name="requester", path="MedicationRequest.requester", description="Returns prescriptions prescribed by this prescriber", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Practitioner") }, target={Device.class, Organization.class, Patient.class, Practitioner.class, RelatedPerson.class } )
  public static final String SP_REQUESTER = "requester";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>requester</b>
   * <p>
   * Description: <b>Returns prescriptions prescribed by this prescriber</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.requester</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam REQUESTER = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_REQUESTER);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>MedicationRequest:requester</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_REQUESTER = new ca.uhn.fhir.model.api.Include("MedicationRequest:requester").toLocked();

 /**
   * Search parameter: <b>identifier</b>
   * <p>
   * Description: <b>Return prescriptions with this external identifier</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.identifier</b><br>
   * </p>
   */
  @SearchParamDefinition(name="identifier", path="MedicationRequest.identifier", description="Return prescriptions with this external identifier", type="token" )
  public static final String SP_IDENTIFIER = "identifier";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>identifier</b>
   * <p>
   * Description: <b>Return prescriptions with this external identifier</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.identifier</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.TokenClientParam IDENTIFIER = new ca.uhn.fhir.rest.gclient.TokenClientParam(SP_IDENTIFIER);

 /**
   * Search parameter: <b>intended-dispenser</b>
   * <p>
   * Description: <b>Returns prescriptions intended to be dispensed by this Organization</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.dispenseRequest.performer</b><br>
   * </p>
   */
  @SearchParamDefinition(name="intended-dispenser", path="MedicationRequest.dispenseRequest.performer", description="Returns prescriptions intended to be dispensed by this Organization", type="reference", target={Organization.class } )
  public static final String SP_INTENDED_DISPENSER = "intended-dispenser";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>intended-dispenser</b>
   * <p>
   * Description: <b>Returns prescriptions intended to be dispensed by this Organization</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.dispenseRequest.performer</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam INTENDED_DISPENSER = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_INTENDED_DISPENSER);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>MedicationRequest:intended-dispenser</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_INTENDED_DISPENSER = new ca.uhn.fhir.model.api.Include("MedicationRequest:intended-dispenser").toLocked();

 /**
   * Search parameter: <b>code</b>
   * <p>
   * Description: <b>Return prescriptions of this medication code</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.medicationCodeableConcept</b><br>
   * </p>
   */
  @SearchParamDefinition(name="code", path="MedicationRequest.medication.as(CodeableConcept)", description="Return prescriptions of this medication code", type="token" )
  public static final String SP_CODE = "code";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>code</b>
   * <p>
   * Description: <b>Return prescriptions of this medication code</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.medicationCodeableConcept</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.TokenClientParam CODE = new ca.uhn.fhir.rest.gclient.TokenClientParam(SP_CODE);

 /**
   * Search parameter: <b>patient</b>
   * <p>
   * Description: <b>The identity of a patient to list orders  for</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.patient</b><br>
   * </p>
   */
  @SearchParamDefinition(name="patient", path="MedicationRequest.patient", description="The identity of a patient to list orders  for", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Patient") }, target={Patient.class } )
  public static final String SP_PATIENT = "patient";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>patient</b>
   * <p>
   * Description: <b>The identity of a patient to list orders  for</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.patient</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam PATIENT = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_PATIENT);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>MedicationRequest:patient</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_PATIENT = new ca.uhn.fhir.model.api.Include("MedicationRequest:patient").toLocked();

 /**
   * Search parameter: <b>datewritten</b>
   * <p>
   * Description: <b>Return prescriptions written on this date</b><br>
   * Type: <b>date</b><br>
   * Path: <b>MedicationRequest.dateWritten</b><br>
   * </p>
   */
  @SearchParamDefinition(name="datewritten", path="MedicationRequest.dateWritten", description="Return prescriptions written on this date", type="date" )
  public static final String SP_DATEWRITTEN = "datewritten";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>datewritten</b>
   * <p>
   * Description: <b>Return prescriptions written on this date</b><br>
   * Type: <b>date</b><br>
   * Path: <b>MedicationRequest.dateWritten</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.DateClientParam DATEWRITTEN = new ca.uhn.fhir.rest.gclient.DateClientParam(SP_DATEWRITTEN);

 /**
   * Search parameter: <b>context</b>
   * <p>
   * Description: <b>Return prescriptions with this encounter or episode of care identifier</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.context</b><br>
   * </p>
   */
  @SearchParamDefinition(name="context", path="MedicationRequest.context", description="Return prescriptions with this encounter or episode of care identifier", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Encounter") }, target={Encounter.class, EpisodeOfCare.class } )
  public static final String SP_CONTEXT = "context";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>context</b>
   * <p>
   * Description: <b>Return prescriptions with this encounter or episode of care identifier</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.context</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam CONTEXT = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_CONTEXT);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>MedicationRequest:context</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_CONTEXT = new ca.uhn.fhir.model.api.Include("MedicationRequest:context").toLocked();

 /**
   * Search parameter: <b>medication</b>
   * <p>
   * Description: <b>Return prescriptions of this medication reference</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.medicationReference</b><br>
   * </p>
   */
  @SearchParamDefinition(name="medication", path="MedicationRequest.medication.as(Reference)", description="Return prescriptions of this medication reference", type="reference", target={Medication.class } )
  public static final String SP_MEDICATION = "medication";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>medication</b>
   * <p>
   * Description: <b>Return prescriptions of this medication reference</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>MedicationRequest.medicationReference</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam MEDICATION = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_MEDICATION);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>MedicationRequest:medication</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_MEDICATION = new ca.uhn.fhir.model.api.Include("MedicationRequest:medication").toLocked();

 /**
   * Search parameter: <b>category</b>
   * <p>
   * Description: <b>Returns prescriptions with different categories</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.category</b><br>
   * </p>
   */
  @SearchParamDefinition(name="category", path="MedicationRequest.category", description="Returns prescriptions with different categories", type="token" )
  public static final String SP_CATEGORY = "category";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>category</b>
   * <p>
   * Description: <b>Returns prescriptions with different categories</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.category</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.TokenClientParam CATEGORY = new ca.uhn.fhir.rest.gclient.TokenClientParam(SP_CATEGORY);

 /**
   * Search parameter: <b>status</b>
   * <p>
   * Description: <b>Status of the prescription</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.status</b><br>
   * </p>
   */
  @SearchParamDefinition(name="status", path="MedicationRequest.status", description="Status of the prescription", type="token" )
  public static final String SP_STATUS = "status";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>status</b>
   * <p>
   * Description: <b>Status of the prescription</b><br>
   * Type: <b>token</b><br>
   * Path: <b>MedicationRequest.status</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.TokenClientParam STATUS = new ca.uhn.fhir.rest.gclient.TokenClientParam(SP_STATUS);


}

