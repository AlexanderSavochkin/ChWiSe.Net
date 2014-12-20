/*
 * This file contains code that was borrowed from OSCAR3 software.
 * (originally class in oscar3 was uk.ac.cam.ch.wwmm.ptclib.cdk.MultiFragmentStructureDiagramGenerator).
 * As far as could understand in relied on elder version of CDK, so I have rewritten in according
 * to CDK 1.4.19.
 *
 * I left original methods names and comments where it was possible
 */

/*
 * This is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License version 2 as published by the Free Software
 * Foundation.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * Copyright: mixed. See WordPress.org for copyright and licensing information.
 */

package net.chwise.common.conversion;

import javax.vecmath.Point2d;

//import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.MoleculeSet;
//I didn't found such class in CDK 1.4.19. It seems that I can use org.openscience.cdk.geometry.GeometryTools
//import org.openscience.cdk.geometry.GeometryToolsInternalCoordinates;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IMoleculeSet;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.layout.TemplateHandler;
//import uk.ac.cam.ch.wwmm.ptclib.io.ResourceGetter;

/**
 * A wrapper for StructureDiagramGenerator that can handle
 * multi-fragment molecules.
 *
 * @author ptc24
 */
public final class MultiFragmentStructureDiagramGenerator {
    private static MultiFragmentStructureDiagramGenerator myInstance;

    //Alex Savochkin: in chwise.net we don't use templates yet, so I comment out all template-related stuff
    //private TemplateHandler th;

    //private MoleculeSet mols;

    /**
     * Re-initialise the MultiFragmentStructureDiagramGenerator, re-loading
     * the templates.
     */
    public static void reinitialise() {
        myInstance = null;
        getInstance();
    }

    private static MultiFragmentStructureDiagramGenerator getInstance() {
        if (myInstance == null) myInstance = new MultiFragmentStructureDiagramGenerator();
        return myInstance;
    }

    /**
     * Tests to see if the molecule has 2D coordinates already. This returns
     * false if the molecule is null, has fewer than two atoms, has atoms with
     * no-coordinates or has no atoms that are not at (0.0, 0.0).
     *
     * @param mol The molecule to test.
     * @return Whether it has useful 2D coordinates.
     */
    public static boolean hasStructure(IMolecule mol) {
        //System.out.println("CheckNull");
        if (mol == null) return false;
        //System.out.println("CheckOneAtom");
        if (mol.getAtomCount() < 2) return true;
        Point2d origin = new Point2d(0.0, 0.0);
        //System.out.println("CheckAtoms");
        for (int i = 0; i < mol.getAtomCount(); i++) {
            Point2d p = mol.getAtom(i).getPoint2d();
            //System.out.println(mol.getAtom(i).getPoint2d());
            if (p != null && !origin.equals(p)) return true;
        }
        //System.out.println("CheckedAtoms");
        return false;
    }

    /**
     * Produces a molecule with 2D coordinates from a molecule.
     *
     * @param inputMol The input molecule.
     * @return The molecule, with 2D coordinates.
     * @throws Exception
     */
    public static IMolecule getMoleculeWith2DCoords(IMolecule inputMol) throws Exception {
        return getInstance().getMoleculeStructure(inputMol);
    }

    private MultiFragmentStructureDiagramGenerator() {
        //Alex Savochkin: in chwise.net we don't use templates yet, so I comment out all template-related stuff
        /*
        th = new TemplateHandler(DefaultChemObjectBuilder.getInstance());
        try {
            ResourceGetter rg = new ResourceGetter("uk/ac/cam/ch/wwmm/ptclib/cdk/resources/");
            for (String s : rg.getStrings("extraTemplates.txt")) {
                if (s != null && s.length() > 0) th.addMolecule(StructureConverter.cmlToMolecule(rg.getStream(s)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
    }

    private IMolecule getMoleculeStructure(IMolecule inputMol) throws Exception {
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();
        IMoleculeSet originalMols = ConnectivityChecker.partitionIntoMolecules(inputMol);
        MoleculeSet mols = new MoleculeSet();
        for (int i = 0; i < originalMols.getMoleculeCount(); i++) {
            IMolecule mol = originalMols.getMolecule(i);
            sdg.setMolecule(mol);
            //sdg.setMolecule(new Molecule(th.getTemplateAt(6)));
            //Alex Savochkin: in chwise.net we don't use templates yet, so I comment out all template-related stuff
            //sdg.setTemplateHandler(th);
            sdg.generateCoordinates();
            mols.addMolecule(sdg.getMolecule());
        }
        if (mols.getMoleculeCount() == 0) return null;
        sdg = null;
        return getMoleculeForMoleculeRange(0, mols.getMoleculeCount() - 1, mols);
    }

    private IMolecule getMoleculeForMoleculeRange(int start, int end, MoleculeSet mols) throws Exception {
        if (start == end) {
            return mols.getMolecule(start);
        } else if (start + 1 == end) {
            return combineMolecules(mols.getMolecule(start), mols.getMolecule(end));
        } else {
            int midPoint = (start + end) / 2;
            return combineMolecules(getMoleculeForMoleculeRange(start, midPoint, mols),
                    getMoleculeForMoleculeRange(midPoint + 1, end, mols));
        }
    }

    private IMolecule combineMolecules(IMolecule molA, IMolecule molB) throws Exception {
        //GeometryTools.t
        GeometryTools.translate2DCenterTo(molA, new Point2d(0, 0));
        GeometryTools.translate2DCenterTo(molB, new Point2d(0, 0));
         /* XY - minX, minY, maxX, maxY */
        double[] interXY = GeometryTools.getMinMax(molA);
        double[] outputXY = GeometryTools.getMinMax(molB);
        double sideBySideWidth = (interXY[2] + outputXY[2] - interXY[0] - outputXY[0]) + 2;
        double onTopHeight = (interXY[3] + outputXY[3] - interXY[1] - outputXY[1]) + 2;
        if (sideBySideWidth < onTopHeight) {
            GeometryTools.translate2D(molA, outputXY[2] - interXY[0] + 1.0, 0.0);
        } else {
            GeometryTools.translate2D(molA, 0.0, outputXY[3] - interXY[1] + 1.0);
        }
        molB.add(molA);

        return molB;
    }

}