package demo;

import java.io.IOException;

import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureException;
import org.biojava.nbio.structure.io.mmtf.MmtfActions;

/**
 * Class to show how to read a Biojava structure using MMTF
 * @author Anthony Bradley
 *
 */
public class DemoMmtfReader {

	/**
	 * Main function to run the demo
	 * @param args no args to specify
	 * @throws IOException 
	 * @throws StructureException
	 */
	public static void main(String[] args) throws IOException, StructureException {
		Structure structure = MmtfActions.readFromWeb("4cup");
		System.out.println(structure.getChains().size());
	}
	
}
