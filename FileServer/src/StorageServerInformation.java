import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * Created by qiu-bo on 2016/7/17.
 * 记录每个文件存储服务节点的信息
 */
public class StorageServerInformation {

    private String serverName;
    private String ipAddress;
    private int port = 0;
    private double volume = 0;//单位：kb
    private  double availableVolume = 0;
    private int fileAmount = 0;
    private boolean isAvailable;

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getAvailableVolume() {
        return availableVolume;
    }

    public void setAvailableVolume(double availableVolume) {
        this.availableVolume = availableVolume;
    }

    public int getFileAmount() {
        return fileAmount;
    }

    public void setFileAmount(int fileAmount) {
        this.fileAmount = fileAmount;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public void getStorageInformation(File f) {
        Properties p = new Properties();

        try {
            FileInputStream fis = new FileInputStream(f);
            p.load(fis);
        }
        catch(Exception e) {
            e.printStackTrace();
            System.out.println("properties not exist!");
            System.exit(-1);
        }
        serverName = p.getProperty("server");
        ipAddress = p.getProperty("ip");
        port = Integer.parseInt(p.getProperty("port"));
        String Volume = p.getProperty("volume");
        if(serverName == null || ipAddress == null || port == 0 || Volume == null) {
            System.out.println("properties configuration are not correct!");
            System.exit(-2);
        }
        if(!( Volume.endsWith("GB") || Volume.endsWith("MB") || Volume.endsWith("KB") )) {
            System.out.println("volume configuration is not correct");
            System.exit(-3);
        }
        if(Volume.endsWith("GB")) {
            volume = 1024*1024*Integer.parseInt(Volume.substring(0,Volume.length()-2));
        }
        else if(Volume.endsWith("MB")) {
            volume = 1024*Integer.parseInt(Volume.substring(0,Volume.length()-2));
        }
        else {
            volume = Integer.parseInt(Volume.substring(0,Volume.length()-2));
        }

    }

}
