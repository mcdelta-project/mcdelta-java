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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Scanner;

import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;

import com.google.gson.JsonObject;

/**
 *
 * @author CMAN Team
 */
public class CMAN 
{
	int nightlyVersion = 002;
	boolean isNightly = true; 
	String version = "1.1.0";
	public String getVersion() { //Set devBuild to null if stable and not nightly
		int devBuild = nightlyVersion;
		if (isNightly) {
			return version + "-nightly-b" + String.format("%03d", devBuild);
		} else {
			return version;
		}
	}
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	//public static Scanner input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	CMAN_install install = new CMAN_install();
	CMAN_remove remove = new CMAN_remove();
	CMAN_upgrade upgrade = new CMAN_upgrade();
	CMAN_importexport importexport = new CMAN_importexport();
	static Inputs input; 
	static String tab = "        ";

	
	public void check_for_updates()
	{
		URL url;
		try 
		{
			if(isNightly)
			{
				url = new URL("https://raw.githubusercontent.com/Comprehensive-Minecraft-Archive-Network/CMAN-Java/nightly/version.txt");
			}
			else
			{
				url = new URL("https://raw.githubusercontent.com/Comprehensive-Minecraft-Archive-Network/CMAN-Java/stable/version.txt");
			}
			//url = new URL("https://raw.githubusercontent.com/randomtestfive/CMAN-Java/master/version.txt");
			Scanner s = new Scanner(url.openStream(), "UTF-8");
			String latestversion = s.next();
			if(!latestversion.equals(getVersion()))
			{
				System.out.println("WARNING! YOU ARE USING OLD VERSION " + getVersion() + "! NEWEST VERSION IS " + latestversion + "!");
			}
			else
			{
				System.out.println("CMAN-Java is up to date.");
			}
			s.close();
		} 
		catch (MalformedURLException e) 
		{
			System.out.println("Something is wrong with the url.");
		} 
		catch (IOException e) 
		{
			System.out.println("Something is wrong with the url?");
			e.printStackTrace();
		}	
	}
	
	public void update_archive()
	{
		this.update_archive(false);
	}
	
	public void update_archive(boolean loud)
	{
		if(new File(execdir + "/Data/CMAN-Archive").exists())
		{
			try 
			{
				util.delete_recursivly(execdir + "/Data/CMAN-Archive");
			} catch (IOException e) {e.printStackTrace();}
		}
		new File(execdir + "/Data").mkdir();
		//new File(execdir + "/Data/CMAN-Archive").mkdir();
		URL url;
		String file_name = "CMAN.tar.gz";
		try 
		{
			if(loud)
			System.out.println("Downloading Archive");
			url = new URL("https://github.com/Comprehensive-Minecraft-Archive-Network/CMAN-Archive/archive/master.zip");
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(execdir + "/Data/" + file_name);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			if(loud)
			System.out.println("Done");
		}
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		File sourceFile = new File(execdir + "/Data/" + file_name);
		File destDir = new File(execdir + "/Data");
		File endDir = new File(execdir + "/Data/CMAN-Archive-master");
		if(loud)
		System.out.println("Extracting " + sourceFile.getName() + " to " + endDir.getName());
		UnArchiver ua = new TarGZipUnArchiver(sourceFile);
		ua.setSourceFile(sourceFile);
		destDir.mkdirs();
		ua.setDestDirectory(destDir);
		ua.extract("CMAN-Archive-master/", destDir);
		endDir.renameTo(new File(execdir + "/Data/CMAN-Archive"));
		if(loud)
		System.out.println("Renamed CMAN-Archive-master to CMAN-Archive");
		sourceFile.delete();
		if(loud)
		{
			System.out.println("Deleting CMAN.tar.gz");
			System.out.println("Done");
		}
	}
	
	public void get_info(String modname)
	{
		if(modname == null)
		{
			System.out.println("Enter mod name: ");
			modname = input.nextLine();
		}
		
		JsonObject json_data = util.get_json(modname);
		if(json_data != null)
		{
			String stable = "Unstable";
			if(!json_data.get("Unstable").getAsBoolean())
			{
				stable = "Stable";
			}
			String reqs = "None";
			if(json_data.get("Requirements").getAsJsonArray().size() > 0)
			{
				reqs = "";
				for(int i = 0; i<json_data.get("Requirements").getAsJsonArray().size(); i++)
				{
					reqs = reqs + json_data.get("Requirements").getAsJsonArray().get(i).getAsString() + ", ";
				}
				reqs = reqs.substring(0, reqs.length() - 2);
			}
			String incomp = "None";
			if(json_data.get("Incompatibilities").getAsJsonArray().size() > 0)
			{
				incomp = "";
				for(int i = 0; i<json_data.get("Incompatibilities").getAsJsonArray().size(); i++)
				{
					incomp = incomp + json_data.get("Incompatibilities").getAsJsonArray().get(i).getAsString() + ", ";
				}
				incomp = incomp.substring(0, incomp.length() - 2);
			}
			System.out.println(json_data.get("Name").getAsString() + ":");
			System.out.println(tab + "Version: " + json_data.get("Version").getAsString() + " (" + stable + ")");
			System.out.println(tab + "Author(s): " + json_data.get("Author").getAsString());
			System.out.println(tab + "Description: " + json_data.get("Desc").getAsString());
			System.out.println(tab + "Requirements: " + reqs);
			System.out.println(tab + "Known Incompatibilities: " + incomp);
			System.out.println(tab + "Download Link: " + json_data.get("Link").getAsString());
			System.out.println(tab + "License: " + json_data.get("License").getAsString());
		}
	}
	
	public void print_help()
	{
		System.out.println("Commands:");
		System.out.println(tab + "install 'mod': install the mod 'mod'");
		System.out.println(tab + "installm: install multiple mods");
		System.out.println(tab + "info 'mod': get info for the mod 'mod'");
		System.out.println(tab + "remove 'mod': remove the mod 'mod'");
		System.out.println(tab + "removem: remove multiple mods");
		System.out.println(tab + "upgrade 'mod': upgrade the mod 'mod'");
		System.out.println(tab + "upgradem: upgrade multiple mods");
		System.out.println(tab + "upgradeall: upgrade all outdated mods");
		System.out.println(tab + "upgrades: list available mod upgrades");
		System.out.println(tab + "update: update the CMAN archive");
		System.out.println(tab + "help: display this help message");
		System.out.println(tab + "version: display the CMAN version number");
		System.out.println(tab + "list: list installed mods");
		System.out.println(tab + "export 'name': export a modlist with the name 'name' , which can be imported later");
		System.out.println(tab + "import 'pathtomodlist': import the modlist 'pathtomodlist'");
		System.out.println(tab + "exit: exit CMAN");
	}
	
	public static void main(String[] args) throws IOException 
	{
		CMAN cman = new CMAN();
		input = new Inputs(cman.getVersion());
		cman.execdir = new java.io.File( "." ).getCanonicalPath(); //decodedPath.substring(1, decodedPath.length() - 1);
		//System.out.println(decodedPath);
		//v.text.setText(cman.execdir);
		cman.util.execdir = cman.execdir;
		String[] places = cman.util.read_config();
		cman.util.init_config_util(places[0], places[1], cman.execdir);
		cman.install.init_config_install(places[0], places[1], cman.execdir);
		cman.remove.init_config_remove(places[0], places[1], cman.execdir);
		cman.upgrade.init_config_upgrade(places[0], places[1], cman.execdir);
		cman.importexport.init_config_importexport(places[0], places[1], cman.execdir);
		cman.update_archive();
		System.out.println("CMAN-Java v" + cman.getVersion());
		cman.check_for_updates();
		if(cman.upgrade.get_upgrades().length != 0)
		{
			System.out.println("The following upgrades are availible:");
			for(JsonObject[] upgrade : cman.upgrade.get_upgrades())
			{	
				System.out.println(tab + upgrade[0].get("Name").getAsString() + "(current version: " + upgrade[1].get("Version").getAsString() + ", you have: " + upgrade[0].get("Version").getAsString() + ")");
			}
		}
		int i = 0;
		for(String arg : args)
		{
			if(arg.equals("-i") || arg.equals("--install"))
			{
				int o = 1;
				while(!args[i + o].startsWith("-"))
				{
					cman.install.install_mod(args[i+o]);
					o++;
				}
			}
			if(arg.equals("-r") || arg.equals("--remove"))
			{
				int o = 1;
				while(!args[i + o].startsWith("-"))
				{
					cman.remove.remove_mod(args[i+o]);
					o++;
				}
			}
			if(arg.equals("-u") || arg.equals("--upgrade"))
			{
				int o = 1;
				while(!args[i + o].startsWith("-"))
				{
					cman.upgrade.upgrade_mod(args[i+o]);
					o++;
				}
			}
			if(arg.equals("--info"))
			{
				int o = 1;
				while(!args[i + o].startsWith("-"))
				{
					cman.get_info(args[i + o]);
					o++;
				}
			}
			if(arg.equals("-e") || arg.equals("--export"))
			{
				cman.importexport.export_mods(args[i + 1]);
			}
			if(arg.equals("--import"))
			{
				cman.importexport.import_mods(args[i + 1]);
			}
			i++;
		}
		cman.print_help();
		
		while(true)
		{
			System.out.print("> ");
			String command = input.nextLine();
			if(command.split(" ")[0].equals("update"))
			{
				cman.update_archive(true);
			}
			else if(command.split(" ")[0].equals("upgrades"))
			{
				cman.update_archive();
				cman.upgrade.check_upgrades(true);
			}
			else if(command.split(" ")[0].equals("upgrade"))
			{
				if(command.split(" ").length == 2 && !command.split(" ")[1].equals(""))
				{
					String mod = command.split(" ")[1];
					cman.update_archive();
					cman.upgrade.upgrade_mod(mod);
				}
				else if(command.split(" ").length == 1)
				{
					cman.update_archive();
					cman.upgrade.upgrade_mod(null);
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("upgradeall"))
			{
				cman.update_archive();
				JsonObject[][] updates = cman.upgrade.get_upgrades();
				if(updates.length == 0)
				{
					System.out.println("All mods up to date.");
				}
				else
				{
					for(JsonObject[] update : updates)
					{
						cman.upgrade.upgrade_mod(update[0].get("Name").getAsString());
					}
				}
			}
			else if(command.split(" ")[0].equals("install"))
			{
				if(command.split(" ").length == 2 && !command.split(" ")[1].equals(""))
				{
					String mod = command.split(" ")[1];
					cman.update_archive();
					cman.install.install_mod(mod);
				}
				else if(command.split(" ").length == 1)
				{
					cman.update_archive();
					cman.install.install_mod(null);
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("remove"))
			{
				if(command.split(" ").length == 2 && !command.split(" ")[1].equals(""))
				{
					String mod = command.split(" ")[1];
					cman.update_archive();
					cman.remove.remove_mod(mod);
				}
				else if(command.split(" ").length == 1)
				{
					cman.update_archive();
					cman.remove.remove_mod(null);
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("installm") || command.split(" ")[0].equals("installmany"))
			{
				if(command.split(" ").length >= 2)
				{
					String[] modslist = command.split(" ");
					cman.update_archive();
					for(String mod : modslist)
					{
						System.out.println("Attempting to install " + mod);
						cman.install.install_mod(mod);
					}
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("removem") || command.split(" ")[0].equals("removemany"))
			{
				if(command.split(" ").length >= 2)
				{
					String[] modslist = command.split(" ");
					cman.update_archive();
					for(String mod : modslist)
					{
						System.out.println("Attempting to remove " + mod);
						cman.remove.remove_mod(mod);
					}
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("upgradem") || command.split(" ")[0].equals("upgrademany"))
			{
				if(command.split(" ").length >= 2)
				{
					String[] modslist = command.split(" ");
					cman.update_archive();
					for(String mod : modslist)
					{
						System.out.println("Attempting to upgrade " + mod);
						cman.upgrade.upgrade_mod(mod);
					}
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("export"))
			{
				if(command.split(" ").length == 2 && !command.split(" ")[1].equals(""))
				{
					String name = command.split(" ")[1];
					cman.update_archive();
					cman.importexport.export_mods(name);
				}
				else if(command.split(" ").length == 1)
				{
					cman.update_archive();
					cman.importexport.export_mods(null);
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("import"))
			{
				if(command.split(" ").length == 2 && !command.split(" ")[1].equals(""))
				{
					String name = command.split(" ")[1];
					cman.update_archive();
					cman.importexport.import_mods(name);
				}
				else if(command.split(" ").length == 1)
				{
					cman.update_archive();
					cman.importexport.import_mods(null);
				}
				else
				{
					System.out.println("Invalid command syntax.");
				}
			}
			else if(command.split(" ")[0].equals("list"))
			{
				cman.util.listmods();
			}
			else if(command.split(" ")[0].equals("info"))
			{
				cman.get_info(command.split(" ")[1]);
			}
			else if(command.split(" ")[0].equals("help") || command.split(" ")[0].equals("?"))
			{
				cman.print_help();
			}
			else if(command.split(" ")[0].equals("exit"))
			{
				if(CMAN.input.v != null)
				{
					CMAN.input.v.dispose();
				}
				return;
			}
			else if(command.split(" ")[0].equals("")){}
			else
			{
				System.out.println("Unknown command");
			}
		}
	}
}
