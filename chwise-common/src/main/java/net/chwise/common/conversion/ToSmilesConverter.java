/**
 Copyright (c) 2013 Alexander Savochkin
 Chemical wikipedia search (chwise.net) web-site source code

 This file is part of ChWiSe.Net infrastructure.

 ChWiSe.Net infrastructure is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.chwise.common.conversion;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import java.io.StringReader;

public class ToSmilesConverter
{

    public String convert(IMolecule molecule) {
        SmilesGenerator sg = new SmilesGenerator();
        sg.setUseAromaticityFlag(true);
        String smiles = sg.createSMILES(molecule);
        return smiles;
    }

    public String convert(String mol) {
        StringReader strReader = new StringReader(mol);
        MDLReader mdlReader = new MDLReader( strReader );

        Molecule moleculeContainer = new Molecule();
        IMolecule molecule = null;
        try {
            molecule = mdlReader.read( moleculeContainer );
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return convert(molecule);
    }

}