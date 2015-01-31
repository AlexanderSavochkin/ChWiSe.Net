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

package net.chwise.dataextraction;

import net.chwise.common.document.DocDefinitions;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import java.util.Map;

public class SimpleFieldToFieldProcessor implements InfoboxDataProcessor {

    private static String[][] arrInfoboxFieldToDocField = {
        {"Name", DocDefinitions.SYNONYM_FIELD_NAME},
        {"IUPACName", DocDefinitions.SYNONYM_FIELD_NAME},
        {"OtherNames", DocDefinitions.SYNONYM_FIELD_NAME},
        {"CASNo", DocDefinitions.CAS_NO},
        {"CAS", DocDefinitions.CAS_NO},
        {"PubChem", DocDefinitions.PUBCHEM_ID},
        {"ChemSpider", DocDefinitions.CHEMSPIDER},
        {"ChEBI", DocDefinitions.CHEBI}
    };

    public void process( Map<String, String> infoboxContent, Document doc) {
        for (String[] fieldToField: arrInfoboxFieldToDocField) {
            String infoBoxValue = infoboxContent.get(fieldToField[0]);
            if (infoBoxValue != null && !infoBoxValue.isEmpty())
                doc.add( new TextField( fieldToField[1], infoBoxValue, Field.Store.YES ) );
        }
    }
}