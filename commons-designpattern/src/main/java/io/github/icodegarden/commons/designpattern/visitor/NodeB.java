package io.github.icodegarden.commons.designpattern.visitor;

public class NodeB extends Node
{
    public void accept(Visitor visitor)
    {
        visitor.visit(this);
    }

    public String operationB()
    {
       return "NodeB is visited";
    }
}
