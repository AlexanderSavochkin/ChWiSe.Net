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

package net.chwise.websearch.jsonmessages;

import net.chwise.spellcheck.Correction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class SpellCorrectionsJSONResponse {

    private final static String SPELLCORRECTIONS_JSON_FIELD = "spellcorrections";
    public static final String SPELLCORRECTION_JSON_TERM_FIELD = "term";
    public static final String SPELLCORRECTION_JSON_QUERY_FIELD = "query";

    public static JSONObject create (Map<String, Correction[]>  corrections) {
        JSONObject jsonResponse = new JSONObject();
        try {
            JSONObject jsonCorrections = new JSONObject();
            for (Map.Entry<String, Correction[]> entry : corrections.entrySet()) {
                JSONArray jsonCorrectionsArray = new JSONArray();
                for (Correction correction : entry.getValue()) {
                    JSONObject jsonCorrection = new JSONObject();
                    jsonCorrection.put(SPELLCORRECTION_JSON_TERM_FIELD, correction.fixedTerm);
                    jsonCorrection.put(SPELLCORRECTION_JSON_QUERY_FIELD, correction.fixedQuery);
                    jsonCorrectionsArray.put(jsonCorrection);
                }
                jsonCorrections.put(entry.getKey(), jsonCorrectionsArray);
            }
            jsonResponse.put(SPELLCORRECTIONS_JSON_FIELD, jsonCorrections );
        } catch (JSONException e) {
            throw new RuntimeException("Couldn't create alert notification message in JSON forma", e);
        }
        return jsonResponse;
    }
}