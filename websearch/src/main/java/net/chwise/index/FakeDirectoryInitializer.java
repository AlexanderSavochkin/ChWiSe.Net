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

package net.chwise.index;

import net.chwise.common.conversion.ToMOLConverter;
import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;

public class FakeDirectoryInitializer implements IDirectoryInitializer {

    @Override
    public Directory getDirectory() {
        Directory directory = null;
        IndexWriter indexWriter = null;
        try {
            directory = new RAMDirectory();
            Analyzer analyzer = DocDefinitions.getAnalyzer();
            Document[] fakeDocs = getFakeDocuments();
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);

            indexWriter = new IndexWriter( directory, config);
            for (Document doc: fakeDocs) {
                indexWriter.addDocument(doc);
            }

        } catch (IOException e) {
            throw new RuntimeException( "Exception during creatingi fake index.", e );
        } finally {
            if (indexWriter != null)
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException( "Exception during closing index.", e );
            }
        }
        return directory;
    }

    @Override
    public void setParameters(String params) {
        //Just ignore params
    }

    private static Document[] getFakeDocuments() {
        Document[] documents = {getButhaneDocument(), getAceticAcidDocument(),getBenzeneDocument(),
                getTolueneDocument(), getPhenolDocument(), getTrinitrotolueneDocument(),
                getIndoleDocument(), getTryptophanDocument()  };

        return documents;
    }


//**************************************************************
//              T E S T S (T O  B E  R E M O V E D)
//**************************************************************
    final static String buthaneDescription = "Normal buthane. Alkane with 4 carbon atoms in. Unbranched isomer. Gas. at room temperature and atmospheric pressure." +
        "Butane is an organic compound with the formula C4H10 that is an alkane with four carbon atoms. Butane is a gas at room temperature and atmospheric pressure. The term may refer to either of two structural isomers, n-butane or isobutane (or \"methylpropane\"), or to a mixture of these isomers. In the IUPAC nomenclature, however, \"butane\" refers only to the n-butane isomer (which is the isomer with the unbranched structure). Butanes are highly flammable, colorless, easily liquefied gases. The name butane comes from the roots but- (from butyric acid) and -ane." +
        "Normal butane is mainly used for gasoline blending, as a fuel gas, either alone or in a mixture with propane, and as a feedstock for the manufacture of ethylene and butadiene, a key ingredient of synthetic rubber. Isobutane is primarily used by refineries to enhance the octane content of motor gasoline.[5][6][7][8]\n" +
        "\n" +
        "When blended with propane and other hydrocarbons, it is referred to commercially as LPG, for liquified petroleum gas. It is used as a petrol component, as a feedstock for the production of base petrochemicals in steam cracking, as fuel for cigarette lighters and as a propellant in aerosol sprays such as deodorants. In addition, butane acts as a blending agent for gasoline at varying levels throughout the year.[9]\n" +
        "\n" +
        "Very pure forms of butane, especially isobutane, can be used as refrigerants and have largely replaced the ozone layer-depleting halomethanes, for instance in household refrigerators and freezers. The system operating pressure for butane is lower than for the halomethanes, such as R-12, so R-12 systems such as in automotive air conditioning systems, when converted to butane will not function optimally.\n" +
        "\n" +
        "Butane is also used as lighter fuel for a common lighter or butane torch and is sold bottled as a fuel for cooking and camping.\n" +
        "\n" +
        "Cordless hair irons are usually powered by butane cartridges.";
    final static String buthaneSMILES = "CCCC";

    final static String aceticAcidDescription = "Acetic acid is one of the simplest carboxylic acids. Liquid.";
    final static String aceticAcidSMILES = "CC(O)=O";

    final static String benzeneDescription = "It is an aromatic hydrocarbon. A cyclic hydrocarbon with a continuous pi bond.";
    final static String benzeneSMILES = "c1ccccc1";

    final static String tolueneDescription = "It is an aromatic hydrocarbon. It is a mono-substituted benzene derivative. Methylbenzene.";
    final static String tolueneSMILES = "Cc1ccccc1";

    final static String phenolDescription = "An aromatic organic compound. The molecule consists of a phenyl group (-C6H5) bonded to a hydroxyl group (-OH).";
    final static String phenolSMILES = "c1ccc(cc1)O";

    final static String trinitrotolueneDescription = "TNT. Common expolsive. Nitrated toluene.";
    final static String trinitrotolueneSMILES = "O=[N+]([O-])c1c(c(ccc1C)[N+]([O-])=O)[N+]([O-])=O";

    final static String indoleDescription = "An aromatic heterocyclic organic compound. Indole is a common component of fragrances and the precursor to many pharmaceuticals.";
    final static String indoleSMILES = "c1ccc2c(c1)cc[nH]2";

    final static String tryptophanDescription="Essential amino acid. It is encoded in the standard genetic code as the codon UGG.";
    final static String tryptophanSMILES="c1ccc2c(c1)c(c[nH]2)C[C@@H](C(=O)O)N";

    static ToMOLConverter toMOLConverter = new ToMOLConverter();

    static Document getButhaneDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "buthane", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, buthaneDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, buthaneSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(buthaneSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getAceticAcidDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "acetic acid", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, aceticAcidDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, aceticAcidSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(aceticAcidSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getBenzeneDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "benzene", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, benzeneDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, benzeneSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(benzeneSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getTolueneDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "toluene", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, tolueneDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, tolueneSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(tolueneSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getPhenolDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "phenol", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, phenolDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, phenolSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(phenolSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getTrinitrotolueneDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "trinitrotoluene", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, trinitrotolueneDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, trinitrotolueneSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(trinitrotolueneSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getIndoleDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "indole", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, indoleDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, indoleSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(indoleSMILES), Field.Store.YES ) );
        return doc;
    }

    static Document getTryptophanDocument() {
        Document doc = new Document();
        doc.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, "tryptophan", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, tryptophanDescription, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, tryptophanSMILES, Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        doc.add( new TextField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.convert(tryptophanSMILES), Field.Store.YES ) );
        return doc;
    }

}
