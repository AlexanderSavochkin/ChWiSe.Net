package net.chwise.dataextraction;

import net.chwise.common.document.DocDefinitions;
import net.chwise.indexing.FileIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Test;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.*;

public class InfoBoxDataExtractorTest {

    private final String MANZANATE_ARTICLE = "Manzanate\n" +
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

    private final String[][] manzanateExpectedInfoboxValues = {
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


    private final String[][] nedazosinExpectedInfoboxValues = {
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


    private final String NELDAZOSIN_ARTICLE = "{{Chembox\n" +
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

    private boolean testArticleAndValue(String article, String[][] infoboxValues)
            throws EngineException, LinkTargetException {
        WikiConfig config = DefaultConfigEnWp.generate();

        // Instantiate a compiler for wiki pages
        WtEngineImpl engine = new WtEngineImpl(config);

        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, "manzanate");

        PageId pageId = new PageId(pageTitle, -1);

        // Compile the retrieved page
        EngProcessedPage cp = engine.postprocess(pageId, article, null);

        InfoBoxDataExtractor p = new InfoBoxDataExtractor();
        Map<String, String> infoBoxFields = (Map<String, String>) p.go(cp.getPage());

        for (String[] expectedInfoboxKeyValue : infoboxValues) {
            String key = expectedInfoboxKeyValue[0];
            String val = expectedInfoboxKeyValue[1];
            if( !infoBoxFields.containsKey(key) )
                return false;
            String actualValue = infoBoxFields.get(key);
            if( !val.equals(actualValue) )
                return false;
        }
        return true;
    }

    @Test
    public void testManzanateChemBoxExtraction() throws LinkTargetException, EngineException {
        assertTrue( testArticleAndValue(MANZANATE_ARTICLE, manzanateExpectedInfoboxValues) );
    }

    @Test
    public void testNedazoinChemBoxExtraction() throws LinkTargetException, EngineException {
        assertTrue( testArticleAndValue(NELDAZOSIN_ARTICLE, nedazosinExpectedInfoboxValues) );
    }

    @Test
    public void testNedazoinIndexAndSearchByCASNo() throws LinkTargetException, EngineException {

        Directory directory = null;
        IndexWriter indexWriter = null;

        FileIndexer fileIndexer = new FileIndexer(null);
        SimpleFieldToFieldProcessor simpleFieldToFieldProcessor = new SimpleFieldToFieldProcessor();
        Document doc = fileIndexer.getLuceneDocument(simpleFieldToFieldProcessor, "z", "Neldazosin", "CC(CC(=O)N1CCN(CC1)C2=NC3=CC(=C(C=C3C(=N2)N)OC)OC)O", NELDAZOSIN_ARTICLE);

        try {
            directory = new RAMDirectory();
            //directory = new SimpleFSDirectory( new File("D:\\work\\ttt\\") );  //For index structure debugging
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, DocDefinitions.getAnalyzer() );
            indexWriter = new IndexWriter( directory, config);
            indexWriter.addDocument(doc);
        } catch (IOException e) {
            throw new RuntimeException( "Exception during creating index.", e );
        } finally {
            if (indexWriter != null)
                try {
                    indexWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException( "Exception during closing index.", e );
                }
        }

        IndexReader reader = null;
        try {
            reader = IndexReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            String querystr = "externalid:109713-79-3";
            Query q = null;
            q = new QueryParser(Version.LUCENE_43, "", DocDefinitions.getAnalyzer()).parse(querystr);

            TopScoreDocCollector collector = TopScoreDocCollector.create(5, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            assertTrue( hits.length > 0 );

        } catch (Exception e) {
            throw new RuntimeException( "Exception during parsing query.", e );
        }

        return;
    }

}