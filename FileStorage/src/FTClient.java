import java.net.*;
import java.io.*;
import java.util.*;

public class FTClient {

	Socket s = null;
	DataInputStream dis = null;
	DataOutputStream dos = null;

	String[] args = null;

	public void start(String server, int port) throws Exception {
		s = establish(server, port);
		dis = new DataInputStream(s.getInputStream());
		dos = new DataOutputStream(s.getOutputStream());

		if (args[1].equals("get")) {
			dos.writeInt(3);
			dos.flush();
			int files = dis.readInt();
			if (files == 0) {
				System.out.println("no files available on the FTServer");
				s.close();
				System.exit(-1);
			}
			String[] filenames = new String[files];
			for (int i = 0; i < files; i++) {
				filenames[i] = dis.readUTF();
				System.out.println(i + 1 + "\t\t" + filenames[i]);
			}

			System.out.print("please input your choice:");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));
			String c = br.readLine();

			if (c.equalsIgnoreCase("q")) {
				s.close();
				System.exit(0);
			}

			if (c.equalsIgnoreCase("a")) {
				for (int i = 0; i < filenames.length; i++) {
					download(filenames[i]);
				}
				s.close();
				System.exit(0);
			}
			int choice = 0;
			try {
				choice = Integer.parseInt(c);
			} catch (NumberFormatException e) {
				System.out.println("your input is wrong");
				s.close();
				System.exit(-2);
			}

			if (choice >= 1 && choice <= filenames.length) {
				download(filenames[choice - 1]);
			} else {
				System.out.println("your input is wrong");
				s.close();
				System.exit(-5);
			}
			s.close();
			System.exit(0);
		} else if (args[1].equals("put")) {
			File f = new File(args[2]);
			if (f.isFile()) {
				upload(args[2]);
			} else if (f.isDirectory()) {
				String[] filenames = f.list();
				if (filenames.length == 0) {
					s.close();
					System.out.println("no files available in the directory");
					System.exit(-8);
				}
				for (int i = 0; i < filenames.length; i++) {
					System.out.println(i + 1 + "\t\t" + filenames[i]);
				}
				System.out.print("please input your choice:");
				BufferedReader br = new BufferedReader(new InputStreamReader(
						System.in));
				String c = br.readLine();

				if (c.equalsIgnoreCase("q")) {
					s.close();
					System.exit(0);
				}

				if (c.equalsIgnoreCase("a")) {
					for (int i = 0; i < filenames.length; i++) {
						String dir = f.getCanonicalPath();
						String tf = null;
						if (dir.endsWith(File.separator)) {
							tf = dir + filenames[i];
						} else {
							tf = dir + File.separator + filenames[i];
						}
						if(new File(tf).isDirectory()) continue;
						upload(tf);
					}
					s.close();
					System.exit(0);
				}
				int choice = 0;
				try {
					choice = Integer.parseInt(c);
				} catch (NumberFormatException e) {
					System.out.println("your input is wrong");
					s.close();
					System.exit(-2);
				}

				if (choice >= 1 && choice <= filenames.length) {
					String dir = f.getCanonicalPath();
					if (dir.endsWith(File.separator)) {
						upload(dir + filenames[choice - 1]);
					} else {
						upload(dir + File.separator + filenames[choice - 1]);
					}

				} else {
					System.out.println("your input is wrong");
					s.close();
					System.exit(-5);
				}

			} else {
				s.close();
				System.out.println(args[2] + " not exists");
				System.exit(-7);
			}

			s.close();
			System.exit(0);

		}

	}

	public Socket establish(String server, int port) {
		try {
			Socket s = new Socket(server, port);
			return s;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void upload(String filename) throws Exception {
		
	
		File f = new File(filename);

		if (!f.exists() || !f.isFile()) {
			System.out
					.println("it's wrong, maybe it is not a file or not exists");
			System.exit(-6);
		}

		byte[] buffer = new byte[4096];
		int rr = 0;

		dos.writeInt(1);
		dos.writeUTF(f.getName());
		dos.writeLong(f.length());
		dos.flush();

		FileInputStream fis = new FileInputStream(f);
		BufferedInputStream bis = new BufferedInputStream(fis);

		while ((rr = bis.read(buffer)) != -1) {
			dos.write(buffer, 0, rr);
			dos.flush();
		}

		bis.close();
		fis.close();

	}

	public void download(String filename) throws Exception {
		dos.writeInt(2);
		dos.writeUTF(filename);
		dos.flush();

		filename = dis.readUTF();
		long len = dis.readLong();

		byte[] buffer = new byte[4096];
		long r = 0;
		int rr = 0;

		FileOutputStream fos = new FileOutputStream(filename);
		BufferedOutputStream bos = new BufferedOutputStream(fos);

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

	}

	public static void main(String[] args) throws Exception {
		if(args.length==0) {
			System.out.println("Usage:");
			System.out.println("java FTClient host get");
			System.out.println("java FTClient host put afile");
			System.exit(0);
			
		}
		FTClient ftc = new FTClient();
		ftc.args = args;
		ftc.start(args[0], 4321);

	}
}
