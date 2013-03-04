/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sc4ltextor;

import java.io.BufferedWriter;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javax.swing.JOptionPane;
import ssp.dbpf.DBPFCollection;
import ssp.dbpf.event.DBPFException;
import ssp.dbpf.io.DBPFReader;
import ssp.dbpf.io.DBPFWriter;
import ssp.dbpf.tgi.TGIKey;
import ssp.dbpf.tgi.TGIKeys;
import ssp.dbpf.types.DBPFLText;
import ssp.dbpf.types.DBPFType;

/**
 * FXML Controller class
 *
 * @author Casper
 */
public class LTEXTorController implements Initializable {

    @FXML
    private Insets x2;
    @FXML
    private Font x1;
    public DBPFCollection theDAT;
    private List<DBPFType> typeList;
    public TableView<LTTable> table;
    private final ObservableList<LTTable> tabledata = FXCollections.observableArrayList();
    public TableColumn gidcol, iidcol, contcol, indexcol;
    public TextField transin, transout, incr, regexField;
    public Label lastaction;
    public TextArea textar;
    public CheckBox regexCheck;

    public void handleIncrement() throws DBPFException {
        long maxLength = Long.parseLong("4294967280");
        //long maxLength = Long.parseLong("100");           for testing purposes
        long increment = Long.parseLong(incr.getText());
        for (DBPFType type : typeList) {
            long currGID = type.getGID();
            if (currGID + increment >= maxLength) {
                JOptionPane.showMessageDialog(null, "Could not increment the Group IDs.\nOne (or more) entries have a GID higher than the maximum value of 0xFFFFFFF0", "Failed to increment!", JOptionPane.ERROR_MESSAGE);
                lastAction("ERROR Incrementing", Color.DARKRED);
                return;
            }
        }
        for (DBPFType type : typeList) {
            long currGID = type.getGID();
            if (currGID + increment < maxLength) {
                type.setTGIKey(new TGIKey(type.getTID(), type.getGID() + increment, type.getIID()));
            }
        }
        lastAction("Incremented GIDs by " + increment, Color.DARKGREEN);

        readDataIntoTable(true);
    }

    public void handleOpenButton() throws DBPFException {
        FileChooser fc = new FileChooser();
        File file;

        //Set extension filter
        FileChooser.ExtensionFilter extFilterDat = new FileChooser.ExtensionFilter("DAT files (*.dat)", "*.dat");
        FileChooser.ExtensionFilter extFilterLot = new FileChooser.ExtensionFilter("SC4LOT files (*.sc4lot)", "*.sc4lot");
        FileChooser.ExtensionFilter extFilterDesc = new FileChooser.ExtensionFilter("SC4DESC files (*.sc4desc)", "*.sc4desc");
        FileChooser.ExtensionFilter extFilterModel = new FileChooser.ExtensionFilter("SC4MODEL files (*.sc4model)", "*.sc4model");
        fc.getExtensionFilters().addAll(extFilterDat, extFilterLot, extFilterDesc, extFilterModel);

        //Show open file dialog
        file = fc.showOpenDialog(null);
        theDAT = DBPFReader.readCollection(file);

        lastAction("Opened " + theDAT.getFilename().getName(), Color.DARKGREEN);
        readDataIntoTable(false);
    }

    public void handleSaveButton() throws DBPFException {
        FileChooser fc = new FileChooser();
        File file;

        //Set extension filter
        FileChooser.ExtensionFilter extFilterDat = new FileChooser.ExtensionFilter("DAT files (*.dat)", "*.dat");
        FileChooser.ExtensionFilter extFilterLot = new FileChooser.ExtensionFilter("SC4LOT files (*.sc4lot)", "*.sc4lot");
        FileChooser.ExtensionFilter extFilterDesc = new FileChooser.ExtensionFilter("SC4DESC files (*.sc4desc)", "*.sc4desc");
        FileChooser.ExtensionFilter extFilterModel = new FileChooser.ExtensionFilter("SC4MODEL files (*.sc4model)", "*.sc4model");
        fc.getExtensionFilters().addAll(extFilterDat, extFilterLot, extFilterDesc, extFilterModel);

        //Show save file dialog
        file = fc.showSaveDialog(null);

        if (file != null) {
            theDAT.setFilename(file);
            DBPFWriter.writeCollection(theDAT);
            lastAction("Saved " + file.getName(), Color.DARKGREEN);
        }
    }

    public void handleExport() throws Exception {
        FileChooser fc = new FileChooser();
        File file;
        typeList = theDAT.getTypeList();

        //Set extension filter
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (.txt)", "*.dat");
        fc.getExtensionFilters().addAll(extFilter);

        //Show save file dialog
        file = fc.showSaveDialog(null);

        if (file != null) {
            try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
                out.write("~~~ BEGIN ~~~");
                out.write("FNAME: " + theDAT.getFilename());
                out.newLine();
                out.newLine();
                for (int i = 0; i < typeList.size(); i++) {
                    DBPFType type = typeList.get(i);
                    if (type.getTGIKey().equals(TGIKeys.LTEXT.getTGIKey())) {
                        DBPFLText ltext = (DBPFLText) type;
                        out.write("~~~" + ltext.getTGIKey() + "~~~");
                        out.newLine();
                        out.write(ltext.getString());
                        out.newLine();
                    }
                }
                out.write("~~~ END ~~~");
                lastAction("Exported LTEXTs", Color.DARKGREEN);
            } catch (Exception e) {
                System.out.println("Could not export data to file. " + e.getLocalizedMessage());
                lastAction("ERROR Exporting", Color.DARKRED);
            }
        } else {
            lastAction("ERROR Exporting", Color.DARKRED);
        }
    }

    public void handleImport() throws Exception {
        throw new UnsupportedOperationException("Not yet implemented");

        /**
         * FileChooser fc = new FileChooser(); File file;
         *
         * //Set extension filter FileChooser.ExtensionFilter extFilter = new
         * FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
         * fc.getExtensionFilters().addAll(extFilter);
         *
         * //Show open file dialog file = fc.showOpenDialog(null); theDAT = new
         * DBPFCollection();
         *
         * try (BufferedReader in = new BufferedReader(new FileReader(file))) {
         * theDAT.setFilename(new File(in.readLine().substring(7))); //first
         * line, filename for imported dat in.readLine(); //second line, empty
         * String line = null; while((line = in.readLine()) != null) { //read
         * until EOF } } catch (IOException e) { System.out.println("Could not
         * export data to file. " + e.getLocalizedMessage()); }
         */
    }

    public void handleBatchTranslation() {
        typeList = theDAT.getTypeList();
        String transintext = transin.getText();
        String transouttext = transout.getText();
        int counter = 0;

        for (int i = 0; i < typeList.size(); i++) {
            DBPFType type = typeList.get(i);

            if (type.getTGIKey().equals(TGIKeys.LTEXT.getTGIKey())) {
                DBPFLText ltext = (DBPFLText) type;
                ltext.setString(ltext.getString().replace(transintext, transouttext));
                counter++;
            }
        }

        lastAction("Translated " + counter + " LTEXTs", Color.DARKGREEN);
        //Refresh the table
        readDataIntoTable(true);
    }

    public void handleRegex() throws DBPFException {
        typeList = theDAT.getTypeList();
        int counter = 0;
        String input = regexCheck.isSelected() ? regexField.getText().replace("\\","\\\\") : regexField.getText();
        String[] inputregex = input.split("/");
        
        for (int i = 0; i < typeList.size(); i++) {
            DBPFType type = typeList.get(i);

            if (type.getTGIKey().equals(TGIKeys.LTEXT.getTGIKey())) {
                DBPFLText ltext = ((DBPFLText) type);
                String text = ltext.getString();
                text = text.replaceAll(inputregex[0], inputregex[1]);

                if (!text.equals(ltext.getString())) {
                    counter++;
                }

                ltext.setString(text);
            }
        }

        lastAction("Regexed " + counter + " LTEXTs", Color.DARKGREEN);
        //Refresh the table
        readDataIntoTable(true);
    }

    private void lastAction(String StoSet, Color CtoSet) {
        lastaction.setText(StoSet);
        lastaction.setTextFill(CtoSet);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Syntactic sugar, it has to be here, but does nothing
        lastaction.setTextOverrun(OverrunStyle.ELLIPSIS);
        regexCheck.setSelected(true);
        regexCheck.setTooltip(new Tooltip("Checked: Automatically double up backslashes. Not checked: You have to use double blackslashes yourself"));
        regexField.setTooltip(new Tooltip("Format: REPLACE/BY"));
    }

    private void readDataIntoTable(boolean refreshFirst) {
        if (refreshFirst) {
            tabledata.clear();
        }

        typeList = theDAT.getTypeList();
        for (int i = 0; i < typeList.size(); i++) {
            DBPFType type = typeList.get(i);
            if (type.getTGIKey().equals(TGIKeys.LTEXT.getTGIKey())) {
                tabledata.add(new LTTable(type.getGID(), type.getIID(), ((DBPFLText) type).getString(), i));
            }
        }

        table.setEditable(true);
        Callback<TableColumn, TableCell> cellFactory =
                new Callback<TableColumn, TableCell>() {
            @Override
            public TableCell call(final TableColumn p) {
                final EditingCell nieuw = new EditingCell();
                nieuw.setOnMouseClicked(new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent event) {
                        textar.setText(p.getCellData(nieuw.getIndex()).toString());
                        textar.setId(tabledata.get(nieuw.getIndex()).getGid()+"_"+tabledata.get(nieuw.getIndex()).getIid());
                        System.out.println(textar.getId());
                        //need to add buttons to confirm those edits.. not sure how yet tho :S
                    }
                });
                return nieuw;
            }
        };

        gidcol.setCellValueFactory(new PropertyValueFactory<LTTable, String>("gid"));
        iidcol.setCellValueFactory(new PropertyValueFactory<LTTable, String>("iid"));
        contcol.setCellValueFactory(new PropertyValueFactory<LTTable, String>("string"));
        indexcol.setCellValueFactory(new PropertyValueFactory<LTTable, Integer>("index"));
        //--- Add for Editable Cell of Value field, in Double
        contcol.setCellFactory(cellFactory);
        contcol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<LTTable, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<LTTable, String> t) {
                String newValue = t.getNewValue();
                ((LTTable) t.getTableView().getItems().get(t.getTablePosition().getRow())).setString(newValue);
                //Sets the actual DBPF string to the new string
                ((DBPFLText) theDAT.getTypeList().get(t.getTableView().getItems().get(t.getTablePosition().getRow()).getIndex())).setString(newValue);
                lastAction("Edited row " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getIndex(), Color.DARKGREEN);
            }
        });
        //---
        contcol.setEditable(true);
        contcol.setResizable(true);

        table.setItems(tabledata);
    }
}
