public class FileStorage {
	
	public static void main(String[] args) throws Exception  {

		getServerInformation fs = new getServerInformation();

		int port = 4321;

		port = Integer.parseInt(getServerInformation.Port);

		FTProtocol protocol = new FTProtocol();
		AdvancedSupport as = new AdvancedSupport(protocol);
		NwServer nw = new NwServer(as,port);
			
	}

}
