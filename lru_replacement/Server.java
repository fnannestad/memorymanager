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
   private static RandomAccessFile store;
   private static HashMap<Integer, Integer> storeIndex;
   private static BufferedWriter serverLogger;
   private static MonitorArray monitors;
   private static int noClients = -1;
   private static int bufferSize = -1;

   public static void main(String[] args) 
   {
      if (args.length != 2)
      {
         System.out.println("usage: java Server num_clients buffer_size");
         System.out.println("  num_clients: number of client threads to be created");
         System.out.println("  buffer_size: number of pages stored in memory buffer");
      }
      else
      {
         /***************************
          * INITIALIZE MEMORY BUFFER
          * *************************/
         bufferSize = Integer.parseInt(args[1]);
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
      storeIndex = new HashMap<Integer, Integer>();
      try
      {
         store = new RandomAccessFile("store_file.dat", "rw");
         in = new Scanner(new FileReader("sample_files/init_buffer_pages.dat"));
         int lineNo = 0;
         int byteNo = 0;
         while (in.hasNext())
         {
            int pageId = Integer.parseInt(in.next());
            String pageContents = in.next();
            Page p = new Page(pageId, pageContents);
            //Write page data to the buffer until we hit the max buffer size,
            //but write all of the data to the store file.
            if (lineNo < bufferSize)
            {
               memory.add(p);
            }
            byte[] pageContentsBytes = new byte[4096];
            System.arraycopy(pageContents.getBytes(), 0, pageContentsBytes, 0, pageContents.length());
            store.seek(byteNo);
            store.write(pageContentsBytes);
            storeIndex.put(pageId, byteNo);
            //Each string is buffered in 4096 bytes
            byteNo += 4096;
            ++lineNo;
         }
         in.close();
      }
      catch(IOException e)
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
      try
      {
         in = new Scanner(new FileReader("sample_files/all_requests.dat"));
         DataInputStream storeIn = new DataInputStream(new FileInputStream("store_file.dat"));
         serverLogger = new BufferedWriter(new FileWriter(new File("output/server_log.dat"), false));
         while (in.hasNext())
         {
            int clientId = Integer.parseInt(in.next());
            String requestType = in.next();
            int pageId = Integer.parseInt(in.next());
            boolean wasWrite = false;

            monitors.serverWaitTurn();
            Page p = searchMemory(pageId);
            if (requestType.equals("write"))
            {
               System.out.println("[SERVER] Request is write");
               //Set contents sets the Page's 'dirtied' attribute to true so
               //that we can decide whether to flush or just delete it if it is
               //evicted from memory
               p.setContents(in.nextLine().trim());
               wasWrite = true;
            }
            else
            {
               System.out.println("[SERVER] Request is read (" + p + ") for client " + clientId);
               monitors.readRequest(p, clientId);
            }
            monitors.serverSignalDone(wasWrite);
         }
         in.close();
         serverLogger.close();
         monitors.terminateThreads();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private static Page searchMemory(int pageId)
   {
      Page page = null;
      boolean pageFoundInBuffer = false;
      for (Page p : memory)
      {
         if (p.getId() == pageId)
         {
            pageFoundInBuffer = true;
            //Move the most recently used page to the end of the list
            memory.remove(p);
            memory.add(p);
            page = p;
            break;
         }
      }

      if (!pageFoundInBuffer)
      {
         page = searchStore(pageId);
      }

      return page;
   }

   private static Page searchStore(int pageId)
   {
      Page page = null;
      try
      {
         //Takes the pointer to the store file to the start of the
         //data associated with pageId
         store.seek(storeIndex.get(pageId));
         byte[] pageBytes = new byte[4096];
         store.read(pageBytes);
         String pageContents = new String(pageBytes);
         page = new Page(pageId, pageContents);

         //As we had to retrieve a page from the store, we must delete a page
         //from memory to make room for it.
         Page victim = memory.get(0);

         //But first we must update it in the store file if changes have been
         //made in memory
         if (victim.isDirtied())
         {
            pageBytes = new byte[4096];
            System.arraycopy(victim.getContents().getBytes(), 0, pageBytes, 0, victim.getContents().length());
            store.seek(storeIndex.get(victim.getId()));
            //Page has been modified that must be flushed to the store file
            store.write(pageBytes);
         }

         serverLogger.write(victim.toString() + '\n');
         memory.remove(0);
         memory.add(page);
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }

      return page;
   }
}
