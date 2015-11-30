/*
 * Author: Finn Nannestad
 * Number: 1744 2446
 */

import java.util.concurrent.*;
import java.util.*;   
import java.io.*;

public class Server 
{
   private static ArrayList<Page> memory;
   private static MonitorArray monitors;
   private static int noClients = -1;

   public static void main(String[] args) 
   {
      if (args.length != 1)
      {
         System.out.println("usage: java Server num_clients");
         System.out.println("  num_clients: number of client threads to be created");
      }
      else
      {
         /***************************
          * INITIALIZE MEMORY BUFFER
          * *************************/
         Scanner in = null;
         initMemory(in);

         /***************************
          * CREATE CLIENT THREADS
          * *************************/
         noClients = Integer.parseInt(args[0]);
         Client[] threads = new Client[noClients];
         monitors = new MonitorArray(noClients);
         for (int i = 0; i < noClients; i++) 
         {
            threads[i] = new Client(i, monitors);
            threads[i].start();
         }

         /***************************
          * READ AND PROCESS REQUESTS
          * *************************/
         processRequests(in);
      }
   }

   private static void initMemory(Scanner in)
   {
      memory = new ArrayList<Page>();
      try
      {
         in = new Scanner(new FileReader("sample_files/init_buffer_pages.dat"));
         while (in.hasNext())
         {
            int pageId = Integer.parseInt(in.next());
            String pageContents = in.next();
            memory.add(new Page(pageId, pageContents));
         }
         in.close();
      }
      catch(FileNotFoundException e)
      {
         e.printStackTrace();
      }

      System.out.println("\n[SERVER] Initial memory contents:");
      for (Page p : memory)
      {
         System.out.println(p);
      }
      System.out.println();
   }

   private static void processRequests(Scanner in)
   {
      while (true)
      {
         Request r = monitors.serverWaitTurn();
         //Server thread will end if it is signalled and there are no requests
         //left, ie, r is null
         if (r == null)
            break;
         System.out.println("[SERVER] Recieved request: " + r);
         Page p = searchMemory(r.getPage().getId());
         if (r.getType().equals("write"))
         {
            System.out.println("[SERVER] Request is write");
            p.setContents(r.getPage().getContents());
         }
         else
         {
            System.out.println("[SERVER] Request is read (" + p + ") for client " + r.getClientId());
         }
         monitors.serverSignalDone(p.clone());
      }
   }

   private static Page searchMemory(int pageId)
   {
      Page page = null;
      for (Page p : memory)
      {
         if (p.getId() == pageId)
         {
            page = p;
            break;
         }
      }
      return page;
   }
}
