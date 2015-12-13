package cman;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class CMAN_install 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public static Scanner input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	
	/**
	Initialization for install.
	*/
	public void init_config_install(String mf, String vf, String ed)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
		this.util.init_config_util(mf, vf, ed);
	}
	
	/**
	Installs modname. Will attempt to install requirements and recommendations.
	*/
	public void install_mod(String modname)
	{
		Gson gson = new Gson();
		if(modname == null)
		{
			System.out.print("Enter mod name: ");
			modname = input.nextLine();
		}
		
		if(new File(execdir + "/Data/CMAN-Archive/" + modname + ".json").exists())
		{
			System.out.println(modname + ".json found.");
		}
		else
		{
			System.out.println("Mod " + modname + " not found.");
			return;
		}
		
		JsonObject json_data = util.get_json(modname);
		JsonElement modtype = json_data.get("Type");
		//System.out.println(modtype.getAsString());
		boolean IsUnstable = json_data.get("Unstable").getAsBoolean();
		if(IsUnstable)
		{
			System.out.print("This mod may be unstable. Type OK to install, or anything else to cancel: ");
			String temp = input.nextLine();
			if(!temp.equals("OK"))
			{
				System.out.println("Install canceled.");
				return;
			}
		}
		
		if(util.mod_installed(modname))
		{
			System.out.println(modname + " is already installed!");
			return;
		}
		
		File originalfile = new File(execdir + "/Data/CMAN-archive/" + modname + ".json");
		File newfile = new File(execdir + "/LocalData/ModsDownloaded/" + modname + ".installed");
		if(!new File(execdir + "/LocalData/ModsDownloaded/").exists())
		{
			new File(execdir + "/LocalData/ModsDownloaded/").mkdirs();
		}
		try 
		{
			Files.copy(originalfile.toPath(), newfile.toPath());
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("Couldn't find \"" + modname + ".json\". Please update CMAN-Archive");
		} 
		catch (IOException e) 
		{
			System.out.println(execdir);
			System.out.println("Couldn't find \"" + modname + ".json\" or something went horribly wrong.");
			e.printStackTrace();
		}
		
		String[] requirements = util.get_deps(modname);
		for (String requirement : requirements)
		{
			if(!new File(execdir + "/LocalData/ModsDownloaded/" + requirement + ".installed").exists())
			{
				System.out.println("You must install " + requirement + " first!");
				System.out.print("Do you want to install it? (y or n)");
				if(input.nextLine().equals("y"))
				{
					install_mod(requirement);
				}
				else
				{
					return;
				}
				
			}
		}
		ArrayList<String> recommendations = gson.fromJson(json_data.get("Recommended").getAsJsonArray().toString(), new TypeToken<ArrayList<String>>(){}.getType());
		for (String recommendation : recommendations)
		{
			if(!new File(execdir + "/LocalData/ModsDownloaded/" + recommendation + ".installed").exists())
			{
				System.out.println(modname + " recommends installing " + recommendation + "!");
			}
			System.out.print("Do you want to install it? (y or n)");
			if(input.nextLine().equals("y"))
			{
				install_mod(recommendation);
			}
		}
		
		ArrayList<String> incompatibilities = gson.fromJson(json_data.get("Incompatibilities").getAsJsonArray().toString(), new TypeToken<ArrayList<String>>(){}.getType());
		for(String incompatibility : incompatibilities)
		{
			if(new File(execdir + "/LocalData/ModsDownloaded/" + incompatibility + ".installed").exists())
			{
				System.out.println("You cannot have " + incompatibility + " and " + modname + " installed at the same time!");
				return;
			}
		}
		
		if(modtype.getAsString().equals("Basemod"))
		{
			System.out.println("Basemod install not currently supported in Java version.");
		}
		else if(modtype.getAsString().equals("Forge"))
		{
			System.out.println("forge");
			String url = json_data.get("Link").getAsString();
			String version = json_data.get("Version").getAsString();
			System.out.println(modname + " is at version " + version);
			String file_name = modname + "-" + version + ".jar";
			URL link;
			System.out.println("Downloading " + url + " as " + file_name);
			try 
			{
				link = new URL(url);
				ReadableByteChannel rbc = Channels.newChannel(link.openStream());
				FileOutputStream fos = new FileOutputStream(modfolder + "/" + file_name);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				System.out.println("Done");
			} 
			catch (MalformedURLException e) 
			{
				System.out.println("Something is wrong with the url, Please update the CMAN Archive");
				e.printStackTrace();
			}
			catch (FileNotFoundException e) 
			{
				System.out.println("Could not find the file, Please update the CMAN Archive");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else if(modtype.getAsString().equals("Liteloader"))
		{
			String url = json_data.get("Link").getAsString();
			String version = json_data.get("Version").getAsString();
			System.out.println(modname + " is at version " + version);
			String file_name = modname + "-" + version + ".litemod";
			URL link;
			System.out.println("Downloading " + url + " as " + file_name);
			try 
			{
				link = new URL(url);
				ReadableByteChannel rbc = Channels.newChannel(link.openStream());
				FileOutputStream fos = new FileOutputStream(modfolder + "/" + file_name);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				System.out.println("Done");
			} 
			catch (MalformedURLException e) 
			{
				System.out.println("Something is wrong with the url, Please update the CMAN Archive");
			}
			catch (FileNotFoundException e) 
			{
				System.out.println("Could not find the file, Please update the CMAN Archive");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else if(modtype.getAsString().equals("Installer"))
		{
			String url = json_data.get("Link").getAsString();
			String version = json_data.get("Version").getAsString();
			System.out.println(modname + " is at version " + version);
			String file_name = json_data.get("InstallerName").getAsString();
			URL link;
			System.out.println("Downloading " + url + " as " + file_name);
			try 
			{
				link = new URL(url);
				ReadableByteChannel rbc = Channels.newChannel(link.openStream());
				FileOutputStream fos = new FileOutputStream(modfolder + "/" + file_name);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				System.out.println("Done. Please run the installer.");
			} 
			catch (MalformedURLException e) 
			{
				System.out.println("Something is wrong with the url, Please update the CMAN Archive");
			}
			catch (FileNotFoundException e) 
			{
				System.out.println("Could not find the file, Please update the CMAN Archive");
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	/**
	Installs requirements for a mod but not the mod itself.
	*/
	public void install_deps(String modname)
	{
		String[] deps = util.get_deps(modname);
		for (String dep : deps)
		{
			if(!util.mod_installed(dep))
			{
				install_mod(dep);
			}
		}
	}
}
