/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author ariane
 */
public class CsvWriter {
    
    String filename;
    FileWriter fileWriter;
    
    public void csvWriter(String filename) {
        this.filename = filename;
        try {
            fileWriter = new FileWriter(filename);
        } catch (IOException ex) {
            System.err.println("Counldn't open file: " + filename);
        }
    }
    
    private void addHeader(String header) {
        try {
            fileWriter.append(header);
            fileWriter.append("\n");
        } catch (IOException ex) {
            System.err.println("Couldn't write to file: " + filename);
        }
    }
    
    public void write(String row) {
         try {
            fileWriter.append(row);
            fileWriter.append("\n");
        } catch (IOException ex) {
            System.err.println("Couldn't write to file: " + filename);
        }
    }
    
   
}
