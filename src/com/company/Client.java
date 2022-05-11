package com.company;

// A Java program for a Client

import java.io.*;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Client
{
    // initialize socket and input output streams
    private Socket socket		 = null;
    private DataInputStream input = null;
    private DataInputStream serverIn = null;
    private DataOutputStream out	 = null;

    //this generates the new window based on whether a packet was transmitted or lost.
    public static int genWindow(int winSize, boolean permPacketLost, boolean currpacketLost, int numOfPackets) {

        if (currpacketLost) winSize = (int) (winSize / 2);
        if (permPacketLost) {
            winSize += 1;
        }
        else if (!permPacketLost) {
            winSize *= 2;
        }
        if (winSize > numOfPackets)
            winSize = numOfPackets;

        return winSize;
    }
    // constructor to put ip address and port
    public Client(String address, int port) throws FileNotFoundException {
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

        int numOfPackets;
        int winSize;
        int newWinSize = 0;
        int startByte = 1;
        int endByte = 0;
        int noOfAck = 0;
        int noOfSent = 0;
        boolean permPacketLost = false;
        boolean currPacketLost = false;
        int retransThreshold = 25;


//      Number of packets
        numOfPackets = 100;

//        Window size
        winSize = 1;

        int overFlow = (int) (numOfPackets*1.2);
        ArrayList<Integer> droppedPackets = new ArrayList<>();

//        Array for keeping track of information for graphs
//        TODO: HERE
        ArrayList<String> windowSizeList = new ArrayList<>();
        int[][] numOfRetransmissionsList = new int[overFlow][2];

        final long startTime = System.currentTimeMillis();


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

                PrintWriter writer = new PrintWriter("windowSizeList.csv");


                //for loop iterates through the window from start to end
                for (int i = startByte; i <= numOfPackets; i++) {
                    System.out.println("inside for loop - i = " + i);
                    if (endByte > numOfPackets) {
                        endByte = numOfPackets; //noofframe is last frame in 10 mil or 2^16
                    }


                    double dropChance = Math.random();

                    //if dropChance > 1% then we send the packet, if < 1% we dropChance the packet
//                    This if statment is where the packets get sent
                    if (dropChance > 0.1){
//                    if (dropChance > 0.01){
                        //when we have finished sending number of packets equal to retransthreshold, we look to retransmit from the arraylist
                         if(endByte >= retransThreshold || i == numOfPackets){
                            System.out.println("-----------------Inside Retransmission-----------------");
                            //out.writeUTF("RESEND:" + String.valueOf(i));

//                          Check if there are missing packets, then resend them.
                            if (!droppedPackets.isEmpty()){
                                System.out.println("```````````Inside Dropped packets List```````````");
//                                for(int j = 0; j < droppedPackets.size(); j++){
                                while(!droppedPackets.isEmpty()){
                                    double retransmitDropChance = Math.random();


                                    //even for the dropped packets in the arraylist, if dropChance > 1% we send the packet and remove it from arraylist else we drop it
                                    if (retransmitDropChance > 0.1){
                                        System.out.println("-----------------Retransmitting packet------- [" + droppedPackets.get(0)+"]");
                                        out.writeUTF(String.valueOf(droppedPackets.get(0)));

                                        currPacketLost = false;
                                        newWinSize = genWindow(winSize,permPacketLost,currPacketLost, numOfPackets);
                                        endByte += newWinSize - winSize;
                                        winSize = newWinSize;
                                        System.out.println("Sending frame " + i
                                                + " - RESENDING frame " + String.valueOf(droppedPackets.get(0))
                                                + " - newWindow size " + newWinSize
                                                + " - endByte " + endByte
                                                + " - winSize " + winSize
                                                + " - retransThreshold " + retransThreshold);
                                        droppedPackets.remove(0);


                                    }
//                                    This else is for if the retransmission failed again
                                    else{
                                        System.out.println("-----------------Retransmitting packet FAILED !! ------- " + droppedPackets.get(0));
                                        currPacketLost = true;
                                        numOfPackets++;
                                        numOfRetransmissionsList[droppedPackets.get(0)][0]++;
                                        newWinSize = genWindow(winSize,permPacketLost,currPacketLost, numOfPackets);
                                        endByte += newWinSize - winSize;
                                        winSize = newWinSize;
                                        System.out.println("Sending frame " + i
                                                + " - newWindow size " + newWinSize
                                                + " - endByte " + endByte
                                                + " - winSize " + winSize
                                                + " - retransThreshold " + retransThreshold);
                                    }
                                }//End of for loop
                            }// End of if (!droppedPackets.isEmpty())



//                             This is where the packets are sent if they are not dropped.
                             //until retransthreshold is reached, we send packets using this else block.
                        else {

                                 out.writeUTF(String.valueOf(i));
                                 currPacketLost = false;
                                 if (winSize < numOfPackets){
                                     newWinSize = genWindow(winSize,permPacketLost,currPacketLost, numOfPackets);
                                     endByte += newWinSize - winSize;
                                     winSize = newWinSize;
                                     System.out.println("Sending frame " + i
                                             + " - newWindow size " + newWinSize
                                             + " - endByte " + endByte
                                             + " - winSize " + winSize
                                             + " - retransThreshold " + retransThreshold);
                                 }
                             }

                        retransThreshold += 25;//cuz endbyte goes till 10 mil
                        }//End of if(endByte >= retransThreshold)


//                        This is where the packets are sent if they are not dropped.
                        //until retransthreshold is reached, we send packets using this else block.
                        else {

                            out.writeUTF(String.valueOf(i));
                            currPacketLost = false;
                            if (winSize < numOfPackets){
                            newWinSize = genWindow(winSize,permPacketLost,currPacketLost, numOfPackets);
                            endByte += newWinSize - winSize;
                            winSize = newWinSize;

                        }System.out.println("Sending frame " + i
                                     + " - newWindow size " + newWinSize
                                     + " - endByte " + endByte
                                     + " - winSize " + winSize
                                     + " - retransThreshold " + retransThreshold);
                         }
                    }//End of if (dropChance > 0.1)

                    //in this else block, packets are dropped and added to arraylist.
                    else{
//                        System.out.println("Dropped PACKET " + i);
                        System.out.println("---------------------------------------------PACKET (" + i +") WAS DROPPED");
                        droppedPackets.add(i);
                        numOfPackets++;

                        numOfRetransmissionsList[i][0] = 1;
                        currPacketLost = true;
                        permPacketLost = true;
                        newWinSize = genWindow(winSize,permPacketLost,currPacketLost, numOfPackets);
                        endByte += newWinSize - winSize;
                        winSize = newWinSize;
                        System.out.println("DROPPED Sending frame " + i
                                + " - newWindow size " + newWinSize
                                + " - endByte " + endByte
                                + " - winSize " + winSize
                                + " - retransThreshold " + retransThreshold);
                    }


//                    writer.println(String.join(",", windowSizeList));
                }//End of main for loop

                out.writeUTF("Exit");
                final long endTime = System.currentTimeMillis();
                System.out.println("Total execution time: " + (endTime - startTime) + "ms");
                windowSizeList.add(String.valueOf(endTime - startTime));
;
                for (int x = 0; x < numOfRetransmissionsList.length; x++) {
                    for (int xx = 0; xx < numOfRetransmissionsList[x].length; xx++) {
                        if(numOfRetransmissionsList[x][xx] != 0)
                            System.out.println("Packet [" + x + "] has been retransmitted [" + numOfRetransmissionsList[x][xx] + "] times.");
                    }
                }

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

    public static void main(String args[]) throws FileNotFoundException {
        Client client = new Client("127.0.0.1", 5000);
//        Client client = new Client("192.168.1.125", 5000);
    }
}