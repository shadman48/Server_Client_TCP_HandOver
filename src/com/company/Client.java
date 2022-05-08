package com.company;

// A Java program for a Client

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

public class Client
{
    // initialize socket and input output streams
    private Socket socket		 = null;
    private DataInputStream input = null;
    private DataInputStream serverIn = null;
    private DataOutputStream out	 = null;

    //this generates the new window based on whether a packet was transmitted or lost.
    public static int genWindow(int winSize, boolean permPacketLost, boolean currpacketLost) {

        if (currpacketLost) winSize = (int) (winSize / 2);
        else if (permPacketLost) {
            winSize += 1;
        }
        if (!permPacketLost) {
            winSize *= 2;
        }
        return winSize;
    }
    // constructor to put ip address and port
    public Client(String address, int port)
    {
        // establish a connection
        try
        {
            socket = new Socket(address, port);
            System.out.println("Connected to server.");


            // takes input from terminal
            input = new DataInputStream(System.in);

//            Takes input from server socket
            serverIn = new DataInputStream(socket.getInputStream());
//            System.out.println("Server: " + serverIn.readUTF());

            // sends output to the socket
            out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF("Connected");


        }
        catch(UnknownHostException u)
        {
            System.out.println(u);
        }
        catch(IOException i)
        {
            System.out.println(i);
        }

        // string to read message from input
        String line = "";
        int packets = 0;

        int noOfPackets;
        int winSize;
        int newWinSize;
        int startByte = 1;
        int endByte = 0;
        int noOfAck = 0;
        int noOfSent = 0;
        boolean permPacketLost = false;
        boolean currPacketLost = false;
        int retransThreshold = 1000;

//        Scanner scn = new Scanner(System.in);

//        System.out.println("Enter the total no of frames: ");
//        noOfPackets = scn.nextInt();
        noOfPackets = 20;

//        System.out.println("Enter the window size: ");
//        winSize = scn.nextInt();
        winSize = 1;

        int duePackets = noOfPackets;
        ArrayList<Integer> missedPackets = new ArrayList<Integer>();

        // keep reading until "Exit" is input
        while (!line.equals("Exit"))
        {
            try
            {
//                Checks if server has any outputstream content and then prints it
                if(serverIn.available() >= 0) {
                    System.out.println("Server: " + serverIn.readUTF());
                }

                endByte += winSize; //end of window

                System.out.println("start byte " + startByte + "/ end byte " + endByte);

                //for loop iterates through the window from start to end
                for (int i = startByte; i <= 10; i++) {
                    System.out.println("inside for loop - i = " + i);
                    if (endByte > noOfPackets) {
                        endByte = 20; //noofframe is last frame in 10 mil or 2^16
                    }


                    double dropChance = Math.random();

                    //if dropChance > 1% then we send the packet, if < 1% we dropChance the packet
                    if (dropChance > 0.01){
                        //when we have finished sending number of packets equal to retransthreshold, we look to retransmit from the arraylist
                        if(endByte >= retransThreshold){
                            System.out.println("inside end byte");
                            //out.writeUTF("RESEND:" + String.valueOf(i));

//                          Check if there are missing packets, then resend them.
                            if (!missedPackets.isEmpty()){
                                System.out.println("inside missed packets");
                                for(int j = 0; j < missedPackets.size(); j++){
                                    double retransDrop = Math.random();

                                    //even for the dropped packets in the arraylist, if dropChance > 1% we send the packet and remove it from arraylist else we dropChance it

                                    if (retransDrop > 0.01){
                                        System.out.println("retrans " + missedPackets.get(j));
                                        out.writeUTF("MISSED:" + String.valueOf(missedPackets.get(j)));
                                        //if we get ack back. As in do we wait till we get ack back before removing packet from arraylist?
                                        missedPackets.remove(j);
                                        duePackets--;
                                        currPacketLost = false;
                                        newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
                                        endByte += newWinSize - winSize;
                                        winSize = newWinSize;
                                        //obvi we dont remove if no ack comes back
                                    }
                                    else{
                                        currPacketLost = true;
                                        newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
                                        endByte += newWinSize - winSize;
                                        winSize = newWinSize;
                                    }
                                }//End of for loop
                            }


                            retransThreshold += 1000;//cuz endbyte goes till 10 mil
                        }


//                        This is where the packets are sent if they are not dropped.
                        //until retransthreshold is reached, we send packets using this else block.
                        else {

                            out.writeUTF(String.valueOf(i));
                            currPacketLost = false;
                            newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
                            endByte += newWinSize - winSize;
                            winSize = newWinSize;
                            System.out.println("Sending frame " + i
                                    + " - newWindow size " + newWinSize
                                    + " - endByte " + endByte
                                    + " - winSize " + winSize);
                        }
                    }

                    //in this else block, packets are dropped and added to arraylist.
                    else{
                        System.out.println("MISSED PACKET " + i);
                        System.out.println("---------------------------------------------PACKET WAS DROPPED");
                        missedPackets.add(i);
                        currPacketLost = true;
                        permPacketLost = true;
                        newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
                        endByte += newWinSize - winSize;
                        winSize = newWinSize;
                    }

                }//End of main for loop

                out.writeUTF("Exit");
                break;

                //after sending 10 million packets. we gotta resend dropped packets present in the arraylist one last time
                //as we would have reached the retransThreshold after sending the 10 million packets
//                if (!missedPackets.isEmpty()){
//                    for(int j = 0; j < missedPackets.size(); j++){
//                        double retransDrop = Math.random();
//
//                        //even for the dropped packets in the arraylist, if drop > 1% we send the packet and remove it from arraylist else we drop it
//                        if (retransDrop > 0.01){
//                            out.writeUTF("retansmitting MISSINGPACKETS:" + String.valueOf(missedPackets.get(j)));
//
//                            //if we get ack back. As in do we wait till we get ack back before removing packet from arraylist?
//                            missedPackets.remove(j);
//                            duePackets--;
//                            currPacketLost = false;
//                            newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
//                            endByte += newWinSize - winSize;
//                            winSize = newWinSize;
//                            //obvi we dont remove if no ack comes back
//                        }
//                        else{
//                            currPacketLost = true;
//                            newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
//                            endByte += newWinSize - winSize;
//                            winSize = newWinSize;
//                        }
//                    }
//                }

            }//end of try
            catch(IOException i)
            {
                System.out.println(i);
            }
        }//end of while loop

        // close the connection
        try
        {
            input.close();
            out.close();
            socket.close();
        }
        catch(IOException i)
        {
            System.out.println(i);
        }
    }

    public static void main(String args[])
    {
        Client client = new Client("127.0.0.1", 5000);
//        Client client = new Client("192.168.1.125", 5000);
    }
}





//package com.company;
//
//// A Java program for a Client
//import java.net.*;
//import java.io.*;
//
//public class Client
//{
//    // initialize socket and input output streams
//    private Socket socket		 = null;
//    private DataInputStream input = null;
//    private DataInputStream serverIn = null;
//    private DataOutputStream out	 = null;
//
//    // constructor to put ip address and port
//    public Client(String address, int port)
//    {
//        // establish a connection
//        try
//        {
//            socket = new Socket(address, port);
//            System.out.println("Connected to server.");
//
//
//            // takes input from terminal
//            input = new DataInputStream(System.in);
//
////            Takes input from server socket
//            serverIn = new DataInputStream(socket.getInputStream());
////            System.out.println("Server: " + serverIn.readUTF());
//
//            // sends output to the socket
//            out = new DataOutputStream(socket.getOutputStream());
//            out.writeUTF("Connected");
//
//
//        }
//        catch(UnknownHostException u)
//        {
//            System.out.println(u);
//        }
//        catch(IOException i)
//        {
//            System.out.println(i);
//        }
//
//        // string to read message from input
//        String line = "";
//        int packets = 0;
//
//        // keep reading until "Exit" is input
//        while (!line.equals("Exit"))
//        {
//            try
//            {
////                Checks if server has any outputstream content and then prints it
//                if(serverIn.available() >= 0) {
//                    System.out.println("Server: " + serverIn.readUTF());
//                }
//
////                sends 5 packets to server
//                if(packets < 5){
//                    out.writeUTF(String.valueOf(packets));
//                    packets++;
//                }else {
//                    line = "Exit";
//                    out.writeUTF(line);
//                }
//
//
//            }
//            catch(IOException i)
//            {
//                System.out.println(i);
//            }
//
//        }
//
//        // close the connection
//        try
//        {
//            input.close();
//            out.close();
//            socket.close();
//        }
//        catch(IOException i)
//        {
//            System.out.println(i);
//        }
//    }
//
//    public static void main(String args[])
//    {
//        Client client = new Client("127.0.0.1", 5000);
////        Client client = new Client("192.168.1.125", 5000);
//    }
//}