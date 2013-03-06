/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sc4ltextor;

/**
 *
 * @author Casper
 */
public class SC4Language {
    
    private String incr;
    private String name;
    
    public SC4Language() {
    }
    
    public SC4Language(int i, String n) {
        this.incr = ""+i;
        this.name = n;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public void setIncr(String incr) {
        this.incr = incr;
    }
    
    
    public String getIncr() {
        return incr;
    }
    
    @Override
    public String toString() {
        return name + " (0x" + Long.toHexString(Long.parseLong(incr)) + ")";
    }
}
