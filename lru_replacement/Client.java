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
      File log = new File("output/client_log_" + id);
      log.delete(); //So we dont append onto an existing file.
      while (true)
      {
         mArray.waitTurn(id);
         if (mArray.getRequestsDone())
         {
            break;
         }
         System.out.println("[CLIENT" + id + "] Running");
         try
         {
            BufferedWriter bw = new BufferedWriter(new FileWriter(log, true));
            Page p = mArray.getPage();
            bw.write(p.toString() + '\n');
            bw.close();
         }
         catch (IOException e)
         {
            e.printStackTrace();
         }
         mArray.signalDone(id);
      }
      System.out.println("[CLIENT" + id + "] Terminated");
   }
}
