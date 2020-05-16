/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package whatsappdownloader;



import com.whatsapp.MediaData;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.whispersystems.libaxolotl.kdf.HKDFv3;


/**
 *
 * @author Joao
 */
public class LinkExtractor {
    private String dbname;
    private Connection con;
    private ArrayList<LinkDownloader> links;
    private String folder;
    public LinkExtractor(String dbname){
        this.dbname=dbname;
        File a=new File(dbname);
        folder=a.getParent();
        con=null;
        links=new ArrayList<>();
    }
    public Connection createConnection() throws SQLException{
         try {
                Class.forName("org.sqlite.JDBC");
                return DriverManager.getConnection("jdbc:sqlite:"+dbname);
            } catch (ClassNotFoundException ex ) {
                System.err.println(ex.toString());
            }

        return null;
    }
    
    public void getKeyFromMediaKey(byte[] mediaKey){
        
    }
    /*
    public byte[] HKDF(key, length, appInfo=""){
        key = 
        keyStream = "";
        keyBlock = "";
        blockIndex = 1;
        while len(keyStream) < length:
            keyBlock = hmac.new(key, msg=keyBlock+appInfo+chr(blockIndex), digestmod=hashlib.sha256).digest();
            blockIndex += 1;
            keyStream += keyBlock;
        return keyStream[:length];
    }
    */
    public static int tot=0;
    public byte[] getCipherKey(byte[] rawData) {
        
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
            ObjectInput in = new ObjectInputStream(bis);
            MediaData media = (MediaData) in.readObject();
            
           
            if(media.cipherKey!=null)
                return media.cipherKey;
            else{
                HKDFv3 hk=new HKDFv3();

                byte[] key=hk.deriveSecrets(media.mediaKey,aux.getBytes("UTF-8") , 112);
            
                byte[] cpk=Arrays.copyOfRange(key,16,48);

                return cpk;
            }
        } catch (Exception ex) {
            Logger.getLogger(LinkExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
        
    }
    public byte[] getIV(byte[] rawData){
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(rawData);
            ObjectInput in = new ObjectInputStream(bis);
            MediaData media = (MediaData) in.readObject();
            if(media.iv!=null){
                return media.iv;
            }else{
                HKDFv3 hk=new HKDFv3();

                byte[] key=hk.deriveSecrets(media.mediaKey,aux.getBytes("UTF-8") , 112);
            
                byte[] iv=Arrays.copyOfRange(key,0,16);

                return iv;
            }
        } catch (Exception ex) {
            Logger.getLogger(LinkExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    private String aux="";
    public static String capitalize(String aux){
        String temp=aux=aux.substring(0,1).toUpperCase()+aux.substring(1);
        return temp;
    }
    public void extractLinks() throws SQLException{
        if(con==null){
            con=createConnection();
        }
        Statement stmt= con.createStatement();
        ResultSet rs=stmt.executeQuery(sql_android);
        while(rs.next()){
            String link=rs.getString("url");
            String hash=rs.getString("hash");
            String tipo=rs.getString("tipo");
            long id=rs.getLong("_id");
            if(tipo==null){
                continue;
            }
            aux=tipo.substring(0,tipo.indexOf("/"));
            aux=capitalize(aux).trim();
            aux="WhatsApp "+aux+" Keys";
            
            tipo=tipo.substring(tipo.indexOf("/")+1);
            
            byte[] rawData=rs.getBytes("data");
            byte[] cipherkey=getCipherKey(rawData);
            byte[] iv=getIV(rawData);
            if(cipherkey==null || iv==null){
                tot++;
            }
            
          
            
            LinkDownloader ld=new LinkDownloader(link,tipo, hash,cipherkey,iv);
            if(ld.getFileName()!=null)
                links.add(ld);
        }
    }
    public ArrayList<LinkDownloader> getLinks(){
        return links;
    }
    
    public static final String sql_android="SELECT media_url as url,media_hash as hash ,media_mime_type as tipo,thumb_image as data,_id from messages "
            + "where media_url is not null and media_hash is not null\n" +
            "group by url";
    public static final String sql_ios="SELECT mi.ZMEDIAURL as url,mi.ZVCARDNAME as hash from ZWAMESSAGE m inner join ZWAMEDIAITEM mi on m.ZMEDIAITEM=mi.Z_PK\n" +
"where hash is not null and url is not null\n" +
" group by hash"
            + " ORDER by m.ZMESSAGEDATE desc";

}
