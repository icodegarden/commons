package io.github.icodegarden.commons.designpattern.mediator;

public class Colleague1 extends Colleague
{
	public Colleague1(Mediator m)
	{
		super( m );
	}

    public void action()
    {
        System.out.println("This is an action from Colleague 1");
    }
}
