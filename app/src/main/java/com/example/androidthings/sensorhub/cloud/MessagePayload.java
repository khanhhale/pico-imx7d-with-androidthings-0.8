/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androidthings.sensorhub.cloud;

import com.example.androidthings.sensorhub.SensorData;
import com.example.androidthings.sensorhub.cloud.cloudiot.CloudIotOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Properties;
import java.net.InetAddress;
import java.util.List;
import android.os.Build;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class handles the serialization of the SensorData objects into a String
 */
public class MessagePayload {

    /**
     * Serialize a List of SensorData objects into a JSON string, for sending to the cloud
     * @param
     * @return JSON String
     */
    public static String getSystemProp(String key) {
        String value = null;

        try {
            value = (String) Class.forName("android.os.SystemProperties").getMethod("get", String.class).invoke(null, key);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value;
    }
    public static String createMessagePayload(List<SensorData> data, CloudIotOptions cloudIotOptions, int index) {
        try {

            JSONObject messagePayload = new JSONObject();
            JSONArray dataArray = new JSONArray();
            for (SensorData el : data) {
                JSONObject sensor = new JSONObject();
                sensor.put(el.getSensorName(), el.getValue());
                sensor.put("timestamp_" + el.getSensorName(),
                        el.getTimestamp());
                dataArray.put(sensor);
            }

            messagePayload.put("data", dataArray);


            String hostName = "Unknown";
            try {
                hostName = InetAddress.getLocalHost().getHostName();
            } catch (Exception ex) {
                hostName = null;
            }


            Properties props = System.getProperties();

            SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSS");
            String timestamp = dateformat.format(new Date());

            messagePayload.put("timestamp", timestamp);
            messagePayload.put("index", index);
            messagePayload.put("project", cloudIotOptions.getProjectId());
            messagePayload.put("registry", cloudIotOptions.getRegistryId());
            messagePayload.put("devicename", cloudIotOptions.getDeviceId());
            messagePayload.put("hostname", getSystemProp("ro.vendor.product.device")); //ro.vendor.product.device]
            messagePayload.put("machine", props.getProperty("os.arch")); //architecture
            messagePayload.put("processor", getSystemProp("ro.product.cpu.abi")); //processor
            messagePayload.put("platform", "Android" + "-" + props.getProperty("os.arch") + "-" + getSystemProp("ro.product.cpu.abi") + "-with-" + getSystemProp("ro.vendor.product.brand")); //os with architecture and os variant
            messagePayload.put("system", "Android " + getSystemProp("ro.vendor.product.brand")); //os name, "android" + ro.vendor.product.brand
            messagePayload.put("version", getSystemProp("ro.build.version.iot"));
            messagePayload.put("release", getSystemProp("ro.build.version.release")); //Build.VERSION.RELEASE

            return messagePayload.toString();

        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid message");
        }
    }

    public static String createMessagePayloadForState(List<SensorData> data) {
        try {

            JSONObject messagePayload = new JSONObject();
            JSONArray dataArray = new JSONArray();
            for (SensorData el : data) {
                JSONObject sensor = new JSONObject();
                sensor.put(el.getSensorName(), el.getValue());
                sensor.put("timestamp_" + el.getSensorName(),
                    el.getTimestamp());
                dataArray.put(sensor);
            }

            messagePayload.put("version", 1);
            messagePayload.put("telemetry-events-per-hour", 20);
            messagePayload.put("state-updates-per-hour", 30);
            messagePayload.put("sensors", dataArray);
            messagePayload.put("active-sensors", dataArray);

            return messagePayload.toString();
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid message");
        }
    }
}
