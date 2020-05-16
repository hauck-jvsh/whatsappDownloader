/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whatsappdownloader;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author ADMHauck
 */
public class DecryptFile {
    private byte[] iv;
    private byte[] cipherKey;
    private byte[] hmacKey;
    private byte[] file;
    private byte[] decripted;
    private File encripted;
    private String filename;
    public DecryptFile(byte [] iv,byte[] cipherKey,String filename){
        this.iv=iv;
        this.cipherKey=cipherKey;
        this.encripted=new File(filename);
        this.filename=filename;
    }
    
    public void readEncFile() throws Exception{
        file=FileUtils.readFileToByteArray(encripted);
        if(file.length==0){
            throw new Exception("Empty File");
        }
        file=Arrays.copyOfRange(file, 0, file.length-10);
    }
    public byte[] decrypt(String ext) throws Exception{
      readEncFile();
      IvParameterSpec iv = new IvParameterSpec(this.iv);
      SecretKeySpec skeySpec = new SecretKeySpec(cipherKey, "AES");
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
      decripted=cipher.doFinal(file);
      
      File f=new File(filename+"."+ext);
      FileUtils.writeByteArrayToFile(f, decripted);
      return decripted;
      
    }
    
}
