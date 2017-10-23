package cscie55.hw5.bank;

import cscie55.hw5.bank.*;
import cscie55.hw5.bank.command.Command;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import cscie55.hw5.bank.command.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * This BankServerImpl class implements the BankServer interface class
 * @author John Quinn, CSCIE-55, Fall 2015
 */

public class BankServerImpl implements BankServer, Runnable
{
	/**
	 * This bank declaration denotes bank as type Bank
	 */
	public Bank bank;
	
	/**
	 * This Accounts signature allows for direct reference
	 * to that static collection created in AccountImpl
	 */
	public ArrayList<Account> Accounts;
	
	/**
	 * This int threadId is the signature of the one received as a paramater
	 * in CommandExecutionThread
	 */
	int threadId;
	
	/**
	 * This int clientThreads is the signature of the one received as a
	 * parameter in CommandExecution Thread
	 */
	int clientThreads;
	
	/**
	 * This thread signature denotes thread as type thread
	 */
	protected Thread thread;
	
	/**
	 * This thread signature denotes thread as type thread
	 */
	public Command command;
	/**
	 * This executeCommandInsideMonitor is true if Commands
	 * are executed within the monitor, false otherwise
	 */
	public boolean executeCommandInsideMonitor;
	
	/**
	 * This int TRANSACTIONS is the number of Commands
	 * expected to be processed and a paramater passed
	 * from JUnit file itself
	 */
	public static int TRANSACTIONS = 10000;
	
	/**
	 * This int SERVER_THREADS is the number of threads
	 * expected to be processed and a paramater passed
	 * from JUnit file itself
	 */
	public static int SERVER_THREADS;
	
	/**
	 * This int CLIENT_THREADS is the number of threads
	 * expected to be processed and a paramater passed
	 * from JUnit file itself
	 */
	public static int CLIENT_THREADS = 20;
	
	/**
	 * This int ACCOUNTS is the number of accounts created
	 * and one of the paramaters passed from the JUnit
	 */
	public static int ACCOUNTS = 10000;
	
	/**
	 * This commandQueue BlockingQueue denotes the static queue 
	 * of Commands to be maintained
	 */
	public ArrayBlockingQueue<Command> commandQueue = new ArrayBlockingQueue<Command>(10000000);
		
	/**
	 * This threads synchronized Array List denotes the array of threads 
	 * ordered by the Tester class
	 */
	public CommandExecutionThread[] threads;
	
		
	/**
	 * This BankServer constructor creates a bank server
	 * @param bank is this bank
	 * @param SERVER_THREADS is the number of server threads to run
	 * @param executeCommandInsideMinotor is the boolean for whether
	 * the commandQueue will be accessed inside or outside of the monitor
	 */
	public BankServerImpl(Bank bank, int SERVER_THREADS, boolean executeCommandInsideMonitor)
	{
		this.bank = bank;
		this.SERVER_THREADS = SERVER_THREADS;
		this.executeCommandInsideMonitor = executeCommandInsideMonitor;
		threads = new CommandExecutionThread[SERVER_THREADS];
		for (int t = 0; t < SERVER_THREADS; t++) 
		{
        threads[t] = new CommandExecutionThread(this, SERVER_THREADS, t);
        }
		startThreads();
	}
		
	/**
	 * This execute method executes the command object it receives
	 * @param command the command received and expected to be executed
	 * @throws InterruptedException 
	 */
	public synchronized void execute(Command command) throws IllegalArgumentException
	{
		
		{
			try 
			{
				commandQueue.put(command);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
		}
	}	
	/**
	 * This stop method simply stops the bank server, so that no
	 * further commands will be executed
	 */
	public synchronized void stop() throws InterruptedException
	{
		
		{
			for (int m = 0; m < SERVER_THREADS; m++) 
			{
		    command= new CommandStop();
		    try
			{
		    	threads[m].sleep(1000);
		    	commandQueue.put(command);
			} 
			catch (InterruptedException e1) 
			{
				e1.printStackTrace();
			}
		 }
		}
	}
	
	/**
	 * This totalBalances method show the current total balance of all accounts
	 * @return bank.totalBalances() the sum of the deposits subsidiary ledger accounts
	 */
	public synchronized long totalBalances()
	{
		return bank.totalBalances();
	}
	
	/**
	 * This startThreads method initializes and commences the
	 * running of the server threads
	 */
	public synchronized void startThreads()
	{
		for (int k=0; k < SERVER_THREADS; k++)
		{
		   	 threads[k].start();
		}
	}
		
	/**
	 * This run method is required to implement Runnable
	 */
	public synchronized void run()
	{
		//
	}
}	
	

	