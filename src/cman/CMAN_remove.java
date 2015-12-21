package cman;

import java.io.File;

public class CMAN_remove 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	//public static Scanner CMAN.input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	
	/**
	Initialization for remove.
	*/
	public void init_config_remove(String mf, String vf, String ed)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
		this.util.init_config_util(mf, vf, ed);
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
		if(!new File(execdir + "/LocalData/ModsDownloaded/" + modname + ".installed").delete())
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
