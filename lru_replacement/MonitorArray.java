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
   private int currentClient;
   private int serverId;
   private boolean clientDone;
   private boolean requestsDone;

	public MonitorArray(int num) 
	{
      num += 1; //For the server condition
		lockvar = new ReentrantLock();
		conditions = new Condition[num];
		for (int i = 0; i < num ; i++) 
		{
			conditions[i] = lockvar.newCondition();
		}
      serverId = conditions.length - 1;
      currentClient = serverId;
      clientDone = true;
      requestsDone = false;
	}

	public void waitTurn(int id) 
	{
		lockvar.lock();
		while (id != currentClient && !requestsDone) 
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
		lockvar.unlock();
	}

   public void serverWaitTurn()
   {
      lockvar.lock();
      while (!clientDone)
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
      lockvar.unlock();
   }

	public void signalDone(int id) 
	{
      System.out.println("[MONITOR] Client " + id + " finished");
		lockvar.lock();
      clientDone = true;
      //Set currentClient to -1 otherwise the client can possibly go and run
      //again before the grabs the lock
      currentClient = -1;
      conditions[serverId].signal();
		lockvar.unlock();
	}

   public void serverSignalDone(boolean wasWrite)
   {
      lockvar.lock();
      clientDone = wasWrite;
      conditions[serverId].signal();
      lockvar.unlock();
   }

   public void readRequest(Page p, int id)
   {
      lockvar.lock();
      page = p;
      currentClient = id;
      conditions[currentClient].signal();
      lockvar.unlock();
   }

   public Page getPage()
   {
      return page;
   }

   public void terminateThreads()
   {
      lockvar.lock();
      requestsDone = true;
      //Signal all the threads so they wake up and terminate
      for(int i = 0; i < conditions.length; ++i)
      {
         conditions[i].signal();
      }
      lockvar.unlock();
   }

   public boolean getRequestsDone()
   {
      return requestsDone;
   }
}
