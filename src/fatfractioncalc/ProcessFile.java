/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import ij.process.ImageProcessor;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import niftijio.NiftiVolume;


/**
 *
 * @author ariane
 */
public class ProcessFile implements Runnable{
    
    CsvWriter csv;
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
    String[] pathlist;
    
    /**
     * 
     * @param singlePatient
     * @param csv 
     */
   ProcessFile( CsvWriter csv, ComputeCalc calc , 
           String[] pathlist) {
        this.csv = csv;
        this.calc = calc;
        openDicom = new OpenDicom();
        reader = new NiiGzFileReader();
        calc.setOpenDicomInstance(openDicom);
        this.pathlist = pathlist;
    }
    
   public void setParams(String niFilePath, String listOfAllDicoms, int[] ffParams) {
       this.niFilePath = niFilePath;
       this.listOfAllDicoms = listOfAllDicoms;
       this.ffParams = ffParams;

   }
   
   public void start() {
         thread = new Thread(this);
         thread.start();
   }
    /**
     * 
     * @throws java.io.IOException 
     */
    @Override
    public void run(){
        //String dicomPath = openDicom.getDicomFromNii(niFilePath, dicomDirPath, singlePatient);
        // If there are no dicom files matching then we insert a row stating that
        String row = "";
        String[] niname = niFilePath.split("_");
        String patientNum = niname[niname.length - 2].split("/")[1];
        String mriDirPath = openDicom.getPatientFolder(listOfAllDicoms,
                patientNum, pathlist);
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
                System.err.println("This is the MRI dir path: " + mriDirPath);
                       
                String[] dicomList = sortFileSeq(mriDirPath);
                ImageProcessor firstIMAFile = openDicom.openImaFile(dicomList[0]);
                //Gets size from the first dicom image
                width = firstIMAFile.getWidth();
                height = firstIMAFile.getHeight();
                calc.setHeightWidth(height, width);
                //Get header information from the first dicom file
                String[] pd = openDicom.getHeaderandCalcVoxVol(dicomList[0]);
                voxVol = openDicom.voxVol;
                File file = new File(mriDirPath);
                Date dateMod = new Date(file.lastModified());
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");            
                String dateLastMod = format.format(dateMod);
                ff = calc.countSliceVolumes(dicomList, niFile, width, height, ffParams, voxVol);
                row = pd[0] + "," + ff[0] + "," + ff[1] + "," + ff[2] + "," + ff[3] + "," + ff[4] + "," + ff[5] +
                        "," + ff[6] + "," + ff[7] + "," + ff[8] + "," + ff[9] + "," + ff[10] + "," + ff[11] + 
                        "," + ff[12] + "," + ff[13] + "," + ff[14] + "," + ff[15] + "," + ff[16] + "," + ff[17] +
                        "," + pd[1] + "," + pd[2] + "," + pd[3] + "," + pd[4] + "," + pd[5] + "," + pd[6] + 
                        "," + pd[7] + "," + dateLastMod + "," + pd[8] + "," + 
                        openDicom.pixelYSize + "," + openDicom.pixelXSize + "," + openDicom.sliceThickness + "," + voxVol
                        + "," + mriDirPath;
                
                //System.err.print("output: " + Arrays.toString(pd) + "\n");
            }         
        csv.write(row);
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
