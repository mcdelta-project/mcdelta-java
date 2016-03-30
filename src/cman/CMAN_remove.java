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

import java.io.File;

/**
 *
 * @author CMAN Team
 */
public class CMAN_remove 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public String instance = "@ERROR@";
	//public static Scanner CMAN.input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	
	/**
	Initialization for remove.
	*/
	public void init_config_remove(String mf, String vf, String ed, String i)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
		instance = i;
		this.util.init_config_util(mf, vf, ed, i);
	}
	
	/**
	Removes installed modname.
	*/
	public void remove_mod(String modname)
	{
		if(modname == null)
		{
			System.out.print("Enter mod name: ");
			modname = CMAN.input.nextLine();
		}
		System.out.println("Removing file for mod in ModsDownloaded");
		if(!new File(execdir + "/LocalData/ModsDownloaded/" + instance + "/" + modname + ".installed").delete())
		{
			System.out.println("Either " + modname + " is not installed, or something went horribly wrong.");
			return;
		}
		if((util.get_json(modname).get("Type").getAsString().equals("Forge")) || (util.get_json(modname).get("Type").getAsString().equals("Liteloader")))
		{
			File[] mods = new File(modfolder).listFiles();
			for (File f : mods)
			{
				if(f.getName().startsWith(modname + "-") && f.getName().endsWith(".jar"))
				{
					System.out.print("Delete \"" + f.getName() + "\"? Type OK to delete, or anything else to skip: ");
					if(CMAN.input.nextLine().equals("OK"))
					{
						f.delete();
						System.out.println("Deleted " + f.getName() + ".");
					}
					else
					{
						System.out.println("Skipped " + f.getName() + ".");
					}
				}
			}
		}
		else
		{
			System.out.println("I cannot remove installer mods or basemods! (If your mod is not an installermod or basemod, then something went horribly wrong.)");
		}
	}
}
