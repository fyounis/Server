package cps730.server;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.Semaphore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import cps730.server.httpRequest.*;
import cps730.server.httpRequest.HttpRequest.RequestType;
import cps730.server.httpResponse.HttpResponse;


public class SocketHandler implements Runnable {

    Socket connectionSocket;
    String docRoot;		         //Where .config is stored.
	ArrayList<String> docTypes;  //Type of file(htm,html etc)
	Semaphore sem; //resource access variable
   

    public SocketHandler(Socket connectionSocket, String root, ArrayList<String> types, Semaphore s)
    {
        this.connectionSocket = connectionSocket;
        this.docRoot = root;
        this.docTypes = types;
        this.sem = s;
    }

	public void run()
	{
    	BufferedReader in = null;
    	DataOutputStream out = null;
        try 
        {
        	
        	in = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        	out = new DataOutputStream(connectionSocket.getOutputStream());
        	HttpRequest request = new HttpRequest();
        	String line = "";
        
        	while ((line = in.readLine()) != null && !line.equals("")) 
            {
            	
            	
            	System.out.println("Read: " + line);
            	if(request.getRequest() == null)
        		{
	    			if(line.startsWith("GET"))
	    			{
	    				request.setRequest(RequestType.GET);
	    			}
	    			else if(line.startsWith("HEAD"))
	    			{
	    				request.setRequest(RequestType.HEAD);
	    			}
	    			else if(line.startsWith("POST"))
	    			{
	    				request.setRequest(RequestType.POST);
	    			}
	    			int start = line.indexOf(" ") +1;
	    			int end = line.indexOf(" ", start);
	    			try
	    			{
	    				request.setPath(line.substring(start, end));
		    			request.setProtocol(line.substring(end+1));
	    			}
	    			catch(StringIndexOutOfBoundsException e) // http1/0 left off.
	    			{
	    				System.err.println("No http specified");
	    				throw new InvalidHttpRequest();
	    			}
	    			
	    			if(!request.getProtocol().equals("HTTP/1.0") && !request.getProtocol().equals("HTTP/1.1"))
	    			{
	    				System.err.println(request.getProtocol() + " not supported. Bad Request");
	    				throw new InvalidHttpRequest();
	    			}
        		}
            	
            	//System.out.println(line);
    			if(line.startsWith("Content-Length:"))
    			{
    				request.set_length(Integer.parseInt(line.substring(line.lastIndexOf(" "))));
    			}
                
            }
            if(request.getRequest() == RequestType.POST)
            {
            	char readin;
            	boolean firstending = false;
            	boolean r = false;
            	boolean done = false;
            	StringBuilder sb = new StringBuilder();
	        	while ( !done && (readin = (char)in.read()) >=0) 
	        	{
	        		if(readin == '\n')
	        		{
	        			if(firstending)
	        			{
	        				done = true;
	        			}
	        			else
	        			{
	        				if(r)
	        				{
	        					sb.append('\r');
	        					r = false;
	        				}
	        				
	        				sb.append('\n');
	        				firstending = true;
	        			}
	        		}
	        		else if(readin == '\r') 
	        		{
	        			r = true;	
	        		}
	        		else	        				
        			{
	        			sb.append(readin);
	        			firstending = false;
        			}	        		
	        	}
	        	System.err.println("Data: " + sb.toString());
	        	request.set_data(sb.toString());	
            }
            
            if(request.getRequest() != null)
            {
            	handleHttpRequest(request, out);
            }
            else
            {
            	httpRespond(HttpResponse._501, null, out);
            }
        } 
        catch (InvalidHttpRequest e)
		{
        	httpRespond(HttpResponse._400, null, out);
		} 
        catch (IOException IOE) 
        {
			
        } 
        catch (Exception e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
			
		}
        finally
        {
        	try{
		        connectionSocket.close();
        	} catch(IOException e)
        	{
        		//do nothing
        	}
        	sem.release();
        }
       
	}


    private boolean handleHttpRequest(HttpRequest req, DataOutputStream out) throws InvalidHttpRequest
    {

		 if(req.getPath().startsWith("/") )
         {
			if(supportedDoctype(req.getPath()))
			{
	         	try
	         	{	
	         		if(req.getRequest() == RequestType.GET || req.getRequest() == RequestType.HEAD)
	         		{
		                System.out.println("Attempting to open file: " + docRoot + req.getPath());
		                
		                /////////////////////////////CRITICAL SECTION//////////////////////////////////
		   
		                File readFile = new File(docRoot + req.getPath());
		                
		                if(!readFile.exists())
		                {
		                	httpRespond(HttpResponse._404, null, out );
		                	//String not_Found = "404 - FILE DOES NOT EXIST.\n";
		             		//out.writeBytes(not_Found);
		                }
		                else if(!readFile.canRead())
		                {
		                	httpRespond(HttpResponse._403, null, out);
		                	//String not_Read = "403 - NO READ PERMISSIONS.\n";
		                	//out.writeBytes(not_Read);		                
		         		}
		         		else
		         		{
		         			StringBuilder sb = new StringBuilder();
	
	         				long file_length = readFile.length( );
	         				
	         				Date today_date = new Date();
	            			SimpleDateFormat now_date = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
	            			sb.append("Date: " + now_date.format(today_date) + "\r\n");
	             		
	            			
	            			sb.append("Last-Modified: " + now_date.format(readFile.lastModified()) + "\r\n");
	            			sb.append("Content-Length: ");
	            			sb.append(Long.toString(file_length));
	            			sb.append("\r\n");
	            			sb.append("\r\n");
		    
		         			if(req.getRequest() == RequestType.GET)
		         			{
			         			FileInputStream clientFile = new FileInputStream(docRoot + req.getPath());
			         			DataInputStream in3 = new DataInputStream(clientFile);
			         			BufferedReader buffer1 = new BufferedReader(new InputStreamReader(in3));
			         			
			         			while(buffer1.ready())
			         			{
			         				
			         				sb.append((char)buffer1.read());
			         			}
			         			
			         			/*
			         			out.writeBytes("200 OK - REQUEST HAS SUCCEEDED. \n");
			         			
			         			while(buffer1.ready())
			         			{
			         				System.out.println("Writing to socket");
			         				out.writeBytes(buffer1.readLine()); //writes to client
			         			}
			         			out.flush(); 		 //disposes of remaining data in Outputstream.
			         			 */ 
			         			buffer1.close();	
			         			in3.close();		 //Closes DataInputStream
			         			clientFile.close();  //Closes clientFile.
		         			}
		         			httpRespond(HttpResponse._200, sb.toString(), out);
		         		}
	         		}
	         		else //post
	         		{
	         			System.out.println("Processing POST ");
	         			BufferedWriter fileOut = new BufferedWriter(new FileWriter(docRoot + req.getPath()));
	         			fileOut.write(req.get_data());
	         			fileOut.flush();
	         			fileOut.close();
	                    httpRespond(HttpResponse._201, null, out);
	         		}
	         		//////////////////////////////END CRITICAL SECTION/////////////////////////////////
	         		
		        }
	         	catch(IOException e)
	         	{
	         		System.err.println(e.getMessage());
	         	}
			}
         }
		 else
		 {
			throw new InvalidHttpRequest(); 
		 }
	
		 return true;
    }
    
    private boolean supportedDoctype(String file)
    {
    	return docTypes.contains(file.substring(file.lastIndexOf(".")+1));
    	
    }
    
    private boolean httpRespond(String code, String payload, DataOutputStream out)
    {
    	try
		{
			out.writeBytes(code);
			if(payload != null)
			{
				out.writeBytes(payload);
				
			}
			out.flush();
		} 
    	catch (IOException e)
		{
			
			return false;
		}
    	return true;
    }
}
