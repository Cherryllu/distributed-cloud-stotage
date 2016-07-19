/**
 * FileClient客户端，提供面向FileServer的Socket连接接口，并且定义了文件上传，下载，删除，改名的不同功能，同时对异常（断电重传）做出相应的处理
 */

import java.net.*;
import java.io.*;

public class FileClient {
    //FileClient中的数据成员
    Socket socket = null;
    DataInputStream in = null;
    DataOutputStream out = null;
    String[] arg = null;

    //建立与FileServe的链接  serve--FileServe（服务器名称）  port--FileServe服务器的端口号  若建立失败抛出异常
    public Socket establish(String serve, int port) throws Exception {
        Socket s = new Socket(serve, port);
        return s;
    }

    //向服务器端上传一份文件
    public void upload(String FileName) {

        try {
            File f = new File(FileName);
            byte[] buffer = new byte[4096];
            int rr = 0;
            out.writeInt(1);
            out.writeUTF(f.getName());
            out.writeLong(f.length());
            out.flush();

            if (in.readInt() == 0) {

                FileInputStream fis = new FileInputStream(f);
                BufferedInputStream bis = new BufferedInputStream(fis);

                while ((rr = bis.read(buffer)) != -1) {
                    out.write(buffer, 0, rr);
                    out.flush();
                }

                System.out.println(in.readUTF());

                bis.close();
                fis.close();
            } else {
                System.out.println("Server unavailable");
            }
        } catch (Exception e) {
            if(e instanceof SocketException) {
                upload(FileName);
            }
            else {
                e.printStackTrace();
            }
        }
    }


    //从服务器端下载一份文件
    public void download(String FileName) {
        try {
            out.writeInt(2);
            out.writeUTF(FileName);
            out.flush();
            int i = in.readInt();
            if (i == 0)     //服务器端有这个文件，可以执行下载操作
            {
                FileName = in.readUTF();
                long len = in.readLong();

                byte[] buffer = new byte[4096];
                long r = 0;
                int rr = 0;

                FileOutputStream fos = new FileOutputStream(FileName);
                BufferedOutputStream bos = new BufferedOutputStream(fos);

                while (r < len) {
                    if (len - r >= buffer.length) {
                        rr = in.read(buffer, 0, buffer.length);
                    } else {
                        rr = in.read(buffer, 0, (int) (len - r));
                    }

                    r = r + rr;
                    bos.write(buffer, 0, rr);
                }

                bos.close();
                fos.close();
                System.out.println("successful!");
            } else if (i == 1) {
                System.out.println("can not find the file in server");
                //System.exit(-2);
            } else {
                System.out.println("the server is not available now!");
                //System.exit(-3);
            }
        } catch (Exception e) {
            if(e instanceof SocketException) {
                download(FileName);
            }
            else {
                e.printStackTrace();
            }
        }

    }

    //从服务器端中删除文件
    public void remove(String FileName) throws Exception {

        out.writeInt(3);
        out.writeUTF(FileName);
        out.flush();
        int i = in.readInt();
        if (i == 0) {
            System.out.println(FileName + "\t" + "remove is successful");
        } else if (i == 1) {
            System.out.println("can not find the file in the server");
            //System.exit(-3);
        } else {
            System.out.println("the server has errors");
            //System.exit(-2);
        }
    }

    //更改服务器端的文件名称
    public void rename(String FileName) throws Exception {
        out.writeInt(4);
        out.writeUTF(FileName);
        if (in.readInt() == 0) {
            System.out.print("Please input the new name:  ");
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    System.in));
            String c = br.readLine();
            out.writeUTF(c);
            out.flush();
            System.out.println("rename is successful");
        } else {
            System.out.println("can not find the file in the server");
            //System.exit(-3);
        }
    }

    //启动客户端
    public void start(String serve, int port) throws Exception    //启动客户端的函数
    {
        socket = establish(serve, port);
        in = new DataInputStream(socket.getInputStream());   //将输入流绑定到socket中
        out = new DataOutputStream(socket.getOutputStream());   //将输出流绑定到socket中
        if (arg[1].equals("upload"))   //代表该客户端即将执行上传文件操作
        {
            File file = new File(arg[2]);
            if (file.isFile()) {                     //直接上传该文件
                upload(arg[2]);
                socket.close();
                //System.exit(0);

            } else if (file.isDirectory()) {         //目录
                String[] files = file.list();
                if (files.length == 0) {                  //目录下没有文件
                    socket.close();
                    System.out.println("no files available in the directory");
                    System.exit(-2);
                }
                for (int i = 0; i < files.length; i++) {
                    System.out.println((i + 1) + "\t\t" + files[i]);   //如果有文件，那么打印目录下的所有文件名称
                }
                System.out.print("please input your choice:");
                BufferedReader br = new BufferedReader(new InputStreamReader(
                        System.in));
                String c = br.readLine();

                if (c.equalsIgnoreCase("quit")) {
                    socket.close();
                    System.exit(0);
                }

                if (c.equalsIgnoreCase("uploadAll")) {
                    for (int i = 0; i < files.length; i++) {
                        String sr = file.getCanonicalPath();
                        String tr = null;
                        if (sr.endsWith(File.separator)) {
                            tr = sr + files[i];
                        } else {
                            tr = sr + File.separator + files[i];
                        }
                        if (new File(tr).isDirectory()) continue;
                        upload(tr);
                    }
                    socket.close();
                    System.exit(0);

                } else {
                    int choice = 0;
                    try {
                        choice = Integer.parseInt(c);
                    } catch (NumberFormatException e) {
                        System.out.println("your input is wrong");
                        socket.close();
                        System.exit(-3);
                    }

                    if (choice >= 1 && choice <= files.length) {
                        String sr = file.getCanonicalPath();
                        if (sr.endsWith(File.separator)) {
                            upload(sr + files[choice - 1]);
                        } else {
                            upload(sr + File.separator + files[choice - 1]);
                        }

                    } else {
                        System.out.println("your input is wrong");
                        socket.close();
                        System.exit(-5);
                    }
                }

            } else {
                socket.close();
                System.out.println(arg[2] + " not exists");
                System.exit(-7);
            }


        }

        if (arg[1].equals("download"))    //代表该客户端即将执行下载文件操作
        {
            String filename = arg[2];
            if (!filename.isEmpty()) {
                download(filename);
            } else {
                System.out.println("can not find the input data");
            }
            socket.close();
            System.exit(0);
        }
        if (arg[1].equals("rename"))    //代表客户端即将执行改名操作
        {
            if (!arg[2].isEmpty()) {
                String FileName = arg[2];
                rename(FileName);
                socket.close();
                System.exit(0);
            } else {
                System.out.print("wrong input filename");
                socket.close();
                System.exit(-3);
            }

        }
        if (arg[1].equals("remove"))    //代表客户端即将执行删除操作
        {
            if (!arg[2].isEmpty()) {
                remove(arg[2]);
                socket.close();
                System.exit(0);
            } else {
                System.out.print("wrong input data");
                socket.close();
                System.exit(-1);
            }


        }
    }
    public static void main(String[] args) {
        try {
            if (args.length == 0) {
                System.out.println("Usage:");
                System.out.println("java FileClient host get");
                System.out.println("java FileClient host put afile");
                System.exit(0);
            }
            FileClient fc = new FileClient();
            fc.arg = args;
            fc.start(args[0], 5432);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
