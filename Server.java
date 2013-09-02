/*
 * CPS730 - Assignment 1.
 * Single Threaded HTTP Server.
 * Authors:
 * 			Tyler Hackwood 500 147 848.
 * 			Fadi Younis 500 321 978.	   
 * 
 */
package cps730.server;
import java.io.*;
import java.net.*;
import java.lang.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.text.*;


public class Server extends Thread
{
	static String docRoot;		        //Where .config is stored.
	static ArrayList<String> docTypes;  //Type of file(htm,html etc)
	static ArrayList<SocketHandler> poolList; //array list to hold simultaneous connections pool.
	
	//Assignment 2 new variable.
	static int pool;
	static int queue;
	static int default_pool = 5;
	static int default_queue = 5;
	static String myhttpRequest; 		//GET,POST,HEAD.
	static String configPath = "myhttpd.conf";
	
	private static Semaphore sem; //resource access variable
	
    public static void main(String[]args) throws IOException
    {	
        System.out.println("*********** HTTP SERVER ************");
        
        int portnumber = -1; //Set at default 61978 in Run Configurations.
        boolean error = false;
        //Read port number from user's command line.
        for(int i = 0; i < args.length; i++)
        {
        	if(args[i].equals("-p"))
        	{
        		portnumber = Integer.parseInt(args[i+1]);
        	}
        	else if(args[i].equals("-c"))
        	{
        		configPath = args[i+1];
        	}
        }
        
        
        if(portnumber == -1)
        {
        	error = true;
        	System.out.println("Port number between 60000 and 65535 must be specified");
        	
        }
        else if(portnumber > 65535 || portnumber <= 60000)
        {
        	error = true;
        	System.out.println("Port number must be between 60000 and 65535.");
        	
        }
        
        
        //print usage if error
        if(error)
        {
        	System.out.println("Usage:");
        	
            System.out.print("Port: -p  [port] ");
            System.out.println();
            System.out.println("Optional:");
            System.out.println("Configuration File Path: -c [path]");
            System.exit(1);
        }
        	
        
        
        readConfig(configPath); //Reads myhttpd.config
        
        System.out.println("Document Root set as: " + docRoot);
        System.out.print("Supported Document Types:");
        
        //lists the document types.
        for(String s : docTypes)
        {
        	System.out.print(" " + s);
        }
        System.out.println( );
        
        
        pool = pool > 0 ? pool : default_pool;
        queue = queue > 0 ? queue : default_queue;
        System.out.println("Connection pool set to: " + pool);
        System.out.println("Max connnection queue set to: " + queue);
        System.out.println( );
        ServerSocket server = null;

        sem = new Semaphore(pool);
        
        try //set up socket Server
        {
        	server = new ServerSocket(portnumber,pool); //listen on port number.
        	System.out.println("Connetion set on port number " + portnumber);
        	
        }
        catch(IOException e)//catch failure
        {
            System.err.println("Error Listening on port number " + portnumber);
            System.exit(1);
        }
        
        try //set up listening Client
        {
        	//System.out.println("Waiting for connections . . .");
        	while(true) //Single thread connection.
        	{
        		sem.acquire();
        		System.out.println("Waiting for connections . . .");
        		Socket s = server.accept( ); //set up client - Server connection.
		        System.out.println("Client connected at " + s.getInetAddress() );
	        	Thread serverThread = new Thread(new SocketHandler(s, docRoot, docTypes, sem));
	        	//System.out.println("There are " +  sem.availablePermits() + " connections left in the pool");
	        	serverThread.start();
	        	System.out.println("Thread Run");
        	}
        }
        catch(IOException e ) //catch failure.
        {
            System.err.println("Accepting socket connection with port number failed."); 
        } 
        catch (InterruptedException e)
		{
        	//semaphor class could throw this. But should never be interupted.
			e.printStackTrace();	
			System.exit(-1);
		}
        

        System.out.println("Closing server connection.");
        server.close( );  //close socket at server end.
        System.out.println("Closing client connection.");
    }
                           //********** ReadConfiguration() method ***********//
    public static void readConfig(String path)
    {
    	System.out.println("Reading Configuration file.");
    	
    	try
    	{
    			FileInputStream fis = new FileInputStream(path);
    			DataInputStream dis = new DataInputStream(fis);
    			BufferedReader br = new BufferedReader(new InputStreamReader (dis));
    			
    			String line;
    			
    			
    			while( (line = br.readLine()) != null)
    			{
    				if(line.startsWith("HTTP1.0")) //first line of my myhttpd.config, the root
    				{
    					docRoot = line.substring(line.indexOf('[') +1, line.indexOf(']'));	
    				}
    				else if(line.startsWith("HTML"))
					{
						docTypes = new ArrayList<String>(Arrays.asList(line.substring(line.indexOf(" ")).split("\\s")));
					}
    				else if(line.startsWith("POOL"))
    				{
						String p = line.substring(line.indexOf(" "));
						p=p.trim();
					    pool = Integer.parseInt(p);
    					    
    				}
    				else if(line.startsWith("QUEUE"))
    				{
    					String q = line.substring(line.indexOf(" "));
    					q=q.trim();
    				    queue = Integer.parseInt(q);
    					
    				}
    				
    			}
    			
       		
    		    			
    			br.close();  //Close Buffered Reader.
    		    dis.close(); //Close Data InputStream.
    		    fis.close(); //Close FileInputStream.
    		    
    		    if(docRoot.contains("\\"))
    		    {
    		    	docRoot = docRoot.replace("\\", "/");
    		    }
    		}
    		catch (Exception e)
    		{
    		}
    }
   
}
                  