package com.company;

// A Java program for a Server
import java.net.*;
import java.io.*;
import java.util.ArrayList;

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
            int num = 0;

            ArrayList<Integer> arrayList = new ArrayList<>();
            int goodPutTimer = 10;
            int goodput = 0;
            int currentPacketNumber = 0;
            int recivedPacketNumber = 0;
            int LastRecivedPacketNumber = 0;
            int missingPacketNumber;
            int missingPacketCount = 0;


            // reads message from client until "Exit" is sent
            while (!packet.equals("Exit")) {
                try {
//                  Read in client messages
                    packet = ClientIn.readUTF();
                    System.out.println("<Client: Packet->" + packet);

                    if(packet.equals("Exit"))
                        break;

//                  Server replying with ACK of packet number
                    Serverline = "ACK->";

//                    Check if its sending packet numbers yet.

                    if (packet.matches("-?\\d+(\\.\\d+)?")) {
                        recivedPacketNumber = Integer.parseInt(packet);
//                        System.out.println("PACKET NUMBER " + currentPacketNumber + " recieved" + recivedPacketNumber);
                    }

                    if (recivedPacketNumber >= 1 ) {
                        if (recivedPacketNumber == 1)
                            currentPacketNumber = recivedPacketNumber;
//
//                    if the new packet number matches the internal counter then send an ACK
                        if (recivedPacketNumber == currentPacketNumber) {

//                            SeverOut.writeUTF(Serverline + packet);
//                            System.out.println("PACKET NUMBER " + currentPacketNumber +" recieved" + recivedPacketNumber);
                        }
//                    add missing packet number to arraylist tracker
                        else {
//                      Get recivedpacketNumber and compar to PrevRecivedpacket
                            missingPacketNumber = recivedPacketNumber - 1;
                            System.out.println("MISSING PACKET NUMBER " + missingPacketNumber);
                            arrayList.add(missingPacketNumber);
                            missingPacketCount++;

                            currentPacketNumber = recivedPacketNumber;
                        }

                        currentPacketNumber++;


//                  checking the goodput ever 1000 packets
                        if (recivedPacketNumber % goodPutTimer == 0) {
                            goodput = recivedPacketNumber / (recivedPacketNumber + missingPacketCount);
                            missingPacketCount = 0;
                            System.out.println("-------------------Current Good-put: " + goodput);
                        }

                    }
                    SeverOut.flush();

                } catch (IOException i) {
                    System.out.println(i);
                }

            }
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
