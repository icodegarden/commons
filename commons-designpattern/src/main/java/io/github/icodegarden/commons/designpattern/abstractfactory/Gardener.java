package io.github.icodegarden.commons.designpattern.abstractfactory;

public interface Gardener
{
    public Fruit createFruit(String name);

    public Veggie createVeggie(String name);
}
