package org.example.backend.domain;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Reducer {
    private static int serverPort;
    private static String masterHost;
    private static int masterPort;
    private static int expectedChunks;
    private static Map<Integer, ArrayList<Chunk>> chunkMap = new HashMap<>();

    private  static final  int numberOfChunks = 1;

    public static void init() {
        Properties prop = new Properties();
        String filename = "src/main/java/org/example/backend/config/reducer.config";

        try (FileInputStream f = new FileInputStream(filename)) {
            prop.load(f);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        masterHost = prop.getProperty("masterHost");
        masterPort = Integer.parseInt(prop.getProperty("masterPort"));
        serverPort = Integer.parseInt(prop.getProperty("serverPort"));
        expectedChunks = Integer.parseInt(prop.getProperty("expectedChunks"));
    }

    public static void main(String[] args) {
        init();
        startReducerServer();
    }

    private static void startReducerServer() {
        try (ServerSocket providerSocket = new ServerSocket(serverPort, 100)) {
            while (true) {
                Socket workerSocket = providerSocket.accept();
                System.out.println("Worker connected");

                new Thread(() -> handleWorkerRequest(workerSocket)).start();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    private static void handleWorkerRequest(Socket workerSocket){
        try {
            ObjectInputStream in = new ObjectInputStream(workerSocket.getInputStream());
            while (true) {
                Chunk chunk = (Chunk) in.readObject(); // Read chunks sent by the worker
                // Add the chunk to the map based on its ID
                synchronized (chunkMap) {
                    int chunkId = chunk.getSegmentID();
                    if (!chunkMap.containsKey(chunkId)) {
                        chunkMap.put(chunkId, new ArrayList<>());
                    }
                    chunkMap.get(chunkId).add(chunk);

                    if (chunkMap.get(chunkId).size() == expectedChunks) {
                        ArrayList<Chunk> chunks = chunkMap.get(chunkId);
                        Chunk mergedChunk = mergeChunks(chunks);
                        sentToMaster(mergedChunk);

                        chunkMap.remove(chunkId); // Clear the chunks from the map
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void sentToMaster(Chunk chunk){
        try {
            Socket masterSocket = new Socket(masterHost, masterPort); // Connect to the master
            ObjectOutputStream out = new ObjectOutputStream(masterSocket.getOutputStream());

            out.writeObject(chunk); // Send the merged chunk to the master
            out.flush();

            // Close connections
            out.close();                                    ///////////////////////////////////////////////////////////////////////
            masterSocket.close();                           ///////////////////////////////////////////////////////////////////////
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Chunk mergeChunks(ArrayList<Chunk> chunks) {
        String userID = "";
        int id = 0;
        int type = 0;
        ArrayList<Chunk> workerChunk = new ArrayList<>();
        ArrayList<Chunk> finalList = new ArrayList<>();
        for(int i=0; i<expectedChunks; i++) {
            workerChunk = (ArrayList<Chunk>) chunks.get(i).getData();
            for(int j=0; j<workerChunk.size(); j++){
                finalList.add(workerChunk.get(j));
                userID = workerChunk.get(j).getUserID();
                id = workerChunk.get(j).getSegmentID();
                type = workerChunk.get(j).getSegmentID();
            }
        }
        Chunk finalChunk = new Chunk(userID, type, finalList);
        finalChunk.setSegmentID(id);
        return finalChunk;
    }
}

