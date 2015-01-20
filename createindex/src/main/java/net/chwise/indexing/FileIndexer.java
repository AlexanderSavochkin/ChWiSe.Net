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

import net.chwise.common.conversion.ToMOLConverter;
import net.chwise.common.document.DocDefinitions;
import net.chwise.dataextraction.InfoBoxDataExtractor;
import net.chwise.dataextraction.SimpleFieldToFieldProcessor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.sweble.wikitext.engine.EngineException;
import org.sweble.wikitext.engine.PageId;
import org.sweble.wikitext.engine.PageTitle;
import org.sweble.wikitext.engine.WtEngineImpl;
import org.sweble.wikitext.engine.config.WikiConfig;
import org.sweble.wikitext.engine.nodes.EngProcessedPage;
import org.sweble.wikitext.engine.utils.DefaultConfigEnWp;
import org.sweble.wikitext.parser.parser.LinkTargetException;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;


import net.chwise.dataextraction.InfoBoxDataExtractor;

public class FileIndexer extends SimpleFileVisitor<Path> {

    IndexWriter indexWriter;
    ToMOLConverter toMOLConverter = new ToMOLConverter();

    // Set-up a simple wiki configuration
    WikiConfig config = DefaultConfigEnWp.generate();
    final int wrapCol = 80;
    // Instantiate a compiler for wiki pages
    WtEngineImpl engine = new WtEngineImpl(config);

    //private static Logger logger = Logger.getLogger( FileIndexer.class.getName() );
    Map<String, Integer> infoboxKeysCout = new HashMap<String, Integer>();

    boolean calculateStatistics =  false;


    FileIndexer(IndexWriter indexWriter) {
        this.indexWriter = indexWriter;
    }

    FileIndexer(IndexWriter indexWriter, boolean calculateStatistics) {
        this.indexWriter = indexWriter;
        this.calculateStatistics = calculateStatistics;
    }


    @Override
    public FileVisitResult visitFile(
            Path aFile, BasicFileAttributes aAttrs
    )  {
        //logger.info( "Processing file: " + aFile.toString() );
        System.err.println("Processing file: " + aFile.toString() );
        try {
            Document doc = readFile( aFile );
            indexWriter.addDocument(doc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult preVisitDirectory(
            Path aDir, BasicFileAttributes aAttrs
    ) throws IOException {
        return FileVisitResult.CONTINUE;
    }

    Document readFile( Path path ) throws IOException, LinkTargetException, EngineException {

        SimpleFieldToFieldProcessor simpleFieldToFieldProcessor = new SimpleFieldToFieldProcessor();

        String pathStr = path.toString();
        try (BufferedReader br = new BufferedReader(new FileReader( pathStr )))  {
            //First line is compound name
            String title = br.readLine();

            //Second line is smiles
            String smiles = br.readLine();

            //Rest of the file is our content, or "text" field
            StringBuffer sb = new StringBuffer();
            String line = br.readLine();
            while (line != null) {
                sb.append( line );
                sb.append('\n');
                line = br.readLine();
            }
            String wikitext = sb.toString();

            // Retrieve a page
            PageTitle pageTitle = PageTitle.make(config, pathStr);

            PageId pageId = new PageId(pageTitle, -1);

            // Compile the retrieved page
            EngProcessedPage cp = engine.postprocess(pageId, wikitext, null);

            //Check chembox in the begining

            TextConverter markupStripConverter = new TextConverter(config, wrapCol);
            String text = (String) markupStripConverter.go(cp.getPage());

            InfoBoxDataExtractor infoBoxDataExtractor = new InfoBoxDataExtractor();
            Map<String, String> infoboxFields = (Map<String, String>) infoBoxDataExtractor.go(cp.getPage());

            //Update statistics if required
            if (calculateStatistics) {
                for (String key : infoboxFields.keySet()) {
                    int count = infoboxKeysCout.containsKey(key) ? infoboxKeysCout.get(key) : 0;
                    infoboxKeysCout.put(key, count + 1);
                }
            }

            //Create lucene document
            Document document = new Document();
            document.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, title, Field.Store.YES ) );
            document.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, text, Field.Store.YES ) );
            document.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, smiles, Field.Store.YES ) );
            document.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
            document.add( new StoredField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.MOLChargesKludge(toMOLConverter.convert(smiles)) ) );

            simpleFieldToFieldProcessor.process(infoboxFields, document);

            return document;
        }
    }

    public Map<String, Integer> getInfoboxKeysStatistics() {
        return infoboxKeysCout;
    }
}


