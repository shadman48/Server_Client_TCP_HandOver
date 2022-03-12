# Server_Client_TCP_HandOver
Implement  a  very  basic  TCP  client  and  server  implemented  on  two  separate  machines.  The  TCP 
client/server  will  communicate  over  the  network  and  exchange  data  to  maintain  TCP  Sliding  window 
protocol. The server will start in passive mode listening for a transmission from the client. The client will 
then start and contact the server (on a given IP address and port number). The client will pass the server 
an initial string (e.g.: “network”). On receiving a string from a client, the server  should  respond  with 
connection setup “success” message. 
 
Now the client will start sending the TCP segments in the sliding window manner. For simplicity, instead 
of sending actual packets, the client will send only the TCP sequence numbers to the server. On reception 
of the sequence numbers, the server will respond to the client with the corresponding “ACK numbers”. 
On receiving an ACK, the client will adjust the sliding window. 
 
The server, on the other hand, will continue receiving the sequence numbers and keep track of any missing 
sequence number. If there is no missing sequence number, the server will consider all the packets till that 
sequence number are received.  
 
Now, while sending the packets (represented by sequence numbers), the client probabilistically drop 1% 
of  the  packets.  Server  will  keep  track  of  the  missing  packets.  After  a  specific  time  (e.g.  100  sequence 
numbers), the client will retransmit the dropped packets with the same probability.  
 
 
Note:  
The program should be executed for 10,000,000 packets and maximum sequence number should 
be limited to 216.  
 
Output:  The  server  will  keep  a  count  of  received  packets  and  missing  packets.  Calculate  good-put 
(received packets/sent packets) periodically  after every  1000 packets received at  the server and report 
the average good-put. 
