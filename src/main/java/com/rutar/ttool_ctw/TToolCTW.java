package com.rutar.ttool_ctw;

import java.io.*;
import java.awt.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import java.nio.file.*;
import javax.imageio.*;
import java.util.jar.*;
import java.awt.event.*;
import java.awt.image.*;
import java.nio.charset.*;
import javax.swing.event.*;
import javax.swing.table.*;
import com.formdev.flatlaf.*;
import javax.swing.filechooser.*;
import com.rutar.ua_translator.*;
import com.formdev.flatlaf.themes.*;

import java.util.List;

import static javax.swing.JOptionPane.*;
import static javax.swing.JFileChooser.*;
import static java.nio.charset.StandardCharsets.*;

// ............................................................................
/// Головний клас програми
/// @author Rutar_Andriy
/// 14.01.2026

public class TToolCTW extends JFrame {

private File inputFile;                                         // вхідний файл
private File outputFile;                                       // вихідний файл

private final JFileChooser fileOpen;           // відкривання/збереження файлів
private final JFileChooser fntCompile;                  // компілювання шрифтів
private final JFileChooser fntDecompile;              // декомпілювання шрифтів

private String appDescription;                                 // опис програми
private DefaultTableModel tableModel;              // стандартна модель таблиці

private boolean dataWasChanged;                // якщо true - дані були змінені

// ............................................................................

private File tmpFile;                                       // допоміжна змінна
private SearchDialog searchDialog;         // діалогове вікно пошуку інформації

private List<String> allStrings;             // масив усіх рядків мовного файлу
private List<String> patchStrings;                   // масив усіх рядків патчу

public static int EDITABLE_COLUMN = 1;  // номер стовбця, який можна редагувати

// Домашня директорія користувача
public static final File HOME_DIR = FileSystemView.getFileSystemView()
                                                  .getHomeDirectory();

public static boolean debug = true;  // якщо true - увімк. режим налагоджування

// ============================================================================
/// Конструктор за замовчуванням

public TToolCTW() {

initComponents();
initAppIcons();

fileOpen     = Utils.getFileChooser("csv", FILES_ONLY,
                                    "CTW файли локалізації");
fntCompile   = Utils.getFileChooser("fnt", DIRECTORIES_ONLY,
                                    "CTW файли шрифтів");
fntDecompile = Utils.getFileChooser("fnt", FILES_ONLY,
                                    "CTW файли шрифтів");
}

// ============================================================================
/// Головний метод програми
/// @param args масив переданих параметрів

public static void main (String args[]) {
    
    if (args.length > 0 &&
        args[0].equals("--debug")) { debug = true; }
    
    // ........................................................................
    
    UATranslator.init();
    UIManager.put("FileChooser.readOnly", true);

    JFrame .setDefaultLookAndFeelDecorated(true);
    JDialog.setDefaultLookAndFeelDecorated(true);
    
    FlatLaf.registerCustomDefaultsSource("com.rutar.ttool_ctw.themes");

    try { FlatMacDarkLaf.setup(); }
    catch (Exception e) {}
    
    // ........................................................................
    
    EventQueue.invokeLater(() -> {
        new TToolCTW().setVisible(true);
    });
}

// ============================================================================
/// Відкривання файлів

private void showOpenDialog() {

// Дані змінилися - запитуємо чи відкривати новий файл
if (dataWasChanged) { 

String saveDataQuestion = """
    У відкритому файлі присутні зміни. При відкриванні
    нового файлу вони будуть втрачені. Бажаєте продовжити?
    """;

int answer = showConfirmDialog(this, saveDataQuestion,
                              "Повідомлення", YES_NO_OPTION);

if (answer != YES_OPTION) { return; }

}

// ............................................................................

int result = fileOpen.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

openCsvFile();
updateAppTitle();

}

// ============================================================================
/// Відкривання *.csv файлів

private void openCsvFile() {

allStrings = null;
inputFile = fileOpen.getSelectedFile();

try { allStrings = Files.readAllLines(inputFile.toPath(), UTF_8); }
catch (IOException _) { showMessageDialog(this, "Не вдалося прочитати файл",
                                                "Помилка", 0);
                        return; }

prepareNewTable(allStrings.getFirst().split(";"));

// ............................................................................

String[] values;
ArrayList<String> newRow = new ArrayList<>();

for (int z = 1; z < allStrings.size(); z++) {
    
    values = allStrings.get(z).split(";");
    
    newRow.clear();
    newRow.add(String.valueOf(z));
    
    for (String value : values)
        { newRow.add(values.length == 0 ? "" : value); }
    
    tableModel.addRow(newRow.toArray(String[]::new));

}

// ............................................................................
    
finalizeNewTable();

}

// ============================================================================
/// Збереження файлів

private void showSaveDialog() {

// Перевіряємо чи немає у таблиці символу ";"
for (int r = 0; r < tbl_main.getRowCount();    r++) {
for (int c = 1; c < tbl_main.getColumnCount(); c++) {
    Object valueAt = tbl_main.getValueAt(r, c);
    if (valueAt != null && valueAt.toString().contains(";"))
        { tbl_main.setRowSelectionInterval(r, r);
          tbl_main.setColumnSelectionInterval(c, c);
          
          Rectangle rect = tbl_main.getCellRect(r, c, true);
          tbl_main.scrollRectToVisible(rect);
          tbl_main.changeSelection(r, c, false, false); 
          
          showMessageDialog(this, "У тексті не можна використовувати \";\"",
                                  "Повідомлення", 1);
          return; } } }

// ............................................................................

fileOpen.setSelectedFile(inputFile);
int result = fileOpen.showSaveDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

saveCsvFile();

}

// ============================================================================
/// Збереження *.csv файлів

private void saveCsvFile() {

outputFile = fileOpen.getSelectedFile();

String line, value;
int rowCount = tbl_main.getRowCount();
int colCount = tbl_main.getColumnCount();
ArrayList<String> result = new ArrayList<>();

// ............................................................................
// Записуємо назви стовбців таблиці

line = "";
for (int z = 1; z < colCount; z++)
    { line += (String) tbl_main.getColumnModel().getColumn(z).getHeaderValue();
      line += (z < colCount - 1) ? ";" : ""; }

result.add(line);

// ............................................................................
// Записуємо значення рядків таблиці

for (int r = 0; r < rowCount; r++) {
    
    line = "";
    for (int c = 1; c < colCount; c++)
        { value = (String) tbl_main.getValueAt(r, c);
          value = value != null ? Utils.replaceUnusedChars(value) : value;
          line += value == null ? "" : value;
          line += (c < colCount - 1) ? ";" : ""; }
    
    result.add(line);
}

try {

dataWasChanged = false;
Files.write(outputFile.toPath(), result, UTF_8);

updateAppTitle();
JOptionPane.showMessageDialog(this, "Файл " + outputFile.getName()
                          + " успішно збережено", "Повідомлення", 1);
}

catch (HeadlessException | IOException _)
    { showMessageDialog(this, "При збереженні файлу відбулася "
                            + "критична помилка", "Помилка", 0); }
}

// ============================================================================
/// Відображення інформації про програму

private void showInfoDialog() {

// Отримуємо текст опису програми
if (appDescription == null) {

URL descriptionUrl = getClass().getResource("others/appDescription.txt");
URL channelUrl     = getClass().getResource("others/channelURL.txt");
URL manifestUrl    = getClass().getClassLoader()
                    .getResource("META-INF/MANIFEST.MF");

try (InputStream desc = descriptionUrl.openStream();
     InputStream link = channelUrl    .openStream();
     InputStream data = manifestUrl   .openStream()) {

Attributes attributes = new Manifest(data).getMainAttributes();
    
String channelURL = new String(link.readAllBytes(), StandardCharsets.UTF_8);
String appVersion = attributes.getValue("Version");
String buildDate  = attributes.getValue("Build-Date");

appVersion = (appVersion == null) ? "0.0.1" : appVersion;
buildDate  = (buildDate  == null) ? "25.04.1995" : buildDate.split(" ")[0];

appDescription = new String(desc.readAllBytes(), StandardCharsets.UTF_8)
                    .formatted(channelURL, appVersion, buildDate); }

catch (IOException _) {} }

// ............................................................................

JEditorPane pane = new JEditorPane("text/html", appDescription);
pane.setEditable(false);
pane.setFocusable(false);

pane.addHyperlinkListener((HyperlinkEvent e) -> {
    if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
        try { Desktop.getDesktop().browse(e.getURL().toURI()); }
        catch (IOException | URISyntaxException _) { }
    }
});

showMessageDialog(this, pane, "Про програму", INFORMATION_MESSAGE);

}

// ============================================================================
/// Відображення вікна пошуку інформації

private void showSearchDialog()
    { searchDialog = new SearchDialog(this);   
      searchDialog.setVisible(true); }

// ============================================================================
/// Відображення вікна підтвердження виходу

private void showExitDialog() {

// Якщо дані не змінювалися - просто виходимо
if (!dataWasChanged) { System.exit(0); }

String saveDataQuestion = """
    Ви бажаєте вийти з програми?
    Усі незбережені дані буде втрачено
    """;

int answer = showConfirmDialog(this, saveDataQuestion,
                              "Підтвердження виходу", YES_NO_OPTION);

if (answer == YES_OPTION) { System.exit(0); }

}

// ============================================================================
/// Вибір шрифту для розпакування

private void showDecompileFontDialog() {

int result = fntDecompile.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = fntDecompile.getSelectedFile();

int resultCode = new FontProcessor().decompileFont(inputFile);

if (resultCode == 0)
    { showMessageDialog(this, "Шрифт успішно розібрано", "Повідомлення", 1); }
else
    { showMessageDialog(this, "Сталася критична помилка!", "Помилка", 0); }

}

// ============================================================================
/// Вибір розпакованого шрифту для пакування

private void showCompileFontDialog() {

tmpFile = Utils.getLastDir(fntDecompile);
if (tmpFile != null) { fntCompile.setCurrentDirectory(tmpFile); }

int result = fntCompile.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

inputFile = fntCompile.getSelectedFile();

int resultCode = new FontProcessor().compileFont(inputFile);

if (resultCode == 0)
    { showMessageDialog(this, "Шрифт успішно зібрано", "Повідомлення", 1); }
else
    { showMessageDialog(this, "Сталася критична помилка!", "Помилка", 0); }

}

// ============================================================================
/// Вибір попередньо перекладеного файлу для об'єднання прекладів

private void showLangPatchDialog() {

int count = 0;
String[] values;
String number, oldKey, newKey;

int result = fileOpen.showOpenDialog(this);
if (result != JFileChooser.APPROVE_OPTION) { return; }

patchStrings = null;
inputFile = fileOpen.getSelectedFile();

try { patchStrings = Files.readAllLines(inputFile.toPath(), UTF_8); }
catch (IOException _) { showMessageDialog(this, "Не вдалося прочитати файл",
                                                "Помилка", 0);
                        return; }

// ............................................................................
// Обробка файлів, які містять ключі та значення

if (patchStrings.getFirst().startsWith("key")) {

for (int z = 1; z < patchStrings.size(); z++) {
    
    if (z > tbl_main.getRowCount()) { break; }
    
    values = patchStrings.get(z).split(";");
    if (values.length <= 1) { continue; }

    number = (String) tbl_main.getValueAt(z - 1, 0);
    oldKey = (String) tbl_main.getValueAt(z - 1, 1);
    oldKey = oldKey == null ? "" : oldKey;
    newKey = values[0];
    
    if (z == Integer.parseInt(number) && oldKey.equals(newKey))
        { tbl_main.setValueAt(values[1], z - 1, 2);
          count++; } } }

// ............................................................................
// Обробка решти файлів

else {

for (int z = 1; z < patchStrings.size(); z++) {
    
    if (z > tbl_main.getRowCount()) { break; }
    
    values = patchStrings.get(z).split(";");
    if (values.length == 0) { continue; }

    number = (String) tbl_main.getValueAt(z - 1, 0);
    
    if (z == Integer.parseInt(number))
        { tbl_main.setValueAt(values[0], z - 1, 1);
          count++; } } }

// ............................................................................

if (count > 0) { showMessageDialog(this, "Переклади успішно об'єднано, " +
                                         "замінено " + count + " рядків",
                                         "Об'єднання перекладів", 1); }
}

// ============================================================================
/// Попередня ініціалізація нової таблиці

private void prepareNewTable (String[] columns) {

dataWasChanged = false;
inputFile = fileOpen.getSelectedFile();
sp_table.getVerticalScrollBar().setValue(0);
EDITABLE_COLUMN = columns[0].equals("key") ? 2 : 1;

tableModel = new DefaultTableModel() {
    @Override
    public boolean isCellEditable (int row, int column)
        { return column == EDITABLE_COLUMN; }
};

tbl_main.setModel(tableModel);

tableModel.addColumn("№");
for (String colimn : columns) { tableModel.addColumn(colimn); }

}

// ============================================================================
/// Завершальна ініціалізація нової таблиці

private void finalizeNewTable() {

TableColumn tColumn;

CellRender cellRenderer = new CellRender();
cellRenderer.setHorizontalAlignment(SwingConstants.CENTER);

tColumn = tbl_main.getColumnModel().getColumn(0);
tColumn.setCellRenderer(cellRenderer);
tColumn.setPreferredWidth(45);
tColumn.setResizable(false);

for (int z = 1; z < tbl_main.getColumnCount(); z++) {
    tbl_main.getColumnModel().getColumn(z).setCellRenderer(new CellRender());
    tbl_main.getColumnModel().getColumn(z).setPreferredWidth(175);    
}

// ............................................................................

updateTableInfo();

mni_find.setEnabled(true);
mni_langPatch.setEnabled(true);
tableModel.addTableModelListener((TableModelEvent e) -> {
    mni_save.setEnabled(true);
    dataWasChanged = true;
    updateAppTitle();
});

}

// ============================================================================
/// Оновлення інформації про таблицю

private void updateTableInfo() {

    String tmp;

    tmp = lbl_rowCount.getText();
    tmp = tmp.substring(0, tmp.indexOf(":") + 1) + " "
                      + tableModel.getRowCount();
    lbl_rowCount.setText(tmp);

    tmp = lbl_colCount.getText();
    tmp = tmp.substring(0, tmp.indexOf(":") + 1) + " "
                      + tableModel.getColumnCount();
    lbl_colCount.setText(tmp);
    
}

// ============================================================================
/// Оновлення заголовку головного вікна

private void updateAppTitle() {
    
    String newTitle = !dataWasChanged ? inputFile.getName() :
                                 "* " + inputFile.getName() + " *";
    
    if (!getTitle().equals(newTitle)) { setTitle(newTitle); }
}

// ============================================================================
/// Встановлення іконок для головного вікна

private void initAppIcons() {

    BufferedImage icon;
    ArrayList<Image> appIcons = new ArrayList<>();

    try {
        
    for (String resource : new String[] { "icon_16.png",
                                          "icon_32.png" }) {
        resource = "icons/" + resource;
        icon = ImageIO.read(getClass().getResourceAsStream(resource));
        appIcons.add(icon); }
    
    setIconImages(appIcons); }
    
    catch (IOException _) { }
    
}

// ============================================================================
/// Цей метод викликається з конструктора для ініціалізації форми.
/// УВАГА: НЕ змінюйте цей код. Вміст цього методу завжди 
/// перезапишеться редактором форм

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sp_table = new JScrollPane();
        tbl_main = new JTable();
        pnl_footer = new JPanel();
        lbl_colCount = new JLabel();
        lbl_rowCount = new JLabel();
        mnb_main = new JMenuBar();
        mn_file = new JMenu();
        mni_open = new JMenuItem();
        mni_save = new JMenuItem();
        sep_one = new JPopupMenu.Separator();
        mni_find = new JMenuItem();
        sep_two = new JPopupMenu.Separator();
        mni_exit = new JMenuItem();
        mn_edit = new JMenu();
        mni_fntDecompile = new JMenuItem();
        mni_fntCompile = new JMenuItem();
        sep_three = new JPopupMenu.Separator();
        mni_langPatch = new JMenuItem();
        mn_info = new JMenu();
        mni_about = new JMenuItem();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("TTool_CTW");
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                onWindowClose(evt);
            }
        });

        tbl_main.setModel(new DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tbl_main.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbl_main.setAutoscrolls(false);
        tbl_main.setIntercellSpacing(new Dimension(2, 2));
        tbl_main.setRowSelectionAllowed(false);
        tbl_main.setShowGrid(true);
        tbl_main.getTableHeader().setReorderingAllowed(false);
        sp_table.setViewportView(tbl_main);

        pnl_footer.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 5));

        lbl_colCount.setText("Кількість стовбців: 0");
        pnl_footer.add(lbl_colCount);

        lbl_rowCount.setText("Кількість рядків: 0");
        pnl_footer.add(lbl_rowCount);

        mn_file.setText("Файл");

        mni_open.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        mni_open.setText("Відкрити");
        mni_open.setActionCommand("open");
        mni_open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_open);

        mni_save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        mni_save.setText("Зберегти");
        mni_save.setActionCommand("save");
        mni_save.setEnabled(false);
        mni_save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_save);
        mn_file.add(sep_one);

        mni_find.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_DOWN_MASK));
        mni_find.setText("Пошук");
        mni_find.setActionCommand("find");
        mni_find.setEnabled(false);
        mni_find.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_find);
        mn_file.add(sep_two);

        mni_exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));
        mni_exit.setText("Вихід");
        mni_exit.setActionCommand("exit");
        mni_exit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_file.add(mni_exit);

        mnb_main.add(mn_file);

        mn_edit.setText("Правка");

        mni_fntDecompile.setText("Розпакувати шрифт");
        mni_fntDecompile.setActionCommand("decompileFont");
        mni_fntDecompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_fntDecompile);

        mni_fntCompile.setText("Запакувати шрифт");
        mni_fntCompile.setActionCommand("compileFont");
        mni_fntCompile.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_fntCompile);
        mn_edit.add(sep_three);

        mni_langPatch.setText("Об'єднати переклади");
        mni_langPatch.setActionCommand("langPatch");
        mni_langPatch.setEnabled(false);
        mni_langPatch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_edit.add(mni_langPatch);

        mnb_main.add(mn_edit);

        mn_info.setText("Інфо");

        mni_about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        mni_about.setText("Про програму");
        mni_about.setActionCommand("info");
        mni_about.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                onMenuClick(evt);
            }
        });
        mn_info.add(mni_about);

        mnb_main.add(mn_info);

        setJMenuBar(mnb_main);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sp_table, GroupLayout.DEFAULT_SIZE, 588, Short.MAX_VALUE)
                    .addComponent(pnl_footer, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sp_table, GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnl_footer, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

// ============================================================================
/// Прослуховування пунктів меню програми

    private void onMenuClick(ActionEvent evt) {//GEN-FIRST:event_onMenuClick

    switch (evt.getActionCommand()) {

        case "open" -> showOpenDialog();
        case "save" -> showSaveDialog();
        case "find" -> showSearchDialog();
        case "exit" -> showExitDialog();
        case "info" -> showInfoDialog();

        case "decompileFont" -> showDecompileFontDialog();
        case "compileFont"   -> showCompileFontDialog();
        case "langPatch"     -> showLangPatchDialog();

    }   
    }//GEN-LAST:event_onMenuClick

// ============================================================================
/// Прослуховування закривання вікна

    private void onWindowClose(WindowEvent evt) {//GEN-FIRST:event_onWindowClose
        showExitDialog();
    }//GEN-LAST:event_onWindowClose

// ============================================================================
/// Список усіх об'явлених змінних

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel lbl_colCount;
    private JLabel lbl_rowCount;
    private JMenu mn_edit;
    private JMenu mn_file;
    private JMenu mn_info;
    private JMenuBar mnb_main;
    private JMenuItem mni_about;
    private JMenuItem mni_exit;
    private JMenuItem mni_find;
    private JMenuItem mni_fntCompile;
    private JMenuItem mni_fntDecompile;
    private JMenuItem mni_langPatch;
    private JMenuItem mni_open;
    private JMenuItem mni_save;
    private JPanel pnl_footer;
    private JPopupMenu.Separator sep_one;
    private JPopupMenu.Separator sep_three;
    private JPopupMenu.Separator sep_two;
    private JScrollPane sp_table;
    public JTable tbl_main;
    // End of variables declaration//GEN-END:variables

// Кінець класу TToolCTW ======================================================

}