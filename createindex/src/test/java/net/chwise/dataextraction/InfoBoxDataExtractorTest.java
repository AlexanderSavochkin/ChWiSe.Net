package net.chwise.dataextraction;

import org.junit.Test;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

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

    String[][] expectedInfoboxValues = {
            {},
            {}
    };

    @Test
    public void testChemBoxExtraction() throws EngineException, LinkTargetException {
        WikiConfig config = DefaultConfigEnWp.generate();

        // Instantiate a compiler for wiki pages
        WtEngineImpl engine = new WtEngineImpl(config);

        // Retrieve a page
        PageTitle pageTitle = PageTitle.make(config, "manzanate");

        PageId pageId = new PageId(pageTitle, -1);

        // Compile the retrieved page
        EngProcessedPage cp = engine.postprocess(pageId, MANZANATE_ARTICLE, null);

        //Check chembox in the begining

        InfoBoxDataExtractor p = new InfoBoxDataExtractor();
        Map<String, String> infoBoxFields = (Map<String, String>) p.go(cp.getPage());

        System.out.print( infoBoxFields.toString() );

    }
}