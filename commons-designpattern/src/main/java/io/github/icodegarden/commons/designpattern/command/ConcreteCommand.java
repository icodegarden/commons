package io.github.icodegarden.commons.designpattern.command;

public class ConcreteCommand implements Command
{
    public ConcreteCommand(Receiver receiver)
    {
        this.receiver = receiver;
    }

    public void execute()
    {
        receiver.action();
    }

    /**
     * @directed
     * @clientRole receiver
     */
    private Receiver receiver;
}
