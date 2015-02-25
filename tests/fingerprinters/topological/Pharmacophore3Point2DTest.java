package fingerprinters.topological;

import org.junit.BeforeClass;
import org.junit.Test;

import de.zbit.jcmapper.fingerprinters.topological.Encoding2DPharmacophore3Point;

import fingerprinters.SameMoleculeTester;


public class Pharmacophore3Point2DTest {
	static SameMoleculeTester tester;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		tester = new SameMoleculeTester(new Encoding2DPharmacophore3Point());
	}

	@Test
	public void checkLength() {
		tester.checkLength();
	}
	
	@Test
	public void checkFeatures() {
		tester.checkFeatures();
	}
	
	@Test
	public void checkHashedFeatures() {
		tester.checkHashedFeatures();
	}
}
