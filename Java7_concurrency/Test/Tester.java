package cscie55.hw5.Test;

import cscie55.hw5.bank.Account;
import cscie55.hw5.bank.AccountImpl;
import cscie55.hw5.bank.Bank;
import cscie55.hw5.bank.BankImpl;
import cscie55.hw5.bank.BankServer;
import cscie55.hw5.bank.BankServerImpl;
import cscie55.hw5.bank.DuplicateAccountException;
import cscie55.hw5.bank.command.Command;
import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/*
 * Tester contains three tests:
 *
 * 1) testSetup: Sets up a BankServer, creating a set of Accounts and depositing INITIAL_BALANCE in each, using
 *    the Deposit command. The BankServer is then shut down, and verifies that the total of all balances is as expected.
 *
 * 2) testCorrectness: Sets up a BankServer, creating a set of Accounts and depositing INITIAL_BALANCE in each,
 *    using the Deposit command. It then uses a set of TestThreads to submit commands to the BankServer concurrently.
 *    Once all the commands have been executed, the total of all the balances is checked for correctness. Because
 *    the TestThreads submit only Transfer commands, the total balance should be INITIAL_BALANCE * ACCOUNTS.
 *
 * 2) measurePerformanceOfCommandInsideAndOutsideMonitor: This runs the exact same test as testCorrectness, twice.
 *    once with command execution inside the monitor, once outside. The total execution time is measured each way,
 *    and the transaction rate for each run is reported.
 *
 * All of these tests call runTest to do their work. runTest does the following:
 *
 *     - Creates a BankServer, by calling createBankServer. createBankServer creates a BankImpl, and creates a set
 *       of Accounts and associates each with the Bank. Then, a BankServerImpl is created to encapsulate the BankImpl.
 *       Finally, INITIAL_BALANCE is added to each Account by submitting a Deposit command.
 *
 *     - A set of TestThreads are created (unless clientThreads is zero). Each TestThread will submit a number of
 *       Transfer transactions to the Bank, via the BankServer, (this is similar to what was done in HW4). Note that
 *       runTest has three loops, to create the TestThreads, to start each thread, and then to join them.
 *       (Joining a thread returns from execution when the thread being joined terminates.)
 *
 *     - Stops the BankServer, which causes all of the CommandExecutionThreads to terminate.
 *
 *     - Checks the balance, now that all of the Transfer commands have executed.
 */

public class Tester
{
    @Test
    public void testSetup() throws InterruptedException, DuplicateAccountException
    {
        runTest(false, 0);
    }

    @Test
    public void testCorrectness() throws InterruptedException, DuplicateAccountException
    {
        runTest(false, CLIENT_THREADS);
    }

    @Test
    public void measurePerformanceOfCommandInsideAndOutsideMonitor() throws DuplicateAccountException, InterruptedException
    {
        // Most java implementations use "just-in-time" compilation. Frequently used classes start out being
        // interpreted, and then get compiled as they are used more. This results in the most performance-critical
        // code running faster. warmup() exercises all the bank server code to ensure that compilation occurs
        // before timings start.
        warmup();
        Stopwatch timer;
        {
            timer = new Stopwatch();
            runTest(true, CLIENT_THREADS);
            timer.stop();
            double txnsPerMsec = (1000000.0 * TRANSACTIONS) / timer.nSec();
            System.out.format("Command INSIDE monitor:  %f txns/msec\n", txnsPerMsec);
        }
        {
           timer = new Stopwatch();
           runTest(false, CLIENT_THREADS);
           timer.stop();
           double txnsPerMsec = (1000000.0 * TRANSACTIONS) / timer.nSec();
           System.out.format("Command OUTSIDE monitor: %f txns/msec\n", txnsPerMsec);
        }
    }

    private void warmup() throws DuplicateAccountException, InterruptedException
    {
        runTest(true, CLIENT_THREADS);
        runTest(false, CLIENT_THREADS);
    }

    private void runTest(boolean executeCommandInsideMonitor, int clientThreads)
        throws InterruptedException, DuplicateAccountException
    {
        BankServer bankServer = createBankServer(executeCommandInsideMonitor);
        // Create the test threads
        TestThread[] threads = new TestThread[clientThreads];
        for (int t = 0; t < clientThreads; t++) {
            threads[t] = new TestThread(bankServer, TRANSACTIONS / clientThreads, t);
        }
        // Start the test threads
        for (TestThread thread : threads) {
            thread.start();
        }
        // Wait for the threads to complete
        for (TestThread thread : threads) {
            thread.join();
        }
        // Stop the server
        bankServer.stop();
        // Check balances
        assertEquals(ACCOUNTS * INITIAL_BALANCE, bankServer.totalBalances());
    }

    private BankServer createBankServer(boolean executeCommandInsideMonitor) throws DuplicateAccountException
    {
        // Create Bank and Accounts
        Bank bank = new BankImpl();
        for (int id = 0; id < ACCOUNTS; id++) {
            Account account = new AccountImpl(id);
            bank.addAccount(account);
        }
        // Create BankServer
        BankServer bankServer = new BankServerImpl(bank, SERVER_THREADS, executeCommandInsideMonitor);
        // Deposit initial funds
        for (int id = 0; id < ACCOUNTS; id++) {
            bankServer.execute(Command.deposit(id, INITIAL_BALANCE));

        }
        return bankServer;
    }

    private static final int INITIAL_BALANCE = 1000;
    private static final int ACCOUNTS = 100;
    private static final int TRANSACTIONS = 10000;
    private static final int CLIENT_THREADS = 20;
    private static final int SERVER_THREADS = 5;

    // Client-side thread for submitting commands to the BankServer, (similar to HW4).

    private static class TestThread extends Thread
    {
        @Override
        public void run()
        {
            for (int t = 0; t < transactions; t++) {
                // Pick two different accounts at random
                int from = random.nextInt(ACCOUNTS);
                int to;
                do {
                    to = random.nextInt(ACCOUNTS);
                } while (to == from);
                // Pick a random transfer amount
                long amount = random.nextInt(MAX_TRANSFER) + 1; // 1 .. MAX_TRANSFER
                bankServer.execute(Command.transfer(from, to, amount));
            }
        }

        public TestThread(BankServer bankServer, int transactions, int threadId)
        {
            this.bankServer = bankServer;
            this.transactions = transactions;
            this.random = new Random(threadId);
        }

        private static final int MAX_TRANSFER = 100;

        private final BankServer bankServer;
        private final int transactions;
        private final Random random;
    }

    // Utility class, to simplify time measurements.

    public class Stopwatch
    {
        public void start()
        {
            start = System.nanoTime();
        }

        public long stop()
        {
            long stop = System.nanoTime();
            long delta = stop - start;
            accumulated += delta;
            return delta;
        }

        public long nSec()
        {
            return accumulated;
        }

        public Stopwatch()
        {
            start();
        }

        private long start;
        private long accumulated = 0;
    }
}

