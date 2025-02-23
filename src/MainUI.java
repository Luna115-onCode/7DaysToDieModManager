import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class MainUI extends JFrame {
    private JPanel pane;
    private JTextField pathField;
    private JButton chgLangButton;
    private JLabel pathLabel;
    private JButton selectPathButton;
    private String lang;
    private String execLocation = System.getProperty("user.dir");
    private Image icon = ImageIO.read(Objects.requireNonNull(MainUI.class.getResourceAsStream("/img/icon.png")));

    public MainUI(String title) throws IOException {
        super(title);
        setContentPane(pane);
        initConfig();
        initLang();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(null);
        setIconImage(icon);

        chgLangButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Objects.equals(lang, "Spanish")) {
                    lang = "English";
                } else {
                    lang = "Spanish";
                }
                try {
                    setProperties(Paths.get(execLocation, "config.properties").toString(), "lang", lang);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        selectPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setProperties(Paths.get(execLocation, "config.properties").toString(), "gamePath", pickFolder());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

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
        initConfig();
        initLang();
    }

    public void initLang() throws IOException {
        Properties langText = getProperties("%sLabels.properties".formatted(lang));
        pathLabel.setText(langText.getProperty("pathLocationLabel"));
        chgLangButton.setText(langText.getProperty("changeLangButton"));
        selectPathButton.setText(langText.getProperty("selectButton"));
    }

    public void initConfig() throws IOException {
        String filename = Paths.get(execLocation, "config.properties").toString();
        Properties config = getProperties(filename);

        if (config == null) {
            config = new Properties();
            config.setProperty("mods", "");
            config.setProperty("lang", "English");
            config.setProperty("gamePath", Paths.get("C:", "Program Files (x86)", "Steam", "steamapps", "common", "7 Days To Die").toString());
            try (FileOutputStream output = new FileOutputStream(filename)) {
                config.store(output, "Generated Configuration File");
            }
        }

        pathField.setText(config.getProperty("gamePath"));
        lang = config.getProperty("lang");
    }

    public static String pickFolder() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); // Only allow directories
        chooser.setDialogTitle("Select a Folder");

        int result = chooser.showOpenDialog(null); // Open dialog

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFolder = chooser.getSelectedFile();
            return selectedFolder.getAbsolutePath(); // Return folder path
        }
        return null; // No selection
    }

    public static void main(String[] args) throws IOException {
        JFrame panel = new MainUI("7 Days to die Mod Manager");
    }
}
