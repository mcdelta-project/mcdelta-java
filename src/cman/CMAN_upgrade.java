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
import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 *
 * @author CMAN Team
 */
public class CMAN_upgrade 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public String instance = "@ERROR@";
	//public static Scanner CMAN.input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	CMAN_remove remove = new CMAN_remove();
	CMAN_install install = new CMAN_install();
	
	/**
	Initialization for upgrade.
	*/
	public void init_config_upgrade(String mf, String vf, String ed, String i)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
		instance = i;
		this.util.init_config_util(mf, vf, ed, i);
		this.remove.init_config_remove(mf, vf, ed, i);
		this.install.init_config_install(mf, vf, ed, i);
	}
	
	/**
	Upgrades installed mod to latest version in archive.
	*/
	public void upgrade_mod(String modname)
	{
		if(modname == null)
		{
			System.out.println("Enter mod name: ");
			modname = CMAN.input.nextLine();
		}
		JsonObject[] update = {util.get_installed_json(modname), util.get_json(modname)};
		if(new File(execdir + "/LocalData/ModsDownloaded/" + instance + "/" + modname + ".installed").exists())
		{
			System.out.println(modname + ".installed found");
		}
		else
		{
			System.out.println("Mod " + modname + " not found.");
			return;
		}
		JsonElement current = update[0].get("Version");
		JsonElement archive = update[1].get("Version");
		if(current.getAsString() != archive.getAsString() && util.mod_installed(modname))
		{
			remove.remove_mod(modname);
			install.install_mod(modname);
		}
		else if(!util.mod_installed(modname))
		{
			System.out.println(modname + " is not installed");
		}
		else
		{
			System.out.println(modname + " is already up to date");
		}
	}
	
	public JsonObject[][] get_upgrades()
	{
		return get_upgrades(instance);
	}
	
	/**
	Returns a 2D array. 1st coordinate is the upgrade set, 2nd has current installed version (0) and latest version (1)
	*/
	public JsonObject[][] get_upgrades(String inst)
	{
		ArrayList<JsonObject[]> updates = new ArrayList<JsonObject[]>();
		if(!(util.get_installed_jsons() == null))
		{
			JsonObject[] mods = util.get_installed_jsons(inst);
			for(JsonObject mod : mods)
			{
				if(mod != null)
				{
					JsonObject json_data = util.get_json(mod.get("Name").getAsString());
					if(json_data != null && !json_data.get("Version").getAsString().equals(mod.get("Version").getAsString()))
					{
						JsonObject[] temp = {mod, json_data};
						updates.add(temp);
					}
				}
			}
			JsonObject[][] out = new JsonObject[updates.size()][2];
			for(int i = 0; i < updates.size(); i++)
			{
				out[i] = updates.get(i);
			}
			return out;
		}
		return new JsonObject[0][2];
	}
	
	/**
	Checks if upgrades are available. Full shows list of all upgradable mods in console.
	*/
	public void check_upgrades(boolean full)
	{
		JsonObject[][] updates = get_upgrades();
		if(updates.length > 0)
		{
			if(!full)
			{
				System.out.println("Mod updates available!");
			}
			else
			{
				for (int i = 0; i < updates.length; i++)
				{
					System.out.println("Available Updates:");
					System.out.println(" " + updates[i][0].get("Name").getAsString() + " (current version: " + updates[i][1].get("Version").getAsString() + ", you have: " +updates[i][0].get("Version").getAsString() + ")");
				}
			}
		}
		else
		{
			if(full)
			{
				System.out.println("No updates available");
			}
		}
	}
}
