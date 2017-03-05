/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.DICOM;
import ij.plugin.FITS_Writer;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author ariane
 */
public class OpenDicom {

    String[] patient;
    String patientNum;
    ij.plugin.DICOM dicom;
    double sliceThickness;
    double pixelXSize;
    double pixelYSize;
    double voxVol;
    
    OpenDicom(){
        dicom = new DICOM();
    }
    
    /**
     * 
     * @param dicomFolDir, path of folder containing .ima folders
     * @param filesWithMask, gives 
     * @return Image stack of the dicom sequence
     */
    public ImageStack createImageStack(String dicomFolDir, int[] filesWithMask){
        boolean stackMade = false;
        ij.ImageStack dicomSeq = null;
        //Obtain list of .ima paths from the folder path
        File[] imageFiles = getSubfoldersFiles(dicomFolDir);
        try {
            for (int i = filesWithMask[0]; i < filesWithMask[1]; i++) {
                //System.err.print(imageFiles[i].getName() + "\n");
                try {
                    ImageProcessor image = openImaFile(imageFiles[i].getAbsolutePath());
                    if (!stackMade) {
                        stackMade = true;
                        //Create empty image stack
                        dicomSeq = new ImageStack(image.getWidth(), image.getHeight());
                    }
                    dicomSeq.addSlice(image);
                } catch (Exception e) {
                    //System.out.println(e.getMessage());
                    System.err.print("Bad file in dicom folder" + imageFiles[i].getAbsolutePath() + "\n\n");
                }
            }
        } catch (Exception e) {
            System.err.print("Bad dicom folder\n\n");
        }
        return dicomSeq;        
    }
    
    /**
     * 
     * @param dirPath
     * @return A lexographically sorted list of the contents of the folder
     */
    public File[] getSubfoldersFiles(String dirPath){
        File folder = new File(dirPath);
        File[] folderContents = folder.listFiles();
        //lexographically sort
        Arrays.sort(folderContents);
        return folderContents;
    }
    
    
     /**
     * Returns a particular directory for a patient number Based on the
     * segementation file name
     * @param pathlist is a list of the stings that make up the path, there are
     * a couple of things that it could be. The user needs to specify this at 
     * the start
     * @param dicomDirPath
     * @return
     */
    String getPatientFolder(String dicomDirPath, String patientNum, String[] pathlist) {
        System.err.println("dicom path" + dicomDirPath + " Pathlist: " + pathlist[0] + pathlist[1] + 
                pathlist[2] + pathlist[3] + pathlist[4] + 
                pathlist[5] );
        String path = null;
        boolean found = false;
        // Find folder of patient based on number
        String[] listDirPaths = getSubFolders(dicomDirPath);
        for (String dirpath : listDirPaths) {
            if (dirpath.contains(patientNum)) {
                path = dicomDirPath + '/' + dirpath;
                found = true;
            }
        }
        if (found == false) {
            return null;
        }
        found = false;
        
        // Find relevent MRI folder in patient folder
        String[] patientDirList = getSubFolders(path);
        for (String patientFolder : patientDirList) {
            if (patientFolder.contains(pathlist[0]) || patientFolder.contains(pathlist[1])) {
            //if (patientFolder.contains("MRI_RESEARCH") || patientFolder.contains("MRI_BRAIN")) {
                path = path + '/' + patientFolder;
                found = true;
            }
        }
        //Find specific BAT MRI folder
        if (found == false) {
            return null;
        }
        found = false;
        String[] patientScanList = getSubFolders(path);
        ArrayList<String> listFPFolders = new ArrayList();
        for (String patientSubFolders : patientScanList) {
            System.out.println(patientSubFolders);
            if (patientSubFolders.contains(pathlist[2]) || patientSubFolders.contains(pathlist[3])) {
                if (patientSubFolders.contains(pathlist[4]) || patientSubFolders.contains(pathlist[5])) {            
            //if (patientSubFolders.contains("AX") || patientSubFolders.contains("SCAPULA")) {
            //    if (patientSubFolders.contains("FP") || patientSubFolders.contains("FF")) {
                    found = true;
                    listFPFolders.add(patientSubFolders);
                }
            }
        }
        // Find the most recent in the list of folders (containing RR - reviewed)
        // First check if there is only 1 and return that
        if (listFPFolders.size() == 0 || found == false) {
            return null;
        }
        String latestRevision = null;
        int revision = 0;
        for (String folder : listFPFolders) {
            //if (folder.contains("RR")) {
            if (folder.contains(pathlist[6])) {
                String[] folderName = folder.split("_");
                int curRev = Integer.parseInt(folderName[folderName.length - 1]);
                if (curRev > revision) {
                    revision = curRev;
                    latestRevision = folder;
                }
            }
        }
        // Check if there was a latest revision otherwise just return the first FP
        if (latestRevision != null) {
            return latestRevision;
        }
        
        for (String folder: listFPFolders) {
            String[] folderName = folder.split("_");
            String curRevStr = folderName[folderName.length - 1];
            int curRev = Integer.parseInt(curRevStr);
            String fpNum = "_" + pathlist[4] + "_" + curRevStr;
            String ffNum = "_" + pathlist[5] + "_" + curRevStr;
            //String fpNum = "_FP_" + curRevStr;
            //String ffNum = "_FF_" + curRevStr;
            if (curRev > revision) {
                if (folder.contains(ffNum) || folder.contains(fpNum)) {
                    revision = curRev;
                    latestRevision = folder;
                }
            }
        }
        if (latestRevision != null) {
            return path + "/" + latestRevision;
        }
        return listFPFolders.get(0);

    }
    
    
        /**
     * Gets sub folders form a directory
     *
     * @param dicomDirPath
     * @return
     */
    String[] getSubFolders(String dicomDirPath) {
        File file = new File(dicomDirPath);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isDirectory();
            }
        });
        return directories;
    }
    
    /**
     * Gets the IMA files from a directory
     * @param dicomDirPath
     * @return 
     */
      String[] getImagesFromFolder(String dicomDirPath) {
        File file = new File(dicomDirPath);
        String[] directories = file.list(new FilenameFilter() {
            @Override
            public boolean accept(File current, String name) {
                return new File(current, name).isFile();
            }
        });
        return directories;
    }
    
      /**
       * 
       * @param image
       * @return 
       */
    public String[] getHeaderandCalcVoxVol(String image) {
        String info = dicom.getInfo(image);
        String[] header = info.split("\n");
        String[] releventInfo = new String[9];
        String pixelSizing = header[102].split(":")[1];
        String[] px = pixelSizing.split("\\\\");
        //System.err.println("pixelSizing: " +   Arrays.toString(pixelSizing.split("\\\\")));
        //System.err.println("" + px.length);
        String thickness = header[53].split(":")[1].split(" ")[1];
        sliceThickness =  Double.parseDouble(thickness); //Slice Thicknes
        //System.err.println("" + thickness + " " + sliceThickness);
        pixelXSize =  Double.parseDouble(px[0]);
        //System.err.println("pixelSizing: " + pixelXSize);
        pixelYSize =  Double.parseDouble(px[1]);
        voxVol = pixelXSize * pixelYSize * sliceThickness;
        releventInfo[0] = header[40].split(":")[1]; //Patient ID
        releventInfo[1] = header[44].split(":")[1]; //Patient Size
        releventInfo[2] = header[45].split(":")[1]; //Patient Weight
        releventInfo[3] = header[42].split(":")[1]; //Patient Sex
        releventInfo[4] = header[43].split(":")[1]; //Patient Age
        releventInfo[5] = header[41].split(":")[1]; //Patient Birth Date
        releventInfo[6] = header[86].split(":")[1]; //Study instance ID
        releventInfo[7] = header[11].split(":")[1]; //Study date
        releventInfo[8] = header[60].split(":")[1]; //Magnetic field strength
        return releventInfo;
    }
   
    
   
    /**
     * 
     * @param imaPath, path to a single .IMA file
     * @return image processor of image
     */
    public ImageProcessor openImaFile(String imaPath){
        dicom.open(imaPath);
        

        return dicom.getProcessor();
    }
}