/**
 Copyright (c) 2015 Alexander Savochkin
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

package net.chwise.test;

public class TestData {
    public static final String MANZANATE_ARTICLE = "Manzanate\n" +
            "CCCC(C)C(=O)OCC\n" +
            "{{Orphan|date=April 2011}}\n" +
            "\n" +
            "{{chembox\n" +
            "| verifiedrevid = 425210668\n" +
            "|   Name =Ethyl 2-methylpentanoate\n" +
            "|   Reference =\n" +
            "|   ImageFile = Manzanate.png\n" +
            "|   ImageSize = 200px\n" +
            "|   ImageName =\n" +
            "|   IUPACName = Ethyl 2-methylpentanoate\n" +
            "|   OtherNames = Ethyl α-methylvalerate; Melon valerate\n" +
            "| Section1 = {{Chembox Identifiers\n" +
            "|   CASNo_Ref = {{cascite|correct|??}}\n" +
            "| CASNo = 39255-32-8\n" +
            "|   SMILES = CCCC(C)C(=O)OCC\n" +
            "  }}\n" +
            "| Section2 = {{Chembox Properties\n" +
            "|   Formula = C<sub>8</sub>H<sub>16</sub>O<sub>2</sub>\n" +
            "|   MolarMass = 144.21 g/mol\n" +
            "|   Density =\n" +
            "|   MeltingPt =\n" +
            "|   BoilingPt =\n" +
            "  }}\n" +
            "}}\n" +
            "\n" +
            "'''Manzanate''' is a [[flavor]] ingredient which has a fruity apple smell and with aspects of cider and sweet pineapple.<ref>[http://www.thegoodscentscompany.com/data/rw1006801.html Melon valerate]</ref>\n" +
            "\n" +
            "==References==\n" +
            "{{reflist}}\n" +
            "\n" +
            "[[Category:Carboxylate esters]]\n" +
            "\n" +
            "\n" +
            "{{ester-stub}}";

    public static final String[][] manzanateExpectedInfoboxValues = {
            {"verifiedrevid", "425210668"},
            {"Name","Ethyl 2-methylpentanoate"},
            {"Reference",""},
            {"ImageFile","Manzanate.png"},
            {"ImageSize", "200px"},
            {"ImageName",""},
            {"IUPACName","Ethyl 2-methylpentanoate"},
            {"OtherNames","Ethyl α-methylvalerate; Melon valerate"},
            {"CASNo_Ref","correct"}, //?????
            {"CASNo", "39255-32-8"},
            {"SMILES","CCCC(C)C(=O)OCC"},
            {"Formula","C<sub>8</sub>H<sub>16</sub>O<sub>2</sub>"},
            {"MolarMass", "144.21 g/mol"},
            {"Density",""},
            {"MeltingPt",""},
            {"BoilingPt",""}
    };


    public static final String[][] nedazosinExpectedInfoboxValues = {
            {"ImageFile", "Neldazosin.svg"},
            {"ImageSize","200px"},
            {"ImageAlt",""},
            {"IUPACName","1-[4-(4-Amino-6,7-dimethoxyquinazolin-2-yl)piperazin-1-yl]-3-hydroxybutan-1-one"},
            {"OtherNames", ""},
            {"CASNo","109713-79-3"},
            {"EINECS",""},
            {"EINECSCASNO",""},
            {"PubChem","65908"},
            {"ChemSpiderID", "59317"},
            {"InChI","1S/C18H25N5O4/c1-11(24)8-16(25)22-4-6-23(7-5-22)18-20-13-10-15(27-3)14(26-2)9-12(13)17(19)21-18/h9-11,24H,4-8H2,1-3H3,(H2,19,20,21)"},
            {"InChIKey","IOSMPEJNAQZKJT-UHFFFAOYSA-N"},
            {"SMILES", "CC(CC(=O)N1CCN(CC1)C2=NC3=CC(=C(C=C3C(=N2)N)OC)OC)O"},
            //Molecular formula?
            {"Appearance",""},
            {"Density",""},
            {"MeltingPt",""},
            {"BoilingPt",""},
            {"Solubility",""},
            {"MainHazards",""},
            {"FlashPt",""},
            {"Autoignition",""}
    };


    public static final String NELDAZOSIN_ARTICLE = "{{Chembox\n" +
            "| ImageFile = Neldazosin.svg\n" +
            "|  ImageSize = 200px\n" +
            "|  ImageAlt = \n" +
            "| IUPACName = 1-[4-(4-Amino-6,7-dimethoxyquinazolin-2-yl)piperazin-1-yl]-3-hydroxybutan-1-one\n" +
            "| OtherNames = \n" +
            "| Section1 = {{Chembox Identifiers\n" +
            "|  CASNo = 109713-79-3\n" +
            "|  EINECS = \n" +
            "|  EINECSCASNO = \n" +
            "|  PubChem = 65908\n" +
            "|  ChemSpiderID = 59317\n" +
            "|  InChI=1S/C18H25N5O4/c1-11(24)8-16(25)22-4-6-23(7-5-22)18-20-13-10-15(27-3)14(26-2)9-12(13)17(19)21-18/h9-11,24H,4-8H2,1-3H3,(H2,19,20,21)\n" +
            "|  InChIKey= IOSMPEJNAQZKJT-UHFFFAOYSA-N\n" +
            "|  SMILES = CC(CC(=O)N1CCN(CC1)C2=NC3=CC(=C(C=C3C(=N2)N)OC)OC)O}}\n" +
            "| Section2 = {{Chembox Properties\n" +
            "|  C=18|H=25|N=5|O=4\n" +
            "|  Appearance = \n" +
            "|  Density = \n" +
            "|  MeltingPt = \n" +
            "|  BoilingPt = \n" +
            "|  Solubility = }}\n" +
            "| Section3 = {{Chembox Hazards\n" +
            "|  MainHazards = \n" +
            "|  FlashPt = \n" +
            "|  Autoignition = }}\n" +
            "}}\n" +
            "\n" +
            "'''Neldazosin''' is an [[alpha adrenoreceptor antagonist]].<ref>{{cite journal|doi=10.1002/cbdv.200590100|title=Α1- andα2-Adrenoreceptor Antagonist Profiles of 1- and 2-\\ω-(4-Arylpiperazin-1-yl)alkyl]-1,2,3-benzotriazoles|year=2005|last1=Boido|first1=Alessandro|last2=Budriesi|first2=Roberta|last3=Boido|first3=Caterina Canu|last4=Ioan|first4=Pierfranco|last5=Terranova|first5=Emanuela|last6=Chiarini|first6=Alberto|last7=Sparatore|first7=Fabio|journal=Chemistry & Biodiversity|volume=2|issue=10|pages=1290}}</ref>\n" +
            "\n" +
            "==References==\n" +
            "{{reflist}}\n" +
            "\n" +
            "\n" +
            "{{organic-compound-stub}}\n" +
            "{{pharma-stub}}\n" +
            "\n" +
            "[[Category:Alpha blockers]]\n";

    public static final String MAJDINE_ARTICLE = "{{Chembox\n" +
            "| ImageFile = Majdine.png\n" +
            "|  ImageSize = 150px\n" +
            "| IUPACName = Methyl (19α,20α)-11,12-dimethoxy-19-methyl-2-oxoformosanan-16-carboxylate\n" +
            "| OtherNames = \n" +
            "| Section1 = {{Chembox Identifiers\n" +
            "|  CASNo = \n" +
            "|  PubChem = 21627963\n" +
            "|  ChemSpiderID = 10254858\n" +
            "|  ChEMBL = 2333535\n" +
            "|  SMILES = C[C@H]1[C@@H]2CN3CC[C@]4([C@@H]3C[C@@H]2C(=CO1)C(=O)OC)C5=C(C(=C(C=C5)OC)OC)NC4=O\n" +
            "|  InChI = 1/C23H28N2O6/c1-12-14-10-25-8-7-23(18(25)9-13(14)15(11-31-12)21(26)30-4)16-5-6-17(28-2)20(29-3)19(16)24-22(23)27/h5-6,11-14,18H,7-10H2,1-4H3,(H,24,27)/t12-,13-,14-,18-,23+/m0/s1\n" +
            "|  InChIKey = TTZWEOINXHJHCY-UHJVZONPBK\n" +
            "|  StdInChI = 1S/C23H28N2O6/c1-12-14-10-25-8-7-23(18(25)9-13(14)15(11-31-12)21(26)30-4)16-5-6-17(28-2)20(29-3)19(16)24-22(23)27/h5-6,11-14,18H,7-10H2,1-4H3,(H,24,27)/t12-,13-,14-,18-,23+/m0/s1\n" +
            "|  StdInChIKey = TTZWEOINXHJHCY-UHJVZONPSA-N\n" +
            "  }}\n" +
            "| Section2 = {{Chembox Properties\n" +
            "|  Formula = C<sub>23</sub>H<sub>28</sub>N<sub>2</sub>O<sub>6</sub>\n" +
            "|  MolarMass = 428.478\n" +
            "|  Appearance = \n" +
            "|  Density = \n" +
            "|  MeltingPt = \n" +
            "|  BoilingPt = \n" +
            "|  Solubility =\n" +
            "  }}\n" +
            "| Section3 = {{Chembox Hazards\n" +
            "|  MainHazards = \n" +
            "|  FlashPt = \n" +
            "|  Autoignition =\n" +
            "  }}\n" +
            "}}\n" +
            "'''Majdine''' is a bio-active isolate of ''[[Vinca minor]]'' and ''Vinca herbacea''.\n" +
            "\n" +
            "==External links==\n" +
            "* [http://www.ncbi.nlm.nih.gov/pubmed/21883037 Apoptotic, antioxidant and antiradical effects of majdine and isomajdine from Vinca herbacea Waldst. and kit]\n" +
            "\n" +
            "[[Category:Alkaloids found in Apocynaceae]]\n" +
            "\n" +
            "{{organic-chem-stub}}\n";
}
