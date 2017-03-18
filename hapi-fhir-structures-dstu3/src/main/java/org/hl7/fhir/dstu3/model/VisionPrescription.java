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

import java.math.*;
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
 * An authorization for the supply of glasses and/or contact lenses to a patient.
 */
@ResourceDef(name="VisionPrescription", profile="http://hl7.org/fhir/Profile/VisionPrescription")
public class VisionPrescription extends DomainResource {

    public enum VisionStatus {
        /**
         * The instance is currently in-force.
         */
        ACTIVE, 
        /**
         * The instance is withdrawn, rescinded or reversed.
         */
        CANCELLED, 
        /**
         * A new instance the contents of which is not complete.
         */
        DRAFT, 
        /**
         * The instance was entered in error.
         */
        ENTEREDINERROR, 
        /**
         * added to help the parsers with the generic types
         */
        NULL;
        public static VisionStatus fromCode(String codeString) throws FHIRException {
            if (codeString == null || "".equals(codeString))
                return null;
        if ("active".equals(codeString))
          return ACTIVE;
        if ("cancelled".equals(codeString))
          return CANCELLED;
        if ("draft".equals(codeString))
          return DRAFT;
        if ("entered-in-error".equals(codeString))
          return ENTEREDINERROR;
        if (Configuration.isAcceptInvalidEnums())
          return null;
        else
          throw new FHIRException("Unknown VisionStatus code '"+codeString+"'");
        }
        public String toCode() {
          switch (this) {
            case ACTIVE: return "active";
            case CANCELLED: return "cancelled";
            case DRAFT: return "draft";
            case ENTEREDINERROR: return "entered-in-error";
            default: return "?";
          }
        }
        public String getSystem() {
          switch (this) {
            case ACTIVE: return "http://hl7.org/fhir/fm-status";
            case CANCELLED: return "http://hl7.org/fhir/fm-status";
            case DRAFT: return "http://hl7.org/fhir/fm-status";
            case ENTEREDINERROR: return "http://hl7.org/fhir/fm-status";
            default: return "?";
          }
        }
        public String getDefinition() {
          switch (this) {
            case ACTIVE: return "The instance is currently in-force.";
            case CANCELLED: return "The instance is withdrawn, rescinded or reversed.";
            case DRAFT: return "A new instance the contents of which is not complete.";
            case ENTEREDINERROR: return "The instance was entered in error.";
            default: return "?";
          }
        }
        public String getDisplay() {
          switch (this) {
            case ACTIVE: return "Active";
            case CANCELLED: return "Cancelled";
            case DRAFT: return "Draft";
            case ENTEREDINERROR: return "Entered in Error";
            default: return "?";
          }
        }
    }

  public static class VisionStatusEnumFactory implements EnumFactory<VisionStatus> {
    public VisionStatus fromCode(String codeString) throws IllegalArgumentException {
      if (codeString == null || "".equals(codeString))
            if (codeString == null || "".equals(codeString))
                return null;
        if ("active".equals(codeString))
          return VisionStatus.ACTIVE;
        if ("cancelled".equals(codeString))
          return VisionStatus.CANCELLED;
        if ("draft".equals(codeString))
          return VisionStatus.DRAFT;
        if ("entered-in-error".equals(codeString))
          return VisionStatus.ENTEREDINERROR;
        throw new IllegalArgumentException("Unknown VisionStatus code '"+codeString+"'");
        }
        public Enumeration<VisionStatus> fromType(Base code) throws FHIRException {
          if (code == null || code.isEmpty())
            return null;
          String codeString = ((PrimitiveType) code).asStringValue();
          if (codeString == null || "".equals(codeString))
            return null;
        if ("active".equals(codeString))
          return new Enumeration<VisionStatus>(this, VisionStatus.ACTIVE);
        if ("cancelled".equals(codeString))
          return new Enumeration<VisionStatus>(this, VisionStatus.CANCELLED);
        if ("draft".equals(codeString))
          return new Enumeration<VisionStatus>(this, VisionStatus.DRAFT);
        if ("entered-in-error".equals(codeString))
          return new Enumeration<VisionStatus>(this, VisionStatus.ENTEREDINERROR);
        throw new FHIRException("Unknown VisionStatus code '"+codeString+"'");
        }
    public String toCode(VisionStatus code) {
      if (code == VisionStatus.ACTIVE)
        return "active";
      if (code == VisionStatus.CANCELLED)
        return "cancelled";
      if (code == VisionStatus.DRAFT)
        return "draft";
      if (code == VisionStatus.ENTEREDINERROR)
        return "entered-in-error";
      return "?";
      }
    public String toSystem(VisionStatus code) {
      return code.getSystem();
      }
    }

    @Block()
    public static class VisionPrescriptionDispenseComponent extends BackboneElement implements IBaseBackboneElement {
        /**
         * Identifies the type of vision correction product which is required for the patient.
         */
        @Child(name = "product", type = {CodeableConcept.class}, order=1, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Product to be supplied", formalDefinition="Identifies the type of vision correction product which is required for the patient." )
        @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/vision-product")
        protected CodeableConcept product;

        /**
         * The eye for which the lens applies.
         */
        @Child(name = "eye", type = {CodeableConcept.class}, order=2, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="right | left", formalDefinition="The eye for which the lens applies." )
        @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/vision-eye-codes")
        protected CodeableConcept eye;

        /**
         * Lens power measured in diopters (0.25 units).
         */
        @Child(name = "sphere", type = {DecimalType.class}, order=3, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Lens sphere", formalDefinition="Lens power measured in diopters (0.25 units)." )
        protected DecimalType sphere;

        /**
         * Power adjustment for astigmatism measured in diopters (0.25 units).
         */
        @Child(name = "cylinder", type = {DecimalType.class}, order=4, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Lens cylinder", formalDefinition="Power adjustment for astigmatism measured in diopters (0.25 units)." )
        protected DecimalType cylinder;

        /**
         * Adjustment for astigmatism measured in integer degrees.
         */
        @Child(name = "axis", type = {IntegerType.class}, order=5, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Lens axis", formalDefinition="Adjustment for astigmatism measured in integer degrees." )
        protected IntegerType axis;

        /**
         * Amount of prism to compensate for eye alignment in fractional units.
         */
        @Child(name = "prism", type = {DecimalType.class}, order=6, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Lens prism", formalDefinition="Amount of prism to compensate for eye alignment in fractional units." )
        protected DecimalType prism;

        /**
         * The relative base, or reference lens edge, for the prism.
         */
        @Child(name = "base", type = {CodeableConcept.class}, order=7, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="up | down | in | out", formalDefinition="The relative base, or reference lens edge, for the prism." )
        @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/vision-base-codes")
        protected CodeableConcept base;

        /**
         * Power adjustment for multifocal lenses measured in diopters (0.25 units).
         */
        @Child(name = "add", type = {DecimalType.class}, order=8, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Lens add", formalDefinition="Power adjustment for multifocal lenses measured in diopters (0.25 units)." )
        protected DecimalType add;

        /**
         * Contact lens power measured in diopters (0.25 units).
         */
        @Child(name = "power", type = {DecimalType.class}, order=9, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Contact lens power", formalDefinition="Contact lens power measured in diopters (0.25 units)." )
        protected DecimalType power;

        /**
         * Back curvature measured in millimeters.
         */
        @Child(name = "backCurve", type = {DecimalType.class}, order=10, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Contact lens back curvature", formalDefinition="Back curvature measured in millimeters." )
        protected DecimalType backCurve;

        /**
         * Contact lens diameter measured in millimeters.
         */
        @Child(name = "diameter", type = {DecimalType.class}, order=11, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Contact lens diameter", formalDefinition="Contact lens diameter measured in millimeters." )
        protected DecimalType diameter;

        /**
         * The recommended maximum wear period for the lens.
         */
        @Child(name = "duration", type = {SimpleQuantity.class}, order=12, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Lens wear duration", formalDefinition="The recommended maximum wear period for the lens." )
        protected SimpleQuantity duration;

        /**
         * Special color or pattern.
         */
        @Child(name = "color", type = {StringType.class}, order=13, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Color required", formalDefinition="Special color or pattern." )
        protected StringType color;

        /**
         * Brand recommendations or restrictions.
         */
        @Child(name = "brand", type = {StringType.class}, order=14, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Brand required", formalDefinition="Brand recommendations or restrictions." )
        protected StringType brand;

        /**
         * Notes for special requirements such as coatings and lens materials.
         */
        @Child(name = "note", type = {StringType.class}, order=15, min=0, max=1, modifier=false, summary=false)
        @Description(shortDefinition="Notes for coatings", formalDefinition="Notes for special requirements such as coatings and lens materials." )
        protected StringType note;

        private static final long serialVersionUID = 1057778243L;

    /**
     * Constructor
     */
      public VisionPrescriptionDispenseComponent() {
        super();
      }

        /**
         * @return {@link #product} (Identifies the type of vision correction product which is required for the patient.)
         */
        public CodeableConcept getProduct() { 
          if (this.product == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.product");
            else if (Configuration.doAutoCreate())
              this.product = new CodeableConcept(); // cc
          return this.product;
        }

        public boolean hasProduct() { 
          return this.product != null && !this.product.isEmpty();
        }

        /**
         * @param value {@link #product} (Identifies the type of vision correction product which is required for the patient.)
         */
        public VisionPrescriptionDispenseComponent setProduct(CodeableConcept value) { 
          this.product = value;
          return this;
        }

        /**
         * @return {@link #eye} (The eye for which the lens applies.)
         */
        public CodeableConcept getEye() { 
          if (this.eye == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.eye");
            else if (Configuration.doAutoCreate())
              this.eye = new CodeableConcept(); // cc
          return this.eye;
        }

        public boolean hasEye() { 
          return this.eye != null && !this.eye.isEmpty();
        }

        /**
         * @param value {@link #eye} (The eye for which the lens applies.)
         */
        public VisionPrescriptionDispenseComponent setEye(CodeableConcept value) { 
          this.eye = value;
          return this;
        }

        /**
         * @return {@link #sphere} (Lens power measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getSphere" gives direct access to the value
         */
        public DecimalType getSphereElement() { 
          if (this.sphere == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.sphere");
            else if (Configuration.doAutoCreate())
              this.sphere = new DecimalType(); // bb
          return this.sphere;
        }

        public boolean hasSphereElement() { 
          return this.sphere != null && !this.sphere.isEmpty();
        }

        public boolean hasSphere() { 
          return this.sphere != null && !this.sphere.isEmpty();
        }

        /**
         * @param value {@link #sphere} (Lens power measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getSphere" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setSphereElement(DecimalType value) { 
          this.sphere = value;
          return this;
        }

        /**
         * @return Lens power measured in diopters (0.25 units).
         */
        public BigDecimal getSphere() { 
          return this.sphere == null ? null : this.sphere.getValue();
        }

        /**
         * @param value Lens power measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setSphere(BigDecimal value) { 
          if (value == null)
            this.sphere = null;
          else {
            if (this.sphere == null)
              this.sphere = new DecimalType();
            this.sphere.setValue(value);
          }
          return this;
        }

        /**
         * @param value Lens power measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setSphere(long value) { 
              this.sphere = new DecimalType();
            this.sphere.setValue(value);
          return this;
        }

        /**
         * @param value Lens power measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setSphere(double value) { 
              this.sphere = new DecimalType();
            this.sphere.setValue(value);
          return this;
        }

        /**
         * @return {@link #cylinder} (Power adjustment for astigmatism measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getCylinder" gives direct access to the value
         */
        public DecimalType getCylinderElement() { 
          if (this.cylinder == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.cylinder");
            else if (Configuration.doAutoCreate())
              this.cylinder = new DecimalType(); // bb
          return this.cylinder;
        }

        public boolean hasCylinderElement() { 
          return this.cylinder != null && !this.cylinder.isEmpty();
        }

        public boolean hasCylinder() { 
          return this.cylinder != null && !this.cylinder.isEmpty();
        }

        /**
         * @param value {@link #cylinder} (Power adjustment for astigmatism measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getCylinder" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setCylinderElement(DecimalType value) { 
          this.cylinder = value;
          return this;
        }

        /**
         * @return Power adjustment for astigmatism measured in diopters (0.25 units).
         */
        public BigDecimal getCylinder() { 
          return this.cylinder == null ? null : this.cylinder.getValue();
        }

        /**
         * @param value Power adjustment for astigmatism measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setCylinder(BigDecimal value) { 
          if (value == null)
            this.cylinder = null;
          else {
            if (this.cylinder == null)
              this.cylinder = new DecimalType();
            this.cylinder.setValue(value);
          }
          return this;
        }

        /**
         * @param value Power adjustment for astigmatism measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setCylinder(long value) { 
              this.cylinder = new DecimalType();
            this.cylinder.setValue(value);
          return this;
        }

        /**
         * @param value Power adjustment for astigmatism measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setCylinder(double value) { 
              this.cylinder = new DecimalType();
            this.cylinder.setValue(value);
          return this;
        }

        /**
         * @return {@link #axis} (Adjustment for astigmatism measured in integer degrees.). This is the underlying object with id, value and extensions. The accessor "getAxis" gives direct access to the value
         */
        public IntegerType getAxisElement() { 
          if (this.axis == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.axis");
            else if (Configuration.doAutoCreate())
              this.axis = new IntegerType(); // bb
          return this.axis;
        }

        public boolean hasAxisElement() { 
          return this.axis != null && !this.axis.isEmpty();
        }

        public boolean hasAxis() { 
          return this.axis != null && !this.axis.isEmpty();
        }

        /**
         * @param value {@link #axis} (Adjustment for astigmatism measured in integer degrees.). This is the underlying object with id, value and extensions. The accessor "getAxis" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setAxisElement(IntegerType value) { 
          this.axis = value;
          return this;
        }

        /**
         * @return Adjustment for astigmatism measured in integer degrees.
         */
        public int getAxis() { 
          return this.axis == null || this.axis.isEmpty() ? 0 : this.axis.getValue();
        }

        /**
         * @param value Adjustment for astigmatism measured in integer degrees.
         */
        public VisionPrescriptionDispenseComponent setAxis(int value) { 
            if (this.axis == null)
              this.axis = new IntegerType();
            this.axis.setValue(value);
          return this;
        }

        /**
         * @return {@link #prism} (Amount of prism to compensate for eye alignment in fractional units.). This is the underlying object with id, value and extensions. The accessor "getPrism" gives direct access to the value
         */
        public DecimalType getPrismElement() { 
          if (this.prism == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.prism");
            else if (Configuration.doAutoCreate())
              this.prism = new DecimalType(); // bb
          return this.prism;
        }

        public boolean hasPrismElement() { 
          return this.prism != null && !this.prism.isEmpty();
        }

        public boolean hasPrism() { 
          return this.prism != null && !this.prism.isEmpty();
        }

        /**
         * @param value {@link #prism} (Amount of prism to compensate for eye alignment in fractional units.). This is the underlying object with id, value and extensions. The accessor "getPrism" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setPrismElement(DecimalType value) { 
          this.prism = value;
          return this;
        }

        /**
         * @return Amount of prism to compensate for eye alignment in fractional units.
         */
        public BigDecimal getPrism() { 
          return this.prism == null ? null : this.prism.getValue();
        }

        /**
         * @param value Amount of prism to compensate for eye alignment in fractional units.
         */
        public VisionPrescriptionDispenseComponent setPrism(BigDecimal value) { 
          if (value == null)
            this.prism = null;
          else {
            if (this.prism == null)
              this.prism = new DecimalType();
            this.prism.setValue(value);
          }
          return this;
        }

        /**
         * @param value Amount of prism to compensate for eye alignment in fractional units.
         */
        public VisionPrescriptionDispenseComponent setPrism(long value) { 
              this.prism = new DecimalType();
            this.prism.setValue(value);
          return this;
        }

        /**
         * @param value Amount of prism to compensate for eye alignment in fractional units.
         */
        public VisionPrescriptionDispenseComponent setPrism(double value) { 
              this.prism = new DecimalType();
            this.prism.setValue(value);
          return this;
        }

        /**
         * @return {@link #base} (The relative base, or reference lens edge, for the prism.)
         */
        public CodeableConcept getBase() { 
          if (this.base == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.base");
            else if (Configuration.doAutoCreate())
              this.base = new CodeableConcept(); // cc
          return this.base;
        }

        public boolean hasBase() { 
          return this.base != null && !this.base.isEmpty();
        }

        /**
         * @param value {@link #base} (The relative base, or reference lens edge, for the prism.)
         */
        public VisionPrescriptionDispenseComponent setBase(CodeableConcept value) { 
          this.base = value;
          return this;
        }

        /**
         * @return {@link #add} (Power adjustment for multifocal lenses measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getAdd" gives direct access to the value
         */
        public DecimalType getAddElement() { 
          if (this.add == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.add");
            else if (Configuration.doAutoCreate())
              this.add = new DecimalType(); // bb
          return this.add;
        }

        public boolean hasAddElement() { 
          return this.add != null && !this.add.isEmpty();
        }

        public boolean hasAdd() { 
          return this.add != null && !this.add.isEmpty();
        }

        /**
         * @param value {@link #add} (Power adjustment for multifocal lenses measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getAdd" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setAddElement(DecimalType value) { 
          this.add = value;
          return this;
        }

        /**
         * @return Power adjustment for multifocal lenses measured in diopters (0.25 units).
         */
        public BigDecimal getAdd() { 
          return this.add == null ? null : this.add.getValue();
        }

        /**
         * @param value Power adjustment for multifocal lenses measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setAdd(BigDecimal value) { 
          if (value == null)
            this.add = null;
          else {
            if (this.add == null)
              this.add = new DecimalType();
            this.add.setValue(value);
          }
          return this;
        }

        /**
         * @param value Power adjustment for multifocal lenses measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setAdd(long value) { 
              this.add = new DecimalType();
            this.add.setValue(value);
          return this;
        }

        /**
         * @param value Power adjustment for multifocal lenses measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setAdd(double value) { 
              this.add = new DecimalType();
            this.add.setValue(value);
          return this;
        }

        /**
         * @return {@link #power} (Contact lens power measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getPower" gives direct access to the value
         */
        public DecimalType getPowerElement() { 
          if (this.power == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.power");
            else if (Configuration.doAutoCreate())
              this.power = new DecimalType(); // bb
          return this.power;
        }

        public boolean hasPowerElement() { 
          return this.power != null && !this.power.isEmpty();
        }

        public boolean hasPower() { 
          return this.power != null && !this.power.isEmpty();
        }

        /**
         * @param value {@link #power} (Contact lens power measured in diopters (0.25 units).). This is the underlying object with id, value and extensions. The accessor "getPower" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setPowerElement(DecimalType value) { 
          this.power = value;
          return this;
        }

        /**
         * @return Contact lens power measured in diopters (0.25 units).
         */
        public BigDecimal getPower() { 
          return this.power == null ? null : this.power.getValue();
        }

        /**
         * @param value Contact lens power measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setPower(BigDecimal value) { 
          if (value == null)
            this.power = null;
          else {
            if (this.power == null)
              this.power = new DecimalType();
            this.power.setValue(value);
          }
          return this;
        }

        /**
         * @param value Contact lens power measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setPower(long value) { 
              this.power = new DecimalType();
            this.power.setValue(value);
          return this;
        }

        /**
         * @param value Contact lens power measured in diopters (0.25 units).
         */
        public VisionPrescriptionDispenseComponent setPower(double value) { 
              this.power = new DecimalType();
            this.power.setValue(value);
          return this;
        }

        /**
         * @return {@link #backCurve} (Back curvature measured in millimeters.). This is the underlying object with id, value and extensions. The accessor "getBackCurve" gives direct access to the value
         */
        public DecimalType getBackCurveElement() { 
          if (this.backCurve == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.backCurve");
            else if (Configuration.doAutoCreate())
              this.backCurve = new DecimalType(); // bb
          return this.backCurve;
        }

        public boolean hasBackCurveElement() { 
          return this.backCurve != null && !this.backCurve.isEmpty();
        }

        public boolean hasBackCurve() { 
          return this.backCurve != null && !this.backCurve.isEmpty();
        }

        /**
         * @param value {@link #backCurve} (Back curvature measured in millimeters.). This is the underlying object with id, value and extensions. The accessor "getBackCurve" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setBackCurveElement(DecimalType value) { 
          this.backCurve = value;
          return this;
        }

        /**
         * @return Back curvature measured in millimeters.
         */
        public BigDecimal getBackCurve() { 
          return this.backCurve == null ? null : this.backCurve.getValue();
        }

        /**
         * @param value Back curvature measured in millimeters.
         */
        public VisionPrescriptionDispenseComponent setBackCurve(BigDecimal value) { 
          if (value == null)
            this.backCurve = null;
          else {
            if (this.backCurve == null)
              this.backCurve = new DecimalType();
            this.backCurve.setValue(value);
          }
          return this;
        }

        /**
         * @param value Back curvature measured in millimeters.
         */
        public VisionPrescriptionDispenseComponent setBackCurve(long value) { 
              this.backCurve = new DecimalType();
            this.backCurve.setValue(value);
          return this;
        }

        /**
         * @param value Back curvature measured in millimeters.
         */
        public VisionPrescriptionDispenseComponent setBackCurve(double value) { 
              this.backCurve = new DecimalType();
            this.backCurve.setValue(value);
          return this;
        }

        /**
         * @return {@link #diameter} (Contact lens diameter measured in millimeters.). This is the underlying object with id, value and extensions. The accessor "getDiameter" gives direct access to the value
         */
        public DecimalType getDiameterElement() { 
          if (this.diameter == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.diameter");
            else if (Configuration.doAutoCreate())
              this.diameter = new DecimalType(); // bb
          return this.diameter;
        }

        public boolean hasDiameterElement() { 
          return this.diameter != null && !this.diameter.isEmpty();
        }

        public boolean hasDiameter() { 
          return this.diameter != null && !this.diameter.isEmpty();
        }

        /**
         * @param value {@link #diameter} (Contact lens diameter measured in millimeters.). This is the underlying object with id, value and extensions. The accessor "getDiameter" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setDiameterElement(DecimalType value) { 
          this.diameter = value;
          return this;
        }

        /**
         * @return Contact lens diameter measured in millimeters.
         */
        public BigDecimal getDiameter() { 
          return this.diameter == null ? null : this.diameter.getValue();
        }

        /**
         * @param value Contact lens diameter measured in millimeters.
         */
        public VisionPrescriptionDispenseComponent setDiameter(BigDecimal value) { 
          if (value == null)
            this.diameter = null;
          else {
            if (this.diameter == null)
              this.diameter = new DecimalType();
            this.diameter.setValue(value);
          }
          return this;
        }

        /**
         * @param value Contact lens diameter measured in millimeters.
         */
        public VisionPrescriptionDispenseComponent setDiameter(long value) { 
              this.diameter = new DecimalType();
            this.diameter.setValue(value);
          return this;
        }

        /**
         * @param value Contact lens diameter measured in millimeters.
         */
        public VisionPrescriptionDispenseComponent setDiameter(double value) { 
              this.diameter = new DecimalType();
            this.diameter.setValue(value);
          return this;
        }

        /**
         * @return {@link #duration} (The recommended maximum wear period for the lens.)
         */
        public SimpleQuantity getDuration() { 
          if (this.duration == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.duration");
            else if (Configuration.doAutoCreate())
              this.duration = new SimpleQuantity(); // cc
          return this.duration;
        }

        public boolean hasDuration() { 
          return this.duration != null && !this.duration.isEmpty();
        }

        /**
         * @param value {@link #duration} (The recommended maximum wear period for the lens.)
         */
        public VisionPrescriptionDispenseComponent setDuration(SimpleQuantity value) { 
          this.duration = value;
          return this;
        }

        /**
         * @return {@link #color} (Special color or pattern.). This is the underlying object with id, value and extensions. The accessor "getColor" gives direct access to the value
         */
        public StringType getColorElement() { 
          if (this.color == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.color");
            else if (Configuration.doAutoCreate())
              this.color = new StringType(); // bb
          return this.color;
        }

        public boolean hasColorElement() { 
          return this.color != null && !this.color.isEmpty();
        }

        public boolean hasColor() { 
          return this.color != null && !this.color.isEmpty();
        }

        /**
         * @param value {@link #color} (Special color or pattern.). This is the underlying object with id, value and extensions. The accessor "getColor" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setColorElement(StringType value) { 
          this.color = value;
          return this;
        }

        /**
         * @return Special color or pattern.
         */
        public String getColor() { 
          return this.color == null ? null : this.color.getValue();
        }

        /**
         * @param value Special color or pattern.
         */
        public VisionPrescriptionDispenseComponent setColor(String value) { 
          if (Utilities.noString(value))
            this.color = null;
          else {
            if (this.color == null)
              this.color = new StringType();
            this.color.setValue(value);
          }
          return this;
        }

        /**
         * @return {@link #brand} (Brand recommendations or restrictions.). This is the underlying object with id, value and extensions. The accessor "getBrand" gives direct access to the value
         */
        public StringType getBrandElement() { 
          if (this.brand == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.brand");
            else if (Configuration.doAutoCreate())
              this.brand = new StringType(); // bb
          return this.brand;
        }

        public boolean hasBrandElement() { 
          return this.brand != null && !this.brand.isEmpty();
        }

        public boolean hasBrand() { 
          return this.brand != null && !this.brand.isEmpty();
        }

        /**
         * @param value {@link #brand} (Brand recommendations or restrictions.). This is the underlying object with id, value and extensions. The accessor "getBrand" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setBrandElement(StringType value) { 
          this.brand = value;
          return this;
        }

        /**
         * @return Brand recommendations or restrictions.
         */
        public String getBrand() { 
          return this.brand == null ? null : this.brand.getValue();
        }

        /**
         * @param value Brand recommendations or restrictions.
         */
        public VisionPrescriptionDispenseComponent setBrand(String value) { 
          if (Utilities.noString(value))
            this.brand = null;
          else {
            if (this.brand == null)
              this.brand = new StringType();
            this.brand.setValue(value);
          }
          return this;
        }

        /**
         * @return {@link #note} (Notes for special requirements such as coatings and lens materials.). This is the underlying object with id, value and extensions. The accessor "getNote" gives direct access to the value
         */
        public StringType getNoteElement() { 
          if (this.note == null)
            if (Configuration.errorOnAutoCreate())
              throw new Error("Attempt to auto-create VisionPrescriptionDispenseComponent.note");
            else if (Configuration.doAutoCreate())
              this.note = new StringType(); // bb
          return this.note;
        }

        public boolean hasNoteElement() { 
          return this.note != null && !this.note.isEmpty();
        }

        public boolean hasNote() { 
          return this.note != null && !this.note.isEmpty();
        }

        /**
         * @param value {@link #note} (Notes for special requirements such as coatings and lens materials.). This is the underlying object with id, value and extensions. The accessor "getNote" gives direct access to the value
         */
        public VisionPrescriptionDispenseComponent setNoteElement(StringType value) { 
          this.note = value;
          return this;
        }

        /**
         * @return Notes for special requirements such as coatings and lens materials.
         */
        public String getNote() { 
          return this.note == null ? null : this.note.getValue();
        }

        /**
         * @param value Notes for special requirements such as coatings and lens materials.
         */
        public VisionPrescriptionDispenseComponent setNote(String value) { 
          if (Utilities.noString(value))
            this.note = null;
          else {
            if (this.note == null)
              this.note = new StringType();
            this.note.setValue(value);
          }
          return this;
        }

        protected void listChildren(List<Property> childrenList) {
          super.listChildren(childrenList);
          childrenList.add(new Property("product", "CodeableConcept", "Identifies the type of vision correction product which is required for the patient.", 0, java.lang.Integer.MAX_VALUE, product));
          childrenList.add(new Property("eye", "CodeableConcept", "The eye for which the lens applies.", 0, java.lang.Integer.MAX_VALUE, eye));
          childrenList.add(new Property("sphere", "decimal", "Lens power measured in diopters (0.25 units).", 0, java.lang.Integer.MAX_VALUE, sphere));
          childrenList.add(new Property("cylinder", "decimal", "Power adjustment for astigmatism measured in diopters (0.25 units).", 0, java.lang.Integer.MAX_VALUE, cylinder));
          childrenList.add(new Property("axis", "integer", "Adjustment for astigmatism measured in integer degrees.", 0, java.lang.Integer.MAX_VALUE, axis));
          childrenList.add(new Property("prism", "decimal", "Amount of prism to compensate for eye alignment in fractional units.", 0, java.lang.Integer.MAX_VALUE, prism));
          childrenList.add(new Property("base", "CodeableConcept", "The relative base, or reference lens edge, for the prism.", 0, java.lang.Integer.MAX_VALUE, base));
          childrenList.add(new Property("add", "decimal", "Power adjustment for multifocal lenses measured in diopters (0.25 units).", 0, java.lang.Integer.MAX_VALUE, add));
          childrenList.add(new Property("power", "decimal", "Contact lens power measured in diopters (0.25 units).", 0, java.lang.Integer.MAX_VALUE, power));
          childrenList.add(new Property("backCurve", "decimal", "Back curvature measured in millimeters.", 0, java.lang.Integer.MAX_VALUE, backCurve));
          childrenList.add(new Property("diameter", "decimal", "Contact lens diameter measured in millimeters.", 0, java.lang.Integer.MAX_VALUE, diameter));
          childrenList.add(new Property("duration", "SimpleQuantity", "The recommended maximum wear period for the lens.", 0, java.lang.Integer.MAX_VALUE, duration));
          childrenList.add(new Property("color", "string", "Special color or pattern.", 0, java.lang.Integer.MAX_VALUE, color));
          childrenList.add(new Property("brand", "string", "Brand recommendations or restrictions.", 0, java.lang.Integer.MAX_VALUE, brand));
          childrenList.add(new Property("note", "string", "Notes for special requirements such as coatings and lens materials.", 0, java.lang.Integer.MAX_VALUE, note));
        }

      @Override
      public Base[] getProperty(int hash, String name, boolean checkValid) throws FHIRException {
        switch (hash) {
        case -309474065: /*product*/ return this.product == null ? new Base[0] : new Base[] {this.product}; // CodeableConcept
        case 100913: /*eye*/ return this.eye == null ? new Base[0] : new Base[] {this.eye}; // CodeableConcept
        case -895981619: /*sphere*/ return this.sphere == null ? new Base[0] : new Base[] {this.sphere}; // DecimalType
        case -349378602: /*cylinder*/ return this.cylinder == null ? new Base[0] : new Base[] {this.cylinder}; // DecimalType
        case 3008417: /*axis*/ return this.axis == null ? new Base[0] : new Base[] {this.axis}; // IntegerType
        case 106935105: /*prism*/ return this.prism == null ? new Base[0] : new Base[] {this.prism}; // DecimalType
        case 3016401: /*base*/ return this.base == null ? new Base[0] : new Base[] {this.base}; // CodeableConcept
        case 96417: /*add*/ return this.add == null ? new Base[0] : new Base[] {this.add}; // DecimalType
        case 106858757: /*power*/ return this.power == null ? new Base[0] : new Base[] {this.power}; // DecimalType
        case 1309344840: /*backCurve*/ return this.backCurve == null ? new Base[0] : new Base[] {this.backCurve}; // DecimalType
        case -233204595: /*diameter*/ return this.diameter == null ? new Base[0] : new Base[] {this.diameter}; // DecimalType
        case -1992012396: /*duration*/ return this.duration == null ? new Base[0] : new Base[] {this.duration}; // SimpleQuantity
        case 94842723: /*color*/ return this.color == null ? new Base[0] : new Base[] {this.color}; // StringType
        case 93997959: /*brand*/ return this.brand == null ? new Base[0] : new Base[] {this.brand}; // StringType
        case 3387378: /*note*/ return this.note == null ? new Base[0] : new Base[] {this.note}; // StringType
        default: return super.getProperty(hash, name, checkValid);
        }

      }

      @Override
      public void setProperty(int hash, String name, Base value) throws FHIRException {
        switch (hash) {
        case -309474065: // product
          this.product = castToCodeableConcept(value); // CodeableConcept
          break;
        case 100913: // eye
          this.eye = castToCodeableConcept(value); // CodeableConcept
          break;
        case -895981619: // sphere
          this.sphere = castToDecimal(value); // DecimalType
          break;
        case -349378602: // cylinder
          this.cylinder = castToDecimal(value); // DecimalType
          break;
        case 3008417: // axis
          this.axis = castToInteger(value); // IntegerType
          break;
        case 106935105: // prism
          this.prism = castToDecimal(value); // DecimalType
          break;
        case 3016401: // base
          this.base = castToCodeableConcept(value); // CodeableConcept
          break;
        case 96417: // add
          this.add = castToDecimal(value); // DecimalType
          break;
        case 106858757: // power
          this.power = castToDecimal(value); // DecimalType
          break;
        case 1309344840: // backCurve
          this.backCurve = castToDecimal(value); // DecimalType
          break;
        case -233204595: // diameter
          this.diameter = castToDecimal(value); // DecimalType
          break;
        case -1992012396: // duration
          this.duration = castToSimpleQuantity(value); // SimpleQuantity
          break;
        case 94842723: // color
          this.color = castToString(value); // StringType
          break;
        case 93997959: // brand
          this.brand = castToString(value); // StringType
          break;
        case 3387378: // note
          this.note = castToString(value); // StringType
          break;
        default: super.setProperty(hash, name, value);
        }

      }

      @Override
      public void setProperty(String name, Base value) throws FHIRException {
        if (name.equals("product"))
          this.product = castToCodeableConcept(value); // CodeableConcept
        else if (name.equals("eye"))
          this.eye = castToCodeableConcept(value); // CodeableConcept
        else if (name.equals("sphere"))
          this.sphere = castToDecimal(value); // DecimalType
        else if (name.equals("cylinder"))
          this.cylinder = castToDecimal(value); // DecimalType
        else if (name.equals("axis"))
          this.axis = castToInteger(value); // IntegerType
        else if (name.equals("prism"))
          this.prism = castToDecimal(value); // DecimalType
        else if (name.equals("base"))
          this.base = castToCodeableConcept(value); // CodeableConcept
        else if (name.equals("add"))
          this.add = castToDecimal(value); // DecimalType
        else if (name.equals("power"))
          this.power = castToDecimal(value); // DecimalType
        else if (name.equals("backCurve"))
          this.backCurve = castToDecimal(value); // DecimalType
        else if (name.equals("diameter"))
          this.diameter = castToDecimal(value); // DecimalType
        else if (name.equals("duration"))
          this.duration = castToSimpleQuantity(value); // SimpleQuantity
        else if (name.equals("color"))
          this.color = castToString(value); // StringType
        else if (name.equals("brand"))
          this.brand = castToString(value); // StringType
        else if (name.equals("note"))
          this.note = castToString(value); // StringType
        else
          super.setProperty(name, value);
      }

      @Override
      public Base makeProperty(int hash, String name) throws FHIRException {
        switch (hash) {
        case -309474065:  return getProduct(); // CodeableConcept
        case 100913:  return getEye(); // CodeableConcept
        case -895981619: throw new FHIRException("Cannot make property sphere as it is not a complex type"); // DecimalType
        case -349378602: throw new FHIRException("Cannot make property cylinder as it is not a complex type"); // DecimalType
        case 3008417: throw new FHIRException("Cannot make property axis as it is not a complex type"); // IntegerType
        case 106935105: throw new FHIRException("Cannot make property prism as it is not a complex type"); // DecimalType
        case 3016401:  return getBase(); // CodeableConcept
        case 96417: throw new FHIRException("Cannot make property add as it is not a complex type"); // DecimalType
        case 106858757: throw new FHIRException("Cannot make property power as it is not a complex type"); // DecimalType
        case 1309344840: throw new FHIRException("Cannot make property backCurve as it is not a complex type"); // DecimalType
        case -233204595: throw new FHIRException("Cannot make property diameter as it is not a complex type"); // DecimalType
        case -1992012396:  return getDuration(); // SimpleQuantity
        case 94842723: throw new FHIRException("Cannot make property color as it is not a complex type"); // StringType
        case 93997959: throw new FHIRException("Cannot make property brand as it is not a complex type"); // StringType
        case 3387378: throw new FHIRException("Cannot make property note as it is not a complex type"); // StringType
        default: return super.makeProperty(hash, name);
        }

      }

      @Override
      public Base addChild(String name) throws FHIRException {
        if (name.equals("product")) {
          this.product = new CodeableConcept();
          return this.product;
        }
        else if (name.equals("eye")) {
          this.eye = new CodeableConcept();
          return this.eye;
        }
        else if (name.equals("sphere")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.sphere");
        }
        else if (name.equals("cylinder")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.cylinder");
        }
        else if (name.equals("axis")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.axis");
        }
        else if (name.equals("prism")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.prism");
        }
        else if (name.equals("base")) {
          this.base = new CodeableConcept();
          return this.base;
        }
        else if (name.equals("add")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.add");
        }
        else if (name.equals("power")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.power");
        }
        else if (name.equals("backCurve")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.backCurve");
        }
        else if (name.equals("diameter")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.diameter");
        }
        else if (name.equals("duration")) {
          this.duration = new SimpleQuantity();
          return this.duration;
        }
        else if (name.equals("color")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.color");
        }
        else if (name.equals("brand")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.brand");
        }
        else if (name.equals("note")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.note");
        }
        else
          return super.addChild(name);
      }

      public VisionPrescriptionDispenseComponent copy() {
        VisionPrescriptionDispenseComponent dst = new VisionPrescriptionDispenseComponent();
        copyValues(dst);
        dst.product = product == null ? null : product.copy();
        dst.eye = eye == null ? null : eye.copy();
        dst.sphere = sphere == null ? null : sphere.copy();
        dst.cylinder = cylinder == null ? null : cylinder.copy();
        dst.axis = axis == null ? null : axis.copy();
        dst.prism = prism == null ? null : prism.copy();
        dst.base = base == null ? null : base.copy();
        dst.add = add == null ? null : add.copy();
        dst.power = power == null ? null : power.copy();
        dst.backCurve = backCurve == null ? null : backCurve.copy();
        dst.diameter = diameter == null ? null : diameter.copy();
        dst.duration = duration == null ? null : duration.copy();
        dst.color = color == null ? null : color.copy();
        dst.brand = brand == null ? null : brand.copy();
        dst.note = note == null ? null : note.copy();
        return dst;
      }

      @Override
      public boolean equalsDeep(Base other) {
        if (!super.equalsDeep(other))
          return false;
        if (!(other instanceof VisionPrescriptionDispenseComponent))
          return false;
        VisionPrescriptionDispenseComponent o = (VisionPrescriptionDispenseComponent) other;
        return compareDeep(product, o.product, true) && compareDeep(eye, o.eye, true) && compareDeep(sphere, o.sphere, true)
           && compareDeep(cylinder, o.cylinder, true) && compareDeep(axis, o.axis, true) && compareDeep(prism, o.prism, true)
           && compareDeep(base, o.base, true) && compareDeep(add, o.add, true) && compareDeep(power, o.power, true)
           && compareDeep(backCurve, o.backCurve, true) && compareDeep(diameter, o.diameter, true) && compareDeep(duration, o.duration, true)
           && compareDeep(color, o.color, true) && compareDeep(brand, o.brand, true) && compareDeep(note, o.note, true)
          ;
      }

      @Override
      public boolean equalsShallow(Base other) {
        if (!super.equalsShallow(other))
          return false;
        if (!(other instanceof VisionPrescriptionDispenseComponent))
          return false;
        VisionPrescriptionDispenseComponent o = (VisionPrescriptionDispenseComponent) other;
        return compareValues(sphere, o.sphere, true) && compareValues(cylinder, o.cylinder, true) && compareValues(axis, o.axis, true)
           && compareValues(prism, o.prism, true) && compareValues(add, o.add, true) && compareValues(power, o.power, true)
           && compareValues(backCurve, o.backCurve, true) && compareValues(diameter, o.diameter, true) && compareValues(color, o.color, true)
           && compareValues(brand, o.brand, true) && compareValues(note, o.note, true);
      }

      public boolean isEmpty() {
        return super.isEmpty() && ca.uhn.fhir.util.ElementUtil.isEmpty(product, eye, sphere, cylinder
          , axis, prism, base, add, power, backCurve, diameter, duration, color, brand
          , note);
      }

  public String fhirType() {
    return "VisionPrescription.dispense";

  }

  }

    /**
     * Business identifier which may be used by other parties to reference or identify the prescription.
     */
    @Child(name = "identifier", type = {Identifier.class}, order=0, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="Business identifier", formalDefinition="Business identifier which may be used by other parties to reference or identify the prescription." )
    protected List<Identifier> identifier;

    /**
     * The status of the resource instance.
     */
    @Child(name = "status", type = {CodeType.class}, order=1, min=0, max=1, modifier=true, summary=true)
    @Description(shortDefinition="active | cancelled | draft | entered-in-error", formalDefinition="The status of the resource instance." )
    @ca.uhn.fhir.model.api.annotation.Binding(valueSet="http://hl7.org/fhir/ValueSet/fm-status")
    protected Enumeration<VisionStatus> status;

    /**
     * A link to a resource representing the person to whom the vision products will be supplied.
     */
    @Child(name = "patient", type = {Patient.class}, order=2, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Who prescription is for", formalDefinition="A link to a resource representing the person to whom the vision products will be supplied." )
    protected Reference patient;

    /**
     * The actual object that is the target of the reference (A link to a resource representing the person to whom the vision products will be supplied.)
     */
    protected Patient patientTarget;

    /**
     * A link to a resource that identifies the particular occurrence of contact between patient and health care provider.
     */
    @Child(name = "encounter", type = {Encounter.class}, order=3, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Created during encounter / admission / stay", formalDefinition="A link to a resource that identifies the particular occurrence of contact between patient and health care provider." )
    protected Reference encounter;

    /**
     * The actual object that is the target of the reference (A link to a resource that identifies the particular occurrence of contact between patient and health care provider.)
     */
    protected Encounter encounterTarget;

    /**
     * The date (and perhaps time) when the prescription was written.
     */
    @Child(name = "dateWritten", type = {DateTimeType.class}, order=4, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="When prescription was authorized", formalDefinition="The date (and perhaps time) when the prescription was written." )
    protected DateTimeType dateWritten;

    /**
     * The healthcare professional responsible for authorizing the prescription.
     */
    @Child(name = "prescriber", type = {Practitioner.class}, order=5, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Who authorizes the vision product", formalDefinition="The healthcare professional responsible for authorizing the prescription." )
    protected Reference prescriber;

    /**
     * The actual object that is the target of the reference (The healthcare professional responsible for authorizing the prescription.)
     */
    protected Practitioner prescriberTarget;

    /**
     * Can be the reason or the indication for writing the prescription.
     */
    @Child(name = "reason", type = {CodeableConcept.class, Condition.class}, order=6, min=0, max=1, modifier=false, summary=false)
    @Description(shortDefinition="Reason or indication for writing the prescription", formalDefinition="Can be the reason or the indication for writing the prescription." )
    protected Type reason;

    /**
     * Deals with details of the dispense part of the supply specification.
     */
    @Child(name = "dispense", type = {}, order=7, min=0, max=Child.MAX_UNLIMITED, modifier=false, summary=false)
    @Description(shortDefinition="Vision supply authorization", formalDefinition="Deals with details of the dispense part of the supply specification." )
    protected List<VisionPrescriptionDispenseComponent> dispense;

    private static final long serialVersionUID = 603347490L;

  /**
   * Constructor
   */
    public VisionPrescription() {
      super();
    }

    /**
     * @return {@link #identifier} (Business identifier which may be used by other parties to reference or identify the prescription.)
     */
    public List<Identifier> getIdentifier() { 
      if (this.identifier == null)
        this.identifier = new ArrayList<Identifier>();
      return this.identifier;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public VisionPrescription setIdentifier(List<Identifier> theIdentifier) { 
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

    public VisionPrescription addIdentifier(Identifier t) { //3
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
     * @return {@link #status} (The status of the resource instance.). This is the underlying object with id, value and extensions. The accessor "getStatus" gives direct access to the value
     */
    public Enumeration<VisionStatus> getStatusElement() { 
      if (this.status == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.status");
        else if (Configuration.doAutoCreate())
          this.status = new Enumeration<VisionStatus>(new VisionStatusEnumFactory()); // bb
      return this.status;
    }

    public boolean hasStatusElement() { 
      return this.status != null && !this.status.isEmpty();
    }

    public boolean hasStatus() { 
      return this.status != null && !this.status.isEmpty();
    }

    /**
     * @param value {@link #status} (The status of the resource instance.). This is the underlying object with id, value and extensions. The accessor "getStatus" gives direct access to the value
     */
    public VisionPrescription setStatusElement(Enumeration<VisionStatus> value) { 
      this.status = value;
      return this;
    }

    /**
     * @return The status of the resource instance.
     */
    public VisionStatus getStatus() { 
      return this.status == null ? null : this.status.getValue();
    }

    /**
     * @param value The status of the resource instance.
     */
    public VisionPrescription setStatus(VisionStatus value) { 
      if (value == null)
        this.status = null;
      else {
        if (this.status == null)
          this.status = new Enumeration<VisionStatus>(new VisionStatusEnumFactory());
        this.status.setValue(value);
      }
      return this;
    }

    /**
     * @return {@link #patient} (A link to a resource representing the person to whom the vision products will be supplied.)
     */
    public Reference getPatient() { 
      if (this.patient == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.patient");
        else if (Configuration.doAutoCreate())
          this.patient = new Reference(); // cc
      return this.patient;
    }

    public boolean hasPatient() { 
      return this.patient != null && !this.patient.isEmpty();
    }

    /**
     * @param value {@link #patient} (A link to a resource representing the person to whom the vision products will be supplied.)
     */
    public VisionPrescription setPatient(Reference value) { 
      this.patient = value;
      return this;
    }

    /**
     * @return {@link #patient} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (A link to a resource representing the person to whom the vision products will be supplied.)
     */
    public Patient getPatientTarget() { 
      if (this.patientTarget == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.patient");
        else if (Configuration.doAutoCreate())
          this.patientTarget = new Patient(); // aa
      return this.patientTarget;
    }

    /**
     * @param value {@link #patient} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (A link to a resource representing the person to whom the vision products will be supplied.)
     */
    public VisionPrescription setPatientTarget(Patient value) { 
      this.patientTarget = value;
      return this;
    }

    /**
     * @return {@link #encounter} (A link to a resource that identifies the particular occurrence of contact between patient and health care provider.)
     */
    public Reference getEncounter() { 
      if (this.encounter == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.encounter");
        else if (Configuration.doAutoCreate())
          this.encounter = new Reference(); // cc
      return this.encounter;
    }

    public boolean hasEncounter() { 
      return this.encounter != null && !this.encounter.isEmpty();
    }

    /**
     * @param value {@link #encounter} (A link to a resource that identifies the particular occurrence of contact between patient and health care provider.)
     */
    public VisionPrescription setEncounter(Reference value) { 
      this.encounter = value;
      return this;
    }

    /**
     * @return {@link #encounter} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (A link to a resource that identifies the particular occurrence of contact between patient and health care provider.)
     */
    public Encounter getEncounterTarget() { 
      if (this.encounterTarget == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.encounter");
        else if (Configuration.doAutoCreate())
          this.encounterTarget = new Encounter(); // aa
      return this.encounterTarget;
    }

    /**
     * @param value {@link #encounter} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (A link to a resource that identifies the particular occurrence of contact between patient and health care provider.)
     */
    public VisionPrescription setEncounterTarget(Encounter value) { 
      this.encounterTarget = value;
      return this;
    }

    /**
     * @return {@link #dateWritten} (The date (and perhaps time) when the prescription was written.). This is the underlying object with id, value and extensions. The accessor "getDateWritten" gives direct access to the value
     */
    public DateTimeType getDateWrittenElement() { 
      if (this.dateWritten == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.dateWritten");
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
     * @param value {@link #dateWritten} (The date (and perhaps time) when the prescription was written.). This is the underlying object with id, value and extensions. The accessor "getDateWritten" gives direct access to the value
     */
    public VisionPrescription setDateWrittenElement(DateTimeType value) { 
      this.dateWritten = value;
      return this;
    }

    /**
     * @return The date (and perhaps time) when the prescription was written.
     */
    public Date getDateWritten() { 
      return this.dateWritten == null ? null : this.dateWritten.getValue();
    }

    /**
     * @param value The date (and perhaps time) when the prescription was written.
     */
    public VisionPrescription setDateWritten(Date value) { 
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
     * @return {@link #prescriber} (The healthcare professional responsible for authorizing the prescription.)
     */
    public Reference getPrescriber() { 
      if (this.prescriber == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.prescriber");
        else if (Configuration.doAutoCreate())
          this.prescriber = new Reference(); // cc
      return this.prescriber;
    }

    public boolean hasPrescriber() { 
      return this.prescriber != null && !this.prescriber.isEmpty();
    }

    /**
     * @param value {@link #prescriber} (The healthcare professional responsible for authorizing the prescription.)
     */
    public VisionPrescription setPrescriber(Reference value) { 
      this.prescriber = value;
      return this;
    }

    /**
     * @return {@link #prescriber} The actual object that is the target of the reference. The reference library doesn't populate this, but you can use it to hold the resource if you resolve it. (The healthcare professional responsible for authorizing the prescription.)
     */
    public Practitioner getPrescriberTarget() { 
      if (this.prescriberTarget == null)
        if (Configuration.errorOnAutoCreate())
          throw new Error("Attempt to auto-create VisionPrescription.prescriber");
        else if (Configuration.doAutoCreate())
          this.prescriberTarget = new Practitioner(); // aa
      return this.prescriberTarget;
    }

    /**
     * @param value {@link #prescriber} The actual object that is the target of the reference. The reference library doesn't use these, but you can use it to hold the resource if you resolve it. (The healthcare professional responsible for authorizing the prescription.)
     */
    public VisionPrescription setPrescriberTarget(Practitioner value) { 
      this.prescriberTarget = value;
      return this;
    }

    /**
     * @return {@link #reason} (Can be the reason or the indication for writing the prescription.)
     */
    public Type getReason() { 
      return this.reason;
    }

    /**
     * @return {@link #reason} (Can be the reason or the indication for writing the prescription.)
     */
    public CodeableConcept getReasonCodeableConcept() throws FHIRException { 
      if (!(this.reason instanceof CodeableConcept))
        throw new FHIRException("Type mismatch: the type CodeableConcept was expected, but "+this.reason.getClass().getName()+" was encountered");
      return (CodeableConcept) this.reason;
    }

    public boolean hasReasonCodeableConcept() { 
      return this.reason instanceof CodeableConcept;
    }

    /**
     * @return {@link #reason} (Can be the reason or the indication for writing the prescription.)
     */
    public Reference getReasonReference() throws FHIRException { 
      if (!(this.reason instanceof Reference))
        throw new FHIRException("Type mismatch: the type Reference was expected, but "+this.reason.getClass().getName()+" was encountered");
      return (Reference) this.reason;
    }

    public boolean hasReasonReference() { 
      return this.reason instanceof Reference;
    }

    public boolean hasReason() { 
      return this.reason != null && !this.reason.isEmpty();
    }

    /**
     * @param value {@link #reason} (Can be the reason or the indication for writing the prescription.)
     */
    public VisionPrescription setReason(Type value) { 
      this.reason = value;
      return this;
    }

    /**
     * @return {@link #dispense} (Deals with details of the dispense part of the supply specification.)
     */
    public List<VisionPrescriptionDispenseComponent> getDispense() { 
      if (this.dispense == null)
        this.dispense = new ArrayList<VisionPrescriptionDispenseComponent>();
      return this.dispense;
    }

    /**
     * @return Returns a reference to <code>this</code> for easy method chaining
     */
    public VisionPrescription setDispense(List<VisionPrescriptionDispenseComponent> theDispense) { 
      this.dispense = theDispense;
      return this;
    }

    public boolean hasDispense() { 
      if (this.dispense == null)
        return false;
      for (VisionPrescriptionDispenseComponent item : this.dispense)
        if (!item.isEmpty())
          return true;
      return false;
    }

    public VisionPrescriptionDispenseComponent addDispense() { //3
      VisionPrescriptionDispenseComponent t = new VisionPrescriptionDispenseComponent();
      if (this.dispense == null)
        this.dispense = new ArrayList<VisionPrescriptionDispenseComponent>();
      this.dispense.add(t);
      return t;
    }

    public VisionPrescription addDispense(VisionPrescriptionDispenseComponent t) { //3
      if (t == null)
        return this;
      if (this.dispense == null)
        this.dispense = new ArrayList<VisionPrescriptionDispenseComponent>();
      this.dispense.add(t);
      return this;
    }

    /**
     * @return The first repetition of repeating field {@link #dispense}, creating it if it does not already exist
     */
    public VisionPrescriptionDispenseComponent getDispenseFirstRep() { 
      if (getDispense().isEmpty()) {
        addDispense();
      }
      return getDispense().get(0);
    }

      protected void listChildren(List<Property> childrenList) {
        super.listChildren(childrenList);
        childrenList.add(new Property("identifier", "Identifier", "Business identifier which may be used by other parties to reference or identify the prescription.", 0, java.lang.Integer.MAX_VALUE, identifier));
        childrenList.add(new Property("status", "code", "The status of the resource instance.", 0, java.lang.Integer.MAX_VALUE, status));
        childrenList.add(new Property("patient", "Reference(Patient)", "A link to a resource representing the person to whom the vision products will be supplied.", 0, java.lang.Integer.MAX_VALUE, patient));
        childrenList.add(new Property("encounter", "Reference(Encounter)", "A link to a resource that identifies the particular occurrence of contact between patient and health care provider.", 0, java.lang.Integer.MAX_VALUE, encounter));
        childrenList.add(new Property("dateWritten", "dateTime", "The date (and perhaps time) when the prescription was written.", 0, java.lang.Integer.MAX_VALUE, dateWritten));
        childrenList.add(new Property("prescriber", "Reference(Practitioner)", "The healthcare professional responsible for authorizing the prescription.", 0, java.lang.Integer.MAX_VALUE, prescriber));
        childrenList.add(new Property("reason[x]", "CodeableConcept|Reference(Condition)", "Can be the reason or the indication for writing the prescription.", 0, java.lang.Integer.MAX_VALUE, reason));
        childrenList.add(new Property("dispense", "", "Deals with details of the dispense part of the supply specification.", 0, java.lang.Integer.MAX_VALUE, dispense));
      }

      @Override
      public Base[] getProperty(int hash, String name, boolean checkValid) throws FHIRException {
        switch (hash) {
        case -1618432855: /*identifier*/ return this.identifier == null ? new Base[0] : this.identifier.toArray(new Base[this.identifier.size()]); // Identifier
        case -892481550: /*status*/ return this.status == null ? new Base[0] : new Base[] {this.status}; // Enumeration<VisionStatus>
        case -791418107: /*patient*/ return this.patient == null ? new Base[0] : new Base[] {this.patient}; // Reference
        case 1524132147: /*encounter*/ return this.encounter == null ? new Base[0] : new Base[] {this.encounter}; // Reference
        case -1496880759: /*dateWritten*/ return this.dateWritten == null ? new Base[0] : new Base[] {this.dateWritten}; // DateTimeType
        case 1430631077: /*prescriber*/ return this.prescriber == null ? new Base[0] : new Base[] {this.prescriber}; // Reference
        case -934964668: /*reason*/ return this.reason == null ? new Base[0] : new Base[] {this.reason}; // Type
        case 284885341: /*dispense*/ return this.dispense == null ? new Base[0] : this.dispense.toArray(new Base[this.dispense.size()]); // VisionPrescriptionDispenseComponent
        default: return super.getProperty(hash, name, checkValid);
        }

      }

      @Override
      public void setProperty(int hash, String name, Base value) throws FHIRException {
        switch (hash) {
        case -1618432855: // identifier
          this.getIdentifier().add(castToIdentifier(value)); // Identifier
          break;
        case -892481550: // status
          this.status = new VisionStatusEnumFactory().fromType(value); // Enumeration<VisionStatus>
          break;
        case -791418107: // patient
          this.patient = castToReference(value); // Reference
          break;
        case 1524132147: // encounter
          this.encounter = castToReference(value); // Reference
          break;
        case -1496880759: // dateWritten
          this.dateWritten = castToDateTime(value); // DateTimeType
          break;
        case 1430631077: // prescriber
          this.prescriber = castToReference(value); // Reference
          break;
        case -934964668: // reason
          this.reason = castToType(value); // Type
          break;
        case 284885341: // dispense
          this.getDispense().add((VisionPrescriptionDispenseComponent) value); // VisionPrescriptionDispenseComponent
          break;
        default: super.setProperty(hash, name, value);
        }

      }

      @Override
      public void setProperty(String name, Base value) throws FHIRException {
        if (name.equals("identifier"))
          this.getIdentifier().add(castToIdentifier(value));
        else if (name.equals("status"))
          this.status = new VisionStatusEnumFactory().fromType(value); // Enumeration<VisionStatus>
        else if (name.equals("patient"))
          this.patient = castToReference(value); // Reference
        else if (name.equals("encounter"))
          this.encounter = castToReference(value); // Reference
        else if (name.equals("dateWritten"))
          this.dateWritten = castToDateTime(value); // DateTimeType
        else if (name.equals("prescriber"))
          this.prescriber = castToReference(value); // Reference
        else if (name.equals("reason[x]"))
          this.reason = castToType(value); // Type
        else if (name.equals("dispense"))
          this.getDispense().add((VisionPrescriptionDispenseComponent) value);
        else
          super.setProperty(name, value);
      }

      @Override
      public Base makeProperty(int hash, String name) throws FHIRException {
        switch (hash) {
        case -1618432855:  return addIdentifier(); // Identifier
        case -892481550: throw new FHIRException("Cannot make property status as it is not a complex type"); // Enumeration<VisionStatus>
        case -791418107:  return getPatient(); // Reference
        case 1524132147:  return getEncounter(); // Reference
        case -1496880759: throw new FHIRException("Cannot make property dateWritten as it is not a complex type"); // DateTimeType
        case 1430631077:  return getPrescriber(); // Reference
        case -669418564:  return getReason(); // Type
        case 284885341:  return addDispense(); // VisionPrescriptionDispenseComponent
        default: return super.makeProperty(hash, name);
        }

      }

      @Override
      public Base addChild(String name) throws FHIRException {
        if (name.equals("identifier")) {
          return addIdentifier();
        }
        else if (name.equals("status")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.status");
        }
        else if (name.equals("patient")) {
          this.patient = new Reference();
          return this.patient;
        }
        else if (name.equals("encounter")) {
          this.encounter = new Reference();
          return this.encounter;
        }
        else if (name.equals("dateWritten")) {
          throw new FHIRException("Cannot call addChild on a primitive type VisionPrescription.dateWritten");
        }
        else if (name.equals("prescriber")) {
          this.prescriber = new Reference();
          return this.prescriber;
        }
        else if (name.equals("reasonCodeableConcept")) {
          this.reason = new CodeableConcept();
          return this.reason;
        }
        else if (name.equals("reasonReference")) {
          this.reason = new Reference();
          return this.reason;
        }
        else if (name.equals("dispense")) {
          return addDispense();
        }
        else
          return super.addChild(name);
      }

  public String fhirType() {
    return "VisionPrescription";

  }

      public VisionPrescription copy() {
        VisionPrescription dst = new VisionPrescription();
        copyValues(dst);
        if (identifier != null) {
          dst.identifier = new ArrayList<Identifier>();
          for (Identifier i : identifier)
            dst.identifier.add(i.copy());
        };
        dst.status = status == null ? null : status.copy();
        dst.patient = patient == null ? null : patient.copy();
        dst.encounter = encounter == null ? null : encounter.copy();
        dst.dateWritten = dateWritten == null ? null : dateWritten.copy();
        dst.prescriber = prescriber == null ? null : prescriber.copy();
        dst.reason = reason == null ? null : reason.copy();
        if (dispense != null) {
          dst.dispense = new ArrayList<VisionPrescriptionDispenseComponent>();
          for (VisionPrescriptionDispenseComponent i : dispense)
            dst.dispense.add(i.copy());
        };
        return dst;
      }

      protected VisionPrescription typedCopy() {
        return copy();
      }

      @Override
      public boolean equalsDeep(Base other) {
        if (!super.equalsDeep(other))
          return false;
        if (!(other instanceof VisionPrescription))
          return false;
        VisionPrescription o = (VisionPrescription) other;
        return compareDeep(identifier, o.identifier, true) && compareDeep(status, o.status, true) && compareDeep(patient, o.patient, true)
           && compareDeep(encounter, o.encounter, true) && compareDeep(dateWritten, o.dateWritten, true) && compareDeep(prescriber, o.prescriber, true)
           && compareDeep(reason, o.reason, true) && compareDeep(dispense, o.dispense, true);
      }

      @Override
      public boolean equalsShallow(Base other) {
        if (!super.equalsShallow(other))
          return false;
        if (!(other instanceof VisionPrescription))
          return false;
        VisionPrescription o = (VisionPrescription) other;
        return compareValues(status, o.status, true) && compareValues(dateWritten, o.dateWritten, true);
      }

      public boolean isEmpty() {
        return super.isEmpty() && ca.uhn.fhir.util.ElementUtil.isEmpty(identifier, status, patient
          , encounter, dateWritten, prescriber, reason, dispense);
      }

  @Override
  public ResourceType getResourceType() {
    return ResourceType.VisionPrescription;
   }

 /**
   * Search parameter: <b>prescriber</b>
   * <p>
   * Description: <b>Who authorizes the vision product</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>VisionPrescription.prescriber</b><br>
   * </p>
   */
  @SearchParamDefinition(name="prescriber", path="VisionPrescription.prescriber", description="Who authorizes the vision product", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Practitioner") }, target={Practitioner.class } )
  public static final String SP_PRESCRIBER = "prescriber";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>prescriber</b>
   * <p>
   * Description: <b>Who authorizes the vision product</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>VisionPrescription.prescriber</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam PRESCRIBER = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_PRESCRIBER);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>VisionPrescription:prescriber</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_PRESCRIBER = new ca.uhn.fhir.model.api.Include("VisionPrescription:prescriber").toLocked();

 /**
   * Search parameter: <b>identifier</b>
   * <p>
   * Description: <b>Return prescriptions with this external identifier</b><br>
   * Type: <b>token</b><br>
   * Path: <b>VisionPrescription.identifier</b><br>
   * </p>
   */
  @SearchParamDefinition(name="identifier", path="VisionPrescription.identifier", description="Return prescriptions with this external identifier", type="token" )
  public static final String SP_IDENTIFIER = "identifier";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>identifier</b>
   * <p>
   * Description: <b>Return prescriptions with this external identifier</b><br>
   * Type: <b>token</b><br>
   * Path: <b>VisionPrescription.identifier</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.TokenClientParam IDENTIFIER = new ca.uhn.fhir.rest.gclient.TokenClientParam(SP_IDENTIFIER);

 /**
   * Search parameter: <b>patient</b>
   * <p>
   * Description: <b>The identity of a patient to list dispenses for</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>VisionPrescription.patient</b><br>
   * </p>
   */
  @SearchParamDefinition(name="patient", path="VisionPrescription.patient", description="The identity of a patient to list dispenses for", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Patient") }, target={Patient.class } )
  public static final String SP_PATIENT = "patient";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>patient</b>
   * <p>
   * Description: <b>The identity of a patient to list dispenses for</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>VisionPrescription.patient</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam PATIENT = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_PATIENT);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>VisionPrescription:patient</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_PATIENT = new ca.uhn.fhir.model.api.Include("VisionPrescription:patient").toLocked();

 /**
   * Search parameter: <b>datewritten</b>
   * <p>
   * Description: <b>Return prescriptions written on this date</b><br>
   * Type: <b>date</b><br>
   * Path: <b>VisionPrescription.dateWritten</b><br>
   * </p>
   */
  @SearchParamDefinition(name="datewritten", path="VisionPrescription.dateWritten", description="Return prescriptions written on this date", type="date" )
  public static final String SP_DATEWRITTEN = "datewritten";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>datewritten</b>
   * <p>
   * Description: <b>Return prescriptions written on this date</b><br>
   * Type: <b>date</b><br>
   * Path: <b>VisionPrescription.dateWritten</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.DateClientParam DATEWRITTEN = new ca.uhn.fhir.rest.gclient.DateClientParam(SP_DATEWRITTEN);

 /**
   * Search parameter: <b>encounter</b>
   * <p>
   * Description: <b>Return prescriptions with this encounter identifier</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>VisionPrescription.encounter</b><br>
   * </p>
   */
  @SearchParamDefinition(name="encounter", path="VisionPrescription.encounter", description="Return prescriptions with this encounter identifier", type="reference", providesMembershipIn={ @ca.uhn.fhir.model.api.annotation.Compartment(name="Encounter") }, target={Encounter.class } )
  public static final String SP_ENCOUNTER = "encounter";
 /**
   * <b>Fluent Client</b> search parameter constant for <b>encounter</b>
   * <p>
   * Description: <b>Return prescriptions with this encounter identifier</b><br>
   * Type: <b>reference</b><br>
   * Path: <b>VisionPrescription.encounter</b><br>
   * </p>
   */
  public static final ca.uhn.fhir.rest.gclient.ReferenceClientParam ENCOUNTER = new ca.uhn.fhir.rest.gclient.ReferenceClientParam(SP_ENCOUNTER);

/**
   * Constant for fluent queries to be used to add include statements. Specifies
   * the path value of "<b>VisionPrescription:encounter</b>".
   */
  public static final ca.uhn.fhir.model.api.Include INCLUDE_ENCOUNTER = new ca.uhn.fhir.model.api.Include("VisionPrescription:encounter").toLocked();


}

