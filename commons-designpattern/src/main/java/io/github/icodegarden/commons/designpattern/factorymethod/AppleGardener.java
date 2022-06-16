package io.github.icodegarden.commons.designpattern.factorymethod;
                                                         
public class AppleGardener implements FruitGardener 
{
    public Fruit factory()
    {
        return new Apple();
    }
}
