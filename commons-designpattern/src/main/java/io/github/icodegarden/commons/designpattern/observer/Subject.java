package io.github.icodegarden.commons.designpattern.observer;

public interface Subject
{
    public void attach(Observer observer);

    public void detach(Observer observer);

    void notifyObservers();
}
