package cscie55.hw5.bank.command;

import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.InsufficientFundsException;

public class CommandStop extends Command
{
    @Override
    public void execute(Bank bank) throws InsufficientFundsException
    {
        // Nothing to do
    }

    @Override
    public boolean isStop()
    {
        return true;
    }

    public CommandStop()
    {
    }
}
