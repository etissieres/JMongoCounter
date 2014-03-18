package org.mansart.mongocount.util;

import org.json.JSONException;
import org.json.JSONObject;

public final class JSONUtils {

    public static boolean isValid(String json) {
        try {
            new JSONObject(json);
            return true;
        } catch (JSONException e) {
            return false;
        }
    }
}
