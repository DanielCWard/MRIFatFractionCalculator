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
 * Produces and writes to the CSV output file.
 * @authors Daniel Ward and Ariane Mora
 */
public class CsvWriter {
    Mutex mutex;
    String filename;
    FileWriter fileWriter;
    
    CsvWriter(String filename) throws IOException {
        mutex = new Mutex();
        this.filename = filename;
        fileWriter = new FileWriter(filename);
//        try {
//            fileWriter = new FileWriter(filename);
//        } catch (IOException ex) {
//            System.err.println("Counldn't open file: " + filename);
//        }
    }
    
    /**
     * Adds a string to the header of the CSV file.
     * @param header
     * @throws IOException 
     */
    private void addHeader(String header) throws IOException {
        fileWriter.append(header);
        fileWriter.append("\n");
//        try {
//            fileWriter.append(header);
//            fileWriter.append("\n");
//        } catch (IOException ex) {
//            System.err.println("Couldn't write to file: " + filename);
//        }
    }
    
    /**
     * Writes the given string to a row in the CSV file
     * @param row
     * @throws InterruptedException
     * @throws IOException 
     */
    public void write(String row) throws InterruptedException, IOException {
        try {
            mutex.acquire();
            fileWriter.append(row);
            if (!row.contains("\n")) {
                fileWriter.append("\n");
            }
//            try {
//                fileWriter.append(row);
//                if (!row.contains("\n")) {
//                    fileWriter.append("\n");
//                }   
//            
//            } catch (IOException ex) {
//                Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
            }finally {
                mutex.release();
            }
//        } catch (InterruptedException ex) {
//            Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
//        }       
    }
    
     /**
     * Writes the column labels for the output CSV file
     * @param bounds 
     */
    public void writeFirstRow(int[] bounds) throws InterruptedException, IOException {
        String row = "PSCID: Pediatric study centre ID, TFP (FF) (" + bounds[0] + "-" + bounds[1] + "), TFP Vol (cm^3), TFP abs Min (FF), " + 
                "TFP mean Min (FF), TFP abs Max (FF), TFP mean Max (FF), " + 
                "BAT (FF) (" + bounds[4] + "-" + bounds[5] + "), BAT Vol (cm^3), BAT abs Min (FF), BAT mean Min (FF), " +
                "BAT abs Max (FF), BAT mean Max (FF), WAT (FF) (" + bounds[2] + "-" + bounds[3] + "), WAT Vol (cm^3), " +
                "WAT abs Min (FF), WAT mean Min (FF), WAT abs Max (FF), " +
                "WAT mean Max (FF), Subject Height (m), Subject Weight (kg), " +
                "Sex, Age, DOB, Study ID, Study Date, Study Time, Series Date, Series Time, " +
                "Acquisition Date, Acquisition Time, Image Date, Image Time, MRI Folder Time, " +
                "Magnetic Field Strength (T), Voxel Height (mm), " +
                "Voxel Width (mm), Voxel Depth (mm), Voxel Volume (mm^3), " +
                "MRI Image Path";
        
        /*Original
        String row = "PSCID, TIAF (%) (" + bounds[0] + "-" + bounds[1] + "), TIAF Vol (cm^3), TIAF abs Min (%), " + 
                "TIAF mean Min (%), TIAF abs Max (%), TIAF mean Max (%), " + 
                "BAT (%) (" + bounds[2] + "-" + bounds[3] + "), BAT Vol (cm^3), BAT abs Min (%), BAT mean Min (%), " +
                "BAT abs Max (%), BAT mean Max (%), WAT (%) (" + bounds[4] + "-" + bounds[5] + "), WAT Vol (cm^3), " +
                "WAT abs Min (%), WAT mean Min (%), WAT abs Max (%), " +
                "WAT mean Max (%), Subject Height (m), Subject Weight (kg), " +
                "Sex, Age, DOB, Study ID, Study Date, MRI Folder Time, " +
                "Magnetic Field Strength (T), Voxel Height (mm), " +
                "Voxel Width (mm), Voxel Depth (mm), Voxel Volume (mm^2), " +
                "MRI Image Path";
        */
        write(row);
    }
    
    /**
     * Closes the CSV File
     * @throws IOException 
     */
    public void closeCSVWriter() throws IOException {
        fileWriter.flush();
        fileWriter.close();
//        try {
//            fileWriter.flush();
//            fileWriter.close();
//        } catch (IOException ex) {
//            Logger.getLogger(CsvWriter.class.getName()).log(Level.SEVERE, null, ex);
//        }
    }
    
    /**
     * Writes the error output line to the output CSV
     * @param patientNum 
     */
    public void writeBadPatientRow(String patientNum) throws InterruptedException, IOException {
       String row = patientNum + ", No matching MRI files were found for participant " + patientNum;
       write(row);
    }
   
}
