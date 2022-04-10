package com.company;

// A Java program for a Client
import java.net.*;
import java.io.*;

public class Client
{
    // initialize socket and input output streams
    private Socket socket		 = null;
    private DataInputStream input = null;
    private DataInputStream serverIn = null;
    private DataOutputStream out	 = null;

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

        // keep reading until "Exit" is input
        while (!line.equals("Exit"))
        {
            try
            {
                if(serverIn.available() >= 0)
                    System.out.println("Server: " + serverIn.readUTF());
                line = input.readLine();
                out.writeUTF(line + packets);

//                out.writeInt(packets);
                if(packets < 5)
                    packets++;


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
    }
}
