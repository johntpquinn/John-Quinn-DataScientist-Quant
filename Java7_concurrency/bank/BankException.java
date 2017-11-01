package cscie55.hw5.bank;

public abstract class BankException extends Exception
{
    protected BankException(String message)
    {
        super(message);
    }
}
