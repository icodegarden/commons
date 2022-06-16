package io.github.icodegarden.commons.designpattern.simplefactory;

public interface Fruit
{
    void grow();

    void harvest();

    void plant();
    
    /**
     * 工厂角色与抽象产品角色合并
     */
    public static Fruit factory(String which) throws BadFruitException
    {
        if (which.equalsIgnoreCase("apple"))
        {
            return new Apple();
        }
        else if (which.equalsIgnoreCase("strawberry"))
        {
            return new Strawberry();
        }
        else if (which.equalsIgnoreCase("grape"))
        {
            return new Grape();
        }
        else
        {
         	throw new BadFruitException("Bad fruit request");
        }
    }
}
