package cman;

import java.io.File;
import java.util.Scanner;

public class CMAN_remove 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public static Scanner input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	
	public void init_config_remove(String mf, String vf, String ed)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
	}
	
	public void remove_mod(String modname)
	{
		if(modname == null)
		{
			System.out.println("Enter mod name: ");
			modname = input.nextLine();
		}
		System.out.println("Removing file for mod in ModsDownloaded");
		if(!new File(execdir + "/LocalData/ModsDownloaded/" + modname + ".installed").delete())
		{
			System.out.println("Either " + modname + " is not installed, or something went horribly wrong.");
			return;
		}
		if(util.get_json(modname).get("Type").getAsString() == "Forge" || util.get_json(modname).get("Type").getAsString() == "Liteloader")
		{
			File[] mods = new File(modfolder).listFiles();
			for (File f : mods)
			{
				if(f.getName().startsWith(modname + "-") && f.getName().endsWith(".jar"))
				{
					System.out.println("Delete \"" + f.getName() + "\"? Type OK to delete, or anything else to skip: ");
					if(input.nextLine() == "OK")
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
