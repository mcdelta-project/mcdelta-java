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

import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.codehaus.plexus.util.FileUtils;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

/**
 *
 * @author CMAN Team
 */
public class CMAN_install 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	//public static Scanner CMAN.input = new Scanner(System.in);
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
			modname = CMAN.input.nextLine();
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
			String temp = CMAN.input.nextLine();
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
				if(CMAN.input.nextLine().equals("y"))
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
			if(CMAN.input.nextLine().equals("y"))
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
			String url = json_data.get("Link").getAsString();
			String version = json_data.get("Version").getAsString();
			String mcversions = json_data.get("MCVersion").getAsString();
			System.out.println(modname + " is at version " + version);
			String file_name = modname + "-" + version + "CMANtemp.zip";
			URL link;
			System.out.println("Downloading " + url + " as " + file_name);
			try 
			{
				link = new URL(url);
				ReadableByteChannel rbc = Channels.newChannel(link.openStream());
				if(!new File(execdir + "/Data/temp").exists())
				{
					new File(execdir + "/Data/temp").mkdirs();
				}
				FileOutputStream fos = new FileOutputStream(execdir + "/Data/temp/" + file_name);
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
			new File(execdir + "/Data/temp/" + file_name.substring(0, file_name.length() - 4)).mkdir();
			try 
			{
				util.unzip(execdir + "/Data/temp/" + file_name, execdir + "/Data/temp/" + file_name.substring(0, file_name.length() - 4));
			} 
			catch (IOException e) 
			{
				System.out.println("Couldn't create file.");
				e.printStackTrace();
				return;
			}
			String vname;
			String vpath;
			System.out.print("Enter name (as displayed in launcher) of minecraft instance to install into (compatible versions: "+mcversions+"): ");
			vname = CMAN.input.nextLine();
			vpath = versionsfolder + vname;
			while(!new File(vpath).exists())
			{
				System.out.print("Enter name (as displayed in launcher) of minecraft instance to install into (compatible versions: "+mcversions+"): ");
				vname = CMAN.input.nextLine();
				vpath = versionsfolder + vname;
			}
			String foldername = modname + "-" + version;
			System.out.print("Enter install folder name or leave blank for default (default: "+foldername+"): ");
			String foldernamefinal = CMAN.input.nextLine();
			if(foldernamefinal.equals(""))
			{
				foldernamefinal = foldername;
			}
			String newjarname = foldernamefinal + ".jar";
			System.out.println("Installing on version "+vname+" as "+foldernamefinal+".");
			if(new File(versionsfolder + foldernamefinal).exists())
			{
				System.out.print("The folder "+foldernamefinal+" already exists. Type OK to overwrite, or anything else to choose a new name: ");
				if(CMAN.input.nextLine().equals("OK"))
				{
					try 
					{
						util.delete_recursivly(versionsfolder + foldernamefinal);
					} 
					catch (IOException e) 
					{
						System.out.println("Couldn't delete the file");
						return;
					}
				}
				else
				{
					System.out.print("Enter new install folder name (current name: "+foldernamefinal+"): ");
					foldernamefinal = CMAN.input.nextLine();
				}
				String folderpath = versionsfolder + foldernamefinal;
				try 
				{
					FileUtils.copyDirectoryStructure(new File(vpath), new File(folderpath));
				} 
				catch (IOException e) 
				{
					System.out.println("Couldn't copy to " + folderpath);
					return;
				}
				util.fix_names(folderpath, vname, foldernamefinal);
				try 
				{
					util.unzip(versionsfolder + foldernamefinal + newjarname, execdir + "Data/temp/" + file_name + "/CMANtemp");
				} 
				catch (IOException e) 
				{
					System.out.println("Couldn't unzip");
					return;
				}
				util.mergedirs(new File(execdir + "Data/temp/" + modname), new File(execdir + "Data/temp/" + file_name + "/CMANtemp"));
				try 
				{
					util.delete_recursivly(execdir + "/Data/temp/" + file_name + "/CMANtemp" + "/META-INF");
				} 
				catch (IOException e) 
				{
					System.out.println("Couldn't delete META-INF");
					return;
				}
				System.out.println("Making jar (this might take a while).");
				try 
				{
					util.zipDir(execdir + "/Data/temp/" + foldername + ".zip", execdir + "/Data/temp/" + file_name + "/CMANtemp");
				} 
				catch (Exception e)
				{
					System.out.println("Couldn't make the zip.");
					return;
				}
				try 
				{
					Files.move(Paths.get(execdir + "/Data/temp/" + foldername + ".zip"), Paths.get(folderpath + "/" + ".jar"));
				}
				catch (IOException e) 
				{
					System.out.println("Couldn't move file.");
				}
				System.out.println("Done.");
			}
		}
		else if(modtype.getAsString().equals("Forge"))
		{
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
				String tempdir = execdir + "/LocalData/tmp";
				if(!new File(tempdir).exists())
				{
					new File(tempdir).mkdir();
				}
				FileOutputStream fos = new FileOutputStream(tempdir + "/" + file_name);
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
				System.out.println("Done. Please run the installer at: " + tempdir.replace("\\", "/") + "/" + file_name);
				Desktop d;
				try
				{
					if(CMAN.input.v != null)
					{
						d = Desktop.getDesktop();
						d.open(new File(tempdir));
					}
				}
				catch (IOException e)
				{
					System.out.println("Couldn't open a file manager.");
				}
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
