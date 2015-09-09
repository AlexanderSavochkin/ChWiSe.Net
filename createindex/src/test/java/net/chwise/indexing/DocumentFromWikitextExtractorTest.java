package net.chwise.indexing;

import net.chwise.common.document.DocDefinitions;
import net.chwise.dataextraction.SimpleFieldToFieldProcessor;
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
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.IOException;

import static net.chwise.test.TestData.MAJDINE_ARTICLE;
import static net.chwise.test.TestData.NELDAZOSIN_ARTICLE;
import static org.junit.Assert.assertTrue;

public class DocumentFromWikitextExtractorTest {
    public void testCompoundIndexAndSearch(String t/*whatever non-empty string*/,
                                           String compoundName,
                                           String smiles,
                                           String artikleWikiText,
                                           String querystr) throws LinkTargetException, EngineException {
        Directory directory = null;
        IndexWriter indexWriter = null;

        DocumentFromWikitextExtractor documentFromWikitextExtractor = new DocumentFromWikitextExtractor();
        SimpleFieldToFieldProcessor simpleFieldToFieldProcessor = new SimpleFieldToFieldProcessor();
        WikiArticle wikiArticle = documentFromWikitextExtractor.getArticle(simpleFieldToFieldProcessor, t, compoundName, smiles, artikleWikiText);

        try {
            directory = new RAMDirectory();
            //directory = new SimpleFSDirectory( new File("D:\\work\\ttt\\") );  //For index structure debugging
            IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, DocDefinitions.getAnalyzer() );
            indexWriter = new IndexWriter( directory, config);
            indexWriter.addDocument(wikiArticle.getLuceneDocument());
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

    @Test
    public void testNedazoinIndexAndSearchByCASNo() throws LinkTargetException, EngineException {
        testCompoundIndexAndSearch("z","Neldazosin", "CC(CC(=O)N1CCN(CC1)C2=NC3=CC(=C(C=C3C(=N2)N)OC)OC)O", NELDAZOSIN_ARTICLE, "cas:109713-79-3");
    }

    @Test
    public void testMajdineIndexAndSearchByCASNo() throws LinkTargetException, EngineException {
        testCompoundIndexAndSearch("z","Majdine", "C[C@H]1[C@@H]2CN3CC[C@]4([C@@H]3C[C@@H]2C(=CO1)C(=O)OC)C5=C(C(=C(C=C5)OC)OC)NC4=O", MAJDINE_ARTICLE, "pubchem:21627963");
    }

}
