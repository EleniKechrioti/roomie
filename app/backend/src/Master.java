//------------------------------------------------------------------Imports------------------------------------------------------------------
import java.net.*;
import java.io.*;
import java.util.*;

//----------------------------------------------------------------Master-Class---------------------------------------------------------------
public class Master extends Thread{
    public static int num_of_workers;
    private static String host; //
    private static ObjectInputStream in; // Socket for Master to listen the requests from the Master
    private static ObjectOutputStream out; // Socket for each request to send the result to the Reducer
    private static int listenPort; // Port to listen the request
    private static int requestReducerPort; // Port to send the result
    private Chunk chunk;
    public Socket requestSocket;
//-------------------------------------------------------------Master-Constructor------------------------------------------------------------
    public Master(Socket socket, Chunk chunk ){
        this.chunk = chunk;
        this.requestSocket = socket;
    }
//---------------------------------------------------------------Initialization--------------------------------------------------------------
    public static void init(){
        Properties prop = new Properties();
        String filename = "app\\backend\\config\\master.config";

        try (FileInputStream f = new FileInputStream(filename)){
            prop.load(f);
        }catch (IOException exception ) {
            System.err.println("I/O Error\n" + "The system cannot find the path specified");
        }

        Master.host = prop.getProperty("host");
        Master.listenPort = Integer.parseInt(prop.getProperty("listenPort"));
        Master.requestReducerPort = Integer.parseInt(prop.getProperty("requestReducerPort"));
        Master.num_of_workers = Integer.parseInt(prop.getProperty("numberOfWorkers"));
        System.out.println(Integer.parseInt(prop.getProperty("listenPort")));
    }


    @Override
	public void run(){
        try{
            Master.out.writeObject(chunk);
        }catch(IOException e){
            System.err.println("I/O Error\n" + "Error");
        }
        System.out.println("hey i'm working!!");
    }

    public static void main(String[] args){
        Master.init();
        ServerSocket connectionSocket;
        Socket socket ;
        System.out.println(Master.host);

        try{
            connectionSocket = new ServerSocket(Master.listenPort);
            socket = connectionSocket.accept();
            System.out.println("Client accepteed");
            Master.out = new ObjectOutputStream(socket.getOutputStream());
            Master.out.writeUTF("Hello from Masrer!");
            out.flush();
            socket.close();
            out.close();
        }catch(IOException e){
            System.err.println("I/O Error\n" + "Error occured while trying to create the socket for connection enstablishment");
        }


        try{
            Chunk chunk = new Chunk();
            Socket chunkSocket = new Socket(Master.host, Master.requestReducerPort);
            Master worker = new Master(chunkSocket, chunk);
            worker.start();
        }catch(IOException e){
            System.err.println("I/O Error\n" + "Error occured while trying to create the socket for the request");
        }
    }  
}