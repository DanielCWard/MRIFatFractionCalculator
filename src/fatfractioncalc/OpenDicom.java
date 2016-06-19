/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import ij.ImageStack;
import ij.plugin.DICOM;
import ij.process.ImageProcessor;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

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
     *
     * @param dicomDirPath
     * @return
     */
    String getPatientFolder(String dicomDirPath, String patientNum) {
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
            if (patientFolder.contains("MRI_RESEARCH") || patientFolder.contains("MRI_BRAIN")) {
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
            if (patientSubFolders.contains("SCAPULA") && patientSubFolders.contains("AX")) {
                if (patientSubFolders.contains("_FP_") || patientSubFolders.contains("_FF_")) {
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
            if (folder.contains("_RR_")) {
                String[] folderName = folder.split("_");
                int curRev = Integer.parseInt(folderName[folderName.length - 1]);
                String curRevStr = folderName[folderName.length - 1];
                String fpNum = "_FP_" + curRevStr;
                String ffNum = "_FF_" + curRevStr;
                if (curRev > revision) {
                    if (folder.contains(ffNum) || folder.contains(fpNum)) {
                        revision = curRev;
                        latestRevision = folder;
                    }
                }
            }
        }
        // Check if there was a latest revision otherwise just return the first FP
        if (latestRevision != null) {
            return  path + "/" + latestRevision;
        }
        revision = 0;
        for (String folder: listFPFolders) {
            String[] folderName = folder.split("_");
            String curRevStr = folderName[folderName.length - 1];
            int curRev = Integer.parseInt(curRevStr);
            String fpNum = "_FP_" + curRevStr;
            String ffNum = "_FF_" + curRevStr;            
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
          //System.err.println("" + dicomDirPath);
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
        String[] releventInfo = new String[16];
        for (String headLn : header) {
            try {
                if (headLn.contains("Patient ID:")) {
                    releventInfo[0] = headLn.split(":")[1]; //Patient ID
                } else if (headLn.contains("Patient's Size:")) {
                    releventInfo[1] = headLn.split(":")[1];
                } else if (headLn.contains("Patient's Weight:")) {
                    releventInfo[2] = headLn.split(":")[1];
                } else if (headLn.contains("Patient's Sex:")) {
                    releventInfo[3] = headLn.split(":")[1];
                } else if (headLn.contains("Patient's Age:")) {
                    releventInfo[4] = headLn.split(":")[1];
                } else if (headLn.contains("Patient's Birth Date:")) {
                    releventInfo[5] = headLn.split(":")[1];
                } else if (headLn.contains("Study Instance UID:")) {
                    releventInfo[6] = headLn.split(":")[1];
                } else if (headLn.contains("Study Date:")) {
                    releventInfo[7] = headLn.split(":")[1];
                } else if (headLn.contains("Magnetic Field Strength:")) {
                    releventInfo[8] = headLn.split(":")[1];
                } else if (headLn.contains("Study Time:")) {
                    releventInfo[9] = headLn.split(":")[1];
                } else if (headLn.contains("Series Date:")) {
                    releventInfo[10] = headLn.split(":")[1];
                } else if (headLn.contains("Series Time:")) {
                    releventInfo[11] = headLn.split(":")[1];
                } else if (headLn.contains("Acquisition Date:")) {
                    releventInfo[12] = headLn.split(":")[1];
                } else if (headLn.contains("Acquisition Time:")) {
                    releventInfo[13] = headLn.split(":")[1];
                } else if (headLn.contains("Image Date:")) {
                    releventInfo[14] = headLn.split(":")[1];
                } else if (headLn.contains("Image Time:")) {
                    releventInfo[15] = headLn.split(":")[1];
                } else if (headLn.contains("Slice Thickness:")) {
                    String thickness = headLn.split(":")[1].split(" ")[1];
                    sliceThickness =  Double.parseDouble(thickness);
                } else if (headLn.contains("Pixel Spacing:")) {
                    String spacing = headLn.split(":")[1];
                    String[] px = spacing.split("\\\\");
                    pixelXSize =  Double.parseDouble(px[0]);
                    pixelYSize =  Double.parseDouble(px[1]);
                }
            } catch (Exception e) {
                continue;
            }
        }
        voxVol = pixelXSize * pixelYSize * sliceThickness;
        return releventInfo;
//        String pixelSizing = header[102].split(":")[1];
//        
//    //    if ( !(header[60].split(":")[1]).contains("3")) {
//            sliceThickness = 1.6;
//            pixelXSize = 1.5625;
//            pixelYSize = 1.5625;
//            voxVol = pixelXSize * pixelYSize * sliceThickness;
//            releventInfo[0] = " ";//header[40].split(":")[1]; //Patient ID
//            releventInfo[1] = " ";//header[44].split(":")[1]; //Patient Size
//            releventInfo[2] = " ";//header[45].split(":")[1]; //Patient Weight
//            releventInfo[3] = " ";//header[42].split(":")[1]; //Patient Sex
//            releventInfo[4] =" ";//header[43].split(":")[1]; //Patient Age
//            releventInfo[5] = " ";//header[41].split(":")[1]; //Patient Birth Date
//            releventInfo[6] = " ";//header[86].split(":")[1]; //Study instance ID
//            releventInfo[7] = " ";//header[11].split(":")[1]; //Study date
//            releventInfo[8] =" ";// header[60].split(":")[1]; //Magnetic field strength
//            releventInfo[9] = " ";//header[15].split(":")[1]; //Study Time
//            releventInfo[10] =" ";// header[12].split(":")[1]; //Series date
//            releventInfo[11] = " ";//header[16].split(":")[1]; //Series Time
//            releventInfo[12] = " ";//header[13].split(":")[1]; //Acquisition Date
//            releventInfo[13] = " ";//header[17].split(":")[1]; //Acquisition Time
//            releventInfo[14] = " ";//header[14].split(":")[1]; //Image Date
//            releventInfo[15] =" ";// header[18].split(":")[1]; //Image Time
//           // return releventInfo;
////        }//Magnetic field strength
////        String[] px = pixelSizing.split("\\\\");
////        //System.err.println("pixelSizing: " +   Arrays.toString(pixelSizing.split("\\\\")));
////        //System.err.println("" + px.length);
////        String thickness = header[53].split(":")[1].split(" ")[1];
////        sliceThickness =  Double.parseDouble(thickness); //Slice Thicknes
////        //System.err.println("" + thickness + " " + sliceThickness);
////        try {
////            pixelXSize =  Double.parseDouble(px[0]);
////        } catch (Exception e) {
////            return null;
////        }
////        
////        //System.err.println("pixelSizing: " + pixelXSize);
////        try {
////            pixelYSize =  Double.parseDouble(px[1]);
////        } catch (Exception e) {
////            System.err.print("Used X dimen as Y, x dimen = " + pixelXSize + "\n");
////            pixelYSize = pixelXSize;
////        }
////        
////        voxVol = pixelXSize * pixelYSize * sliceThickness;
////        releventInfo[0] = header[40].split(":")[1]; //Patient ID
////        releventInfo[1] = header[44].split(":")[1]; //Patient Size
////        releventInfo[2] = header[45].split(":")[1]; //Patient Weight
////        releventInfo[3] = header[42].split(":")[1]; //Patient Sex
////        releventInfo[4] = header[43].split(":")[1]; //Patient Age
////        releventInfo[5] = header[41].split(":")[1]; //Patient Birth Date
////        releventInfo[6] = header[86].split(":")[1]; //Study instance ID
////        releventInfo[7] = header[11].split(":")[1]; //Study date
////        releventInfo[8] = header[60].split(":")[1]; //Magnetic field strength
////        releventInfo[9] = header[15].split(":")[1]; //Study Time
////        releventInfo[10] = header[12].split(":")[1]; //Series date
////        releventInfo[11] = header[16].split(":")[1]; //Series Time
////        releventInfo[12] = header[13].split(":")[1]; //Acquisition Date
////        releventInfo[13] = header[17].split(":")[1]; //Acquisition Time
////        releventInfo[14] = header[14].split(":")[1]; //Image Date
////        releventInfo[15] = header[18].split(":")[1]; //Image Time
////        sliceThickness = 1.6;
////        pixelXSize = 1.5625;
////        pixelYSize = 1.5625;
////        voxVol = pixelXSize * pixelYSize * sliceThickness;
//        return releventInfo;
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