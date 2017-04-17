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
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import org.apache.commons.lang3.StringUtils;

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
    
    /*
    Finds if there is a match between search me and multiple options
    */
    Boolean findFirst(String searchMe, String[] options) {
        for (String option : options) {
            if (searchMe.contains(option)) {
                return true;
            }
        }
        return false;
    }
    
    /*
    Finds and returns the best MRI folder from the scan list
    Options is a list of templates in order of preference
    Each option in options may contain 1 section of /'s which represents areas
    where numbers may occur in the file name and the largest integer should be
    taken
    
    Returns "" if there is no match
    */
    String findBestMRIFolder(String[] patientScanList, String[] options) {
        for (String option : options) {
            // Find if an item in scan list satisfies an option
            // First to be satisfied will be returned and most preferential
            int numSlashes = StringUtils.countMatches(option, "/");
            if (numSlashes != 0) {
                // Need to process considering number files
                String[] compares = option.split(StringUtils.repeat("/", numSlashes));
                ArrayList<String> possiblePaths = new ArrayList();
                // Filter out paths that dont catain the template less the slashes
                for (String possiblePath : patientScanList) {
                    boolean validPath = true;
                    for (String contain : compares) {
                        if (!possiblePath.contains(contain)) {
                            validPath = false;
                        }
                    }
                    if (validPath) {
                        possiblePaths.add(possiblePath);
                    }
                }
                // Take the string with the largest number from the sorted strings
                // This should be the one which is lexographically sorted at index first //last
                if (possiblePaths.size() != 0) {
                    Collections.sort(possiblePaths);
                    return (String)possiblePaths.get(possiblePaths.size()-1); //0);
                }
            } else {
                // just search with contains
                for (String possiblePath : patientScanList) {
                    if (possiblePath.contains(option)) {
                        return possiblePath;
                    }
                }
            }
        }
        return "";
    }
    
    
     /**
     * Returns a particular directory for a patient number Based on the
     * segementation file name
     * @param pathlist is a list of the templates that make up folders to 
     * search for the images, there are
     * a couple of things that it could be. The user needs to specify this at 
     * the start
     * @param dicomDirPath
     * @return
     */
    String getPatientFolder(String dicomDirPath, String patientNum, Hashtable<String, String[]> pathlist) {
        String path = null;
        boolean found = false;
        // Find folder of patient based on number: PATIENT FOLDER LEVEL
        String[] listDirPaths = getSubFolders(dicomDirPath);
        for (String dirpath : listDirPaths) {
            // If it contains the patient num and the patient Dir template
            if (dirpath.contains(patientNum) && dirpath.contains(pathlist.get("subjectDir")[0])) {
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
            System.err.println(patientFolder);
            if (findFirst(patientFolder, pathlist.get("studyOptions"))) {
                path = path + '/' + patientFolder;
                found = true;
            }
        }
        
        //Find specific BAT MRI folder
        String mriDir = findBestMRIFolder(getSubFolders(path), pathlist.get("mriOptions"));
        if (mriDir.compareTo("") == 0) {
            return null;
        } else {
            return path + '/' + mriDir;
        }
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
      
    public String getHeaderInfo(String[] header, String info) {
        for (String headerRow : header) {
            if (headerRow.contains(info)) {
                return headerRow;
            }
        }
        return "";
    }
    
      /**
       * 
       * @param image
       * @return 
       */
    public String[] getHeaderandCalcVoxVol(String image) {
        String info = dicom.getInfo(image);
        String[] header = info.split("\n");
//        for (String row : header) {
//            System.err.println(row);
//        }
        String[] releventInfo = new String[9];
        String pixelSizing = getHeaderInfo(header, "Pixel Spacing:").split(":")[1];//header[102].split(":")[1];
        String[] px = pixelSizing.split("\\\\");
        //System.err.println("pixelSizing: " +   Arrays.toString(pixelSizing.split("\\\\")));
        //System.err.println("" + px.length);
        String thickness = getHeaderInfo(header, "Slice Thickness:").split(":")[1].split(" ")[1];//header[53].split(":")[1].split(" ")[1];
        sliceThickness =  Double.parseDouble(thickness); //Slice Thicknes
        //System.err.println("" + thickness + " " + sliceThickness);
        pixelXSize =  Double.parseDouble(px[0]);
        //System.err.println("pixelSizing: " + pixelXSize);
        pixelYSize =  Double.parseDouble(px[1]);
        voxVol = pixelXSize * pixelYSize * sliceThickness;
        releventInfo[0] = getHeaderInfo(header, "Patient ID:").split(":")[1];//header[40].split(":")[1]; //Patient ID
        releventInfo[1] = getHeaderInfo(header, "Patient's Size:").split(":")[1];//header[44].split(":")[1]; //Patient Size
        releventInfo[2] = getHeaderInfo(header, "Patient's Weight:").split(":")[1];//header[45].split(":")[1]; //Patient Weight
        releventInfo[3] = getHeaderInfo(header, "Patient's Sex:").split(":")[1];//header[42].split(":")[1]; //Patient Sex
        releventInfo[4] = getHeaderInfo(header, "Patient's Age:").split(":")[1];//header[43].split(":")[1]; //Patient Age
        releventInfo[5] = getHeaderInfo(header, "Patient's Birth Date:").split(":")[1];//eader[41].split(":")[1]; //Patient Birth Date
        releventInfo[6] = getHeaderInfo(header, "Study Instance UID:").split(":")[1];//header[86].split(":")[1]; //Study instance ID
        releventInfo[7] = getHeaderInfo(header, "Study Date:").split(":")[1];//header[11].split(":")[1]; //Study date
        releventInfo[8] = getHeaderInfo(header, "Magnetic Field Strength:").split(":")[1];//header[60].split(":")[1]; //Magnetic field strength
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