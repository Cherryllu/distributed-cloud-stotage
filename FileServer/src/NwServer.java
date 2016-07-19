import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.*;

/*
	ʵ������ͨ�ţ����Է������κ�Ӧ�ã�û���ṩЭ�飬
	Ҳ����˵NwServer�����������κ�Э�顣
*/

public class NwServer
{
	private int port = 5432;  //û�д���˿�
	
	public NwServer(IOStrategy io, int port,FileServer fs) throws Exception {
		//��ֻ������ܿͻ��˵��������󣬽������罨����socket���ӣ�
		//Ȼ�������ύ��Э�鴦�����
		
		this.port = port;

		ServerSocket server = new ServerSocket(port);
		System.out.println("FTServer is ready");


		while(true)
		{
			Socket socket = server.accept();

			InetAddress ia = socket.getInetAddress();
			System.out.println(ia.getHostName() + "(" + ia.getHostAddress() + ") connected."); 

			io.service(socket,fs);
		}
	}
}
