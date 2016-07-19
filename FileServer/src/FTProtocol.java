import java.net.Socket;
import java.io.*;
import java.net.SocketException;
import java.util.*;

/**
 * 文件存储协议，规定了文件上传，文件下载，文件改名以及文件删除等具体的流程，同时提供了对后端节点出现异常的处理功能
 */
public class FTProtocol implements IOStrategy {

    @Override
    public void service(Socket socket, FileServer fs) {
        String client = socket.getInetAddress().getHostName() + "(" + socket.getInetAddress().getHostAddress() + ")";

        Socket s1 = null;
        Socket s2 = null;

        InputStream is1 = null;
        OutputStream os1 = null;
        DataInputStream dis1 = null;
        DataOutputStream dos1 = null;

        InputStream is2 = null;
        OutputStream os2 = null;
        DataInputStream dis2 = null;
        DataOutputStream dos2 = null;

        String filename = null;
        long len = 0;
        byte[] buffer = new byte[4096];
        long r = 0;
        int rr = 0;
        String uuid = null;
        String s = null;
        String[] ss = null;
        int serverId[] = new int[2];
        int[] i = null;
        int finalServer;
        int command = 0;

        try {
            Properties p = new Properties();
            FileInputStream fis = new FileInputStream(fs.getFileList());
            p.load(fis);

            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            DataInputStream dis = new DataInputStream(is);
            DataOutputStream dos = new DataOutputStream(os);

            while (true) {

                command = dis.readInt();

                switch (command) {
                    case 1: // file upload
                        fs.testServer();
                        uuid = UUID.randomUUID().toString();
                        filename = dis.readUTF();
                        len = dis.readLong();
                        r = 0;
                        rr = 0;

                        i = fs.getServer(len);
                        if (i[0] != -1 && i[1] != -1) {
                            dos.writeInt(0);
                            dos.flush();

                            s1 = new Socket(fs.getS()[i[0]].getIpAddress(), fs.getS()[i[0]].getPort());
                            s2 = new Socket(fs.getS()[i[1]].getIpAddress(), fs.getS()[i[1]].getPort());

                            is1 = s1.getInputStream();
                            os1 = s1.getOutputStream();
                            dis1 = new DataInputStream(is1);
                            dos1 = new DataOutputStream(os1);

                            is2 = s2.getInputStream();
                            os2 = s2.getOutputStream();
                            dis2 = new DataInputStream(is2);
                            dos2 = new DataOutputStream(os2);

                            dos1.writeInt(1);
                            dos1.writeUTF(uuid + filename);
                            dos1.writeLong(len);
                            dos2.writeInt(1);
                            dos2.writeUTF(uuid + filename);
                            dos2.writeLong(len);

                            dos1.flush();
                            dos2.flush();

                            while (r < len) {
                                if (len - r >= buffer.length) {
                                    rr = dis.read(buffer, 0, buffer.length);
                                } else {
                                    rr = dis.read(buffer, 0, (int) (len - r));
                                }

                                r = r + rr;

                                dos1.write(buffer, 0, rr);
                                dos2.write(buffer, 0, rr);
                                dos1.flush();
                                dos2.flush();
                            }
                            //返回uuid
                            dos.writeUTF(uuid);
                            dos.flush();
                            //更新存储服务器状态
                            for (int j = 0; j < 2; j++) {
                                fs.getS()[i[j]].setFileAmount(fs.getS()[i[j]].getFileAmount() + 1);
                                fs.getS()[i[j]].setAvailableVolume(fs.getS()[i[j]].getAvailableVolume() - len / 1024);
                                fs.renewServerStatus(i[j]);
                            }
                            //更新文件列表
                            p.setProperty(uuid, filename + "+" + Long.toString(len) + "+" + Integer.toString(i[0]) + "+" + Integer.toString(i[1]) + "+" + filename);
                            p.store(new FileOutputStream(fs.getFileList()), null);
                        } else {
                            dos.writeInt(1);//不可以上传
                            dos.flush();
                        }

                        break;

                    case 2: // file download

                        uuid = dis.readUTF();
                        s = p.getProperty(uuid);
                        if (s == null || s == " ") {
                            dos.writeInt(1);//文件不存在
                            dos.flush();
                            break;
                        }
                        ss = s.split("\\+");
                        filename = ss[0];
                        //len = Long.parseLong(ss[1]);
                        //选择一个服务器
                        serverId[0] = Integer.parseInt(ss[2]);
                        serverId[1] = Integer.parseInt(ss[2]);
                        fs.testServer();//更新存储服务器连通性信息
                        if (fs.getS()[serverId[0]].isAvailable()) {
                            finalServer = serverId[0];
                        } else if (fs.getS()[serverId[1]].isAvailable()) {
                            finalServer = serverId[1];
                        } else {
                            finalServer = -1;
                        }
                        if (finalServer != -1) {
                            dos.writeInt(0);//告诉客户端文件找到
                            dos.flush();
                            s1 = new Socket(fs.getS()[finalServer].getIpAddress(), fs.getS()[finalServer].getPort());
                            is1 = s1.getInputStream();
                            os1 = s1.getOutputStream();
                            dis1 = new DataInputStream(is1);
                            dos1 = new DataOutputStream(os1);
                            dos1.writeInt(2);//存储服务器上传指令
                            dos1.writeUTF(uuid + filename);//原始文件名ss[0],新名字ss[4]
                            dis1.readUTF();
                            dos.writeUTF(ss[4]);
                            len = dis1.readLong();
                            dos.writeLong(len);

                            dos.flush();
                            dos1.flush();

                            while ((rr = dis1.read(buffer)) != -1) {

                                dos.write(buffer, 0, rr);
                                dos.flush();
                            }
                        } else {
                            dos.writeInt(2);//文件服务器当前不可用
                        }

                        break;

                    case 3: // delete files
                        uuid = dis.readUTF();
                        s = p.getProperty(uuid);
                        if (s == null || s == " ") {
                            dos.writeInt(1);
                            dos.flush();
                            break;
                        }
                        ss = s.split("\\+");
                        filename = ss[0];
                        len = Long.parseLong(ss[1]);
                        serverId[0] = Integer.parseInt(ss[2]);
                        serverId[1] = Integer.parseInt(ss[3]);
                        fs.testServer();
                        if (fs.getS()[serverId[0]].isAvailable() && fs.getS()[serverId[1]].isAvailable()) {
                            s1 = new Socket(fs.getS()[serverId[0]].getIpAddress(), fs.getS()[serverId[0]].getPort());
                            s2 = new Socket(fs.getS()[serverId[1]].getIpAddress(), fs.getS()[serverId[1]].getPort());
                            is1 = s1.getInputStream();
                            os1 = s1.getOutputStream();
                            dis1 = new DataInputStream(is1);
                            dos1 = new DataOutputStream(os1);
                            is2 = s2.getInputStream();
                            os2 = s2.getOutputStream();
                            dis2 = new DataInputStream(is2);
                            dos2 = new DataOutputStream(os2);
                            dos1.writeInt(3);
                            dos1.writeUTF(uuid + filename);
                            dos2.writeInt(3);
                            dos2.writeUTF(uuid + filename);
                            dos1.flush();
                            dos2.flush();
                            if (dis1.readInt() == 0 && dis2.readInt() == 0) {
                                dos.writeInt(0);
                            } else {
                                dos.writeInt(1);
                            }
                            dos.flush();
                            //更新文件列表
                            p.setProperty(uuid, " ");
                            p.store(new FileOutputStream(fs.getFileList()), null);
                            //更新存储服务器状态
                            for (int j = 0; j < 2; j++) {
                                System.out.println(fs.getS()[serverId[j]].getFileAmount());
                                fs.getS()[serverId[j]].setFileAmount(fs.getS()[serverId[j]].getFileAmount() - 1);
                                System.out.println(fs.getS()[serverId[j]].getFileAmount());
                                fs.getS()[serverId[j]].setAvailableVolume(fs.getS()[serverId[j]].getAvailableVolume() + len / 1024);
                                fs.renewServerStatus(serverId[j]);
                            }
                        } else {
                            dos.writeInt(2);
                            dos.flush();
                        }

                        break;
                    case 4://rename a file
                        uuid = dis.readUTF();
                        s = p.getProperty(uuid);
                        if (s != null || s == " ") {
                            dos.writeInt(0);
                            dos.flush();
                            ss = s.split("\\+");
                            ss[4] = dis.readUTF();
                            p.setProperty(uuid, ss[0] + "+" + ss[1] + "+" + ss[2] + "+" + ss[3] + "+" + ss[4]);
                            p.store(new FileOutputStream(fs.getFileList()), null);
                        } else {
                            dos.writeInt(1);
                            dos.flush();
                        }
                        break;
                }
            }
        } catch (Exception e) {
            if (e instanceof EOFException) {
                System.out.println(client + " disconnected");
                try {
                    if (command == 1 || command == 3) {

                        dis1.close();
                        dos1.close();
                        is1.close();
                        is1.close();
                        s1.close();

                        dis2.close();
                        dos2.close();
                        is2.close();
                        is2.close();
                        s2.close();
                    }
                    if (command == 2) {
                        dis1.close();
                        dos1.close();
                        is1.close();
                        is1.close();
                        s1.close();
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            } else if (e instanceof SocketException) {
                if (command == 1) {
                    try {
                        File f = new File(System.getProperty("user.dir") + "error.txt");
                        if (!f.exists()) {
                            f.createNewFile();
                        }
                        FileOutputStream fos = new FileOutputStream(f,true);
                        DataOutputStream dos = new DataOutputStream(fos);
                        dos.writeUTF(uuid +"+"+ filename +"+"+ i[0] +"+"+ i[1]);
                        socket.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                if(command == 3) {
                    try {
                        socket.close();
                    } catch(Exception e3) {
                        e3.printStackTrace();
                    }
                }
                }
            }
        }
    }
