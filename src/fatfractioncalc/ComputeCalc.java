/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;


import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
//import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import niftijio.NiftiVolume;

/**
 *
 * @author ariane
 */
public class ComputeCalc {
    int width;
    int height;
    OpenDicom openDicom;
    
    ComputeCalc() {

    }
    
    public void setHeightWidth (int imgHeight, int imgWidth) {
        width = imgWidth;
        height = imgHeight;
    }
    /**
     * Sets the open dicom instance to be that used in process file
     * @param openDicom 
     */
    public void setOpenDicomInstance(OpenDicom openDicom) {
        this.openDicom = openDicom;
    }
    

    /**
     * Nifti volume is stored as volume[x][y][z][dim]
     * @param dicomList
     * @param segmentList
     * @param width
     * @param height
     * @return 
     */
       /**
     * 
     * @param dicomList
     * @param segmentList
     * @param width
     * @param height
     * @param bounds, array of bounds for fat types: [TFLwr, TFUpr, WATLwr, WATUpr, BATLwr, BATUpr]
     * @return 
     */
    double[] countSliceVolumes(String[] dicomList, NiftiVolume segmentList, int width, int height, int[] bounds, double voxDimen) {
        //Declare variables for counting fat volumes
        //Total Fat (TF)
        int TFCount = 0;
        float TFValue = 0;
        ArrayList<Integer> TFMinVals = new ArrayList<>();
        ArrayList<Integer> TFMaxVals = new ArrayList<>();
        int TFCurMin = 1000; //Start off min as max possible value
        int TFCurMax = 0;    //Start off max as min possible value
        
        //White Fat (WAT)
        int WATCount = 0;
        float WATValue = 0;
        ArrayList<Integer> WATMinVals = new ArrayList<>();
        ArrayList<Integer> WATMaxVals = new ArrayList<>();
        int WATCurMin = 1000; //Start off min as max possible value
        int WATCurMax = 0;    //Start off max as min possible value
        
        //Brown Fat (BAT)
        int BATCount = 0;
        float BATValue = 0;
        ArrayList<Integer> BATMinVals = new ArrayList<>();
        ArrayList<Integer> BATMaxVals = new ArrayList<>();
        int BATCurMin = 1000; //Start off min as max possible value
        int BATCurMax = 0;    //Start off max as min possible value
        
        //Multiply all bounds from user input percentage to pixel scale (0-1000)
        int TFLwr = bounds[0] * 10;
        int TFUpr = bounds[1] * 10;
        int WATLwr = bounds[2] * 10;
        int WATUpr = bounds[3] * 10;
        int BATLwr = bounds[4] * 10;
        int BATUpr = bounds[5] * 10;
        
        this.width = width;
        this.height = height;
        int numSegmentedImages = segmentList.data.sizeZ();
        boolean fileIsOpen = false;
        ImageProcessor currentSlice = null;
        int currentMriPixel = 0;
        //If they give a dicom list which has an incorrect number of files compared with
        //its matching masked files this is an error and return
        int pixelColour = 0; // the colour of  agiven pixel (between 0 - 1000) 
        if (numSegmentedImages != dicomList.length) {
            return null;
        }
        
        for (int sliceNum = 0; sliceNum < numSegmentedImages; sliceNum++) {
            currentSlice = null;
            fileIsOpen = false;
            //Reset Max and Min values
            TFCurMin = 1000;
            TFCurMax = 0;
            WATCurMin = 1000;
            WATCurMax = 0;
            BATCurMin = 1000;
            BATCurMax = 0;
            //System.err.println("" + segmentList.data.dimension());
            for(int y = 0; y < height; y ++) {
               for(int x = 0; x < width; x ++) {
                   //System.err.println(" " + x + "  " + y + " " + sliceNum);
                   pixelColour = (int) segmentList.data.get(x, y, sliceNum, 0);
                   if (pixelColour > 0) {
                       //System.err.print("Segment Val: " + pixelColour + "\n");
                       if (fileIsOpen == false) {
                           //System.err.print("Opening " + sliceNum + "\n");
                           fileIsOpen = true;
                            currentSlice = openDicom.openImaFile(dicomList[sliceNum]);
                       }
                       //Have MRI slice and segment, count fat voxels
                       currentMriPixel = currentSlice.get(x, y);
                       //System.err.print("CurrentPixel: " + currentMriPixel + "\n");
                       //Total Fat (TF)
                       if (currentMriPixel >= TFLwr && currentMriPixel <= TFUpr) {
                           //System.err.print("TF Pixel Val: " + currentMriPixel + "\n");
                           TFCount++;
                           TFValue += (currentMriPixel);// / 1000);
                           if (currentMriPixel < TFCurMin) {
                               TFCurMin = currentMriPixel;
                           }
                           if (currentMriPixel > TFCurMax) {
                               TFCurMax = currentMriPixel;
                           }
                        }
                       
                       //WAT
                       if (currentMriPixel >= WATLwr && currentMriPixel <= WATUpr) {
                           WATCount++;
                           WATValue += (currentMriPixel);// / 1000);
                           if (currentMriPixel < WATCurMin) {
                               WATCurMin = currentMriPixel;
                           }
                           if (currentMriPixel > WATCurMax) {
                               WATCurMax = currentMriPixel;
                           }
                        }
                       
                       //BAT
                       if (currentMriPixel >= BATLwr && currentMriPixel <= BATUpr) {
                           BATCount++;
                           //System.err.print("BAT Value Incremented: " + (BATValue) + currentMriPixel + "\n");
                           BATValue += (currentMriPixel);// / 1000);
                           if (currentMriPixel < BATCurMin) {
                               BATCurMin = currentMriPixel;
                           }
                           if (currentMriPixel > BATCurMax) {
                               BATCurMax = currentMriPixel;
                           }
                        }
                       
                   }
               }
           }
           //Update the MAX and MIN for each slice
           //TF
           TFMinVals.add(TFCurMin);
           TFMaxVals.add(TFCurMax);
           //WAT
           WATMinVals.add(WATCurMin);
           WATMaxVals.add(WATCurMax);
           //BAT
           BATMinVals.add(BATCurMin);
           BATMaxVals.add(BATCurMax);
           
        }
        //Finished Counting, Sort and calc stats
        //TF
        Collections.sort(TFMaxVals, Collections.reverseOrder());
        int TFMaxSum = 0;
        for (int i : TFMaxVals) {
            TFMaxSum += i;
        }
        double TFMaxAv = TFMaxSum / TFMaxVals.size();
        Collections.sort(TFMinVals);
        int TFMinSum = 0;
        for (int i : TFMinVals) {
            TFMinSum += i;
        }
        double TFMinAv = TFMinSum / TFMinVals.size();
        double TFAvVal = (TFValue / 1000) / TFCount;
        double TFVol = voxToCm(TFCount, voxDimen);
        
        //WAT
        Collections.sort(WATMaxVals, Collections.reverseOrder());
        int WATMaxSum = 0;
        for (int i : WATMaxVals) {
            WATMaxSum += i;
        }
        double WATMaxAv = WATMaxSum / WATMaxVals.size();
        Collections.sort(WATMinVals);
        int WATMinSum = 0;
        for (int i : WATMinVals) {
            WATMinSum += i;
        }
        double WATMinAv = WATMinSum / WATMinVals.size();
        double WATAvVal = (WATValue / 1000) / WATCount;
        double WATVol = voxToCm(WATCount, voxDimen);
        
        //BAT
        Collections.sort(BATMaxVals, Collections.reverseOrder());
        int BATMaxSum = 0;
        for (int i : BATMaxVals) {
            BATMaxSum += i;
        }
        double BATMaxAv = BATMaxSum / BATMaxVals.size();
        Collections.sort(BATMinVals);
        int BATMinSum = 0;
        for (int i : BATMinVals) {
            BATMinSum += i;
        }
        double BATMinAv = BATMinSum / BATMinVals.size();
        double BATAvVal = (BATValue / 1000) / BATCount;
        double BATVol = voxToCm(BATCount, voxDimen);
        
        //Create Return list
        
        double[] retList = {TFAvVal, TFVol, (TFMinVals.get(0) / 10), (TFMinAv / 10), (TFMaxVals.get(0) / 10), (TFMaxAv / 10), 
            WATAvVal, WATVol, (WATMinVals.get(0) / 10), (WATMinAv / 10), (WATMaxVals.get(0) / 10), (WATMaxAv / 10), 
            BATAvVal, BATVol, (BATMinVals.get(0) / 10), (BATMinAv / 10), (BATMaxVals.get(0) / 10), (BATMaxAv / 10)};
        
//        System.err.print(Arrays.toString(retList) + "\n");
//        System.err.print("BAT Val: " + (BATAvVal) + "\n");
//        System.err.print("TF Val: " + (TFAvVal) + "\n");
//        System.err.print("TFMin Val: " + Arrays.toString(TFMinVals.toArray()) + "\n");
//        System.err.print("BAT min: " + (BATMinVals.get(0) / 10) + "\n");
//        System.err.print("BAT max: " + (BATMaxVals.get(0) / 10) + "\n");
//        System.err.print("WAT min: " + (WATMinVals.get(0) / 10) + "\n");
//        System.err.print("WAT max: " + (WATMaxVals.get(0) / 10) + "\n");
//        System.err.print("TF min: " + (TFMinVals.get(0) / 10) + "\n");
//        System.err.print("TF max: " + (TFMaxVals.get(0) / 10) + "\n");
        return retList;
    }
    
    /**
     * 
     * @param voxCount
     * @param voxDimen, list of voxel dimensions [x, y, z]
     * @return 
     */
    private double voxToCm(int voxCount, double voxDimen) {
        //float voxVol = voxDimen[0] * voxDimen[1] * voxDimen[2];
        return voxCount * voxDimen * 0.001;
    }

    ImagePlus getSliceArr(String segment, int sliceNum) {
        ImagePlus niImage = IJ.openImage(segment);
        niImage.show();
        return niImage;
    }


}
