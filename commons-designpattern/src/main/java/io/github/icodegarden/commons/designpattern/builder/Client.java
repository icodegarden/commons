package io.github.icodegarden.commons.designpattern.builder;

public class Client
{
    /**
     * @link aggregation
     * @directed 
     */
    private Director director;

	private Builder builder = new ConcreteBuilder();
		
    public void requestBuild()
    {
		director = new Director(builder);
    }
}
