package fingerprinters.topological;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;


import junit.framework.Assert;

import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.MACCSFingerprinter;
import org.openscience.cdk.interfaces.IAtomContainer;
import de.zbit.jcmapper.fingerprinters.features.IFeature;
import de.zbit.jcmapper.fingerprinters.topological.MACCS166;
import de.zbit.jcmapper.fingerprinters.topological.features.PositionFeature;
import de.zbit.jcmapper.io.reader.RandomAccessMDLReader;



public class MACCS166FingerprintTest {
	
	MACCS166 fingerprinter = new MACCS166();
	MACCSFingerprinter cdkFingerprinter = new MACCSFingerprinter();
	
	//converts an fingerprint of PositionFeatures to a String
	public static String toString(ArrayList<IFeature> fingerprint){
		//all entries of the storage are set to 0
		int stor[] = new int[166];
		String res = "";
		//the positions of the features are set to 1 
		for (int j = 0; j < fingerprint.size();j++){
			PositionFeature fet = (PositionFeature) fingerprint.get(j);
			stor[fet.hashCode()-1] = (int) fet.getValue();
		}
		//build the string
		for(int i = 0; i < stor.length; i++){
			res = res + stor[i];
		}
		return res;
	}
	
	//covert a bitSet to an String
	public static String bitSetToString(BitSet bitSet){
		String res = "";
		for(int i = 0; i < 166; i++){
			if(bitSet.get(i)){
				res = res + "1";
			} else {
				res = res + "0";
			}
		}
		return res;
	}
	

	@Test
	public void runTestCdkFingerprint() throws CDKException {
		RandomAccessMDLReader reader = null;
		try {
			reader = new RandomAccessMDLReader(new File("./resources/ACE_MM.sdf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < reader.getSize(); i++) {
			IAtomContainer mol = reader.getMol(i);
			ArrayList <IFeature> features = (ArrayList<IFeature>) fingerprinter.getFingerprint(mol);
			String ownFingerprint = MACCS166FingerprintTest.toString(features);
			BitSet cdkBitSetFingerprint = cdkFingerprinter.getBitFingerprint(mol).asBitSet();
			String cdkFingerprint = MACCS166FingerprintTest.bitSetToString(cdkBitSetFingerprint);
			System.out.println(ownFingerprint.charAt(111));
			//System.out.println(cdkFingerprint);
			Assert.assertEquals(ownFingerprint, cdkFingerprint);
		}
	}
	
	//@Test
	public void runTestMayaFingerprint() throws CDKException {
		String mayaFingerprint1 = "000000000000000000000010000000000010000000000000110001000000000000000000001100100011100001110011001100010100011100001101111001011011000111010101011101101111111010111000";
		RandomAccessMDLReader reader = null;
		try {
			reader = new RandomAccessMDLReader(new File("./resources/ACE1_MM.sdf"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println(mayaFingerprint1.length());
		for (int i = 0; i < reader.getSize(); i++) {
			IAtomContainer mol = reader.getMol(i);
			ArrayList <IFeature> features = (ArrayList<IFeature>) fingerprinter.getFingerprint(mol);
			String ownFingerprint = MACCS166FingerprintTest.toString(features);
			//System.out.println(ownFingerprint);
			//System.out.println(mayaFingerprint1);
			Assert.assertEquals(ownFingerprint, mayaFingerprint1);
		}
	}

}
