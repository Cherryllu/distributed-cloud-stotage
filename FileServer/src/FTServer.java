import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.Thread;

public class FTServer {

    public static void main(String[] args) throws Exception {

        FileServer fs = new FileServer();

        Thread t1 = new Thread() {
            public void run() {
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String str;
                while (true) {
                    try {
                        str = br.readLine();
                        if (str.equals("dis")) {
                            fs.showInformation();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        t1.start();


        FTProtocol protocol = new FTProtocol();
        AdvancedSupport as = new AdvancedSupport(protocol);
        NwServer nw = new NwServer(as, FileServer.fileServerPort, fs);
    }
}
