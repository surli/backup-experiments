package edu.stanford.bmir.protege.web.shared.frame;

import edu.stanford.bmir.protege.web.shared.entity.OWLPrimitiveData;
import edu.stanford.bmir.protege.web.shared.entity.OWLPropertyData;

import java.io.Serializable;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 21/11/2012
 */
public abstract class PropertyValue implements Comparable<PropertyValue>, Serializable {

    private State state;

    @SuppressWarnings("GwtInconsistentSerializableClass" )
    private OWLPropertyData property;

    @SuppressWarnings("GwtInconsistentSerializableClass" )
    private OWLPrimitiveData value;

    protected PropertyValue() {

    }

    public PropertyValue(OWLPropertyData property, OWLPrimitiveData value, State state) {
        this.property = property;
        this.value = value;
        this.state = state;
    }

    public OWLPropertyData getProperty() {
        return property;
    }

    public OWLPrimitiveData getValue() {
        return value;
    }

    public State getState() {
        return state;
    }

    public abstract boolean isValueMostSpecific();

    public abstract boolean isAnnotation();

    public abstract boolean isLogical();

    public abstract <R, E extends Throwable>  R accept(PropertyValueVisitor<R, E> visitor) throws E;

    @Override
    final public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PropertyValue");
        sb.append("(");
        sb.append(getProperty());
        sb.append(" ");
        sb.append(getValue());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int compareTo(PropertyValue o) {
        int diff = this.getProperty().compareTo(o.getProperty());
        if(diff != 0) {
            return diff;
        }
        return this.getValue().getBrowserText().compareTo(o.getValue().getBrowserText());

    }

    public PropertyValue setState(State state) {
        if(this.state == state) {
            return this;
        }
        else {
            return duplicateWithState(state);
        }
    }

    protected abstract PropertyValue duplicateWithState(State state);

}
