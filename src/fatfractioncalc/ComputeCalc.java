/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;


import ij.IJ;
import ij.ImagePlus;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.dcm4che2.data.DicomObject;

/**
 *
 * @author ariane
 */
public class ComputeCalc {
    
    OpenDicom openDicom;
    int width;
    int height;
    
    private ComputeCalc(OpenDicom openDicom) {
        this.openDicom = openDicom;
    }

    ArrayList<int[]> getSegmentedVox(String[] dicomList, String[] segmentList, int width, int height) {
        ArrayList<int[]> slices = new ArrayList<>();
        this.width = width;
        this.height = height;
        for (int sliceNum = 0; sliceNum < dicomList.length; sliceNum++) {
            DicomObject img = openDicom.getData(dicomList[sliceNum]);
            int[] dicomArr = openDicom.getPixelData(img);
            ImagePlus segmetArr = getSliceArr(segmentList[sliceNum], sliceNum);
         //   int[] maskedArr = maskBySegment(segmentArr, dicomArr);
        }
        return slices;
    }

    ImagePlus getSliceArr(String segment, int sliceNum) {
        ImagePlus niImage = IJ.openImage(segment);
        niImage.show();
        return niImage;
    }

//    private int[] maskBySegment(int[] segmentArr, int[] dicomArr) {
//
//    }

}
