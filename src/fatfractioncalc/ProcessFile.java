/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import org.dcm4che2.data.DicomObject;


/**
 *
 * @author ariane
 */
public class ProcessFile {
    
    boolean singlePatient;
    CsvWriter csv;
    OpenDicom openDicom;
    double[] voxVol;
    int[] voxArr;
    int width;
    int height;
    ComputeCalc calc;
    NiiGzFileReader reader;
    /**
     * 
     * @param singlePatient
     * @param csv 
     */
    private void processFile(boolean singlePatient, CsvWriter csv, ComputeCalc calc) {
        this.singlePatient = singlePatient;
        this.csv = csv;
        this.calc = calc;
        openDicom = new OpenDicom();
        reader = new NiiGzFileReader();

    }
    
    
    /**
     * 
     * @param niFileName
     * @param dicomDirPath
     * @param ffParams 
     */
    private void process(String niFileName, String dicomDirPath, ArrayList<Integer> ffParams) throws IOException {

        String dicomPath = openDicom.getDicomFromNii(niFileName, dicomDirPath, singlePatient);
        // If there are no dicom files matching then we insert a row stating that
        if (dicomPath == null) {
            System.err.println("Patient : " + openDicom.patientNum + " No matching files");
            csv.write("" + openDicom.patientNum + "," + "No matching file" + "\n");
        }
        // get a list of all he dicom files in the directory
        String[] dicomList = sortFileSeq(dicomPath);
        DicomObject data = openDicom.getData(dicomList[0]);
        String unzippedSegments = reader.unZipNiiGzFile(niFileName, "/home/ariane/Documents/Gusto/Latest Compute FF/Out");
        String[] segmentList = sortFileSeq(unzippedSegments);
        voxVol = openDicom.getDicomVoxelSize(data);
        voxArr = openDicom.getPixelData(data);
        width = openDicom.getWidth(data);
        height = openDicom.getHeight(data);
        ArrayList<int[]> slices = new ArrayList<>();
        slices = calc.getSegmentedVox(dicomList, segmentList, width, height);
    }
    
    /**
     * Returns the section of the dicom image which was 
     * identified as brown fat in the nii.gz file
     * @param dicomPath
     * @return 
     */
    private String[] sortFileSeq(String dicomPath) {
        String[] dicomList = openDicom.getSubFolders(dicomPath);
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
