/**
 Copyright (c) 2014 Alexander Savochkin
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

package net.chwise.websearch;

import org.json.JSONException;
import org.json.JSONObject;

class SearchFailureJSONResponse {

    private final static String SEVERITY_JSON_FIELD = "severity";
    private final static String MESSAGE_JSON_FIELD = "message";
    private final static String STRONG_JSON_FIELD = "strong";

    static JSONObject create (String severity, String strong, String message) {
        JSONObject jsonResponse = new JSONObject();
        try {
            jsonResponse.put(SEVERITY_JSON_FIELD, severity );
            jsonResponse.put(STRONG_JSON_FIELD, strong );
            jsonResponse.put(MESSAGE_JSON_FIELD, message );
        } catch (JSONException e) {
            throw new RuntimeException("Couldn't create alert notification message in JSON forma", e);
        }
        return jsonResponse;
    }
}
