package uk.co.somestuff.NetworkRail.ActiveMQ.GUI;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import uk.co.somestuff.NetworkRail.ActiveMQ.Client;
import uk.co.somestuff.NetworkRail.ActiveMQ.Objects;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/** TODO: Fix the multiple map menu problem **/

public class Main {

    private JPanel panel;
    private static JMenuBar mb;
    private static JMenu x;
    private static JFrame frame;
    private static SplashForm splashForm;
    private static LoginForm loginForm;

    private static JTextField emailField;
    private static JPasswordField passwordField;
    private static JMenuItem m0, m1, m2;
    private static JButton openAConfigurationArchive;

    private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public static String USERNAME = "";
    public static String PASSWORD = "";

    private static int mapFormsOpen = 0;

    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        if (System.getProperty("os.name").toLowerCase().contains("mac")) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }

        frame = new JFrame("Network Rail ActiveMQ TD");

        mb = new JMenuBar();
        x = new JMenu("File");

        ResourceBundle resource = ResourceBundle.getBundle("project");

        m0 = new JMenuItem("Version " + resource.getString("version"));
        m1 = new JMenuItem("Open Config Archive");
        m2 = new JMenuItem("Login to Network Rail Data Feed");

        m1.setAccelerator(KeyStroke.getKeyStroke('O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        m2.setAccelerator(KeyStroke.getKeyStroke('L', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

        x.add(m0);
        x.add(m1);
        x.add(m2);

        mb.add(x);

        m1.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new PackageLoader().loadPackage();
            }
        });

        m2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginForm = new LoginForm();
            }
        });

        splashForm = new SplashForm();

        disableButtons();

    }

    private static void disableButtons() {
        m1.setEnabled(false);
        openAConfigurationArchive.setEnabled(false);
    }

    private static void enableButtons() {
        m1.setEnabled(true);
        openAConfigurationArchive.setEnabled(true);
    }

    private static Action loginAction = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            USERNAME = emailField.getText();
            PASSWORD = passwordField.getText();
            boolean success = true;
            Client c = new Client();
            try {
                c.test();
            } catch (Exception exception) {
                exception.printStackTrace();
                success = false;
                JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while trying to log you in, Please try again later", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
            }
            if (success) {
                c.disconnect();
                enableButtons();
                loginForm.dispose();
                splashForm.setJMenuBar(mb);
            }
        }
    };

    public static class PackageLoader {
        private Path temporaryDirectory;
        private File selectedFile;
        private Objects.Config config;
        private Objects.SOP sop;
        private Document svgDoc;
        private JSVGCanvas svgCanvas = new JSVGCanvas();

        public void loadPackage() {

            FileDialog fd = new FileDialog(frame);
            fd.setMode(FileDialog.LOAD);
            fd.setLocation(50,50);
            fd.setVisible(true);

            this.selectedFile = new File(fd.getDirectory() + fd.getFile());

            if (fd.getFile() != null) {
                /**
                 * Opening the config archive (zip) and saving it in temp? to be read from
                 **/

                boolean success = true;

                try {
                    this.temporaryDirectory = Files.createTempDirectory(null);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                    JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while creating a temp directory, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                    success = false;
                }

                if (success) {

                    /** Explodes the zip config selected to a temporary directory **/
                    File dir = new File(String.valueOf(this.temporaryDirectory));
                    if (!dir.exists()) dir.mkdirs();
                    FileInputStream fis;
                    byte[] buffer = new byte[1024];

                    try {
                        fis = new FileInputStream(this.selectedFile);
                        ZipInputStream zis = new ZipInputStream(fis);
                        ZipEntry ze = zis.getNextEntry();

                        while (ze != null) {

                            String fileName = ze.getName();
                            File newFile = new File(this.temporaryDirectory + File.separator + fileName);
                            System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Unzipping to " + newFile.getAbsolutePath());

                            new File(newFile.getParent()).mkdirs();
                            FileOutputStream fos = new FileOutputStream(newFile);
                            int len;

                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }

                            fos.close();
                            zis.closeEntry();
                            ze = zis.getNextEntry();
                        }
                        zis.closeEntry();
                        zis.close();
                        fis.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while extracting the configuration archive, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                        success = false;
                    }

                    if (success) {
                        this.setDocToView();
                        //setDocsToView(this.temporaryDirectory.toString());
                    }
                }
            } else {
                System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] No File Selected!?");
                JOptionPane.showMessageDialog(new JFrame(), "No File Selected!?", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void setDocToView() {
            try {
                this.setReaderToView(new InputStreamReader(new FileInputStream(String.valueOf(Paths.get(String.valueOf(this.temporaryDirectory), "CONFIG.json")))), new InputStreamReader(new FileInputStream(String.valueOf(Paths.get(String.valueOf(this.temporaryDirectory), "SOP.json")))), new InputStreamReader(new FileInputStream(String.valueOf(Paths.get(String.valueOf(this.temporaryDirectory), "MAP.svg")))));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(new JFrame(), "An error has occurred locating the configuration archive, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void setReaderToView(InputStreamReader configIS, InputStreamReader sopIS, InputStreamReader mapIS) {
            boolean success = true;

            /** Loads the config.json file **/

            BufferedReader reader = null;
            try {
                reader = new BufferedReader(configIS);
            } catch (Exception fileNotFoundException) {
                fileNotFoundException.printStackTrace();
                JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while looking for the CONFIG.json configuration file, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                success = false;
            }

            if (success) {

                try {
                    this.config = new Objects.Config(new JSONObject(IOUtils.toString(configIS)));
                } catch (JSONException | IOException ex) {
                    ex.getMessage();
                    JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while validating the configuration archive, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                    success = false;
                }

                if (success) {

                    /** Loads the sop.json file **/

                    reader = null;
                    try {
                        reader = new BufferedReader(sopIS);
                    } catch (Exception fileNotFoundException) {
                        fileNotFoundException.printStackTrace();
                        JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while looking for the SOP.json configuration file, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                    }

                    try {
                        this.sop = new Objects.SOP(new JSONObject(reader.lines().collect(Collectors.joining())));
                    } catch (JSONException | IOException ex) {
                        ex.getMessage();
                        JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while validating the configuration archive, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                        success = false;
                    }


                    if (success) {
                        /** Sets the SVG map to the svgCanvas **/
                        System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Starting batik");
                        try {
                            String parser = XMLResourceDescriptor.getXMLParserClassName();
                            SAXSVGDocumentFactory f = new SAXSVGDocumentFactory(parser);
                            this.svgDoc = f.createDocument("", mapIS);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while processing MAP.svg, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                            success = false;
                        }

                        if (success) {

                            splashForm.dispose();

                            Client c = new Client();
                            //MapForm map = new MapForm(c, this.config, this.svgDoc);
                            //this.svgCanvas.setDocument(svgDoc);
                            try {
                                c.connect(this.config, new MapForm(c, this.config, this.svgDoc), this.sop);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                                JOptionPane.showMessageDialog(new JFrame(), "An error has occurred while connecting to the client, Please try again", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                }
            }
        }

    }

    static class SplashForm extends JFrame {
        public SplashForm() {

            setJMenuBar(mb);

            JPanel panelOne = new JPanel();
            panelOne.setBackground(new Color(0xFFFFFF));
            JLabel labelOne = new JLabel("Open a configuration archive", SwingConstants.LEFT);
            openAConfigurationArchive = new JButton("Open");

            panelOne.add(labelOne);
            panelOne.add(openAConfigurationArchive);

            openAConfigurationArchive.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new PackageLoader().loadPackage();
                }
            });
            panelOne.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel panelTwo = new JPanel();
            panelTwo.setBackground(new Color(0xFFFFFF));
            JLabel labelTwo = new JLabel("Login to Network Rail Data Feed", SwingConstants.LEFT);
            JButton buttonTwo = new JButton("Login");
            buttonTwo.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    loginForm = new LoginForm();
                }
            });

            panelTwo.add(labelTwo);
            panelTwo.add(buttonTwo);

            panelTwo.setAlignmentX(Component.LEFT_ALIGNMENT);

            JPanel oneNtwo = new JPanel();
            oneNtwo.setLayout(new BoxLayout(oneNtwo, BoxLayout.X_AXIS));
            oneNtwo.add(panelOne);
            oneNtwo.add(panelTwo);
            oneNtwo.setAlignmentX(Component.LEFT_ALIGNMENT);

            URL url = Thread.currentThread().getContextClassLoader().getResource("network_rail_title_redacted.png");

            ImageIcon imageicon = new ImageIcon(url);
            JLabel label = new JLabel(imageicon);

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
            topPanel.add(label);
            topPanel.add(oneNtwo);

            JPanel bottomPanel = new JPanel();
            bottomPanel.setBackground(new Color(0xFFFFFF));
            bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
            JLabel hyperLabel = new JLabel("Data provided by Network Rail");
            hyperLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            hyperLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://www.networkrail.co.uk/who-we-are/transparency-and-ethics/transparency/open-data-feeds/network-rail-infrastructure-limited-data-feeds-licence"));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(new JFrame(), "An error occurred while opening 'https://www.networkrail.co.uk/who-we-are/transparency-and-ethics/transparency/open-data-feeds/network-rail-infrastructure-limited-data-feeds-licence'", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            hyperLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            bottomPanel.add(Box.createHorizontalStrut(2));
            bottomPanel.add(hyperLabel);
            bottomPanel.add(Box.createVerticalStrut(2));

            topPanel.setAlignmentY(Component.TOP_ALIGNMENT);
            bottomPanel.setAlignmentY(Component.BOTTOM_ALIGNMENT);
            getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            getContentPane().add(topPanel);
            getContentPane().add(bottomPanel);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(700, 300);
            setLocationRelativeTo(null);
            setTitle("Network Rail ActiveMQ - Setup");
            setResizable(false);
            setVisible(true);
        }
    }

    static class LoginForm extends JFrame {
        public LoginForm() {

            JPanel panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            GridBagConstraints c = new GridBagConstraints();
            c.fill = NORMAL;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(0,0,10,10);
            c.anchor = GridBagConstraints.EAST;

            JLabel emLab = new JLabel("Email");

            panel.add(emLab, c);

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 0;
            c.insets = new Insets(0,0,10,00);

            emailField = new JTextField(15);

            panel.add(emailField, c);

            c = new GridBagConstraints();
            c.fill = NORMAL;
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(0,0,10,10);
            c.anchor = GridBagConstraints.EAST;

            JLabel passLab = new JLabel("Password");

            panel.add(passLab, c);

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 1;
            c.gridy = 1;
            c.insets = new Insets(0,0,10,0);

            passwordField = new JPasswordField(15);

            panel.add(passwordField, c);

            JLabel hyperLabel = new JLabel("Create a Network Rail data feed account");
            hyperLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://datafeeds.networkrail.co.uk/ntrod/"));
                    } catch (IOException | URISyntaxException e1) {
                        e1.printStackTrace();
                        JOptionPane.showMessageDialog(new JFrame(), "An error occurred while opening 'https://datafeeds.networkrail.co.uk/ntrod/'", "Network Rail ActiveMQ - Exception", JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            hyperLabel.setForeground(new Color(0x00446F));
            hyperLabel.setFont(new Font(null, Font.ITALIC, 12));
            hyperLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridwidth = 3;
            c.gridx = 0;
            c.gridy = 2;
            c.insets = new Insets(0,0,10,0);
            panel.add(hyperLabel, c);

            JButton button = new JButton("Login");

            button.addActionListener(loginAction);

            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 2;
            c.gridy = 3;
            panel.add(button, c);

            add(panel);
            setSize(500, 200);
            setLocationRelativeTo(null);
            setTitle("Network Rail ActiveMQ - Login");
            setResizable(false);
            setVisible(true);
        }
    }

    public static class MapForm extends JFrame {
        public JSVGCanvas svgCanvas = new JSVGCanvas();
        public Document svgDoc;
        public JScrollPane newPanelScroll = new JScrollPane();

        public MapForm(Client client, Objects.Config config, Document svgDoc) {

            this.setJMenuBar(mb);

            mapFormsOpen++;

            this.svgDoc = svgDoc;

            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

            this.svgCanvas.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);

            this.svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
                public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Document Loading...");
                }
                public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Document Loaded.");
                }
            });

            this.svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
                public void gvtBuildStarted(GVTTreeBuilderEvent e) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Build Started...");
                }
                public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
                    System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Build Done.");
                }
            });

            this.svgCanvas.addGVTTreeRendererListener(new RenderAdapted(this));

            this.newPanelScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            this.newPanelScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            this.svgCanvas.setDocument(this.svgDoc);
            this.newPanelScroll.setViewportView(this.svgCanvas);

            this.getContentPane().add(this.newPanelScroll);

            this.addWindowListener(new WindowAdapter(client, this));

            this.setTitle("Network Rail ActiveMQ - " + config.name);
            this.setVisible(true);
            this.setLocationRelativeTo(null);
        }

        public class WindowAdapter extends java.awt.event.WindowAdapter {
            private Client client;
            private MapForm m;

            public WindowAdapter(Client client, MapForm m) {
                this.client = client;
                this.m = m;
            }

            public void windowClosing(WindowEvent e) {
                this.client.disconnect();
                mapFormsOpen--;
                if (mapFormsOpen == 0) {
                    splashForm = new SplashForm();
                }
                this.m.dispose();
            }
        }

        public class RenderAdapted extends GVTTreeRendererAdapter {
            private MapForm mf;

            public RenderAdapted(MapForm mf) {
                this.mf = mf;
            }

            public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Rendering Started...");
            }

            public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
                System.out.println("[uk.co.somestuff.NetworkRail.ActiveMQ] Rendering Done.");

                setCursor(Cursor.getDefaultCursor());

                Element el = (Element) this.mf.svgDoc.getElementsByTagName("svg").item(0);
                int width = Integer.parseInt(el.getAttribute("width"));
                int height = Integer.parseInt(el.getAttribute("height"));

                int newHeight;
                int newWidth;

                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                if (height < screenSize.getHeight()) {
                    newHeight = (int) height + 100;
                } else {
                    newHeight = (int) (screenSize.getHeight() - 100);
                }
                if (width < screenSize.getWidth()) {
                    newWidth = (int) width;
                } else {
                    newWidth = (int) (screenSize.getWidth() - 100);
                }

                this.mf.setSize(newWidth, newHeight);
                //setLocationRelativeTo(null);

            }
        }

    }
}