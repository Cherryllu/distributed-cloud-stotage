import java.net.Socket;
import java.io.*;
import java.util.*;

public class FTProtocol implements IOStrategy {

    @Override
    public void service(Socket socket) {
        String client = socket.getInetAddress().getHostName() + "(" + socket.getInetAddress().getHostAddress() + ")";

        try {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            DataInputStream dis = new DataInputStream(is);
            DataOutputStream dos = new DataOutputStream(os);

            String filename = null;
            long len = 0;
            byte[] buffer = new byte[4096];
            long r = 0;
            int rr = 0;

            while (true) {
                int command = dis.readInt();
                switch (command) {
                    case 1: // file upload
                        filename = dis.readUTF();
                        len = dis.readLong();
                        FileOutputStream fos = new FileOutputStream(new File(
                                getServerInformation.RootFolder, filename));
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        r = 0;
                        rr = 0;

                        while (r < len) {
                            if (len - r >= buffer.length) {
                                rr = dis.read(buffer, 0, buffer.length);
                            } else {
                                rr = dis.read(buffer, 0, (int) (len - r));
                            }

                            r = r + rr;
                            bos.write(buffer, 0, rr);
                        }

                        bos.close();
                        fos.close();
                        break;
                    case 2: // file download
                        filename = dis.readUTF();
                        dos.writeUTF(filename);
                        File t = new File(getServerInformation.RootFolder, filename);
                        dos.writeLong(t.length());
                        dos.flush();
                        FileInputStream fis = new FileInputStream(t);
                        BufferedInputStream bis = new BufferedInputStream(fis);

                        while ((rr = bis.read(buffer)) != -1) {
                            dos.write(buffer, 0, rr);
                            dos.flush();
                        }

                        bis.close();
                        fis.close();
                        break;

                    case 3: // delete files
                        filename = dis.readUTF();
                        File t1 = new File(getServerInformation.RootFolder, filename);
                        if(t1.isFile()) {
                            t1.delete();
                            dos.writeInt(0);
                        }
                        else {
                            dos.writeInt(1);
                        }
                        break;

                }
            }
        } catch (Exception e) {
            if (e instanceof EOFException) {
                System.out.println(client + " disconnected");
            } else {
                e.printStackTrace();
            }

        }
    }
}
