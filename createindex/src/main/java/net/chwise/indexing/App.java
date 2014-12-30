/**
 Copyright (c) 2013-2014 Alexander Savochkin
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

package net.chwise.indexing;

import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;

import java.io.*;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class App 
{
    private static void printUsage() {
        System.err.println("Usage: java -jar createindex.jar <Path to files> <Path to index> [Path to infobox statistics]");
    }

    static void writeInfoboxesStatToFile(String filename, Map<String, Integer> stat) throws IOException {
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filename))) ) {
            for (Map.Entry<String, Integer> entry : stat.entrySet()) {
                writer.format("%s %d\n", entry.getKey(), entry.getValue() );
            }
        }
    }

    public static void main( String[] args ) throws IOException {
        if (args.length < 2 || args.length > 3) {
            printUsage();
            return;
        }
        boolean needInfoboxStatistics = args.length == 3;
        String startFrom = args[0];
        String indexPath = args[1];

        Directory directory = new SimpleFSDirectory( new File(indexPath) );
        Analyzer analyzer = DocDefinitions.getAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_43, analyzer);
        IndexWriter indexWriter = new IndexWriter( directory, config);
        FileIndexer fileIndexer = new FileIndexer( indexWriter, needInfoboxStatistics );
        Files.walkFileTree(Paths.get(startFrom), fileIndexer);
        indexWriter.close();
        if (needInfoboxStatistics)
            writeInfoboxesStatToFile(args[2], fileIndexer.getInfoboxKeysStatistics() );
        System.err.println("Finished!");
    }
}
