package io.github.icodegarden.commons.designpattern.visitor;

public interface Visitor
{
    void visit(NodeA node);

    void visit(NodeB node);
}
