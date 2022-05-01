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

        int noOfFrame;
        int winSize;
        int newWinSize;
        int startByte = 1;
        int endByte = 0;
        int noOfAck = 0;
        int noOfSent = 0;
        boolean permPacketLost = false;
        boolean currPacketLost = false;
        int retransThreshold = 1000;

        Scanner scn = new Scanner(System.in);

        System.out.println("Enter the total no of frames: ");
//        noOfFrame = scn.nextInt();
        noOfFrame = 5;

        System.out.println("Enter the window size: ");
//        winSize = scn.nextInt();
        winSize = 3;

        int dueFrame = noOfFrame;
        ArrayList<Integer> missedFrames = new ArrayList<Integer>();

        // keep reading until "Exit" is input
        while (!line.equals("Exit"))
        {
            try
            {
//                Checks if server has any outputstream content and then prints it
                if(serverIn.available() >= 0) {
                    System.out.println("Server: " + serverIn.readUTF());
                }


                //send packets
                while (dueFrame >= 0) { //what do we come up to end trying to transmit? this dueframes will reach 0
                    //but there will be frames in arraylist. when to stop retransmitting from arraylist?


//            if (endByte >= 65536) {
//                retrans = true;
//                //start byte = start of arraylist and end is end of list
//            }
                    endByte += winSize; //end of window

                    if (endByte > noOfFrame) {
                        endByte = noOfFrame; //noofframe is last frame in 10 mil or 2^16
                    }

                    // for retrans gotta use for loop of arraylist and do.
                    for (int i = startByte; i <= endByte; i++) {
                        double drop = Math.random();
                        if (drop > 0.01){

                            if(endByte >= retransThreshold){
                                out.writeUTF("DROPPED:" + String.valueOf(i));

                                if (!missedFrames.isEmpty()){
                                    for(int j = 0; j < missedFrames.size(); j++){
                                        double retransDrop = Math.random();
                                        if (retransDrop > 0.01){
                                            //System.out.println("Sending frame " + missedFrames.get(j));
                                            out.writeUTF("MISSED:" + String.valueOf(missedFrames.get(j)));
                                            //if we get ack back
                                            missedFrames.remove(j);
                                            dueFrame--;
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
                                    }
                                }


                                retransThreshold += 1000;//cuz endbyte goes till 10 mil
                            }
                            else {
                                //System.out.println("Sending frame " + i);
                                out.writeUTF(String.valueOf(i));
                                currPacketLost = false;
                                dueFrame--;
                                newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
                                endByte += newWinSize - winSize;
                                winSize = newWinSize;
                                //not waiting for ack here?
                            }
                        }
                        else{
                            missedFrames.add(i);
                            currPacketLost = true;
                            permPacketLost = true;
                            newWinSize = genWindow(winSize,permPacketLost,currPacketLost);
                            endByte += newWinSize - winSize;
                            winSize = newWinSize;
                        }

                    }
                    if (startByte > noOfFrame) {
                        startByte = noOfFrame;
                    }
                    startByte = endByte+1;
                }
            }
            catch(IOException i)
            {
                System.out.println(i);
            }
        }

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
