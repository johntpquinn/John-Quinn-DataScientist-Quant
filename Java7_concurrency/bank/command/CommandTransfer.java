package cscie55.hw5.bank.command;

import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.InsufficientFundsException;

public class CommandTransfer extends Command
{
    @Override
    public void execute(Bank bank) throws InsufficientFundsException
    {
        simulateNetworkDelay();
        bank.transfer(fromAccountId, toAccountId, amount);
    }

    public CommandTransfer(int fromAccountId, int toAccountId, long amount)
    {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
    }

    private final int fromAccountId;
    private final int toAccountId;
    private final long amount;
}
