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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author CMAN Team
 */
public class CMAN_importexport 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public String instance = "@ERROR@";
	//public static Scanner CMAN.input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	CMAN_install install = new CMAN_install();
	
	/**
	Initialization for importexport.
	*/
	public void init_config_importexport(String mf, String vf, String ed, String i)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
		instance = i;
		this.util.init_config_util(mf, vf, ed, i);
		this.install.init_config_install(mf, vf, ed, i);
	}
	
	/**
	Exports installed mods into filename.modlist.
	*/
	public void export_mods(String filename)
	{
		JsonParser parser = new JsonParser();
		if(filename == null)
		{
			System.out.println("What would you like your new modlist to be called?");
			filename = CMAN.input.nextLine();
		}
		if(new File(execdir + "/LocalData/ModsDownloaded/" + instance).exists())
		{
			File[] jsons = new File(execdir + "/LocalData/ModsDownloaded/" + instance).listFiles();
			String[] names = new String[jsons.length];
			int dirlength = new String(execdir + "/LocalData/ModsDownloaded/" + instance).length();
			int i = 0;
			for(File f : jsons)
			{
				names[i] = f.getAbsolutePath().substring(dirlength, jsons[i].getAbsolutePath().length() - 10);
				i++;
			}
			
			JsonArray array = new JsonArray();
			for(String name : names)
			{
				JsonElement temp = parser.parse(name);
				array.add(temp);
			}
			JsonObject j = new JsonObject();
			j.add("Mods", array);
	       	FileWriter fw;
			try 
			{
				if(!new File(execdir + "/LocalData/Modlists").exists())
				{
					new File(execdir + "/LocalData/Modlists").mkdir();
				}
				fw = new FileWriter(execdir + "/LocalData/Modlists/" + filename + ".modlist", false);
				fw.write(j.toString());
				fw.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("You don't have any mods installed!");
			return;
		}
	}
	
	/**
	Insatlls mods from a .modlist file in the /LocalData/Modlists/ directory.
	*/
	public void import_mods(String path)
	{
		Gson gson = new Gson();
		if(path == null)
		{
			System.out.print("Please enter the name of the modlist.");
			path = execdir + "/LocalData/Modlists/" + CMAN.input.nextLine() + ".modlist";
		}
		
		if(new File(path).exists())
		{
			System.out.println(path + " found.");
		}
		else
		{
			System.out.println(path + " not found.");
			return;
		}
		
		JsonObject json_data;
        JsonParser parser = new JsonParser();
        JsonElement jsonElement;
		try 
		{
			jsonElement = parser.parse(new FileReader(path));
			json_data = jsonElement.getAsJsonObject();
			ArrayList<String> mods = gson.fromJson(json_data.get("Mods").getAsJsonArray().toString(), new TypeToken<ArrayList<String>>(){}.getType());;
			for(String mod : mods)
			{
				System.out.println("Installing " + mod + "...");
				install.install_mod(mod);
				System.out.println(mod + " installed.");
			}
		} 
		catch (JsonIOException e) 
		{
			System.out.println("The modlist file appears to be invalid.");
		} 
		catch (JsonSyntaxException e) 
		{
			System.out.println("The modlist file appears to be invalid.");
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("The modlist file appears to be invalid.");
		}
	}
}
