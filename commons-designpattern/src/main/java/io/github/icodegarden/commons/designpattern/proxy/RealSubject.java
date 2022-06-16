package io.github.icodegarden.commons.designpattern.proxy;

public class RealSubject extends Subject 
{
	public RealSubject()
	{ 
	}
	
	public void request()
	{ 
		System.out.println("From real subject.");
	}
}
