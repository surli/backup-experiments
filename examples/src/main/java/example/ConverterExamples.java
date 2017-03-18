package example;

import org.hl7.fhir.convertors.NullVersionConverterAdvisor;
import org.hl7.fhir.convertors.VersionConvertorAdvisor;
import org.hl7.fhir.convertors.VersionConvertor_10_20;
import org.hl7.fhir.exceptions.FHIRException;

public class ConverterExamples {

	public void c1020() throws FHIRException {
	//START SNIPPET: 1020
		// Create a converter
		VersionConvertorAdvisor advisor = new NullVersionConverterAdvisor();
		VersionConvertor_10_20 converter = new VersionConvertor_10_20(advisor);
		
		// Create an input resource to convert
		org.hl7.fhir.instance.model.Observation input = new org.hl7.fhir.instance.model.Observation();
		input.setEncounter(new org.hl7.fhir.instance.model.Reference("Encounter/123"));
		
		// Convert the resource
		org.hl7.fhir.dstu3.model.Observation output = converter.convertObservation(input);
		String context = output.getContext().getReference();
	//END SNIPPET: 1020
	}
}
