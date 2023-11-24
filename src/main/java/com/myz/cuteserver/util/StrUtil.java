package com.myz.cuteserver.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: zhaomingyumt
 * @date: 2023/11/24 7:58 PM
 * @description:
 */
public class StrUtil {
    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static Map<String, Object> parseQueryString(String queryString, String enc) {
        Map<String, Object> paramsMap = new HashMap<>();
        if (queryString != null && queryString.length() > 0) {
            int ampersandIndex, lastAmpersandIndex = 0;
            String paramString;
            String paramName;
            String paramValue;
            String[] paramPair, values, newValues;
            do {
                ampersandIndex = queryString.indexOf('&', lastAmpersandIndex) + 1;
                if (ampersandIndex > 0) {
                    paramString = queryString.substring(lastAmpersandIndex, ampersandIndex - 1);
                    lastAmpersandIndex = ampersandIndex;
                } else {
                    paramString = queryString.substring(lastAmpersandIndex);
                }
                paramPair = paramString.split("=");
                paramName = paramPair[0];
                paramValue = paramPair.length == 1 ? "" : paramPair[1];
                try {
                    paramValue = URLDecoder.decode(paramValue, enc);
                } catch (UnsupportedEncodingException ignored) {
                }
                if (paramsMap.containsKey(paramName)) {
                    values = (String[]) paramsMap.get(paramName);
                    int len = values.length;
                    newValues = new String[len + 1];
                    System.arraycopy(values, 0, newValues, 0, len);
                    newValues[len] = paramValue;
                } else {
                    newValues = new String[]{paramValue};
                }
                paramsMap.put(paramName, newValues);
            } while (ampersandIndex > 0);
        }
        return paramsMap;
    }
}
