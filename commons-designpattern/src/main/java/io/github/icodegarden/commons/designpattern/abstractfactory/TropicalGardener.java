package io.github.icodegarden.commons.designpattern.abstractfactory;

public class TropicalGardener implements Gardener
{
    public Fruit createFruit(String name)
    {
        return new TropicalFruit(name);
    }

    public Veggie createVeggie(String name)
    {
        return new TropicalVeggie(name);
    }

    /** @link dependency
     * @label Creates*/
    /*# TropicalVeggie lnkTropicalVeggie; */

    /** @link dependency
     * @label Creates*/
    /*# TropicalFruit lnkTropicalFruit; */
}
