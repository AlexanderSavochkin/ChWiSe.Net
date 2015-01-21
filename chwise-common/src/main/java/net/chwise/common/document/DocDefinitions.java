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

package net.chwise.common.document;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.molecularlucene.tokenizer.SmilesAnalyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DocDefinitions {

    public static final String TITLE_FIELD_NAME = "title";
    public static final String STRUCTURE_SMILES_FIELD_NAME = "smiles";
    public static final String TEXT_FIELD_NAME = "text";
    public static final String URL_FIELD_NAME = "url";
    public static final String STRUCTURE_MOL_FIELD_NAME = "mdlmol";
    public static final String SYNONYM_FIELD_NAME = "synonym";
    public static final String EXTERNAL_ID = "externalid";

    private static String[] textFields = null;
    private static Map<String, Float> fieldWeights = null;

    public static Analyzer getAnalyzer() {
        Map<String,Analyzer> analyzerPerField = new HashMap<String,Analyzer>();
        Analyzer smilesAnalyzer = new SmilesAnalyzer();
        Analyzer keywordAnalyzer = new KeywordAnalyzer();
        analyzerPerField.put(STRUCTURE_SMILES_FIELD_NAME, smilesAnalyzer );
        analyzerPerField.put(EXTERNAL_ID, keywordAnalyzer );
        PerFieldAnalyzerWrapper analyzerWrapper =
                new PerFieldAnalyzerWrapper(new StandardAnalyzer(Version.LUCENE_43), analyzerPerField);
        return analyzerWrapper;
    }

    public static String[] getTextFields() {
        if (textFields == null)
            textFields = new String[]{TITLE_FIELD_NAME, TEXT_FIELD_NAME, SYNONYM_FIELD_NAME, EXTERNAL_ID};
        return textFields;
    }

    public static Map<String, Float> getFieldWeights() {
        if (fieldWeights == null) {
            fieldWeights = new TreeMap<String, Float>();
            fieldWeights.put(TITLE_FIELD_NAME, 10.0f);
            fieldWeights.put(TEXT_FIELD_NAME, 1.0f);
            fieldWeights.put(SYNONYM_FIELD_NAME, 5.0f);
            fieldWeights.put(EXTERNAL_ID, 10.0f);
        }
        return fieldWeights;
    }
}
