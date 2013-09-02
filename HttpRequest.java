package cps730.server.httpRequest;


public class HttpRequest
{
	public enum RequestType {GET, POST, HEAD};
	
	private String _data;
	
	private int _length;
	private String _path;
	private RequestType _request;
	private String _protocol;
	
	public String get_data()
	{
		return _data;
	}
	public void set_data(String _data)
	{
		this._data = _data;
	}
	public int get_length()
	{
		return _length;
	}
	public void set_length(int _length)
	{
		this._length = _length;
	}
	public String getPath()
	{
		return _path;
	}
	public void setPath(String path)
	{
		this._path = path;
	}
	public RequestType getRequest()
	{
		return _request;
	}
	public void setRequest(RequestType request)
	{
		this._request = request;
	}
	public String getProtocol()
	{
		return _protocol;
	}
	public void setProtocol(String protocol)
	{
		this._protocol = protocol;
	}
	public HttpRequest(){}
	public HttpRequest (String request) throws InvalidHttpRequest
	{
		try{
			
			
			if(request.startsWith("GET"))
				this.setRequest(RequestType.GET);
			else if(request.startsWith("HEAD"))
				this.setRequest(RequestType.HEAD);
			else if(request.startsWith("POST"))
			{
				this.setRequest(RequestType.POST);
				
			}
			int start = request.indexOf(" ") +1;
			int end = request.indexOf(" ", start);
			this.setPath(request.substring(start, end));
			this.setProtocol(request.substring(end+1));
			
			
			
		}
		catch(IndexOutOfBoundsException e)
		{
			//substring failed meaning the parse failed, meaning the request was invalid.
			throw new InvalidHttpRequest();
		}       
		
	}
}
