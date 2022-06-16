package io.github.icodegarden.commons.designpattern.visitor;

public class ConcreteVisitorA implements Visitor
{
    public void visit(NodeA nodeA)
    {
        // Write your code here
        System.out.println( nodeA.operationA() );
    }
    public void visit(NodeB nodeB)
    {
        // Write your code here
        System.out.println( nodeB.operationB() );
    }
}
