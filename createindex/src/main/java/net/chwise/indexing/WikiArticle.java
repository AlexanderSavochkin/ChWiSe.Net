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

package net.chwise.indexing;

import net.chwise.common.conversion.ToMOLConverter;
import net.chwise.common.document.DocDefinitions;
import net.chwise.dataextraction.InfoboxDataProcessor;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;

import java.util.Map;

public class WikiArticle {

    String pageId;
    String title;
    String text;
    String smiles;
    String mdlmol;
    Map<String, String> infoboxFields;

    InfoboxDataProcessor infoboxProcessor;
    ToMOLConverter toMOLConverter = new ToMOLConverter();

    WikiArticle(String pageId,
                String title,
                String text,
                String smiles,
                Map<String, String> infoboxFields,
                InfoboxDataProcessor infoboxProcessor) {
        this.pageId = pageId;
        this.title = title;
        this.text = text;
        this.smiles = smiles;
        this.infoboxFields = infoboxFields;
        this.infoboxProcessor = infoboxProcessor;
    }

    Document getLuceneDocument() {
        //Create lucene document
        Document document = new Document();
        document.add( new TextField( DocDefinitions.TITLE_FIELD_NAME, title, Field.Store.YES ) );
        document.add( new TextField( DocDefinitions.TEXT_FIELD_NAME, text, Field.Store.YES ) );
        document.add( new TextField( DocDefinitions.STRUCTURE_SMILES_FIELD_NAME, smiles, Field.Store.YES ) );
        //document.add( new TextField( DocDefinitions.URL_FIELD_NAME, "#", Field.Store.YES ) );
        document.add( new StoredField( DocDefinitions.STRUCTURE_MOL_FIELD_NAME, toMOLConverter.MOLChargesKludge(toMOLConverter.convert(smiles)) ) );
        infoboxProcessor.process(infoboxFields, document);
        return document;
    }

}
