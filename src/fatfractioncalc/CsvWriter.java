/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import com.sun.corba.se.impl.orbutil.concurrent.Mutex;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ariane
 */
public class CsvWriter {
    Mutex mutex;
    String filename;
    FileWriter fileWriter;
    
    CsvWriter(String filename) {
        mutex = new Mutex();
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
            mutex.acquire();
            try {
                fileWriter.append(row);
                if (!row.contains("\n")) {
                    fileWriter.append("\n");
                }   
            
            } catch (IOException ex) {
                Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
            }finally {
                mutex.release();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
        }       
    }
    
     /**
     * Writes the column labels for the output csv file
     * @param bounds 
     */
    public void writeFirstRow(int[] bounds) {
        String row = "PSCID, TIAF (%) (" + bounds[0] + "-" + bounds[1] + "), TIAF Vol (cm^3), TIAF abs Min (%), " + 
                "TIAF mean Min (%), TIAF abs Max (%), TIAF mean Max (%), " + 
                "BAT (%) (" + bounds[2] + "-" + bounds[3] + "), BAT Vol (cm^3), BAT abs Min (%), BAT mean Min (%), " +
                "BAT abs Max (%), BAT mean Max (%), WAT (%) (" + bounds[4] + "-" + bounds[5] + "), WAT Vol (cm^3), " +
                "WAT abs Min (%), WAT mean Min (%), WAT abs Max (%), " +
                "WAT mean Max (%), Subject Height (m), Subject Weight (m), " +
                "Sex, Age, DOB, Study ID, Study Date, MRI Folder Time, " +
                "Magnetic Field Strength (T), Voxel Height (mm), " +
                "Voxel Width (mm), Voxel Depth (mm), Voxel Volume (mm^2), " +
                "MRI Image Path";
        write(row);
    }
    
    public void closeCSVWriter() {
        try {
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * Writes the error output line to the output csv
     * @param patientNum 
     */
    public void writeBadPatientRow(String patientNum) {
       String row = patientNum + ", No matching MRI files were found for patient " + patientNum;
       write(row);
    }
   
}
