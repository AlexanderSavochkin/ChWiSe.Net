package net.chwise.indexing;

import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class App 
{
    private static void printUsage() {
        System.err.println("Usage: java -jar createindex.jar <Path to files> <Path to index>");
    }

    public static void main( String[] args ) throws IOException {
        if (args.length != 2) {
            printUsage();
            return;
        }
        String startFrom = args[0];
        String indexPath = args[1];

        Directory directory = new SimpleFSDirectory( new File(indexPath) );
        Analyzer analyzer = DocDefinitions.getAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
        IndexWriter indexWriter = new IndexWriter( directory, config);
        FileVisitor<Path> fileIndexer = new FileIndexer( indexWriter );
        Files.walkFileTree(Paths.get(startFrom), fileIndexer);
        indexWriter.close();
        System.err.println("Finished!");
    }
}
