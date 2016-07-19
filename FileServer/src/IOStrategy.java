import java.net.*;

/*
	提供协议策略定义
*/

public interface IOStrategy
{
	public void service (Socket socket,FileServer fs);
}
