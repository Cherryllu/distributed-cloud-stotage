import java.io.File;
import java.util.Properties;

/**
 * Created by qiubo on 2016/7/14.
 * 读取配置信息
 */
public class getServerInformation {
    public static String SeverName = null;
    public static String IpAddress = null;
    public static String Port = null;
    public static File RootFolder = null;
    public static String Volume = null;

    public static String getPropertyName(File dir) {
        String PropertyName = null;
        File[] f = dir.listFiles();
        for(int i=0;i<f.length;i++) {
            PropertyName = f[i].getName();
            if(PropertyName.startsWith("Storage") && PropertyName.endsWith(".properties")) {
                return PropertyName;
            }
        }
        return null;
    }

    public getServerInformation() {
        File dir = new File(System.getProperty("user.dir") + "\\src");
        String PropertyName = getPropertyName(dir);
        Properties p = new Properties();
        try {
            p.load(getServerInformation.class.getResourceAsStream(PropertyName));
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("properties not exist!");
            System.exit(-1);
        }
        SeverName = p.getProperty("server");
        IpAddress = p.getProperty("ip");
        Port = p.getProperty("port");
        RootFolder = new File(p.getProperty("root_folder"));
        Volume = p.getProperty("volume");
        if(SeverName == null || IpAddress == null || Port == null || RootFolder == null || Volume == null) {
            System.out.println("properties configuration are not correct!");
            System.exit(-2);
        }
        if(!( Volume.endsWith("GB") || Volume.endsWith("MB") || Volume.endsWith("KB") )) {
            System.out.println("volume configuration is not correct");
            System.exit(-3);
        }
        if(!RootFolder.isDirectory()) {
            System.out.println("share directory not exists or isn't a directory");
            System.exit(-4);
        }
    }
}
