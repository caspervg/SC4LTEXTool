/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sc4ltextor;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author Casper
 */
public class LTTable {

    private SimpleStringProperty gid;
    private SimpleStringProperty iid;
    private SimpleStringProperty string;
    private SimpleIntegerProperty index;

    LTTable(long giD, long iiD, String string, int index) {
        this.gid = new SimpleStringProperty(Long.toHexString(giD));
        this.iid = new SimpleStringProperty(Long.toHexString(iiD));
        this.string = new SimpleStringProperty(string);
        this.index = new SimpleIntegerProperty(index);
    }

    /**
     * @return the gID
     */
    public String getGid() {
        return gid.get();
    }
    
    public Integer getIndex() {
        return index.get();
    }
    
    public void setIndex(Integer nIndex) {
        this.index.set(nIndex);
    }

    /**
     * @param gID the gID to set
     */
    public void setGid(String gID) {
        this.gid.set(gID);
    }

    /**
     * @return the iID
     */
    public String getIid() {
        return iid.get();
    }

    /**
     * @param iID the iID to set
     */
    public void setIid(String iID) {
        this.iid.set(iID);
    }

    /**
     * @return the string
     */
    public String getString() {
        return string.get();
    }

    /**
     * @param string the string to set
     */
    public void setString(String string) {
        this.string.set(string);
    }
}
