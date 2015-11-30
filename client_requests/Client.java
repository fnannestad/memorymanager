/*
 * Author: Finn Nannestad
 * Number: 1744 2446
 */

import java.util.concurrent.*;
import java.util.*;
import java.io.*; 

public class Client extends Thread 
{

   private int id;
   private MonitorArray mArray;

   public Client(int id, MonitorArray mArray) 
   {
      super("thread_" + id); //Set name for clearer debugging
      this.id = id;
      this.mArray = mArray;
   }

   public void run() 
   {
      try
      {

         File log = new File("output/client_log_" + id);
         log.delete(); //So we dont append onto an existing file.
         File requests = new File("sample_files/client_requests_" + id + ".dat");
         if (requests.exists())
         {
            Scanner in = new Scanner(requests);
            BufferedWriter bw = new BufferedWriter(new FileWriter(log, true));

            while (in.hasNext())
            {
               int num = Integer.parseInt(in.next());
               String requestType = in.next();
               int pageId = Integer.parseInt(in.next());
               System.out.println("[CLIENT" + id + "] Sending " + requestType + " request with number " + num);

               if (requestType.equals("write"))
               {
                  String contents = in.nextLine().trim();
                  mArray.sendRequest(new Request(id, num, requestType, pageId, contents));
               }
               else
               {
                  //Constructs a read request with an empty string for page
                  //contents
                  mArray.sendRequest(new Request(id, num, requestType, pageId));
               }

               Page p = mArray.waitTurn(id);
               System.out.println("[CLIENT" + id + "] Notified that request was processed");
               if (requestType.equals("read"))
               {
                  System.out.println("[CLIENT" + id + "] Writing page " + p + " to log");
                  bw.write(p.toString() + '\n');
                  bw.flush();
               }
               mArray.signalDone(id);
            }
            bw.close();
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      System.out.println("[CLIENT" + id + "] Terminated");
      mArray.notifyClientTerminated();
   }
}
