package cscie55.hw5.bank;

import cscie55.hw5.bank.Account;
import cscie55.hw5.bank.AccountImpl;
import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.BankImpl;
import cscie55.hw5.bank.BankServer;
import cscie55.hw5.bank.BankServerImpl;
import cscie55.hw5.bank.DuplicateAccountException;
import cscie55.hw5.bank.command.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.Collections;

/**
 * This CommandExecutionThread class retrieves commands off of the 
 * commandQueue, executes the related commands, and adds and deletes
 * the related entries in the commandQueue and the threads array
 * @author John Quinn, CSCIE-55, Fall 2015
 */
public class CommandExecutionThread extends Thread implements Runnable
{
	/**
	 * This bank signature denotes the bank object
	 */
	public Bank bank;
	
	/**
	 * This bankServer signature denotes the bankServer object
	 */
	public BankServerImpl bankServer;
	
			
	/**
	 * This command is the current command waiting to be added
	 * being added to, or being removed from the commandQueue
	 */
	private volatile Command command;
	
		
	/**
	 * This int threadID is the ID of the thread being
	 * tested by the JUnit and a parameter passed
	 * by that Tester file
	 */
	protected int threadId;
		
	/**
	 * This int clientThreads is the number of client threads
	 * sent as a parameter 
	 */
	public int serverThreads;
	
	/**
	 * This TestThread constructor takes the bankServer, number of
	 * transactions and threadId passed to it via the JUnit
	 * @param bankServer the bankServer itself
	 * @param transaction the number of transactions to execute
	 * @param threadId the ID of the thread 
	 */
	public CommandExecutionThread(BankServerImpl bankServer, int serverThreads, int threadId)
	{
		this.bankServer = bankServer;
		this.serverThreads = serverThreads;
		this.threadId=threadId;
		this.bank=bankServer.bank;
	}	
	
	/**
	 * This run method actually moves commands off of the commanedQueue,
	 * hand them to the threads array, synchronizes those two collections,
	 * and causes the commands to be run	
	 */
	public void executeCommands() 
	{
		int counter = 0;
		Command command = null;
		boolean stopCommandReceived=false;
		while (true && !stopCommandReceived)
		{
			synchronized(bankServer.commandQueue)
			{
				try 
				{
						command = bankServer.commandQueue.take();
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				if(command.isStop())
				{
					stopCommandReceived=true;
				}
				else
				{
					stopCommandReceived=false;
				}
				
			}
			if(bankServer.executeCommandInsideMonitor)
			{	
				synchronized(bankServer.commandQueue)
				{
					try 
					{
						if(!stopCommandReceived)
						{	
							try 
							{
								command.execute(bank);
							}	
							catch (InsufficientFundsException e5) 
							{
								e5.printStackTrace();
							}
						}
						else
						{
							try 
							{
								break;
							}
							finally
							{
							//
							}
						}
					}	
					finally
					{
					//
					}
				}
			}
				if(!bankServer.executeCommandInsideMonitor)
				{	
					try 
					{
						if(!stopCommandReceived)
						{	
							try 
							{
								command.execute(bank);
							}	
					catch (InsufficientFundsException e5) 
					{
						e5.printStackTrace();
					}
					}
					else
					{
						try 
						{
							//stopThreads();
						}
						finally
						{
							//
						}
					}
				}	
				finally
				{
				//
				}
			}
		
		}
	}
	/**
	 * This stopThreads method causes each thread
	 * to exit the loop and the program to complete
	 */
	@SuppressWarnings("deprecation")
	public void stopThreads()
	{
		//synchronized(commandQueue)
		{
			for (CommandExecutionThread thread : bankServer.threads)
			{	
				try 
				{
					if(bankServer.threads.length>0)
					{
						if(thread.isAlive())
						{
							Thread.sleep(10000);
							thread.join();
						}
					}
					else 
					{
						return;
					}
				}
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
 				finally
				{
 					//
				}
			}
		}
	}
	
	/**
	 * This run method initiates this CommandExecutionThread
	 * class when the threads are started in the startThreads
	 * method of the BankServerImpl class
	 */
	public synchronized void run()
	{
		executeCommands();
		try 
		{
			this.join();
		} 
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
	}
}
	

