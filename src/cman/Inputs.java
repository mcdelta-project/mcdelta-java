/*
 * Copyright (C) 2015 CMAN Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cman;

import java.awt.GraphicsEnvironment;
import java.util.Scanner;

/**
 *
 * @author CMAN Team
 */
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
