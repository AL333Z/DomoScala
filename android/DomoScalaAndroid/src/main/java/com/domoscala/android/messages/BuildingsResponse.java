package com.domoscala.android.messages;

/**
 * This class is used by GSON to deserialize the JSON received from the server
 */
public class BuildingsResponse {
    public String status;
    public Building[] buildings;
}
