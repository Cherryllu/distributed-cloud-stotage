import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.Socket;
import java.util.*;

/**
 * Created by qiu-bo on 2016/7/17.
 * 中转服务器，提供面向文件存储服务器以及客户端的接口，维护并更新存储服务器端的结点信息
 */
public class FileServer{

    public static int fileServerPort = 5432;

    public int getStorageServerAmount() {
        return StorageServerAmount;
    }

    public void setStorageServerAmount(int storageServerAmount) {
        StorageServerAmount = storageServerAmount;
    }

    private int StorageServerAmount = 0;

    public StorageServerInformation[] getS() {
        return s;
    }

    public void setS(StorageServerInformation[] s) {
        this.s = s;
    }

    private StorageServerInformation[] s = null;

    public File getFileList() {
        return fileList;
    }

    public void setFileList(File fileList) {
        this.fileList = fileList;
    }

    private File fileList = new File(System.getProperty("user.dir") + "\\src", "fileList.properties");
    ;

    public File getPropertiesFile() {
        return propertiesFile;
    }

    public void setPropertiesFile(File propertiesFile) {
        this.propertiesFile = propertiesFile;
    }

    private File propertiesFile = null;

    public FileServer() {
        propertiesFile = new File(System.getProperty("user.dir") + "\\src", "server.properties");
        //如果是第一次运行
        if (!propertiesFile.exists()) {
            File StorageProperties = new File(System.getProperty("user.dir") + "\\src\\StorageProperties");
            File[] ListFile = StorageProperties.listFiles();
            StorageServerAmount = ListFile.length;
            s = new StorageServerInformation[StorageServerAmount];
            for (int i = 0; i < StorageServerAmount; i++) {
                s[i] = new StorageServerInformation();
            }
            //System.out.println(s[0].getVolume());
            for (int i = 0; i < StorageServerAmount; i++) {
                s[i].getStorageInformation(ListFile[i]);
            }
            try {
                propertiesFile.createNewFile();
                fileList.createNewFile();
                Properties p = new Properties();
                FileInputStream fis = new FileInputStream(propertiesFile);
                p.load(fis);
                p.setProperty("fileServerPort", fileServerPort + "");
                p.setProperty("serverAmount", StorageServerAmount + "");
                for (int i = 0; i < StorageServerAmount; i++) {
                    p.setProperty("serverName" + i, s[i].getServerName());
                    p.setProperty("ipAddress" + i, s[i].getIpAddress());
                    p.setProperty("port" + i, s[i].getPort() + "");
                    p.setProperty("volume" + i, s[i].getVolume() + "");
                    p.setProperty("availableVolume" + i, s[i].getVolume() + "");
                    p.setProperty("fileAmount" + i, s[i].getFileAmount() + "");
                }
                p.store(new FileOutputStream(propertiesFile), null);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        //如果不是第一次运行，读取配置
        //else {
            try {
                Properties p = new Properties();
                FileInputStream fis = new FileInputStream(propertiesFile);
                p.load(fis);
                fileServerPort = Integer.parseInt(p.getProperty("fileServerPort"));
                StorageServerAmount = Integer.parseInt(p.getProperty("serverAmount"));
                s = new StorageServerInformation[StorageServerAmount];
                for (int i = 0; i < StorageServerAmount; i++) {
                    s[i] = new StorageServerInformation();
                }
                for (int i = 0; i < StorageServerAmount; i++) {
                    s[i].setServerName(p.getProperty("serverName" + i));
                    s[i].setIpAddress(p.getProperty("ipAddress" + i));
                    s[i].setPort(Integer.parseInt(p.getProperty("port" + i)));
                    s[i].setVolume(Double.parseDouble(p.getProperty("volume" + i)));
                    s[i].setAvailableVolume(Double.parseDouble(p.getProperty("availableVolume" + i)));
                    s[i].setFileAmount(Integer.parseInt(p.getProperty("fileAmount" + i)));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        //}

    }
    //随机选出2个可用的服务器
    public int[] getServer(long len) {
        int[] i = new int[2];
        int[] available = new int[StorageServerAmount];
        int availableCount = 0;
        List<Integer> list = new ArrayList<Integer>(StorageServerAmount);
        //选出能够联通且容量足够的服务器
        for (int j = 0; j < StorageServerAmount; j++) {
            if (s[j].isAvailable() && s[j].getAvailableVolume() * 1024 > len) {
                available[j] = 1;
            } else {
                available[j] = 0;
            }
        }
        for (int k = 0; k < StorageServerAmount; k++) {
            if (available[k] == 1) {
                list.add(k);
                availableCount++;
            }
        }
        if (availableCount >= 2) {
            Collections.shuffle(list);
            i[0] = list.get(0);
            i[1] = list.get(1);
        } else {
            i[0] = -1;
            i[1] = -1;
        }
        return i;
    }
    //更新文件存储服务器的信息到配置文件中
    public void renewServerStatus(int i) {
        try {
            Properties p = new Properties();
            FileInputStream fis = new FileInputStream(propertiesFile);
            p.load(fis);
            p.setProperty("availableVolume" + i, s[i].getAvailableVolume() + "");
            p.setProperty("fileAmount" + i, s[i].getFileAmount() + "");
            p.store(new FileOutputStream(propertiesFile), null);
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //测试文件存储服务器是否可用
    public void testServer() {
        Socket s1 = null;
        for (int j = 0; j < StorageServerAmount; j++) {
            try {
                s1 = new Socket(s[j].getIpAddress(), s[j].getPort());
                if(s1.isConnected()) {
                    s[j].setAvailable(true);
                }
                else {
                    s[j].setAvailable(false);
                }
                s1.close();
            } catch (Exception e) {
            }
        }
    }
    //打印存储服务器端的各项信息
    public void showInformation() {
        int FileAmount = 0;
        for(int i=0;i<StorageServerAmount;i++) {
            System.out.println("File storage server" + i + ":");
            System.out.println("AvailableVolume: " + s[i].getAvailableVolume());
            System.out.println("FileAmount: " + s[i].getFileAmount());
            FileAmount = FileAmount + s[i].getFileAmount();
        }
        System.out.println("File storage server amount: " + StorageServerAmount);
        System.out.println("Total File amount: " + FileAmount/2);
    }

}

