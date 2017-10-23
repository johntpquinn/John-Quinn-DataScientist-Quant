package cscie55.hw5.bank;

public interface Bank
{
    /**
     * Add the Account to those managed by this Bank. The Account's id must be unique.
     * @param account The Account being added.
     * @throws DuplicateAccountException if this Bank already manages an Account with identifier account.id().
     */

    void addAccount(Account account) throws DuplicateAccountException;

    /**
     * Transfer the given amount from one account to another.
     * @param fromAccountId The identifier of the Account from which funds will be withdrawn.
     * @param toAccountId The identifer of the Account to which funds will be deposited.
     * @param amount The amount to be transferred.
     * @throws InsufficientFundsException The account identifed by fromAccountId has a balance less than amount.
     */
    void transfer(int fromAccountId, int toAccountId, long amount) throws InsufficientFundsException;

    /**
     * Deposit the given amount to the Account identified by accountId.
     * @param accountId Identifies the Account to which funds will be deposited.
     * @param amount The amount to be deposited.
     */
    void deposit(int accountId, long amount);

    /**
     * Compute the total of all the Account balances for the Accounts managed by this Bank. The caller
     * is responsible for guaranteeing that no other Bank methods (addAccount, transfer, deposit) are executing
     * while this method is being executed.
     * @return the total of all the Account balances for the Accounts managed by this Bank.
     */
    long totalBalances();
}
