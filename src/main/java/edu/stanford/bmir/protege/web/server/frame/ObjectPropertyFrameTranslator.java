package edu.stanford.bmir.protege.web.server.frame;

import edu.stanford.bmir.protege.web.server.project.Project;
import edu.stanford.bmir.protege.web.server.renderer.RenderingManager;
import edu.stanford.bmir.protege.web.shared.DataFactory;
import edu.stanford.bmir.protege.web.shared.entity.OWLClassData;
import edu.stanford.bmir.protege.web.shared.entity.OWLObjectPropertyData;
import edu.stanford.bmir.protege.web.shared.frame.*;
import org.semanticweb.owlapi.model.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 14/01/2013
 */
public class ObjectPropertyFrameTranslator implements FrameTranslator<ObjectPropertyFrame, OWLObjectPropertyData> {

    @Override
    public ObjectPropertyFrame getFrame(OWLObjectPropertyData subject, OWLOntology rootOntology, Project project) {
        RenderingManager rm = project.getRenderingManager();
        Set<OWLAxiom> propertyValueAxioms = new HashSet<>();

        Set<OWLClassData> domains = new HashSet<>();
        Set<OWLClassData> ranges = new HashSet<>();
        Set<ObjectPropertyCharacteristic> characteristics = new HashSet<>();
        for(OWLOntology ontology : rootOntology.getImportsClosure()) {
            propertyValueAxioms.addAll(ontology.getAnnotationAssertionAxioms(subject.getEntity().getIRI()));
            for(OWLObjectPropertyDomainAxiom ax : ontology.getObjectPropertyDomainAxioms(subject.getEntity())) {
                final OWLClassExpression domain = ax.getDomain();
                if (!domain.isAnonymous()) {
                    domains.add(rm.getRendering(domain.asOWLClass()));
                }
            }
            for(OWLObjectPropertyRangeAxiom ax : ontology.getObjectPropertyRangeAxioms(subject.getEntity())) {
                OWLClassExpression range = ax.getRange();
                if(!range.isAnonymous()) {
                    ranges.add(rm.getRendering(range.asOWLClass()));
                }
            }
            if(ontology.getAxiomCount(AxiomType.FUNCTIONAL_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.FUNCTIONAL);
            }
            if(ontology.getAxiomCount(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.INVERSE_FUNCTIONAL);
            }
            if(ontology.getAxiomCount(AxiomType.SYMMETRIC_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.SYMMETRIC);
            }
            if(ontology.getAxiomCount(AxiomType.ASYMMETRIC_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.ASYMMETRIC);
            }
            if(ontology.getAxiomCount(AxiomType.REFLEXIVE_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.REFLEXIVE);
            }
            if(ontology.getAxiomCount(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.IRREFLEXIVE);
            }
            if(ontology.getAxiomCount(AxiomType.TRANSITIVE_OBJECT_PROPERTY) > 1) {
                characteristics.add(ObjectPropertyCharacteristic.TRANSITIVE);
            }
        }
        AxiomPropertyValueTranslator translator = new AxiomPropertyValueTranslator();
        Set<PropertyAnnotationValue> propertyValues = new HashSet<>();
        for(OWLAxiom ax : propertyValueAxioms) {
            Set<PropertyValue> translationResult = translator.getPropertyValues(
                    subject.getEntity(), ax, rootOntology,
                    State.ASSERTED, rm);
            for(PropertyValue pv : translationResult) {
                if(pv.isAnnotation()) {
                    propertyValues.add((PropertyAnnotationValue) pv);
                }
            }

        }
        return new ObjectPropertyFrame(subject, propertyValues, domains, ranges, Collections.emptySet(), characteristics);
    }

    @Override
    public Set<OWLAxiom> getAxioms(ObjectPropertyFrame frame, Mode mode) {
        Set<OWLAxiom> result = new HashSet<OWLAxiom>();
        for(PropertyAnnotationValue pv : frame.getAnnotationPropertyValues()) {
            AxiomPropertyValueTranslator translator = new AxiomPropertyValueTranslator();
            result.addAll(translator.getAxioms(frame.getSubject().getEntity(), pv, mode));
        }
        for(OWLClassData domain : frame.getDomains()) {
            OWLAxiom ax = DataFactory.get().getOWLObjectPropertyDomainAxiom(frame.getSubject().getEntity(), domain.getEntity());
            result.add(ax);
        }
        for(OWLClassData range : frame.getRanges()) {
            OWLAxiom ax = DataFactory.get().getOWLObjectPropertyRangeAxiom(frame.getSubject().getEntity(), range.getEntity());
            result.add(ax);
        }
        for(ObjectPropertyCharacteristic characteristic : frame.getCharacteristics()) {
            OWLAxiom ax = characteristic.createAxiom(frame.getSubject().getEntity(), DataFactory.get());
            result.add(ax);
        }
        return result;
    }
}
