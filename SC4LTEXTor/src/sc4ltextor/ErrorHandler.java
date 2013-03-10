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
    
    public void throwAlert(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(null, errorMessage, "WARNING: " + errorTitle, JOptionPane.WARNING_MESSAGE);
        numErrors++;
    }

    public void throwInfo(String errorMessage, String errorTitle) {
        JOptionPane.showMessageDialog(null, errorMessage, "INFO: " + errorTitle, JOptionPane.INFORMATION_MESSAGE);
        numErrors++;
    }

    public void throwError(String errorMessage, String errorTitle, Exception ex) {
        if(ex.getMessage() != null) {
            throwError(errorMessage + "\n" + ex.getMessage(), errorTitle);
        }
        else {
            throwError(errorMessage, errorTitle);
        }
    }
    
    public int getNumErrors() {
        return numErrors;
    }
}
