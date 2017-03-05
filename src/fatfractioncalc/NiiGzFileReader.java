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
    
    public NiftiVolume readNiiGzFile(String niiGzPath) {
        NiftiVolume volume = null;
        try {
            volume = NiftiVolume.read(niiGzPath);
        } catch (IOException ex) {
            System.err.print("open err in NII.GZ reader\n");
            //System.out.println(ex.getMessage());
        }
        return volume;
    }


}
