package cman;

import java.awt.GraphicsEnvironment;
import java.util.Scanner;

public class Inputs 
{
	public static Scanner input = new Scanner(System.in);
	Visual v;
	public Inputs(String version)
	{
		if(!GraphicsEnvironment.isHeadless() && version != null)
		{
			v = new Visual(version);
		}
	}
	
	public String nextLine()
	{
		if(GraphicsEnvironment.isHeadless())
		{
			return input.nextLine();
		}
		else
		{
			return v.getLine();
		}
	}
}
