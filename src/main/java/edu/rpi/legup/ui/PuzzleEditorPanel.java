package edu.rpi.legup.ui;

import static java.lang.System.exit;

import edu.rpi.legup.app.GameBoardFacade;
import edu.rpi.legup.app.LegupPreferences;
import edu.rpi.legup.controller.BoardController;
import edu.rpi.legup.controller.EditorElementController;
import edu.rpi.legup.history.ICommand;
import edu.rpi.legup.history.IHistoryListener;
import edu.rpi.legup.model.Puzzle;
import edu.rpi.legup.model.PuzzleExporter;
import edu.rpi.legup.model.tree.Tree;
import edu.rpi.legup.model.tree.TreeNode;
import edu.rpi.legup.model.tree.TreeTransition;
import edu.rpi.legup.save.ExportFileException;
import edu.rpi.legup.save.InvalidFileFormatException;
import edu.rpi.legup.ui.HomePanel;
import edu.rpi.legup.ui.boardview.BoardView;
import edu.rpi.legup.ui.proofeditorui.treeview.TreeViewSelection;
import edu.rpi.legup.ui.puzzleeditorui.elementsview.ElementFrame;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PuzzleEditorPanel extends LegupPanel implements IHistoryListener {

    private static final Logger LOGGER = LogManager.getLogger(PuzzleEditorPanel.class.getName());
    private JMenu[] menus;
    private JMenuItem helpLegup, aboutLegup;
    private JMenuBar menuBar;
    private JToolBar toolBar1;
    private JToolBar toolBar2;
    private JFileChooser folderBrowser;
    private JFrame frame;
    private JButton[] buttons;
    JSplitPane splitPanel;
    private JButton[] toolBar1Buttons;
    private JButton[] toolBar2Buttons;
    private JPanel elementPanel;
    private DynamicView dynamicBoardView;
    private BoardView boardView;
    private TitledBorder boardBorder;
    // private JSplitPane splitPanel, topHalfPanel;
    private FileDialog fileDialog;
    private JMenuItem undo, redo, fitBoardToScreen;
    private ElementFrame elementFrame;
    private JPanel treePanel;
    private LegupUI legupUI;
    private EditorElementController editorElementController;
    private CreatePuzzleDialog cpd;
    private HomePanel hp;
    private boolean existingPuzzle;
    private String fileName;
    private File puzzleFile;

    public PuzzleEditorPanel(FileDialog fileDialog, JFrame frame, LegupUI legupUI) {
        this.fileDialog = fileDialog;
        this.frame = frame;
        this.legupUI = legupUI;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 700));
    }

    protected void setupContent() {
        JSplitPane splitPanel;
        JPanel elementBox = new JPanel(new BorderLayout());

        editorElementController = new EditorElementController();
        elementFrame = new ElementFrame(editorElementController);
        elementBox.add(elementFrame, BorderLayout.WEST);

        dynamicBoardView =
                new DynamicView(new ScrollView(new BoardController()), DynamicViewType.BOARD);
        TitledBorder titleBoard = BorderFactory.createTitledBorder("Board");
        titleBoard.setTitleJustification(TitledBorder.CENTER);
        dynamicBoardView.setBorder(titleBoard);

        JPanel boardPanel = new JPanel(new BorderLayout());
        splitPanel =
                new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, elementFrame, dynamicBoardView);
        splitPanel.setPreferredSize(new Dimension(600, 400));

        boardPanel.add(splitPanel);
        boardBorder = BorderFactory.createTitledBorder("Board");
        boardBorder.setTitleJustification(TitledBorder.CENTER);

        elementBox.add(boardPanel);
        this.add(elementBox);

        splitPanel.setDividerLocation(splitPanel.getMaximumDividerLocation() + 100);
        this.splitPanel = splitPanel;
        revalidate();
    }

    public void setMenuBar() {
        String os = LegupUI.getOS();
        menuBar = new JMenuBar();
        menus = new JMenu[3];

        // create menus

        // FILE
        menus[0] = new JMenu("File");

        // file>new
        JMenuItem openPuzzle = new JMenuItem("Open");
        openPuzzle.addActionListener((ActionEvent) -> loadPuzzle());
        if (os.equals("mac")) {
            openPuzzle.setAccelerator(
                    KeyStroke.getKeyStroke(
                            'O', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else {
            openPuzzle.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
        }
        // file>create
        JMenuItem createPuzzle = new JMenuItem("Create");
        createPuzzle.addActionListener((ActionEvent) -> {
            hp = new HomePanel(this.fileDialog, this.frame, this.legupUI);
            cpd = new CreatePuzzleDialog(this.frame, hp);
            cpd.setLocationRelativeTo(null);
            cpd.setVisible(true);
            existingPuzzle = false;
        });
        if (os.equals("mac")) {
            createPuzzle.setAccelerator(
                    KeyStroke.getKeyStroke(
                            'C', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else {
            createPuzzle.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
        }


        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener((ActionEvent) -> exitEditor());
        if (os.equals("mac")) {
            exit.setAccelerator(
                    KeyStroke.getKeyStroke(
                            'Q', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else {
            exit.setAccelerator(KeyStroke.getKeyStroke('Q', InputEvent.CTRL_DOWN_MASK));
        }
        menus[0].add(openPuzzle);
        menus[0].add(createPuzzle);
        //menus[0].add(directSavePuzzle);
        menus[0].add(exit);

        // EDIT
        menus[1] = new JMenu("Edit");
        // edit>undo
        undo = new JMenuItem("Undo");
        // edit>redo
        redo = new JMenuItem("Redo");
        fitBoardToScreen = new JMenuItem("Fit Board to Screen");

        menus[1].add(undo);
        undo.addActionListener((ActionEvent) -> GameBoardFacade.getInstance().getHistory().undo());
        if (os.equals("mac")) {
            undo.setAccelerator(
                    KeyStroke.getKeyStroke(
                            'Z', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        } else {
            undo.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
        }

        menus[1].add(redo);

        // Created action to support two keybinds (CTRL-SHIFT-Z, CTRL-Y)
        Action redoAction =
                new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        GameBoardFacade.getInstance().getHistory().redo();
                    }
                };
        if (os.equals("mac")) {
            redo.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                    .put(
                            KeyStroke.getKeyStroke(
                                    'Z',
                                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                                            + InputEvent.SHIFT_DOWN_MASK),
                            "redoAction");
            redo.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                    .put(
                            KeyStroke.getKeyStroke(
                                    'Y', Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                            "redoAction");
            redo.setAccelerator(
                    KeyStroke.getKeyStroke(
                            'Z',
                            Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                                    + InputEvent.SHIFT_DOWN_MASK));
        } else {
            redo.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                    .put(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK), "redoAction");
            redo.getInputMap(WHEN_IN_FOCUSED_WINDOW)
                    .put(
                            KeyStroke.getKeyStroke(
                                    'Z', InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK),
                            "redoAction");
            redo.getActionMap().put("redoAction", redoAction);

            // Button in menu will show CTRL-SHIFT-Z as primary keybind
            redo.setAccelerator(
                    KeyStroke.getKeyStroke(
                            'Z', InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        }

        menus[1].add(fitBoardToScreen);
        fitBoardToScreen.addActionListener(
                (ActionEvent) -> dynamicBoardView.fitBoardViewToScreen());

        // HELP
        menus[2] = new JMenu("Help");
        helpLegup = new JMenuItem("Help Legup");
        aboutLegup = new JMenuItem("About Legup");
        menus[2].add(helpLegup);
        menus[2].add(aboutLegup);
        helpLegup.addActionListener(
                l -> {
                    try {
                        java.awt.Desktop.getDesktop()
                                .browse(URI.create("https://github.com/Bram-Hub/LEGUP/wiki"));
                    } catch (IOException e) {
                        LOGGER.error("Can't open web page");
                    }
                });
        menus[2].add(aboutLegup);
        aboutLegup.addActionListener(
                l -> {
                    JOptionPane.showMessageDialog(null, "Version: 5.1.0");
                });
        // add menus to menubar
        for (JMenu menu : menus) {
            menuBar.add(menu);
        }
        frame.setJMenuBar(menuBar);
    }

    public void exitEditor() {
        // Wipes the puzzle entirely as if LEGUP just started
        GameBoardFacade.getInstance().clearPuzzle();
        this.legupUI.displayPanel(0);
        treePanel = null;
        boardView = null;
    }

    @Override
    public void makeVisible() {
        this.removeAll();
        setupToolBar1();
        setupContent();
        setMenuBar();
    }
    private void setupToolBar1() {
        setToolBar1Buttons(new JButton[2]);

        URL open_url =
                ClassLoader.getSystemClassLoader()
                        .getResource("edu/rpi/legup/images/Legup/Open.png");
        ImageIcon OpenImageIcon = new ImageIcon(open_url);
        Image OpenImage = OpenImageIcon.getImage();
        OpenImageIcon =
                new ImageIcon(
                        OpenImage.getScaledInstance(
                                this.TOOLBAR_ICON_SCALE,
                                this.TOOLBAR_ICON_SCALE,
                                Image.SCALE_SMOOTH));

        JButton open = new JButton("Open", OpenImageIcon);
        open.setFocusPainted(false);
        open.addActionListener((ActionEvent) -> loadPuzzle());

        getToolBar1Buttons()[0] = open;

        toolBar1 = new JToolBar();
        toolBar1.setFloatable(false);
        toolBar1.setRollover(true);
        toolBar1.add(getToolBar1Buttons()[0]);

        URL create_url =
                ClassLoader.getSystemClassLoader()
                        .getResource("edu/rpi/legup/images/Legup/Open Puzzle.png");
        ImageIcon CreateImageIcon = new ImageIcon(create_url);
        Image CreateImage = CreateImageIcon.getImage();
        CreateImageIcon =
                new ImageIcon(
                        CreateImage.getScaledInstance(
                                this.TOOLBAR_ICON_SCALE,
                                this.TOOLBAR_ICON_SCALE,
                                Image.SCALE_SMOOTH));

        JButton create = new JButton("Create", CreateImageIcon);
        create.setFocusPainted(false);
        create.addActionListener((ActionEvent) -> {
            hp = new HomePanel(this.fileDialog, this.frame, this.legupUI);
            cpd = new CreatePuzzleDialog(this.frame, hp);
            cpd.setLocationRelativeTo(null);
            cpd.setVisible(true);
            existingPuzzle = false;
        });
        getToolBar1Buttons()[1] = create;

        toolBar1.setFloatable(false);
        toolBar1.setRollover(true);
        toolBar1.add(getToolBar1Buttons()[1]);

        this.add(toolBar1, BorderLayout.NORTH);
    }

    private void setupToolBar2() {
        toolBar2 = new JToolBar();
        toolBar2.setFloatable(false);
        toolBar2.setRollover(true);
        setToolBar2Buttons(new JButton[3]);

        URL reset =
                ClassLoader.getSystemClassLoader()
                        .getResource("edu/rpi/legup/images/Legup/Reset.png");
        ImageIcon ResetImageIcon = new ImageIcon(reset);
        Image ResetImage = ResetImageIcon.getImage();
        ResetImageIcon =
                new ImageIcon(
                        ResetImage.getScaledInstance(
                                this.TOOLBAR_ICON_SCALE,
                                this.TOOLBAR_ICON_SCALE,
                                Image.SCALE_SMOOTH));

        JButton resetButton = new JButton("Reset", ResetImageIcon);
        resetButton.setFocusPainted(false);

        resetButton.addActionListener(
            a -> {
                if (existingPuzzle) {
                    legupUI.getPuzzleEditor().loadPuzzle(fileName, puzzleFile);
                }
                else {
                    if (cpd.getGame().equals("ShortTruthTable")) {
                        GameBoardFacade.getInstance().loadPuzzle(cpd.getGame(), cpd.getTextArea());
                    }
                    else {
                        GameBoardFacade.getInstance().loadPuzzle(cpd.getGame(), Integer.valueOf(cpd.getRows()), Integer.valueOf(cpd.getColumns()));
                    }
                }
            });

        getToolBar2Buttons()[0] = resetButton;
        toolBar2.add(getToolBar2Buttons()[0]);

        URL save_as =
                ClassLoader.getSystemClassLoader()
                        .getResource("edu/rpi/legup/images/Legup/Save.png");
        ImageIcon SaveAsImageIcon = new ImageIcon(save_as);
        Image SaveAsImage = SaveAsImageIcon.getImage();
        SaveAsImageIcon =
                new ImageIcon(
                        SaveAsImage.getScaledInstance(
                                this.TOOLBAR_ICON_SCALE,
                                this.TOOLBAR_ICON_SCALE,
                                Image.SCALE_SMOOTH));

        JButton saveas = new JButton("Save As", SaveAsImageIcon);
        saveas.setFocusPainted(false);
        saveas.addActionListener((ActionEvent) -> savePuzzle());

        getToolBar2Buttons()[1] = saveas;
        toolBar2.add(getToolBar2Buttons()[1]);

        URL save_and_solve =
                ClassLoader.getSystemClassLoader()
                        .getResource("edu/rpi/legup/images/Legup/Check.png");
        ImageIcon SaveSolveImageIcon = new ImageIcon(save_and_solve);
        Image SaveSolveImage = SaveSolveImageIcon.getImage();
        SaveSolveImageIcon =
                new ImageIcon(
                        SaveSolveImage.getScaledInstance(
                                this.TOOLBAR_ICON_SCALE,
                                this.TOOLBAR_ICON_SCALE,
                                Image.SCALE_SMOOTH));

        JButton saveandsolve = new JButton("Save & Solve", SaveSolveImageIcon);
        saveandsolve.setFocusPainted(false);
        saveandsolve.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (GameBoardFacade.getInstance().getPuzzleModule() != null) {
                            String filename = savePuzzle();
                            File puzzlename = new File(filename);
                            System.out.println(filename);

                            GameBoardFacade.getInstance().getLegupUI().displayPanel(1);
                            GameBoardFacade.getInstance()
                                    .getLegupUI()
                                    .getProofEditor()
                                    .loadPuzzle(filename, new File(filename));
                            String puzzleName =
                                    GameBoardFacade.getInstance().getPuzzleModule().getName();
                            frame.setTitle(puzzleName + " - " + puzzlename.getName());
                        }
                    }
                });
        getToolBar2Buttons()[2] = saveandsolve;
        toolBar2.add(getToolBar2Buttons()[2]);

        this.add(toolBar2, BorderLayout.NORTH);
    }

    public void loadPuzzleFromHome(String game, int rows, int columns)
            throws IllegalArgumentException {
        GameBoardFacade facade = GameBoardFacade.getInstance();
        try {
            facade.loadPuzzle(game, rows, columns);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    public void loadPuzzleFromHome(String game, String[] statements) {
        GameBoardFacade facade = GameBoardFacade.getInstance();
        try {
            facade.loadPuzzle(game, statements);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(exception.getMessage());
        } catch (RuntimeException e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
    }

    // File opener
    public Object[] promptPuzzle() {
        GameBoardFacade facade = GameBoardFacade.getInstance();
        if (facade.getBoard() != null) {
            if (noQuit("Open an existing puzzle?")) {
                return new Object[0];
            }
        }

        LegupPreferences preferences = LegupPreferences.getInstance();
        String preferredDirectory = preferences.getUserPref(LegupPreferences.WORK_DIRECTORY);
        if (preferences.getSavedPath() != "") {
            preferredDirectory = preferences.getSavedPath();
        }

        File preferredDirectoryFile = new File(preferredDirectory);
        JFileChooser fileBrowser = new JFileChooser(preferredDirectoryFile);
        String fileName = null;
        File puzzleFile = null;

        fileBrowser.showOpenDialog(this);
        fileBrowser.setVisible(true);
        fileBrowser.setCurrentDirectory(new File(preferredDirectory));
        fileBrowser.setDialogTitle("Select Proof File");
        fileBrowser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileBrowser.setAcceptAllFileFilterUsed(false);

        File puzzlePath = fileBrowser.getSelectedFile();
        System.out.println(puzzlePath.getAbsolutePath());

        if (puzzlePath != null) {
            fileName = puzzlePath.getAbsolutePath();
            String lastDirectoryPath = fileName.substring(0, fileName.lastIndexOf(File.separator));
            preferences.setSavedPath(lastDirectoryPath);
            puzzleFile = puzzlePath;
        } else {
            // The attempt to prompt a puzzle ended gracefully (cancel)
            return null;
        }

        return new Object[] {fileName, puzzleFile};
    }

    public void loadPuzzle() {
        Object[] items = promptPuzzle();
        // Return if items == null (cancel)
        if (items == null) {
            return;
        }
        String fileName = (String) items[0];
        File puzzleFile = (File) items[1];
        loadPuzzle(fileName, puzzleFile);
    }

    public void loadPuzzle(String fileName, File puzzleFile) {
        if (puzzleFile != null && puzzleFile.exists()) {
            try {
                legupUI.displayPanel(2);
                GameBoardFacade.getInstance().loadPuzzleEditor(fileName);
                String puzzleName = GameBoardFacade.getInstance().getPuzzleModule().getName();
                frame.setTitle(puzzleName + " - " + puzzleFile.getName());
                existingPuzzle = true;
                this.fileName = fileName;
                this.puzzleFile = puzzleFile;
            } catch (InvalidFileFormatException e) {
                legupUI.displayPanel(0);
                LOGGER.error(e.getMessage());
                JOptionPane.showMessageDialog(
                        null,
                        "File does not exist, cannot be read, or cannot be edited",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                loadPuzzle();
            }
        }
    }

    public boolean noQuit(String instr) {
        int n = JOptionPane.showConfirmDialog(null, instr, "Confirm", JOptionPane.YES_NO_OPTION);
        return n != JOptionPane.YES_OPTION;
    }

    @Override
    public void onPushChange(ICommand command) {}

    @Override
    public void onUndo(boolean isBottom, boolean isTop) {}

    @Override
    public void onRedo(boolean isBottom, boolean isTop) {}

    @Override
    public void onClearHistory() {
        // undo.setEnabled(false);
        // redo.setEnabled(false);
    }

    public BoardView getBoardView() {
        return boardView;
    }
    public JButton[] getToolBar1Buttons() {
        return toolBar1Buttons;
    }

    public void setToolBar1Buttons(JButton[] toolBar1Buttons) {
        this.toolBar1Buttons = toolBar1Buttons;
    }

    public JButton[] getToolBar2Buttons() {
        return toolBar2Buttons;
    }

    public void setToolBar2Buttons(JButton[] toolBar2Buttons) {
        this.toolBar2Buttons = toolBar2Buttons;
    }

    private void repaintAll() {
        boardView.repaint();
    }

    public void setPuzzleView(Puzzle puzzle) {
        this.boardView = puzzle.getBoardView();
        editorElementController.setElementController(boardView.getElementController());
        dynamicBoardView = new DynamicView(boardView, DynamicViewType.BOARD);
        if (this.splitPanel != null) {
            this.splitPanel.setRightComponent(dynamicBoardView);
            this.splitPanel.setVisible(true);
        }

        TitledBorder titleBoard =
                BorderFactory.createTitledBorder(boardView.getClass().getSimpleName());
        titleBoard.setTitleJustification(TitledBorder.CENTER);
        dynamicBoardView.setBorder(titleBoard);

        puzzle.addBoardListener(puzzle.getBoardView());
        System.out.println("Setting elements");
        if (this.elementFrame != null) {
            elementFrame.setElements(puzzle);
        }
        toolBar1.setVisible(false);
        setupToolBar2();
    }

    /** Saves a puzzle */
    private void direct_save() {
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        if (puzzle == null) {
            return;
        }
        String fileName = GameBoardFacade.getInstance().getCurFileName();
        if (fileName != null) {
            try {
                PuzzleExporter exporter = puzzle.getExporter();
                if (exporter == null) {
                    throw new ExportFileException("Puzzle exporter null");
                }
                exporter.exportPuzzle(fileName);
            } catch (ExportFileException e) {
                e.printStackTrace();
            }
        }
    }

    private String savePuzzle() {
        Puzzle puzzle = GameBoardFacade.getInstance().getPuzzleModule();
        if (puzzle == null) {
            return "";
        }

        //  for TreeTent, need to check validity before saving
        if (Objects.equals(puzzle.getName(), "TreeTent")) {
            if (!puzzle.checkValidity()) {
                int input =
                        JOptionPane.showConfirmDialog(
                                null,
                                "The puzzle you edited is not "
                                        + "valid, would you still like to save? ");
                if (input != 0) {
                    return "";
                }
            }
        }

        LegupPreferences preferences = LegupPreferences.getInstance();
        File preferredDirectory =
                new File(preferences.getUserPref(LegupPreferences.WORK_DIRECTORY));
        if (preferences.getSavedPath() != "") {
            preferredDirectory = new File(preferences.getSavedPath());
        }
        folderBrowser = new JFileChooser(preferredDirectory);

        folderBrowser.showSaveDialog(this);
        folderBrowser.setVisible(true);
        folderBrowser.setCurrentDirectory(new File(LegupPreferences.WORK_DIRECTORY));
        folderBrowser.setDialogTitle("Select Directory");
        folderBrowser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        folderBrowser.setAcceptAllFileFilterUsed(false);

        String path = folderBrowser.getSelectedFile().getAbsolutePath();

        if (path != null) {
            try {
                PuzzleExporter exporter = puzzle.getExporter();
                if (exporter == null) {
                    throw new ExportFileException("Puzzle exporter null");
                }
                exporter.exportPuzzle(path);
            } catch (ExportFileException e) {
                e.printStackTrace();
            }
        }
        return path;
    }

    public DynamicView getDynamicBoardView() {
        return dynamicBoardView;
    }
}
