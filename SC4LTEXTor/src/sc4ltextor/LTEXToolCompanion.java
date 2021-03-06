/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sc4ltextor;

import com.thoughtworks.xstream.XStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import javafx.scene.input.MouseEvent;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBuilder;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.LabelBuilder;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextAreaBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javax.swing.JOptionPane;
import ssp.dbpf.DBPFCollection;
import ssp.dbpf.event.DBPFException;
import ssp.dbpf.io.DBPFReader;
import ssp.dbpf.io.DBPFWriter;
import ssp.dbpf.tgi.*;
import ssp.dbpf.types.DBPFLText;
import ssp.dbpf.types.DBPFType;

/**
 * FXML Controller class
 *
 * @author Casper
 */
public class LTEXToolCompanion implements Initializable {

    @FXML
    private Insets x2;
    @FXML
    private Font x1;
    public DBPFCollection theDAT;
    public List<DBPFType> typeList;
    public TableView<LTEXTTableRow> table;
    public AnchorPane anchor;
    public final ObservableList<LTEXTTableRow> tabledata = FXCollections.observableArrayList();
    public TableColumn gidcol, iidcol, contcol, indexcol;
    public TextField transin, transout, incr, regexField;
    public Label lastaction;
    public TextArea textar;
    public CheckBox regexCheck;
    public ErrorHandler eh;
    public ChoiceBox cCurr, cWant;
    public ProgressBar bar;

    /**
     * DEPRECATED, replaced by handleIncrementLang()
     *
     * @throws DBPFException
     */
    @Deprecated
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

    public void handleIncrementLang() throws DBPFException {

        long maxLength = Long.parseLong("4294967260");

        long currOffset = Long.parseLong(((SC4Language) cCurr.getSelectionModel().getSelectedItem()).getIncr());
        long wantOffset = Long.parseLong(((SC4Language) cWant.getSelectionModel().getSelectedItem()).getIncr());
        long toOffsetBy = wantOffset - currOffset;

        for (DBPFType type : typeList) {
            long currGID = type.getGID();
            if (currGID + toOffsetBy >= maxLength) {
                JOptionPane.showMessageDialog(null, "Could not increment the Group IDs.\nOne (or more) entries have a GID higher than the maximum value of 0xFFFFFFF0", "ERROR: Failed to increment!", JOptionPane.ERROR_MESSAGE);
                lastAction("ERROR Incrementing", Color.DARKRED);
                return;
            }
        }
        for (DBPFType type : typeList) {
            long currGID = type.getGID();
            if (currGID + toOffsetBy < maxLength) {
                type.setTGIKey(new TGIKey(type.getTID(), type.getGID() + toOffsetBy, type.getIID()));
            }
        }
        lastAction("Incremented GIDs by " + toOffsetBy, Color.DARKGREEN);
        cCurr.getSelectionModel().select(cWant.getSelectionModel().getSelectedIndex());

        readDataIntoTable(true);
    }

    public void handleOpenButton() throws DBPFException {
        try {
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
            if (file != null) {
                theDAT = DBPFReader.readCollection(file);

                lastAction("Opened " + theDAT.getFilename().getName(), Color.DARKGREEN);
                readDataIntoTable(false);
            }
        } catch (Exception e) {
            eh.throwError("Could not open that file", "Opening failed", e);
            lastAction("Failed to open file", Color.DARKRED);
        }
    }

    public void handleSaveButton() throws DBPFException {
        try {
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
        } catch (Exception e) {
            eh.throwError("Could not save that file", "Saving failed", e);
            lastAction("Failed to save file", Color.DARKRED);
        }
    }

    public void handleExport() throws Exception {
        try {
            FileChooser fc = new FileChooser();
            File file;
            typeList = theDAT.getTypeList();

            //Set extension filter FileChooser.ExtensionFilter extFilter = new
            FileChooser.ExtensionFilter extFilterXML = new FileChooser.ExtensionFilter("XML files (.xml)", "*.xml");
            fc.getExtensionFilters().addAll(extFilterXML);

            file = fc.showSaveDialog(null);

            if (file != null) {
                XStream xs = new XStream();
                xs.alias("LTEXT", DBPFLText.class);
                xs.alias("DBPF", String.class);
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                ObjectOutputStream oos = xs.createObjectOutputStream(bw);
                oos.writeObject(theDAT.getFilename().getName());

                for (int i = 0; i < typeList.size(); i++) {
                    DBPFType type = typeList.get(i);

                    if (type.getTGIKey().equals(TGIKeys.LTEXT.getTGIKey())) {
                        DBPFLText ltext = (DBPFLText) type;
                        oos.writeObject(ltext);
                    }
                }

                oos.close();
                bw.close();
                lastAction("Exported to " + file.getName(), Color.DARKGREEN);
            }
        } catch (Exception e) {
            eh.throwError("Could not export this file", "Failed to export", e);
            lastAction("Failed to export file", Color.DARKRED);
        }
    }

    public void handleImport() throws Exception {
        try {
            FileChooser fc = new FileChooser();
            File file;
            typeList = theDAT.getTypeList();

            //Set extension filter FileChooser.ExtensionFilter extFilter = new
            FileChooser.ExtensionFilter extFilterXML = new FileChooser.ExtensionFilter("XML files (.xml)", "*.xml");
            fc.getExtensionFilters().addAll(extFilterXML);

            file = fc.showOpenDialog(null);

            if (file != null) {
                XStream xs = new XStream();
                xs.alias("LTEXT", DBPFLText.class);
                xs.alias("DBPF", String.class);

                BufferedReader br = new BufferedReader(new FileReader(file));
                ObjectInputStream ois = xs.createObjectInputStream(br);
                theDAT.setFilename(new File((String) ois.readObject()));

                //Import rest of the file, should all be LTEXTs.
                while (true) {
                    try {
                        typeList.add((DBPFLText) ois.readObject());
                    } catch (EOFException eof) {
                        //Stop the loop, we have reached the end of the file!
                        break;
                    }
                }
                ois.close();
                br.close();
                readDataIntoTable(true);
                lastAction("Imported LTEXTs", Color.DARKGREEN);
            }
        } catch (Exception e) {
            eh.throwError("Could not import this file", "Failed to import", e);
            lastAction("Failed to import file", Color.DARKRED);
        }
    }

    public void handleBatchTranslation() {
        try {
            if (transin.getText().isEmpty() || transout.getText().isEmpty()) {
                return; // No input
            }
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
        } catch (Exception e) {
            eh.throwError("Could not execute batch translate", "Failed to batch translate", e);
            lastAction("Failed to translate", Color.DARKRED);
        }
    }

    public void handleRegex() throws DBPFException {
        try {
            if (regexField.getText().isEmpty()) {
                return; //No input
            }
            typeList = theDAT.getTypeList();
            int counter = 0;
            String input = regexCheck.isSelected() ? regexField.getText().replace("\\", "\\\\") : regexField.getText();
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
        } catch (Exception e) {
            eh.throwError("Could not execute the Batch RegEx. Perhaps your regular expression was not formatted correctly?", "Failed to batch RegEx", e);
            lastAction("Failed to RegEx", Color.DARKRED);
        }
    }

    public void lastAction(String StoSet, Color CtoSet) {
        lastaction.setText(StoSet);
        lastaction.setTextFill(CtoSet);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Syntactic sugar, it has to be here, but does nothing
        lastaction.setTextOverrun(OverrunStyle.ELLIPSIS);
        regexCheck.setSelected(true);
        regexCheck.setTooltip(new Tooltip("Checked: Automatically double up backslashes.\nNot checked: You have to use double blackslashes yourself"));
        regexField.setTooltip(new Tooltip("Format: REPLACE/BY"));
        textar.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.F1) {
                    handleUpdateLTEXT();
                } else if (t.getCode() == KeyCode.F2) {
                    handleResetLTEXT();
                } else if (t.getCode() == KeyCode.PAGE_DOWN) {
                    handleNextLTEXT();
                } else if (t.getCode() == KeyCode.PAGE_UP) {
                    handlePreviousLTEXT();
                }
            }
        });
        eh = new ErrorHandler();
        cCurr.getSelectionModel().selectFirst();
        cWant.getSelectionModel().selectFirst();
        cCurr.setTooltip(new Tooltip("Language the file is currently in.\nUsed for the GID offsets"));
        cWant.setTooltip(new Tooltip("Language you want the file to be in\nUsed for the GID offsets"));
        bar.progressProperty().setValue(-1);
    }

    public void handleNextLTEXT() {
        try {
            handleUpdateLTEXT();
            int index = Integer.parseInt(textar.getId());
            DBPFLText ltext = (DBPFLText) typeList.get(index + 1);
            textar.setText(ltext.getString());
            textar.setId("" + (index + 1));
        } catch (Exception e) {
            eh.throwError("Could not edit the/goto next LTEXT.", "Failed to Edit/GoToNext LTEXT", e);
            lastAction("Failed to Edit", Color.DARKRED);
        }
    }

    public void handlePreviousLTEXT() {
        try {
            handleUpdateLTEXT();
            int index = Integer.parseInt(textar.getId());
            DBPFLText ltext = (DBPFLText) typeList.get(index - 1);
            textar.setText(ltext.getString());
            textar.setId("" + (index - 1));
        } catch (Exception e) {
            eh.throwError("Could not edit the/goto previous the LTEXT.", "Failed to Edit/GoToPrevious LTEXT", e);
            lastAction("Failed to Edit", Color.DARKRED);
        }
    }

    public void handleResetLTEXT() {
        try {
            int index = Integer.parseInt(textar.getId());
            DBPFLText ltext = (DBPFLText) typeList.get(index);
            textar.setText(ltext.getString());
            lastAction("Reset LTEXT " + index, Color.DARKGREEN);
        } catch (Exception e) {
            eh.throwError("Could not reset the LTEXT.", "Failed to Reset LTEXT", e);
            lastAction("Failed to Reset", Color.DARKRED);
        }
    }

    public void handleUpdateLTEXT() {
        try {
            int index = Integer.parseInt(textar.getId());
            DBPFLText ltext = (DBPFLText) typeList.get(index);
            ltext.setString(textar.getText());
            readDataIntoTable(true);
            lastAction("Edited LTEXT " + index, Color.DARKGREEN);
        } catch (Exception e) {
            eh.throwError("Could not edit the LTEXT.", "Failed to Edit LTEXT", e);
            lastAction("Failed to Edit", Color.DARKRED);
        }
    }

    public void handleRemoveLTEXT() {
        try {
            int index = Integer.parseInt(textar.getId());
            typeList.remove(index);
            readDataIntoTable(true);
            lastAction("Removed LTEXT " + index, Color.DARKGREEN);
        } catch (Exception e) {
            eh.throwError("Could not remove the LTEXT", "Failed to Remove LTEXT", e);
            lastAction("Failed to Remove", Color.DARKRED);
        }
    }

    public void handleClose() {
        Platform.exit();
    }

    public void handleAbout() throws IOException {
        Stage aboutStage = new Stage();
        Parent root = FXMLLoader.load(getClass().getResource("AboutWindow.fxml"));

        aboutStage.setTitle("SC4 LTEXT Tool");
        aboutStage.setScene(new Scene(root, 300, 200));
        aboutStage.show();
    }

    public void handleAddLTEXT() {
        try {
            final Stage secondaryStage = new Stage();
            FlowPane secondaryLayout = new FlowPane();
            final TextField tgid = new TextField();
            final TextField tiid = new TextField();
            Label lgid = LabelBuilder.create().text("Enter GID: ").build();
            Label liid = LabelBuilder.create().text("Enter IID: ").build();
            final TextArea text = TextAreaBuilder.create().text("Enter LTEXT here").prefWidth(300).prefHeight(200).build();
            Button accept = ButtonBuilder.create().text("Create LTEXT").onAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent t) {
                    try {
                        DBPFLText nieuw = new DBPFLText();
                        nieuw.setString(text.getText());
                        TGIKey nieuwTGI = new TGIKey();
                        nieuwTGI.setTID(TGIKeys.LTEXT.getTGIKey().getTID());
                        nieuwTGI.setGID(Long.parseLong(tgid.getText(), 16));
                        nieuwTGI.setIID(Long.parseLong(tiid.getText(), 16));
                        System.out.println(nieuwTGI);
                        nieuw.setTGIKey(nieuwTGI);
                        System.out.println(nieuw);
                        typeList.add(nieuw);
                        secondaryStage.hide();
                        readDataIntoTable(true);
                        lastAction("Added LTEXT", Color.DARKGREEN);
                        System.out.println(typeList);
                    } catch (Exception e) {
                        eh.throwError("Could not add a new LTEXT", "Failed to Add LTEXT", e);
                        lastAction("Failed to Add", Color.DARKRED);
                    }
                }
            }).build();
            HBox hbox1 = new HBox();
            HBox hbox2 = new HBox();
            HBox hbox3 = new HBox();
            HBox hbox4 = new HBox();
            hbox1.getChildren().addAll(lgid, tgid);
            hbox2.getChildren().addAll(liid, tiid);
            hbox3.getChildren().add(accept);
            hbox4.getChildren().add(text);
            secondaryLayout.getChildren().addAll(hbox1, hbox2, hbox4, hbox3);
            secondaryStage.setScene(new Scene(secondaryLayout, 305, 305));
            secondaryStage.setTitle("Add an LTEXT");
            secondaryStage.show();
        } catch (Exception e) {
            eh.throwError("Could not add a new LTEXT", "Failed to Add LTEXT", e);
            lastAction("Failed to Add", Color.DARKRED);
        }

    }

    private void readDataIntoTable(boolean refreshFirst) {
        if (refreshFirst) {
            tabledata.clear();
        }

        typeList = theDAT.getTypeList();
        Task task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                int size = typeList.size();
                boolean LTextAvailable = false;
                
                for (int i = 0; i < size; i++) {
                    DBPFType type = typeList.get(i);
                    if (type.getTGIKey().equals(TGIKeys.LTEXT.getTGIKey())) {
                        tabledata.add(new LTEXTTableRow(type.getGID(), type.getIID(), ((DBPFLText) type).getString(), i));
                        LTextAvailable = true;
                    }
                    updateProgress(i, size);
                }
                if(!LTextAvailable) {
                    eh.throwInfo("There are no LTEXT entries in this file.", "No LTEXTs available");
                }
                return null;
            }
        };

        bar.progressProperty().bind(task.progressProperty());
        new Thread(task).start();
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
                        textar.setId("" + tabledata.get(nieuw.getIndex()).getIndex());
                    }
                });
                return nieuw;
            }
        };

        gidcol.setCellValueFactory(new PropertyValueFactory<LTEXTTableRow, String>("gid"));
        iidcol.setCellValueFactory(new PropertyValueFactory<LTEXTTableRow, String>("iid"));
        contcol.setCellValueFactory(new PropertyValueFactory<LTEXTTableRow, String>("string"));
        indexcol.setCellValueFactory(new PropertyValueFactory<LTEXTTableRow, Integer>("index"));
        //--- Add for Editable Cell of Value field, in Double
        contcol.setCellFactory(cellFactory);
        contcol.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<LTEXTTableRow, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<LTEXTTableRow, String> t) {
                String newValue = t.getNewValue();
                ((LTEXTTableRow) t.getTableView().getItems().get(t.getTablePosition().getRow())).setString(newValue);
                //Sets the actual DBPF string to the new string
                ((DBPFLText) theDAT.getTypeList().get(t.getTableView().getItems().get(t.getTablePosition().getRow()).getIndex())).setString(newValue);
                lastAction("Edited LTEXT " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getIndex(), Color.DARKGREEN);
            }
        });
        //---
        contcol.setEditable(true);
        contcol.setResizable(true);

        table.setItems(tabledata);
        bar.progressProperty().unbind();
        bar.progressProperty().setValue(-1);
    }
}
