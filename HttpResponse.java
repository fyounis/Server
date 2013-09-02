package cps730.server.httpResponse;

public class HttpResponse
{
	//20x
	public final static String _200 = "HTTP/1.0 200 OK\r\n"; //  - REQUEST HAS SUCCEEDED.
	public final static String _201 = "HTTP/1.0 201 CREATED\r\n";
	//40x
	public final static String _400 = "HTTP/1.0 400 BAD REQUEST.\r\n";
	public final static String _403 = "HTTP/1.0 403 NO READ PERMISSIONS.\r\n";
	public final static String _404 = "HTTP/1.0 404 FILE DOES NOT EXIST.\r\n";
	
	//50x
	public final static String _501 = "HTTP/1.0 501 NOT IMPLEMENTED.\r\n";
	
	
}
