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
    private JList<String> modsList;
    private JLabel modsLabel;
    private JList installedModsList;
    private JLabel installedModsLabel;
    private JTextPane modDetailsLabel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton detailsButton;
    private JButton removeButton;
    private JButton applyButton;
    private String lang;
    private String execLocation = System.getProperty("user.dir");
    private Image icon = ImageIO.read(Objects.requireNonNull(MainUI.class.getResourceAsStream("/img/icon.png")));
    private Methods methods = new Methods();

    public MainUI(String title) throws IOException {
        super(title);
        setContentPane(pane);
        methods.initializeModsList(modsList);
        initConfig();
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
                    methods.setProperties(Paths.get(execLocation, "config.properties").toString(), "lang", lang);
                    initConfig();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        selectPathButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    methods.setProperties(Paths.get(execLocation, "config.properties").toString(), "gamePath", methods.pickFolder());
                    initConfig();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    public void initLang() throws IOException {
        Properties langText = methods.getProperties("%sLabels.properties".formatted(lang));
        pathLabel.setText(langText.getProperty("pathLocationLabel"));
        chgLangButton.setText(langText.getProperty("changeLangButton"));
        selectPathButton.setText(langText.getProperty("selectButton"));
        modsLabel.setText(langText.getProperty("modsLabel"));
        modsList.setToolTipText(langText.getProperty("modsLabel"));
        installedModsLabel.setText(langText.getProperty("installedModsLabel"));
        installedModsList.setToolTipText(langText.getProperty("installedModsLabel"));
        addButton.setText(langText.getProperty("addButton"));
        removeButton.setText(langText.getProperty("removeButton"));
        detailsButton.setText(langText.getProperty("detailsButton"));
        deleteButton.setText(langText.getProperty("deleteButton"));
        applyButton.setText(langText.getProperty("applyButton"));
    }

    public void initConfig() throws IOException {
        String filename = Paths.get(execLocation, "config.properties").toString();
        Properties config = methods.getProperties(filename);

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
        initLang();
    }

    public static void main(String[] args) throws IOException {
        JFrame panel = new MainUI("7 Days to die Mod Manager");
    }
}
