package cscie55.hw5.bank;

import cscie55.hw5.bank.InsufficientFundsException;
import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.Account;

/**
 * This AccountImpl class implements the Account interface.
 * It handles deposits and withdrawals
 * @author John Quinn, CSCIE-55, Fall 2015
 */
public class AccountImpl implements Account
{

	/**
	 * The below method returns the current account id number
	 * @return id the index number assigned to the account
	 * in the accountSubLedg collection
	 */
	
	/**
	 * This id field is the identififer (account number)
	 * of a given account
	 */
	private int id;
	
	/**
	 * This id method returns the accounts id number
	 * @return id the account's id number
	 */
	public int id() 
	{
		return id;
	}
	
	/**
	 * This balance field is the balance of the given account
	 */
	private long balance;

	/**
	 * This balance method returns the accounts balance
	 */
	public long balance() 
	{
		return balance;
	}

	/**
	 * This denotes signature Bank for object Bank
	 */
	protected Bank bank;
	
	/** 
	 * The Account constructor creates an account, that has three
	 * pieces of state
	 * @param bank is this bank, of which all these accounts are a part
	 * @param id is the account ID (account number/account identifier)
	 * @param balance is the current balance of the account
	 */
	protected void Account(Bank bank, int id, long balance)
	{
		this.bank = bank;
		this.id = id;
		this.balance = balance;
	}
	
	/**
	 * This account constructor allows for the creation of a new
	 * account, with an initial balance of zero and given id
	 * @param id is the account ID (account number/account identifier)
	 */
	public AccountImpl(int id)
	{
		this.id = id;
		balance=0;
	}
	
	/**
	 * This deposit accepts a deposit into a valid account for a valid amount
	 * @param amount is the dollar amount of the putative deposit
	 */
		
	public void deposit(long amount) throws IllegalArgumentException
		{
			if (amount > 0)
			{	
				balance=balance+amount;
			}
			else
			{
				throw new IllegalArgumentException("amount must be positive");
			}
		}
	
	/**
	 * This withdraw method removes a withdrawal from a valid account for a valid mount
	 * @param amount is the dollar amount of the putative withdrawal
	 */
		
	public void withdraw(long amount) throws IllegalArgumentException,InsufficientFundsException
		{
		if (amount > 0){
			if (balance >= amount)
			{
				balance=balance-amount;
			}
			else
			{
				throw new InsufficientFundsException(this,amount);
			}
		}
		else throw new IllegalArgumentException("amount must be positive");
	}
}

