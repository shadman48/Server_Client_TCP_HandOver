package com.company;

// A Java program for a Server
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Server
{
    //initialize socket and input stream
    private Socket		 socket = null;
    private ServerSocket server = null;
    private DataInputStream ClientIn = null;
    private DataOutputStream SeverOut = null;
    // constructor with port
    public Server(int port)
    {
        // starts server and waits for a connection
        try
        {
            server = new ServerSocket(port);
            System.out.println(">Server started");

            System.out.println(">Waiting for a client ...");

            socket = server.accept();
            System.out.println(">Client accepted" );

            // sends output to the socket
            SeverOut = new DataOutputStream(socket.getOutputStream());
            SeverOut.writeUTF("Welcome");


            // takes input from the client socket
            ClientIn = new DataInputStream(
                    new BufferedInputStream(socket.getInputStream()));

            String Serverline = "";
            String packet = "";
//            int num = 0;

            ArrayList<String> droppedPacketsList = new ArrayList<>();
            ArrayList<String> receivedTimeList = new ArrayList<>();
            ArrayList<String> droppedTimeList = new ArrayList<>();



            int goodPutTimer = 5;
            double goodput = 0;
            int currentPacketNumber = 0;
            int recivedPacketNumber = 0;
//            int LastRecivedPacketNumber = 0;
            int missingPacketNumber;
            int missingPacketCount = 0;
            final long startTime = System.currentTimeMillis();
            FileWriter writer = new FileWriter("server.csv");
//            PrintStream out = new PrintStream(new FileOutputStream("output.txt"));
            // reads message from client until "Exit" is sent
            while (true) {
                try {
//                  Read in client messages
                    packet = ClientIn.readUTF();
                    System.out.println("<Client: Packet->" + packet);
                    goodput = 0;

//                  Server replying with ACK of packet number
                    Serverline = "ACK->";
//                    SeverOut.writeUTF(Serverline);

//                    Check if its sending packet numbers yet.
                    if(packet.equals("Exit"))
                        break;
                    if (packet.matches("-?\\d+(\\.\\d+)?")) {
                        recivedPacketNumber = Integer.parseInt(packet);
//                        System.out.println("PACKET NUMBER " + currentPacketNumber + " received" + recivedPacketNumber);
                    }

                    if (recivedPacketNumber >= 1 ) {
                        if (recivedPacketNumber == 1)
                            currentPacketNumber = recivedPacketNumber;
//
//                    if the new packet number matches the internal counter then send an ACK
                        if (recivedPacketNumber == currentPacketNumber) {

                            final long endTime = System.currentTimeMillis();
                            System.out.println("Total execution time: " + (endTime - startTime) + "ms");
                            receivedTimeList.add(String.valueOf(endTime - startTime));
//                            SeverOut.writeUTF("hi");
//                            System.out.println("PACKET NUMBER " + currentPacketNumber +" received" + recivedPacketNumber);
                        }
//                    add missing packet number to arraylist tracker
                        else {
                            final long endTime = System.currentTimeMillis();
                            System.out.println("Total execution time: " + (endTime - startTime) + "ms");
                            droppedTimeList.add(String.valueOf(endTime - startTime));
                            missingPacketNumber = recivedPacketNumber - 1;
                            System.out.println("MISSING PACKET NUMBER " + missingPacketNumber);

                            droppedPacketsList.add(String.valueOf(missingPacketNumber));
                            missingPacketCount++;

                            currentPacketNumber = recivedPacketNumber;
                        }


//                  checking the goodput ever 1000 packets
                        if (recivedPacketNumber % goodPutTimer == 0) {
                            goodput = (double) recivedPacketNumber / ((double) recivedPacketNumber + missingPacketCount);
                            System.out.printf("-------------------Current Good-put: %.2f %n", goodput);
                            missingPacketCount = 0;

                        }


                        currentPacketNumber++;
                    }
                    SeverOut.flush();

                } catch (IOException i) {
                    System.out.println(i);
                }

            }
            final long endTime = System.currentTimeMillis();

            System.out.println("Total execution time: " + (endTime - startTime) + "ms");

//            Saving to file
//            writer.write("Line 1 = droppedPacketsList, Line 2 = droppedTimeList in milliseconds, Line 3 = receivedTimeList in milliseconds" + "\n");

            String dropped = droppedPacketsList.stream().collect(Collectors.joining(","));
            System.out.println(dropped);
            writer.write(dropped + "\n");

            String dropTime = droppedTimeList.stream().collect(Collectors.joining(","));
            System.out.println(dropTime);
            writer.write(dropTime + "\n");

            String received = receivedTimeList.stream().collect(Collectors.joining(","));
            System.out.println(received);
            writer.write(received);

            writer.close();


            System.out.println("Closing connection");

            // close connection
            socket.close();
            ClientIn.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[])
    {
        Server server = new Server(5000);
    }
}
