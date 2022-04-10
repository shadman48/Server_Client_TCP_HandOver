package com.company;

// A Java program for a Server
import java.net.*;
import java.io.*;

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
            String line = "";
            int num = 0;


            // reads message from client until "Exit" is sent
            while (!line.equals("Exit"))
            {
                try
                {
//                  Read in client messages
//                    line = ClientIn.readUTF();
//                    System.out.println("<Client: " + line);

//                  Read in client messages
                    line = ClientIn.readUTF();
                    System.out.println("<Client: Packet->" + line);


//                  Server Reply
                    Serverline = "ACK->";
                    SeverOut.writeUTF(Serverline + num);
                    num++;
                    SeverOut.flush();
                }
                catch(IOException i)
                {
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
