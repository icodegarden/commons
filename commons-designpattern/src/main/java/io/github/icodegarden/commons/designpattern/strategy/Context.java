package io.github.icodegarden.commons.designpattern.strategy;

public class Context
{
    public void contextInterface()
    {
        strategy.strategyInterface();
    }

    /**
     * @link aggregation
     * @directed 
     */
    private Strategy strategy;
}
