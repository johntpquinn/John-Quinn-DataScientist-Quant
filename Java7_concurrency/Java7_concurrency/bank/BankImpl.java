package cscie55.hw5.bank;

import cscie55.hw5.bank.Account;
import cscie55.hw5.bank.InsufficientFundsException;
import cscie55.hw5.bank.DuplicateAccountException;
import java.util.ArrayList;

/**
 * This BankImpl class implements the Bank interface
 * It effectuates transfers under the three differing specified
 * levels of thread locking
 * @author John Quinn, CSCIE-55, Fall 2015
 *
 */
public class BankImpl implements Bank 
{
	/**
	 * The below provides the signature account for type Account 
	 */
	
	/**
	 * This amount declaration is the dollar amount
	 * of the putative transaction
	 */
	protected int amount;
	
	/**
	 * This balance variable allows for local instance
	 * variables with balances
	 */
	protected long balance = 0;
	
	/**
	 * The below provides the signature for the Accounts collection
	 */
	protected ArrayList<Account> Accounts = new ArrayList<Account>();
	
	public void addAccount(Account account) throws DuplicateAccountException	
	{
		if (Accounts.contains(account))
		{	
			throw new DuplicateAccountException(account.id());
		}
		else
		{
			Accounts.add(account);
		}
	}	
	
	/**
	 * This transfer method transfers amounts between accounts locking
	 * only the pair of accounts to be affected by the putative transfer
	 * @param fromId is the account number from which the putative transfer is to occur
	 * @param toID is the account number to which the putative transfer is to occur
	 * @param amount is the dollar amount of the putative transfer
	 * @throws InsufficientFundsException thrown if an overdraw would result
	 */
	public void transfer(int fromId, int toId, long amount)
	throws InsufficientFundsException
	{
		
		long initialFrom;
		long finalFrom;
		long initialTo;
		long finalTo;
		Account accountFrom = Accounts.get(fromId);
		Account accountTo = Accounts.get(toId);
		Account firstLock;
		Account secondLock;
		
		if (accountFrom.id()!=accountTo.id()){
			if(accountFrom.id() < accountTo.id()){
				firstLock=accountFrom;
				secondLock=accountTo;
			}
			else{
				firstLock=accountTo;
				secondLock=accountFrom;
			}
		
		
			synchronized(firstLock){
				synchronized(secondLock){
					if (accountFrom.balance() >= amount)
					{	
						initialFrom=accountFrom.balance();
						initialTo=accountTo.balance();
						accountFrom.withdraw(amount);
						accountTo.deposit(amount);
						finalFrom=accountFrom.balance();
						finalTo=accountTo.balance();
						if((initialFrom+initialTo) != (finalFrom+finalTo)){
							System.out.format("Mismatch InitialFrom = %d InitialTo= %d, finalFrom = %d finalTo=%d\n", initialFrom,initialTo,finalFrom,finalTo);
						}
					}
					else 
					{
						throw new InsufficientFundsException(accountFrom,amount);
					}
				}
			}
		}
	}
	
	/**
	 * This deposit accepts a deposit into a valid account for a valid amount
	 * @param amount is the dollar amount of the putative deposit
	 */
		
	public synchronized void deposit(int accountID, long amount) throws IllegalArgumentException
		{
			Account account = Accounts.get(accountID);
			if (amount > 0)
			{	
				account.deposit(amount);
			}
			else
			{
				throw new IllegalArgumentException("amount must be positive");
			}
		}
	/**
	 * This totalBalances method show the current total balance of all accounts
	 * @param accountSubLedg is the account subsidiary ledger of deposits
	 */
	public synchronized long totalBalances()
	{
		long grandTotal = 0;
		{
			for(int i=0; i< Accounts.size();i++) 
			{
				grandTotal = grandTotal + Accounts.get(i).balance();
			}
		}
		return grandTotal;
	}
	
	/**
	 * This mumberOfAccounts methold tells the total number of accounts at the bank
	 * @param accountSubLedg is the account subsidiary ledger of deposits
	 */
	public int numberOfAccounts()
	{
		return Accounts.size();
	}
	
	/**
	 * The below getAccounts method returns the Accounts collection
	 * for use in other classes
	 * @return Accounts
	 */
	public ArrayList<Account> getAccounts()
	{
		return Accounts;
	}
}


