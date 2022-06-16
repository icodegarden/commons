package io.github.icodegarden.commons.designpattern.factorymethod;
                                                         
public class StrawberryGardener implements FruitGardener 
{
    public Fruit factory()
    {
        return new Apple();
    }
}
