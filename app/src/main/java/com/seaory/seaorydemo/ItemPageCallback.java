package com.seaory.seaorydemo;

import org.json.JSONObject;

public interface ItemPageCallback {
    void OnItemPageMessage(int state, String result);
    void OnItemPageAddCommand(JSONObject jsonObject);
}
