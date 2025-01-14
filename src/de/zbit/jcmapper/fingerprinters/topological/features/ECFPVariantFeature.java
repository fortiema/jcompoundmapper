package de.zbit.jcmapper.fingerprinters.topological.features;

import java.util.LinkedList;
import java.util.List;

import org.openscience.cdk.PseudoAtom;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.smiles.SmilesGenerator;

import de.zbit.jcmapper.fingerprinters.EncodingFingerprint;
import de.zbit.jcmapper.fingerprinters.topological.Encoding2DECFP.BondOrderIdentifierTupel;

public class ECFPVariantFeature extends ECFPFeature {

	private final DanglingBond[] connectivityBonds;
	
	public ECFPVariantFeature(EncodingFingerprint encodingFingerprint, IAtomContainer parentMolecule, IAtom coreAtom, IAtomContainer substructure, DanglingBond[] connectivityBonds, int iterationNumber, int parent, List<BondOrderIdentifierTupel> connections) {
		super(encodingFingerprint, parentMolecule, coreAtom, substructure, iterationNumber, parent, connections);
		this.connectivityBonds = connectivityBonds;
	}

	@Override
	public String featureToString(boolean useAromaticFlag) {
		final IAtom[] tempAtoms = new IAtom[this.connectivityBonds.length];
		final IAtomContainer substructureClone = this.getNonDeepCloneOfSubstructure();

		for (int i = 0; i < this.connectivityBonds.length; i++) {
			final DanglingBond connectivity = this.connectivityBonds[i];
			final IBond bond = connectivity.getBond();
			tempAtoms[i] = connectivity.getConnectedAtom();
			if (!substructureClone.contains(connectivity.getConnectedAtom())) {
				final IAtom pseudoAtom = new PseudoAtom();
				bond.setAtom(pseudoAtom, connectivity.getConnectedAtomPosition());
				substructureClone.addAtom(pseudoAtom);
			}
			substructureClone.addBond(connectivity.getBond());
		}
		String smile = new SmilesGenerator().createSMILES(substructureClone);

		for (int i = 0; i < this.connectivityBonds.length; i++) {
			final DanglingBond connectivity = this.connectivityBonds[i];
			final IBond bond = connectivity.getBond();
			bond.setAtom(tempAtoms[i], connectivity.getConnectedAtomPosition());
		}

		return smile;
	}

	public DanglingBond getDanglingBond(int i) {
		return this.connectivityBonds[i];
	}

	public int numberOfDanglingBonds() {
		return this.connectivityBonds.length;
	}

	@Override
	public double getValue() {
		return 1;
	}

	public boolean hasEqualSubstructure(ECFPVariantFeature arg) {
		if (arg.representedSubstructure().getAtomCount() != this.representedSubstructure().getAtomCount()) {
			return false;
		}
		for (final IAtom atom : arg.representedSubstructure().atoms()) {
			if (!this.representedSubstructure().contains(atom)) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public Iterable<IBond> representedBonds(){
		List<IBond> bonds = new LinkedList<IBond>();
		for(IBond bond: this.representedSubstructure().bonds()){
			bonds.add(bond);
		}
		for(DanglingBond bond: connectivityBonds){
			bonds.add(bond.getBond());
		}
		return bonds;
	}
}
