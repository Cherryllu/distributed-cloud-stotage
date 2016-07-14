import java.io.File;
import java.util.Properties;

/**
 * Created by qiubo on 2016/7/14.
 */
public class StorageSever {
    public static String SeverName = new String();
    public static String IpAddress = new String();
    public static String Port = new String();
    public static File RootFolder = null;
    public static String Volume = new String();

    public static boolean getPropertyName(File dir,String PropertyName) {
        File[] f = dir.listFiles();
        for(int i=0;i<f.length;i++) {
            PropertyName = f[i].getName();
            if(PropertyName.startsWith("Storage") && PropertyName.endsWith(".properties")) {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        File dir = new File("./");
        String PropertyName = new String();
        getPropertyName(dir,PropertyName);
        Properties p = new Properties();
        try {
            p.load(StorageSever.class.getResourceAsStream(PropertyName));
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("properties not exist!");
            System.exit(-1);
        }
        SeverName = p.getProperty("sever");
        IpAddress = p.getProperty("ip");
        Port = p.getProperty("port");
        RootFolder = new File(p.getProperty("root_folder"));
        Volume = p.getProperty("volume");
        if(SeverName == null || IpAddress == null || Port == null || RootFolder == null || Volume == null) {
            System.out.println("properties are not correct!");
            System.exit(-2);
        }
        if(!( Volume.endsWith("GB") || Volume.endsWith("MB") || Volume.endsWith("KB") )) {
            System.out.println("volume configuration is not correct");
        }
    }
}
