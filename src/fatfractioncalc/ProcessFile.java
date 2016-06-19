/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import ij.process.ImageProcessor;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import niftijio.NiftiVolume;


/**
 *
 * @author ariane
 */
public class ProcessFile{
    
    CsvWriter csv;
    //CsvWriter log;
    OpenDicom openDicom;
    double voxVol;
    ImageProcessor voxArr;
    int width;
    int height;
    ij.ImagePlus plus;
    String niFilePath;
    String listOfAllDicoms;
    int[] ffParams;
    NiftiVolume segment3D; // previously mask
    Thread thread;
    ComputeCalc calc;
    NiiGzFileReader reader;
    
    /**
     * 
     * @param singlePatient
     * @param csv 
     */
   ProcessFile( CsvWriter csv, ComputeCalc calc) {
        this.csv = csv;
        this.calc = calc;
        //this.log = log;
        openDicom = new OpenDicom();
        reader = new NiiGzFileReader();
        //calc.setOpenDicomInstance(openDicom);
    }
    
   public void setParams(String niFilePath, String listOfAllDicoms, int[] ffParams) {
       this.niFilePath = niFilePath;
       this.listOfAllDicoms = listOfAllDicoms;
       this.ffParams = ffParams;
   }
   
 
    /**
     * 
     * @throws java.io.IOException 
     */
    public void run(){
        try {
            //String dicomPath = openDicom.getDicomFromNii(niFilePath, dicomDirPath, singlePatient);
        // If there are no dicom files matching then we insert a row stating that
        String row = "";
        String patientNum;
        try {
              String[] niname = niFilePath.split("\\\\");
              patientNum = Character.toString(niname[niname.length - 1].charAt(0)) + 
                      Character.toString(niname[niname.length - 1].charAt(1)) +
                      Character.toString(niname[niname.length - 1].charAt(2)) +
                      Character.toString(niname[niname.length - 1].charAt(3)) +
                      Character.toString(niname[niname.length - 1].charAt(4)) +
                      Character.toString(niname[niname.length - 1].charAt(5)) +
                      Character.toString(niname[niname.length - 1].charAt(6)) +
                      Character.toString(niname[niname.length - 1].charAt(7)) +
                      Character.toString(niname[niname.length - 1].charAt(8));
            
        } catch (Exception e) {
            patientNum = niFilePath;
        }
        String mriDirPath = openDicom.getPatientFolder(listOfAllDicoms, patientNum);
        double[] ff;
       
            if (mriDirPath == null) {
                if (!niFilePath.contains("nii.gz")) {
                    return;
                }
                row = ("" + patientNum + "," + "No matching file" + "\n");
                
            } else {
                NiftiVolume niFile = reader.readNiiGzFile(niFilePath);
                //Open what ever is in mriDirPath openDicom.open_ima_file(mriDirPath);
                //Get a list of all the dicom files in the directory
                String[] dicomList = sortFileSeq(mriDirPath);
                ImageProcessor firstIMAFile = openDicom.openImaFile(dicomList[0]);
                //Gets size from the first dicom image
                width = firstIMAFile.getWidth();
                height = firstIMAFile.getHeight();
                calc.setHeightWidth(height, width);
                //Get header information from the first dicom file
                String[] pd = openDicom.getHeaderandCalcVoxVol(dicomList[0]);
                if (pd == null) {
                    csv.write("" + patientNum + "," + "error Reading header file." + "\n");
                    return;
                }
                voxVol = openDicom.voxVol;
                File file = new File(mriDirPath);
                Date dateMod = new Date(file.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");            
                String dateLastMod = format.format(dateMod);
                ff = calc.countSliceVolumes(dicomList, niFile, width, height, ffParams, voxVol);
                row = patientNum + "," + //Patient Number
                        ff[0] + "," + ff[1] + "," + ff[2] + "," + ff[3] + "," + ff[4] + "," + ff[5] + "," + //Total Fat: Mean, volume, absMin, meanMin, absMax, meanMax
                        ff[12] + "," + ff[13] + "," + ff[14] + "," + ff[15] + "," + ff[16] + "," + ff[17] + "," + //BAT: Mean, volume, absMin, meanMin, absMax, meanMax
                        ff[6] + "," + ff[7] + "," + ff[8] + "," + ff[9] + "," + ff[10] + "," + ff[11] + "," + //WAT: Mean, volume, absMin, meanMin, absMax, meanMax
                        pd[1] + "," + pd[2] + "," + pd[3] + "," + pd[4] + "," + //Subject height, Weight, sex, age
                        headerDateToHumanDate(pd[5]) + "," + pd[6] + "," + //DOB, Study ID
                        headerDateToHumanDate(pd[7]) + "," + //Study date
                        headerTimeToHumanTime(pd[9]) + "," + //Study Time
                        headerDateToHumanDate(pd[10]) + "," + //Series Date
                        headerTimeToHumanTime(pd[11]) + "," + //Series Time
                        headerDateToHumanDate(pd[12]) + "," + //Acquisition Date
                        headerTimeToHumanTime(pd[13]) + "," + //Acquisition Time
                        headerDateToHumanDate(pd[14]) + "," + //Image Date
                        headerTimeToHumanTime(pd[15]) + "," + //Image Time
                        dateLastMod + "," + pd[8] + "," + //Folder Date, Magnetic Field strength
                        openDicom.pixelYSize + "," + openDicom.pixelXSize + //Voxel height, width
                        "," + openDicom.sliceThickness + "," + voxVol //Voxel depth, volume
                        + "," + mriDirPath; //Folder path of MRI image sequence folder
            }         
        csv.write(row);
        } catch (Exception e) {
            try {
                csv.write("Error Processing file for: " + niFilePath);
                //System.err.println("process file message: " + e.getMessage() + "\n");
                Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, e);
                //log.write(e.getMessage());
            } catch (InterruptedException | IOException ex) {
                Logger.getLogger(ProcessFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        }

    /**
     * Takes the header time in 24hr format <space>hhmmss.fractionsecond<space)
     * Returns human readable 24hr string
     * @param headerTime
     * @return 
     */
    private String headerTimeToHumanTime(String headTm) {
        String humanTime = null;
        //System.err.print("TimeA !" + headTm + "!\n");
        try {
            humanTime = Character.toString(headTm.charAt(1)) + Character.toString(headTm.charAt(2)) + ":" + 
                    Character.toString(headTm.charAt(3)) + Character.toString(headTm.charAt(4));
        } catch (Exception e) {
            humanTime = headTm;
        }
        //System.err.print("TimeB !" + humanTime + "!\n");
        return humanTime;
    }
    
    /**
     * Takes the header Date in format <Space>yyyymmdd
     * Returns human readable string: dd/mm/yyyy
     * @param headDate
     * @return 
     */
    private String headerDateToHumanDate(String headDa) {
        String humanDate = null;
        //System.err.print("DateA !" + headDa + "!\n");
        try {
           humanDate = Character.toString(headDa.charAt(7)) + Character.toString(headDa.charAt(8)) + "/" + 
                Character.toString(headDa.charAt(5)) + Character.toString(headDa.charAt(6)) + "/" + 
                Character.toString(headDa.charAt(1)) + Character.toString(headDa.charAt(2)) + 
                   Character.toString(headDa.charAt(3)) + Character.toString(headDa.charAt(4));
        } catch (Exception e) {
            humanDate = headDa;
        }
        //System.err.print("DateB !" + humanDate + "!\n");
        return humanDate;
    }
    
    /**
     * Returns the section of the dicom image which was 
     * identified as brown fat in the nii.gz file
     * @param dicomPath
     * @return 
     */
    private String[] sortFileSeq(String dicomPath) {
        String[] dicomList = openDicom.getImagesFromFolder(dicomPath);
        ArrayList<String> imagePathList = new ArrayList<>();
        for (String folder: dicomList) {
            imagePathList.add(dicomPath + "/" + folder);
        }
        String[] paths = new String[imagePathList.size()];
        paths = imagePathList.toArray(paths);
        Arrays.sort(paths);
        return paths;
    }

  
}
