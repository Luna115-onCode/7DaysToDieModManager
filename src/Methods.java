import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class Methods {
    private final String execLocation = System.getProperty("user.dir");
    private final Properties config = getProperties(Paths.get(execLocation, "config.properties").toString());
    private final String lang = config == null ? "English" : config.getProperty("lang");
    private final Properties langText = getProperties("%sLabels.properties".formatted(lang));

    public Methods() throws IOException {
    }

    public Properties getProperties(String file) throws IOException {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(file);
        try {
            if (stream == null) {
                stream = new FileInputStream(file);
            }
            prop.load(stream);
        } catch (NullPointerException | IOException e) {
            prop = null;
        }
        return prop;
    }

    public void setProperties(String file, String property, String value) throws IOException {
        Properties prop = getProperties(file);
        prop.setProperty(property, value);
        try (FileOutputStream output = new FileOutputStream(file)) {
            prop.store(output, "Config saved");
        }
    }

    public String pickFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Select a Folder");

        int result = chooser.showOpenDialog(null);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            return selectedFolder.getAbsolutePath();
        }
        return null;
    }

    public void initializeModsList(JList<String> list, String pathToSearch) throws IOException {
        list.setModel(new DefaultListModel<String>());
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();

        for (String item : getMods(pathToSearch)) {
            model.addElement(item);
        }
    }

    public ArrayList<String> getMods(String pathToSearch) throws IOException {
        ArrayList<String> modsListFolder = new ArrayList<>();
        File folder = new File(Paths.get(pathToSearch, "Mods").toString());

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    Document modInfo = readXML(Paths.get(String.valueOf(file), "modinfo.xml").toString());
                    String name = getXMLElement("DisplayName", modInfo);
                    if (file.isDirectory() && name != null) {
                        modsListFolder.add(name);
                    }
                }
            }
        } else {
            folder.mkdir();
        }
        if (modsListFolder.isEmpty()) {
            modsListFolder.add(langText.getProperty("NoModsAvailable"));
        }
        Collections.sort(modsListFolder);
        return modsListFolder;
    }

    public Document readXML(String filename) {
        Document doc = null;
        try {
            File xmlFile = new File(filename);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();
        } catch (Exception ignored) {}
        return doc;
    }

    public String getXMLElement(String element, Document doc) {
        return getXMLElement(element, doc, "value");
    }

    public String getXMLElement(String element, Document doc, String field) {
        String value = null;
        if (doc != null) {
            NodeList nodes = doc.getElementsByTagName(element);
            if (nodes.getLength() > 0) {
                Element object = (Element) nodes.item(0);
                value = object.getAttribute(field);
            }
        }
        return value;
    }

    public Document searchModDetails(String modName, String searchLocation) {
        Document doc = null;
        File folder = new File(Paths.get(searchLocation, "Mods").toString());
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    Document modInfo = readXML(Paths.get(String.valueOf(file), "modinfo.xml").toString());
                    String name = getXMLElement("DisplayName", modInfo);
                    if (file.isDirectory() && Objects.equals(name, modName)) {
                        doc = modInfo;
                        break;
                    }
                }
            }
        }
        return doc;
    }

    public void setSelectedDetails(String selectedOption, JTextPane container, String searchLocation) {
        Document doc = searchModDetails(selectedOption, searchLocation);
        String modName = getXMLElement("Name", doc) == null ? "No Name Available" : getXMLElement("Name", doc);
        String modDisplayName = getXMLElement("DisplayName", doc) == null ? "No Name ID Available" : getXMLElement("DisplayName", doc);
        String description = getXMLElement("Description", doc) == null ? "No Description Available" : getXMLElement("Description", doc);
        String version = getXMLElement("Version", doc) == null ? "Version" : getXMLElement("Version", doc);
        String author = getXMLElement("Author", doc) == null ? "No Author Available" : getXMLElement("Author", doc);
        container.setText(("""
                        %s: %s
                        
                        %s: %s
                        
                        %s: %s
                        
                        %s: %s
                        
                        %s: %s
                        """).formatted(langText.getProperty("name"), modDisplayName, langText.getProperty("nameID"),
                modName, langText.getProperty("description"), description, langText.getProperty("author"), author,
                langText.getProperty("version"), version));
    }

    public void clearSelectedDetails(JTextPane container) {
        container.setText("");
    }
}
