package io.github.icodegarden.commons.designpattern.bridge;

abstract public class Abstraction
{
    public void operation()
    {
    	imp.operationImp();
    }

    /**
     * @link aggregation
     * @directed
     * @supplierRole impl*/
    private Implementor imp;
}
