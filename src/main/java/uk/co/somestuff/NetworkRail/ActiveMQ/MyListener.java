package uk.co.somestuff.NetworkRail.ActiveMQ;

import net.ser1.stomp.Listener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Element;
import uk.co.somestuff.NetworkRail.ActiveMQ.GUI.Main;

import java.util.Map;

public class MyListener implements Listener {
    private Objects.Config config;
    private Main.MapForm map;
    private Objects.SOP sop;

    public MyListener(Objects.Config config, Main.MapForm map, Objects.SOP sop) {
        this.config = config; this.map = map; this.sop = sop;
    }

    @Override
    public void message(Map header, String body) {

        JSONArray Jbody = new JSONArray(body);

        for (int i = 0; i < Jbody.length(); i++) {
            JSONObject localJObjectF = Jbody.getJSONObject(i);
            JSONObject localJObjectO = localJObjectF.getJSONObject(localJObjectF.names().getString(0));
            if (this.config.hasArea(localJObjectO.getString("area_id"))) {
                System.out.println(localJObjectO.getString("area_id"));
                if (localJObjectO.getString("msg_type").equals("SF") || localJObjectO.getString("msg_type").equals("SG")) {

                    /** Process the signal update **/

                    /** Set signal data to 8 bits long as well as reversing it **/

                    JSONArray signalData = new JSONArray();

                    /** The 'data' from the S-Class message is made into 8 bits and then flipped so that the 0th item from the received message (on the left) is the 0th in the array which makes it all easier **/

                    String dataBinary = Integer.toBinaryString(Integer.parseInt(localJObjectO.getString("data"),16));
                    for (int r = 0; r < dataBinary.length(); r++) {
                        signalData.put(String.valueOf(dataBinary.charAt(r)));
                    }
                    for (int r = 0; r < 8-dataBinary.length(); r++) {
                        signalData.put("0");
                    }

                    JSONArray revSignalData = new JSONArray();
                    for (int r = signalData.length()-1; r>=0; r--) {
                        revSignalData.put(signalData.get(r));
                    }

                    int address = Integer.parseInt(localJObjectO.getString("address"),16);

                    for (int r = 0; r < revSignalData.length(); r++) {

                        /** We now have the signal address and number so can check the SOP table and update the display **/

                        //JSONObject localSOP = Main.sop.getJSONObject(localJObjectO.getString("area_id")).getJSONObject("SOP");
                        //System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Signal " + localSOP.getJSONObject(String.valueOf(address)).getString(String.valueOf(r)) + " set too " + revSignalData.getString(r) + " " + (revSignalData.getString(r).equals("1") ? "(G)" : "(R)"));

                        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Signal " + this.sop.getSignal(localJObjectO.getString("area_id"), String.valueOf(address), String.valueOf(r)).signal + " set too " + revSignalData.getString(r) + " " + (revSignalData.getString(r).equals("1") ? "(G)" : "(R)"));

                        String compiledSignalID = localJObjectO.getString("area_id") + this.sop.getSignal(localJObjectO.getString("area_id"), String.valueOf(address), String.valueOf(r)).signal;
                        Element signal = this.map.svgDoc.getElementById(compiledSignalID);
                        signal.setAttribute("fill", (revSignalData.getString(r).equals("1") ? "green" : "red"));
                        //Main.svgCanvas.flush();
                        this.map.svgCanvas.setDocument(this.map.svgDoc);

                    }
                } else if (localJObjectO.getString("msg_type").equals("CA") || localJObjectO.getString("msg_type").equals("CB") || localJObjectO.getString("msg_type").equals("CC")) {

                    /** Train has moved between berths **/

                    String to = "";
                    String from = "";

                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Train " + localJObjectO.getString("descr") + " has moved from " + (localJObjectO.has("from") ? localJObjectO.getString("from") : "empty") + " to " + (localJObjectO.has("to") ? localJObjectO.getString("to") : "empty"));

                    if (localJObjectO.has("to")) {
                        to = localJObjectO.getString("to");
                        Element signal = this.map.svgDoc.getElementById(localJObjectO.getString("area_id") + to);
                        signal.setTextContent(localJObjectO.getString("descr"));
                        //Main.svgCanvas.flush();
                        this.map.svgCanvas.setDocument(this.map.svgDoc);
                        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Updated " + localJObjectO.getString("area_id") + to + " with " + localJObjectO.getString("descr"));
                    }

                    if (localJObjectO.has("from")) {
                        from = localJObjectO.getString("from");
                        Element signal = this.map.svgDoc.getElementById(localJObjectO.getString("area_id") + from);
                        signal.setTextContent("");
                        //Main.svgCanvas.flush();
                        this.map.svgCanvas.setDocument(this.map.svgDoc);
                        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Updated " + localJObjectO.getString("area_id") + from + " with 'empty'");
                    }
                }
            }
        }
    }
}
