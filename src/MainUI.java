import org.w3c.dom.Document;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;

public class MainUI extends JFrame {
    private JPanel pane;
    private JTextField pathField;
    private JButton chgLangButton, selectPathButton, addButton, deleteButton, removeButton;
    private JLabel pathLabel, modsLabel, installedModsLabel, modDetailsLabel;
    private JList<String> modsList, installedModsList;
    private JTextPane modDetailsContainer;
    private JScrollPane installedModsListScroll, modsListScroll;
    private String lang = "English";
    private String selectedOption;
    private String[] selectedOptions;

    private final String execLocation = System.getProperty("user.dir");
    private final Methods methods = new Methods();


    public MainUI(String title) throws IOException {
        super(title);
        Image icon = ImageIO.read(Objects.requireNonNull(MainUI.class.getResourceAsStream("/img/icon.png")));
        setContentPane(pane);
        initConfig();
        Properties langText = methods.getProperties("%sLabels.properties".formatted(lang));
        Properties config = methods.getProperties(Paths.get(execLocation, "config.properties").toString());
        methods.initializeModsList(modsList, execLocation);
        methods.initializeModsList(installedModsList, config.getProperty("gamePath"));
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

        chgLangButton.addActionListener(e -> {
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
        });
        selectPathButton.addActionListener(e -> {
            try {
                methods.setProperties(Paths.get(execLocation, "config.properties").toString(), "gamePath", methods.pickFolder());
                initConfig();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });
        modsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String[] selection = modsList.getSelectedValuesList().toArray(new String[0]);
                if (selection.length > 0 && !Objects.equals(selection[0], langText.getProperty("NoModsAvailable"))) {
                    addButton.setEnabled(true);
                    deleteButton.setEnabled(true);
                    selectedOption = selection[0];
                    selectedOptions = selection;
                    if (selection.length == 1) {
                        methods.setSelectedDetails(selectedOption, modDetailsContainer, execLocation);
                    } else {
                        methods.clearSelectedDetails(modDetailsContainer);
                    }
                } else {
                    addButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    methods.clearSelectedDetails(modDetailsContainer);
                }
            } else {
                installedModsList.clearSelection();
            }
        });
        installedModsList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String[] selection = installedModsList.getSelectedValuesList().toArray(new String[0]);
                if (selection.length > 0 && !Objects.equals(selection[0], langText.getProperty("noModsInstalled"))) {
                    removeButton.setEnabled(true);
                    selectedOption = selection[0];
                    selectedOptions = selection;
                    if (selection.length == 1) {
                        methods.setSelectedDetails(selectedOption, modDetailsContainer, config.getProperty("gamePath"));
                    } else {
                        methods.clearSelectedDetails(modDetailsContainer);
                    }
                } else {
                    removeButton.setEnabled(false);
                    methods.clearSelectedDetails(modDetailsContainer);
                }
            } else {
                modsList.clearSelection();
            }
        });
        pane.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                modsList.clearSelection();
                installedModsList.clearSelection();
            }
        });
        deleteButton.addActionListener(e -> {
            String formattedSelectedOptions = Arrays.toString(selectedOptions).replace(",", "\n* ").replace("[", "* ").replace("]", "");
            int result = JOptionPane.showConfirmDialog(null,
                    ("""
                    %s
                    
                    %s
                    """).formatted(langText.getProperty("deleteMsg"), formattedSelectedOptions), langText.getProperty("confirm"), JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                for (String option : selectedOptions) {
                    Document doc = methods.searchModDetails(option, execLocation);
                    String modFolder = URLDecoder.decode(doc.getDocumentURI().replaceFirst("^file:/", "").replaceFirst("/?modinfo\\.xml$", ""), StandardCharsets.UTF_8);

                    try {
                        Files.walkFileTree(Path.of(modFolder), new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Files.delete(file);
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                Files.delete(dir);
                                return FileVisitResult.CONTINUE;
                            }
                        });
                        methods.initializeModsList(modsList, execLocation);
                        methods.initializeModsList(installedModsList, config.getProperty("gamePath"));
                    } catch (IOException error) {
                        JOptionPane.showMessageDialog(null, "%s: %s".formatted(langText.getProperty("failedToDeleteDir"), error.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.printf("%s: %s%n", langText.getProperty("failedToDeleteDir"), error.getMessage());
                    }
                }
            }
        });
        addButton.addActionListener(e -> {
            String formattedSelectedOptions = Arrays.toString(selectedOptions).replace(",", "\n* ").replace("[", "* ").replace("]", "");
            int result = JOptionPane.showConfirmDialog(null,
                    ("""
                    %s
                    
                    %s
                    """).formatted(langText.getProperty("addMsg"), formattedSelectedOptions), langText.getProperty("confirm"), JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                for (String option : selectedOptions) {
                    Document doc = methods.searchModDetails(option, execLocation);
                    String modFolder = URLDecoder.decode(doc.getDocumentURI().replaceFirst("^file:/", "").replaceFirst("/?modinfo\\.xml$", ""), StandardCharsets.UTF_8);
                    Path modFolderPath = Path.of(modFolder);
                    String modFolderName = modFolderPath.getFileName().toString();
                    Path gameModFolder = Paths.get(config.getProperty("gamePath"), "Mods", modFolderName);

                    try {
                        Files.walkFileTree(modFolderPath, new SimpleFileVisitor<>() {
                            @Override
                            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                                Path targetDir = gameModFolder.resolve(modFolderPath.relativize(dir));
                                if (!Files.exists(targetDir)) {
                                    Files.createDirectories(targetDir);
                                }
                                return FileVisitResult.CONTINUE;
                            }

                            @Override
                            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                Path targetFile = gameModFolder.resolve(modFolderPath.relativize(file));
                                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                                return FileVisitResult.CONTINUE;
                            }
                        });

                        methods.initializeModsList(modsList, execLocation);
                        methods.initializeModsList(installedModsList, config.getProperty("gamePath"));
                    } catch (IOException error) {
                        JOptionPane.showMessageDialog(null, "%s: %s".formatted(langText.getProperty("failedToCopyDir"), error.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.printf("%s: %s%n", langText.getProperty("failedToCopyDir"), error.getMessage());
                    }
                }
            }
        });

        removeButton.addActionListener(e -> {
            String formattedSelectedOptions = Arrays.toString(selectedOptions).replace(",", "\n* ").replace("[", "* ").replace("]", "");
            int result = JOptionPane.showConfirmDialog(null,
                    ("""
                    %s
                    
                    %s
                    """).formatted(langText.getProperty("removeMsg"), formattedSelectedOptions), langText.getProperty("confirm"), JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                for (String option : selectedOptions) {
                    Document doc = methods.searchModDetails(option, config.getProperty("gamePath"));
                    String modFolder = URLDecoder.decode(doc.getDocumentURI().replaceFirst("^file:/", "").replaceFirst("/?modinfo\\.xml$", ""), StandardCharsets.UTF_8);
                    boolean isModOnManager = false;
                    boolean canDelete = true;

                    try {
                        for (String mod : methods.getMods(execLocation)) {
                            if (Objects.equals(mod, option)) {
                                isModOnManager = true;
                                break;
                            }
                        }
                        if (!isModOnManager) {
                            int deleteAnyway = JOptionPane.showConfirmDialog(null,
                                    ("""
                                    %s:
                                    
                                    %s
                                    %s
                                    %s
                                    
                                    %s
                                    
                                    * %s
                                    """).formatted(langText.getProperty("noteMsg"),langText.getProperty("modNotOnManagerWarning1"),langText.getProperty("modNotOnManagerWarning2"),langText.getProperty("modNotOnManagerWarning3"),langText.getProperty("removeMsg"), option),
                                    langText.getProperty("confirm"), JOptionPane.YES_NO_OPTION);
                            if (deleteAnyway == JOptionPane.NO_OPTION) {
                                canDelete = false;
                            }
                        }
                        if (canDelete) {
                            Files.walkFileTree(Path.of(modFolder), new SimpleFileVisitor<>() {
                                @Override
                                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                    Files.delete(file);
                                    return FileVisitResult.CONTINUE;
                                }

                                @Override
                                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                    Files.delete(dir);
                                    return FileVisitResult.CONTINUE;
                                }
                            });
                            methods.initializeModsList(modsList, execLocation);
                            methods.initializeModsList(installedModsList, config.getProperty("gamePath"));
                        }
                    } catch (IOException error) {
                        JOptionPane.showMessageDialog(null, "%s: %s".formatted(langText.getProperty("failedToDeleteDir"), error.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                        System.err.printf("%s: %s%n", langText.getProperty("failedToDeleteDir"), error.getMessage());
                    }
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

        if (currentJar.endsWith(".jar")) {
            ProcessBuilder builderJar = new ProcessBuilder(java, "-jar", currentJar);
            builderJar.start();
        } else {
            ProcessBuilder builderClass = new ProcessBuilder(java, "-cp", classpath, mainClass);
            builderClass.start();
        }

        System.exit(0);
    }

    public static void main(String[] args) throws IOException {
        new MainUI("7 Days to die Mod Manager");
    }
}
