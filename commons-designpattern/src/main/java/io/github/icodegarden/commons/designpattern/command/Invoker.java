package io.github.icodegarden.commons.designpattern.command;

public class Invoker
{
    public Invoker(Command command)
    {
        this.command = command;
    }

    public void action()
    {
		command.execute();
    }

    /**
     * @link aggregation
     * @directed 
     */
    private Command command;
}
