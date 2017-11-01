package cscie55.hw5.bank.command;

import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.InsufficientFundsException;

public class CommandDeposit extends Command
{
    @Override
    public void execute(Bank bank) throws InsufficientFundsException
    {
        simulateNetworkDelay();
        bank.deposit(accountId, amount);
    }

    public CommandDeposit(int accountId, long amount)
    {
        this.accountId = accountId;
        this.amount = amount;
    }

    private final int accountId;
    private final long amount;
}
