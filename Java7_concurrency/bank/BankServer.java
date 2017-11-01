package cscie55.hw5.bank;

import cscie55.hw5.bank.command.Command;

public interface BankServer
{
    /**
     * Executes the given Command.
     * @param command the Command to be executed.
     */
    void execute(Command command);

    /**
     * Stop the BankServer. No further Commands will be executed.
     * @throws InterruptedException
     */
    void stop() throws InterruptedException;

    /**
     * Returns the total of all Account balances for all Accounts managed by the BankServer's Bank.
     * @return The total of all Account balances for all Accounts managed by the BankServer's Bank.
     */
    long totalBalances();
}
