package com.example.sceneviewtest;

import java.util.HashMap;
import java.util.Map;

public final class Constants {

    public static final Map<String, String> API_LINKS = new HashMap<>();
    public static final String baseURL = "http://192.168.1.8/navigation_api/";
    public static final String markerLocation = "public/qr_codes/";

    static {
        API_LINKS.put("getPaths", baseURL + "api/get_path");
        API_LINKS.put("getLocations", baseURL + "api/get_locations");
    }
}
