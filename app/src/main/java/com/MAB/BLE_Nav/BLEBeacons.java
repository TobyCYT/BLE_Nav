package com.MAB.BLE_Nav;

import android.bluetooth.le.ScanResult;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Calendar;

public class BLEBeacons {
    String name;
    String macAddress;
    String filter_mode = "mean";
    String operatingMode = "arrival";// arrival and signage
    Double heading = null;// heading for +y or arrival heading
    String[] Signage = new String[4];// [0] +y, [1] -y, [2] +x, [3] -x

    //signals variable
    CustomQueue RSSI;
    Integer latest_RSSI = null;
    Double filtered_RSSI = null;
    Double prev_filtered_RSSI = null;

    Double threshold;
    long timeOfLastRSSI = Calendar.getInstance().getTime().getTime() - 10000; // Just to initialize to 10 Seconds earlier
    long timeDifferenceSinceLastRSSI;
    Long lastSpoke = Calendar.getInstance().getTime().getTime() - 10000;

    TextToSpeech textToSpeech;

    public void setFilter_mode(String filter_mode) {
        this.filter_mode = filter_mode;
    }

    public void setOperatingMode(String operatingMode) {
        if (operatingMode.equals("arrival") || operatingMode.equals("signage"))
            this.operatingMode = operatingMode;
        else
            return;
    }

    public Double getFiltered_RSSI() {
        if (timeDifferenceSinceLastRSSI > 5000){
            RSSI.clearQueue();
            if (filter_mode.equals("mean")){
                filtered_RSSI = RSSI.filtered("mean");
            }else if(filter_mode.equals("kalman")){
                filtered_RSSI = RSSI.filtered("kalman");
            }
        }
        return filtered_RSSI;
    }

    BLEBeacons(String name, String addr, Double threshold, Double heading, TextToSpeech textToSpeech){
        this.name = name;
        macAddress = addr;
        this.threshold = threshold;
        this.heading = heading;
        this.textToSpeech = textToSpeech;
        RSSI = new CustomQueue(5);
    }

    BLEBeacons(String name, String addr, Double threshold, Double heading, String[] signage, TextToSpeech textToSpeech){
        this.name = name;
        this.macAddress = addr;
        this.operatingMode = "signage";
        this.threshold = threshold;
        this.heading = heading;
        this.Signage = signage;
        this.textToSpeech = textToSpeech;
        RSSI = new CustomQueue(5);
    }

    public void update(ScanResult result, Double heading){
        try {
            if(result.getDevice().getName() != null) {
                if (!result.getDevice().getAddress().equals(macAddress)) {
                    return;
                }
            }else return;
        }catch (SecurityException e){
            Log.v("error", "Fail to get device names.");
        }
        if (filtered_RSSI != null) prev_filtered_RSSI = filtered_RSSI;

        // Calculate time difference between this iteration and last iteration
        timeDifferenceSinceLastRSSI = Calendar.getInstance().getTime().getTime() - timeOfLastRSSI;

        // Clear queue if difference is more than 5 seconds since last received signal
        if (timeDifferenceSinceLastRSSI > 5000){
            RSSI.clearQueue();
        }

        // Add latest RSSI to queue for processing
        latest_RSSI = result.getRssi();
        RSSI.queueEnqueue(latest_RSSI);

        if (filter_mode.equals("mean")){
            filtered_RSSI = RSSI.filtered("mean");
        }else if(filter_mode.equals("kalman")){
            filtered_RSSI = RSSI.filtered("kalman");
        }

        Long now = Calendar.getInstance().getTime().getTime();
        // What to do when user get close to beacon
        if (operatingMode.equals("arrival")){
            if (filtered_RSSI >= threshold && now - lastSpoke > 5000){
                if (this.heading == null) {
                    textToSpeech.speak("You have arrived at " + name, TextToSpeech.QUEUE_FLUSH, null, null);
                    lastSpoke = Calendar.getInstance().getTime().getTime();
                }else{
                    Double headingMin = this.heading - 45;
                    Double headingMax = this.heading + 45;
                    if (headingMin < 0) headingMin += 360;
                    if (headingMin >= 360) headingMin -= 360;
                    if (headingMax < 0) headingMax += 360;
                    if (headingMax >= 360) headingMax -= 360;
                    if (compareAngle(headingMin, headingMax, heading)){
                        textToSpeech.speak("You have arrived at " + name, TextToSpeech.QUEUE_FLUSH, null, null);
                        lastSpoke = Calendar.getInstance().getTime().getTime();
                    }
                }
            }
        }else if(operatingMode.equals("signage")){
            String facing = null;
            //check if facing positive y
            Double headingMin = this.heading - 45;
            Double headingMax = this.heading + 45;
            if (headingMin < 0) headingMin += 360;
            if (headingMin >= 360) headingMin -= 360;
            if (headingMax < 0) headingMax += 360;
            if (headingMax >= 360) headingMax -= 360;
            if (compareAngle(headingMin, headingMax, heading)) facing = "positive y";

            //check if facing negative y
            headingMin = this.heading - 45 + 180;// +180 degrees to flip
            headingMax = this.heading + 45 + 180;
            if (headingMin < 0) headingMin += 360;
            if (headingMin >= 360) headingMin -= 360;
            if (headingMax < 0) headingMax += 360;
            if (headingMax >= 360) headingMax -= 360;
            if (compareAngle(headingMin, headingMax, heading)) facing = "negative y";

            //check if facing positive x
            headingMin = this.heading - 45 + 90;// +90 degrees for positive x
            headingMax = this.heading + 45 + 90;
            if (headingMin < 0) headingMin += 360;
            if (headingMin >= 360) headingMin -= 360;
            if (headingMax < 0) headingMax += 360;
            if (headingMax >= 360) headingMax -= 360;
            if (compareAngle(headingMin, headingMax, heading)) facing = "positive x";

            //check if facing negative y
            headingMin = this.heading - 45 - 90;// -90 degrees for negative x
            headingMax = this.heading + 45 - 90;
            if (headingMin < 0) headingMin += 360;
            if (headingMin >= 360) headingMin -= 360;
            if (headingMax < 0) headingMax += 360;
            if (headingMax >= 360) headingMax -= 360;
            if (compareAngle(headingMin, headingMax, heading)) facing = "negative x";

            // [0] +y, [1] -y, [2] +x, [3] -x
            String left = "unknown";
            String right = "unknown";
            String forward = "unknown";
            String backward = "unknown";
            switch (facing) {
                case "negative x":
                    left = Signage[1];
                    right = Signage[0];
                    forward = Signage[3];
                    backward = Signage[2];
                    break;
                case "positive x":
                    left = Signage[0];
                    right = Signage[1];
                    forward = Signage[2];
                    backward = Signage[3];
                    break;
                case "negative y":
                    left = Signage[2];
                    right = Signage[3];
                    forward = Signage[1];
                    backward = Signage[0];
                    break;
                case "positive y":
                    left = Signage[3];
                    right = Signage[2];
                    forward = Signage[0];
                    backward = Signage[1];
                    break;
            }
            if (filtered_RSSI >= threshold && now - lastSpoke > 10000){
                String text = "Reached junction" + name;
                if (!left.equals(" ")) text += ", turn left to " + left;
                if (!right.equals(" ")) text += ", turn right to " + right;
                if (!forward.equals(" ")) text += ", go forward to " + forward;
                if (!backward.equals(" ")) text += ", go backwards to " + backward;
                textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
                lastSpoke = Calendar.getInstance().getTime().getTime();
            }
        }

        timeOfLastRSSI = Calendar.getInstance().getTime().getTime();
    }

    public boolean compareAngle(Double a, Double b, Double c){
        //start from a to b, checks c exists within or not, unit in degrees
        if (b<a) {// will pass through 0
            if (c < 360 && c > a){//if c is bigger than starting point and lower than 360
                return true;
            }else if(c >= 0 && c < b){//if c is bigger than 0 and lower than end point
                return true;
            }else{
                return false;
            }
        }else if (b > a){// normal case, just compare a < c < b
            if (c > a && c < b){
                return true;
            }else{
                return false;
            }
        }else{//if a == b then nothing exists in between
            return false;
        }
    }
}
