/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sc4ltextor;

import javax.swing.JOptionPane;

/**
 *
 * @author Casper
 */
public class ErrorHandler {

    private static int numErrors = 0;

    public ErrorHandler() {
    }

    public void throwError(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(null, errorMessage, "ERROR: " + errorTitle, JOptionPane.ERROR_MESSAGE);
        numErrors++;
    }

    public void throwError(String errorMessage, String errorTitle, Exception ex) {
        throwError(errorMessage + "\n" + ex.getMessage(),errorTitle);
    }
    
    public int getNumErrors() {
        return numErrors;
    }
}
