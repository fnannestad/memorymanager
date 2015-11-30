/*
 * Author: Finn Nannestad
 * Number: 1744 2446
 */

import java.util.concurrent.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.locks.Condition;
import java.util.*; 

public class MonitorArray 
{
   private Lock lockvar;
   private Condition [] conditions;
   private Page page;
   private PriorityQueue<Request> requests;
   //used to determine if a client should wake up
   private boolean[] clients;
   private int serverId;
   private int currentRequest;
   //used to terminate the server thread
   private int numClients;

   public MonitorArray(int num) 
   {
      numClients = num;
      clients = new boolean[num];
      num += 1; //For the server condition

      lockvar = new ReentrantLock();
      conditions = new Condition[num];
      for (int i = 0; i < num ; i++) 
      {
         conditions[i] = lockvar.newCondition();
      }

      requests = new PriorityQueue<Request>();
      serverId = num - 1;
      currentRequest = 1;
   }

   public Page waitTurn(int id) 
   {
      Page p;
      lockvar.lock();
      while (!clients[id]) 
      {
         try 
         {
            conditions[id].await();
         }
         catch (Exception e) 
         {
            e.printStackTrace();
         }
      }
      p = page;
      lockvar.unlock();
      return p;
   }

   public Request serverWaitTurn()
   {
      Request r;
      lockvar.lock();
      //Wait while there are no requests pending or if the lowest request number in
      //the queue is not the one we are waiting on. (or if all clients have
      //terminated)
      while (numClients > 0 && (requests.size() == 0 || requests.peek().getNumber() != currentRequest))
      {
         try
         {
            conditions[serverId].await();
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      r = requests.peek();
      lockvar.unlock();
      return r;
   }

   public void signalDone(int id) 
   {
      lockvar.lock();
      System.out.println("[MONITOR] Client " + id + " finished");
      //Set the client to false so that it doesn't instantly run again
      clients[id] = false;
      lockvar.unlock();
   }

   public void serverSignalDone(Page p)
   {
      lockvar.lock();
      //Request has processed so now we must process the next one
      ++currentRequest;
      System.out.println("[MONITOR] Next request should be " + currentRequest);
      page = p;
      //Set current client to the one who sent the request to wake them up
      //Also removes the request from the queue
      int client = requests.poll().getClientId();
      clients[client] = true;
      
      System.out.print("\n[MONITOR] Pending requests:\n	  ");
      for(Request rt : requests)
      {
         System.out.print(rt.getNumber() + " ");
      }
      System.out.println('\n');

      System.out.println("[MONITOR] Waking up client " + client);
      conditions[client].signal();
      lockvar.unlock();
   }

   public void sendRequest(Request r)
   {
      lockvar.lock();
      requests.add(r);

      System.out.print("\n[MONITOR] Pending requests:\n	  ");
      for(Request rt : requests)
      {
         System.out.print(rt.getNumber() + " ");
      }
      System.out.println('\n');

      if (requests.peek().getNumber() == currentRequest)
      {
         System.out.println("[MONITOR] Signalling request " + requests.peek().getNumber() + " to server");
         conditions[serverId].signal();
      }
      lockvar.unlock();
   }

   public void notifyClientTerminated()
   {
      lockvar.lock();
      --numClients;
      //If all clients are terminated then signal the server, which should terminate
      if (numClients == 0)
      {
         conditions[serverId].signal();

      }
      lockvar.unlock();
   }
}
