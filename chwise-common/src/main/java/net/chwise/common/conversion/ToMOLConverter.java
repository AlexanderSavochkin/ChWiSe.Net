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

import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.io.listener.PropertiesListener;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.smiles.SmilesParser;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

public class ToMOLConverter
{

    public String convert(IMolecule molecule) {

        // why are valencies being set???
        for(IAtom atom : molecule.atoms()){
            atom.setValency(null);
        }

        StringWriter stringWriter = new StringWriter();
        try {
            StructureDiagramGenerator sdg = new StructureDiagramGenerator();
            sdg.setMolecule(molecule);
            sdg.generateCoordinates();
            molecule = sdg.getMolecule();

            MDLV2000Writer mdlWriter = new MDLV2000Writer( stringWriter );

            Properties prop = new Properties();
            prop.setProperty("WriteAromaticBondTypes","true");
            //prop.setProperty("writeQueryFormatValencies", "true"); does not work
            PropertiesListener listener = new PropertiesListener(prop);
            mdlWriter.addChemObjectIOListener(listener);
            mdlWriter.customizeJob();

            mdlWriter.write(molecule);
            mdlWriter.close();
        } catch (CDKException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return stringWriter.toString();
    }

    public String convert(String smiles) {
        SmilesParser smilesParser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        try {
            IMolecule molecule = smilesParser.parseSmiles( smiles );
            return convert( molecule );
        } catch (InvalidSmilesException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    private String setAtomLineCharge(String molAtomLine, int charge) {
        char molChargeNum = 0;
        switch (charge) {
            case 3:
                molChargeNum = '1';
                break;
            case 2:
                molChargeNum = '2';
                break;
            case 1:
                molChargeNum = '3';
                break;
            case -1:
                molChargeNum = '5';
                break;
            case -2:
                molChargeNum = '6';
                break;
            case -3:
                molChargeNum = '7';
                break;
        }
        StringBuilder sbMolAtomLine = new StringBuilder(molAtomLine);
        sbMolAtomLine.setCharAt( 38, molChargeNum );
        return sbMolAtomLine.toString();
    }

    public String MOLChargesKludge(String mdlmol) {
        String[] molLines = mdlmol.split("\\r?\\n");
        String[] firstMeaningfulLineTokens = molLines[3].trim().split("\\s+");
        int numAtoms = Integer.parseInt(firstMeaningfulLineTokens[0]);
        int numBonds = Integer.parseInt(firstMeaningfulLineTokens[1]);

        for (int l = 4 + numAtoms + numBonds; l < molLines.length; ++l) {
            if ( !molLines[l].matches("^M\\s+CHG.*") )
                continue;
            String[] tokens = molLines[l].trim().split("\\s+");
            int numChargedSpecified = Integer.valueOf( tokens[2] );
            for (int c = 0; c < numChargedSpecified; ++c) {
                int atomIndex = Integer.parseInt( tokens[3 + 2 * c] );
                int charge = Integer.parseInt( tokens[3 + 2 * c + 1] );
                molLines[3 + atomIndex] = setAtomLineCharge( molLines[3 + atomIndex], charge);
            }
        }
        return org.apache.commons.lang3.StringUtils.join(molLines, "\n");
    }

}