/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fatfractioncalc;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author ariane
 */
public class NiiGzFileReader {

    public NiiGzFileReader() {

    }
    
    public String unZipNiiGzFile(String nigzpath, String destination) throws IOException {
        FileInputStream filen = null;
        byte[] buffer = new byte[1024];
        filen = new FileInputStream(nigzpath);
        GZIPInputStream gzipIn = new GZIPInputStream(filen);
        String[] name = nigzpath.split("/");
        int len = name.length;
        String dest = name[len - 1];
        System.err.println("" + dest);
        dest = dest.split("_")[0];
        System.err.println("" + dest);
        FileOutputStream fileOUt = new FileOutputStream(destination + "/" + dest + ".nii");
        int bytesRead;
        while ((bytesRead = gzipIn.read(buffer)) > 0) {
            fileOUt.write(buffer, 0, bytesRead);
        }
        gzipIn.close();
        filen.close();

        return destination + "/" + dest + ".nii";
    }
    


}
