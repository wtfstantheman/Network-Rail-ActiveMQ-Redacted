package uk.co.somestuff.NetworkRail.ActiveMQ;

import net.ser1.stomp.Listener;
import uk.co.somestuff.NetworkRail.ActiveMQ.GUI.Main;

import javax.swing.*;

/**
 * Example client that connects to the Network Rail ActiveMQ
 * and subscribes a listener to receive real-time messages
 *
 * @author Martin.Swanson@blackkitetechnology.com
 */

public class Client {

    private String TOPIC;

    private static final String SERVER = "datafeeds.networkrail.co.uk";

    private static final int PORT = 61618;

    private static net.ser1.stomp.Client client;

    /*
     * Connect to a single topic and subscribe a listener
     * @throws Exception Too lazy to implement exception handling....
     */

    public void test() throws Exception {
        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Connecting...");
        client = new net.ser1.stomp.Client(SERVER, PORT, Main.USERNAME, Main.PASSWORD);
        if (client.isConnected()) {
            System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Connected to " + SERVER + ":" + PORT);
        } else {
            System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Could not connect");
            throw new Exception();
        }
    }

    public void connect(Objects.Config config, Main.MapForm map, Objects.SOP sop) throws Exception {

        this.TOPIC = "/topic/" + config.getTopic();

        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Connecting...");
        client = new net.ser1.stomp.Client(SERVER, PORT, Main.USERNAME, Main.PASSWORD);
        if (client.isConnected()) {
            System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Connected to " + SERVER + ":" + PORT);
        } else {
            System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Could not connect");
            throw new Exception();
        }
        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Subscribing...");
        Listener listener = new MyListener(config, map, sop);
        client.subscribe(this.TOPIC , listener);
        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Subscribed to " + this.TOPIC);
        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Waiting for message...");
    }

    public void disconnect() {
        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Disconnecting...");
        client.disconnect();
        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Disconnected from " + this.TOPIC);
    }
}
