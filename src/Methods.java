import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Properties;

public class Methods {
    private String execLocation = System.getProperty("user.dir");

    public Properties getProperties(String file) throws IOException {
        Properties prop = new Properties();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream stream = loader.getResourceAsStream(file);
        try {
            prop.load(stream);
        } catch (java.lang.NullPointerException e) {
            try {
                stream = new FileInputStream(file);
                prop.load(stream);
            } catch (FileNotFoundException ex) {
                prop = null;
            }
        } catch (IOException e) {
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

    public void initializeModsList(JList list) {
        DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();

        for (String item : getMods()) {
            model.addElement(item);
        }
    }

    public ArrayList<String> getMods() {
        ArrayList<String> modsListFolder = new ArrayList<>();
        File folder = new File(Paths.get(execLocation, "Mods").toString());

        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        modsListFolder.add(file.getName());
                    }
                }
            }
        } else {
            folder.mkdir();
        }
        return modsListFolder;
    }
}
