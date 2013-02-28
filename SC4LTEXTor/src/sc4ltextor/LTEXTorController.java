/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sc4ltextor;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
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
    public TextField transin, transout, incr;
    public Label lastaction;

    public void handleIncrement() throws DBPFException {
        long maxLength = Long.parseLong("4294967280");
        long increment = Long.parseLong(incr.getText());
        for (DBPFType type : typeList) {
            long currGID = type.getGID();
            if (currGID + increment >= maxLength) {
                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.WINDOW_MODAL);
                dialogStage.setScene(new Scene(VBoxBuilder.create().
                        children(new Text("Could not increment the Group IDs. One (or more) entries have a GID higher than the maximum value of 0xFFFFFFF0"), new Button("OK")).
                        alignment(Pos.CENTER).padding(new Insets(5)).build()));
                dialogStage.show();
                lastaction.setText("Incrementing failed");
                return;
            }
        }
        for (DBPFType type : typeList) {
            long currGID = type.getGID();
            if (currGID + increment < maxLength) {
                type.setTGIKey(new TGIKey(type.getTID(), type.getGID() + increment, type.getIID()));
            }
        }
        lastaction.setText("Incremented GID by " + increment);
        
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

        lastaction.setText("Opened " + theDAT.getFilename().getName());
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
            lastaction.setText("Saved " + file.getName());
        }
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

        lastaction.setText("Translated " + counter + " LTEXTs");
        //Refresh the table
        readDataIntoTable(true);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Syntactic sugar, it has to be here, but does nothing
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
                    public TableCell call(TableColumn p) {
                        return new EditingCell();
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
                        lastaction.setText("Edited row " + t.getTableView().getItems().get(t.getTablePosition().getRow()).getIndex());
                    }
                });
        //---
        contcol.setEditable(true);
        contcol.setResizable(true);

        table.setItems(tabledata);
    }
}
