package cscie55.hw5.bank.command;

import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.InsufficientFundsException;

public abstract class Command
{
    /**
     * Execute this Command on the given Bank.
     * @param bank The bank whose Accounts will be operated on by this Command.
     * @throws cscie55.hw5.bank.InsufficientFundsException
     */
    public abstract void execute(Bank bank) throws InsufficientFundsException;

    /**
     * Returns true for the Stop Command, false for all others.
     * @return true for the Stop Command, false for all others.
     */
    public boolean isStop()
    {
        return false;
    }

    /**
     * Returns a Transfer Command.
     * @param fromAccountId The identifier of the Account from which funds will be withdrawn.
     * @param toAccountId The identifer of the Account to which funds will be deposited.
     * @param amount The amount to be transferred.
     * @return a Transfer Command.
     */
    public static Command transfer(int fromAccountId, int toAccountId, long amount)
    {
        return new CommandTransfer(fromAccountId, toAccountId, amount);
    }


    /**
     * Returns a Deposit Command.
     * @param accountId Identifies the Account to which funds will be deposited.
     * @param amount The amount to be deposited.
     * @ return a Deposit Command.
     */
    public static Command deposit(int accountId, long amount)
    {
        return new CommandDeposit(accountId, amount);
    }


    /**
     * Returns a Stop command.
     * @return a Stop command.
     */
    public static Command stop()
    {
        return STOP;
    }

    protected void simulateNetworkDelay()
    {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // Should not happen
            e.printStackTrace();
        }
    }

    private static final Command STOP = new CommandStop();
}
