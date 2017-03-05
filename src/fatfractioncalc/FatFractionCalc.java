/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import ij.IJ;
import ij.ImagePlus;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ariane
 */
public class FatFractionCalc {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        NiiGzFileReader ni = new NiiGzFileReader();
        String niFilePath = null;
        try {
            niFilePath = ni.unZipNiiGzFile("/home/ariane/Documents/Gusto/Latest Compute FF/010-04020_BAT.nii.gz", "/home/ariane/Documents/Gusto/Latest Compute FF/Out");
        } catch (IOException ex) {
            Logger.getLogger(FatFractionCalc.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("" + niFilePath);
        OpenDicom op = new OpenDicom();

            ImagePlus niImage = IJ.openImage("/home/ariane/Documents/Gusto/Latest Compute FF/test.jpg");
            
            niImage.show();
 
    }

}


