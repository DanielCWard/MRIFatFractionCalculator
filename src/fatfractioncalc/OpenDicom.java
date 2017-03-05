/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import org.dcm4che2.data.DicomObject;
import org.dcm4che2.data.Tag;
import org.dcm4che2.data.VR;
import org.dcm4che2.io.DicomInputStream;

/**
 *
 * @author ariane
 */
public class OpenDicom {

    String patientNum;

    /**
     * Gets the dicom path from a particular nii file
     *
     * @param niFileName
     * @param dicomDirPath
     * @param singlePatient
     * @return
     */
    String getDicomFromNii(String niFileName, String dicomDirPath, boolean singlePatient) {
        patientNum = niFileName.split("/")[-1];
        patientNum = patientNum.split("_")[0];
        String patientDicomPath = getPatientFolder(dicomDirPath);
        return patientDicomPath;
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
     * Returns a particular directory for a patient number Based on the
     * segementation file name
     *
     * @param dicomDirPath
     * @return
     */
    String getPatientFolder(String dicomDirPath) {
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
            if (patientSubFolders.contains("AX") || patientSubFolders.contains("SCAPULA")) {
                if (patientSubFolders.contains("FP") || patientSubFolders.contains("FF")) {
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
            if (folder.contains("RR")) {
                int curRev = Integer.parseInt(folder.split("_")[-1]);
                if (curRev > revision) {
                    revision = curRev;
                    latestRevision = folder;
                }
            }
        }
        // Check if there was a latest revision otherwise just return the first FP
        if (latestRevision != null) {
            return path + '/' + latestRevision;
        }
        return listFPFolders.get(0);

    }

    /**
     * 
     * @param file
     * @return 
     */
    DicomObject getData(String file) {
        DicomInputStream dicomInput = null;
        DicomObject dicomImg = null;
        try {

            dicomInput = new DicomInputStream(new File(file));
            dicomImg = dicomInput.readDicomObject();
        } catch (IOException ex) {
            System.err.println("Error opening dicom file");
        } finally {
            try {
                dicomInput.close();
            } catch (IOException ex) {
                System.err.println("Error closing dicom file");
            }
        }
        DicomObject data = dicomImg.fileMetaInfo();
        System.err.println("" + data);
        return data;
    }

    /**
     * Opens the dicom file and returns it as an image
     *
     * @param fileName
     * @return
     */
    BufferedImage openImg(String fileName) {
        BufferedImage img = null;
        try {
            img = ImageIO.read(new File(fileName));
        } catch (IOException ex) {
            System.err.println("Couldn't open dicom file: " + fileName);
        }
        return img;
    }

    /**
     * Gets the voxel sizing from the dicom file
     *
     * @param data
     * @return
     */
    double[] getDicomVoxelSize(DicomObject data) {
        double[] voxVol = data.getDoubles(Tag.PixelSpacing);
        return voxVol;
    }

    /**
     * 
     * @param data
     * @return 
     */
    int getHeight(DicomObject data) {
        int height = data.getInt(Tag.Rows);
        return height;
    }

    /**
     * 
     * @param data
     * @return 
     */
    int getWidth(DicomObject data) {
        int width = data.getInt(Tag.Columns);
        return width;

    }
    
    /**
     * 
     * @param data
     * @return 
     */
    int[] getPixelData(DicomObject data) {
        int pixelArray = Tag.PixelData;
        int[] array = data.getInts(pixelArray);
        return array;
    }
    
}
