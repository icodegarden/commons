package io.github.icodegarden.commons.designpattern.factorymethod;
                                                         
public class GrapeGardener implements FruitGardener 
{
    public Fruit factory()
    {
        return new Apple();
    }
}
