package uk.co.somestuff.NetworkRail.ActiveMQ;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Objects {

    public static class SOP {
        public List<SignalingArea> signalingAreas = new ArrayList<SignalingArea>();

        public SOP(JSONObject sopJson) throws IOException, JSONException {
            if (!sopJson.has("$type")) {
                System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (No $type in 'SOP.json')");
                throw new IOException("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (No $type in 'SOP.json')");
            } else {
                if (!sopJson.getString("$type").equals("NetworkRail.ActiveMQ.Support.SOP")) {
                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (Invalid $type in 'SOP.json')");
                    throw new IOException("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (Invalid $type in 'SOP.json')");
                } else {
                    try {
                        sopJson.remove("$type");
                        Iterator<String> keys = sopJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (sopJson.get(key) instanceof JSONObject) {

                                SignalingArea signalingArea = new SignalingArea(key);
                                JSONObject addresses = sopJson.getJSONObject(key).getJSONObject("SOP"); // Now we've got the SOP Object we need to get the addresses and individual signals

                                Iterator<String> addressKeys = addresses.keys();
                                while (addressKeys.hasNext()) {
                                    String addressKey = addressKeys.next();
                                    if (addresses.get(addressKey) instanceof JSONObject) {

                                        SignalAddress signalAddress = new SignalAddress(addressKey);
                                        JSONObject signals = addresses.getJSONObject(addressKey); // Now we should have the individual signals

                                        for (Iterator iterator = signals.keys(); iterator.hasNext();) {
                                            String signalKey = (String) iterator.next();
                                            Signal signal = new Signal(signalKey, signals.get(signalKey));
                                            signalAddress.signals.add(signal);
                                        }
                                        signalingArea.signalAddresses.add(signalAddress);
                                    }
                                }
                                signalingAreas.add(signalingArea);
                            }
                        }
                    } catch (JSONException _ex) {
                        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] JSONException " + _ex.getLocalizedMessage());
                        throw new JSONException(_ex);
                    }
                }
            }
        }

        public Signal getSignal(String signalingArea, String signalAddress, String localSignalAddress) {

            Signal rtnSignal = new Signal(null, null);

            for (int _i = 0; _i < this.signalingAreas.size(); _i++) {
                if (this.signalingAreas.get(_i).signalArea.equals(signalingArea)) {
                    for (int _ii = 0; _ii < this.signalingAreas.get(_i).signalAddresses.size(); _ii++) {
                        if (this.signalingAreas.get(_i).signalAddresses.get(_ii).signalAddress.equals(signalAddress)) {
                            for (int _iii = 0; _iii < this.signalingAreas.get(_i).signalAddresses.get(_ii).signals.size(); _iii++) {
                                if (this.signalingAreas.get(_i).signalAddresses.get(_ii).signals.get(_iii).address.equals(localSignalAddress)) {
                                    return this.signalingAreas.get(_i).signalAddresses.get(_ii).signals.get(_iii);
                                }
                            }
                        }
                    }
                }
            }

            return rtnSignal;
        }

    }

    public static class SignalingArea {
        public List<SignalAddress> signalAddresses = new ArrayList<SignalAddress>();
        public String signalArea;

        public SignalingArea(String signalArea) {
            this.signalArea = signalArea;
        }

    }

    public static class SignalAddress {
        public List<Signal> signals = new ArrayList<Signal>();
        public String signalAddress;

        public SignalAddress(String signalAddress) {
            this.signalAddress = signalAddress;
        }

    }

    public static class Signal {
        public String address;
        public Object signal;

        public Signal(String address, Object signal) {
            this.address = address;
            this.signal = signal;
        }

        public boolean isSignalNull() {
            if (this.address.equals(null) || this.signal.equals(null)) {
                return true;
            }
            return false;
        }

    }

    public static class Config {
        public String topic;
        public List<Area> areasCovered = new ArrayList<Area>();
        public String name;

        public Config(JSONObject configJson) throws IOException {
            if (!configJson.has("$type")) {
                System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (No $type in 'CONFIG.json')");
                throw new IOException("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (No $type in 'CONFIG.json')");
            } else {
                if (!configJson.getString("$type").equals("NetworkRail.ActiveMQ.Support.Config")) {
                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (Invalid $type in 'CONFIG.json')");
                    throw new IOException("[uk.co.somestuff.NetworkRail.ActiveMQ] Exception (No $type in 'CONFIG.json')");
                } else {
                    this.setTopic(configJson.getString("topic"));
                    this.setName(configJson.getString("name"));

                    for (int i = 0; i < configJson.getJSONArray("areas_covered").length(); i++) {
                        Objects.Area newArea = new Objects.Area(configJson.getJSONArray("areas_covered").getJSONObject(i).getString("id"),
                                configJson.getJSONArray("areas_covered").getJSONObject(i).getString("name"),
                                configJson.getJSONArray("areas_covered").getJSONObject(i).getString("topic"));
                        this.addArea(newArea);
                    }
                }
            }
        }

        public void setTopic(String topic) { this.topic = topic; }

        public void setName(String name) { this.name = name; }

        public int length() { return this.areasCovered.size(); }

        public void clear() { this.areasCovered = new ArrayList<Area>(); }

        public Area getArea(int index) { return this.areasCovered.get(index); }

        public void addArea(Area area) { this.areasCovered.add(area); }

        public String getTopic() { return this.topic; }

        public boolean hasArea(String area) {
            for (int _i = 0; _i < this.areasCovered.size(); _i++) {
                if (this.areasCovered.get(_i).id.equals(area)) {
                    return true;
                }
            }
            return false;
        }

    }

    public static class Area {
        private String id;
        private String name;
        private String topic;

        public Area(String id, String name, String topic) {
            this.id = id; this.name = name; this.topic = topic;
        }

        public String getId() { return this.id; }

        public String getName() { return this.name; }

        public String getTopic() { return this.topic; }

    }
}
