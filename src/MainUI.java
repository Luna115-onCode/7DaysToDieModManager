import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Properties;

public class MainUI extends JFrame {
    private JPanel pane;
    private JTextField pathField;
    private JButton chgLangButton, selectPathButton, addButton, deleteButton, detailsButton, removeButton, applyButton;
    private JLabel pathLabel, modsLabel, installedModsLabel, modDetailsLabel;
    private JList<String> modsList, installedModsList;
    private JTextPane modDetailsContainer;
    private JScrollPane installedModsListScroll, modsListScroll;
    private String lang, execLocation = System.getProperty("user.dir");
    private Image icon = ImageIO.read(Objects.requireNonNull(MainUI.class.getResourceAsStream("/img/icon.png")));
    private Methods methods = new Methods();

    public MainUI(String title) throws IOException {
        super(title);
        setContentPane(pane);
        methods.initializeModsList(modsList);
        initConfig();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(730, 630);
        setResizable(false);
        setVisible(true);
        setLocationRelativeTo(null);
        setIconImage(icon);
        modsList.setVisibleRowCount(5);
        modsListScroll.setViewportView(modsList);
        installedModsList.setVisibleRowCount(5);
        installedModsListScroll.setViewportView(installedModsList);

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
                    restartApplication();
                } catch (IOException | URISyntaxException ex) {
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
        modsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    String[] selection = modsList.getSelectedValuesList().toArray(new String[0]);
                    if (selection.length > 0) {
                        addButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                        detailsButton.setEnabled(selection.length == 1);
                    } else {
                        addButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                        detailsButton.setEnabled(false);
                    }
                }
            }
        });

        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                modsList.clearSelection();
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
        modDetailsLabel.setText(langText.getProperty("modDetailsLabel"));
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

    private void restartApplication() throws IOException, URISyntaxException {
        String java = System.getProperty("java.home") + "/bin/java";
        String currentJar = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getPath();
        String classpath = System.getProperty("java.class.path");
        String mainClass = getClass().getName();

        ProcessBuilder builderJar = new ProcessBuilder(java, "-jar", currentJar);
        ProcessBuilder builderClass = new ProcessBuilder(java, "-cp", classpath, mainClass);
        builderJar.start();
        builderClass.start();
        System.exit(0);
    }


    public static void main(String[] args) throws IOException {
        JFrame panel = new MainUI("7 Days to die Mod Manager");
    }
}
