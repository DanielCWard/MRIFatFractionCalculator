/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import java.io.IOException;
import niftijio.NiftiVolume;

/**
 *
 * @author ariane
 */
public class NiiGzFileReader {

    public NiiGzFileReader() {

    }
    
    public NiftiVolume readNiiGzFile(String niiGzPath) throws IOException {
        NiftiVolume volume = null;
        volume = NiftiVolume.read(niiGzPath);
//        int x = volume.header.dim[1];
//        int y = volume.header.dim[2];
//        int z = volume.header.dim[3];
//        int dim = volume.header.dim[4];
//        for (int k = 0; k < z; k++) {
//            for (int j = 0; j < y; j ++) {
//                for (int i = 0; i < x; i ++) {
//                    double pix = volume.data.get(i, j, k, 0);
//                    if (pix > 0) {
//                        System.err.println(" " + i + "  " + j + " " + k + " " + pix);
//                    }
//                }
//            }
//        }
//        System.err.println("x: " + x + " y: " + y + " z: " + z + " dim: " + dim + "\n");
//        System.err.println("" + volume.header.toString() + "\n");
//        try {
//            volume = NiftiVolume.read(niiGzPath);
//        } catch (IOException ex) {
//            System.err.print("open err in NII.GZ reader\n");
//            //System.out.println(ex.getMessage());
//        }
        return volume;
    }


}
