package cman;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

public class CMAN_util 
{
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public static Scanner input = new Scanner(System.in);
	
	/**
	Initialization for util.
	*/
	public void init_config_util(String mf, String vf, String ed)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
	}
	
	/**
	Reads and creates configuration file. Returns a String[] with two elements, modfolder and versionfolder.
	*/
	public static String[] read_config()
	{
		String newLine = System.getProperty("line.separator");
		String mFolder = "@ERROR@";
		String vFolder = "@ERROR@";
		String path = CMAN_util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		String decodedPath = System.getProperty("user.dir");
		try 
		{
			decodedPath = URLDecoder.decode(path, "UTF-8");
		} 
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
		
		decodedPath.substring(0, decodedPath.length() - 13);
		
		File jsonfile = new File(decodedPath + "LocalData/config.json");
		Gson gson = new Gson();
		if(jsonfile.exists())
		{
			JsonReader reader;
			try 
			{
	            JsonParser parser = new JsonParser();
	            JsonElement jsonElement = parser.parse(new FileReader(jsonfile.getAbsoluteFile()));
	            JsonObject j = jsonElement.getAsJsonObject();
	            JsonElement mfolder = j.get("modfolder");
	            if(!mfolder.isJsonNull())
	            {
	            	mFolder = mfolder.getAsString();
	            }
	            else
	            {
	            	System.out.println("Enter mod folder location (absolute path): ");
	            	mFolder = input.nextLine();
	            	JsonElement mfelement = new JsonParser().parse(mFolder);
	            	j.add("modfolder", mfelement);
	            }
	            JsonElement vfolder = j.get("versionfolder");
	            if(!vfolder.isJsonNull())
	            {
	            	mFolder = vfolder.getAsString();
	            }
	            else
	            {
	            	System.out.println("Enter mod version folder location (absolute path): ");
	            	vFolder = input.nextLine();
	            	JsonElement vfelement = new JsonParser().parse(vFolder);
	            	j.add("versionfolder", vfelement);
	            }
	            
	            FileWriter fw = new FileWriter(jsonfile, false);
	            fw.write(gson.toJson(j));
	            fw.close();
			}
			catch (FileNotFoundException e) 
			{
				e.printStackTrace();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		else
		{
			System.out.println("Config file not found.");
			System.out.print("Enter mod folder location (absolute path): ");
			mFolder = input.nextLine();
			System.out.print("Enter mod versions folder location (absolute path): ");
			vFolder = input.nextLine();
            
            try 
            {
            	FileWriter fw = new FileWriter(jsonfile, false);
				fw.write("{\"modfolder\":\"" + mFolder + ",\"versionsfolder\":\"" + vFolder + "\"}");
				fw.close();
			} 
            catch (IOException e) 
            {
				e.printStackTrace();
			}
            
		}
		
		return new String[] {mFolder, vFolder};
	}
	
	/**
	Returns the JsonObject file for the modname given.
	*/
	public JsonObject get_json(String modname)
	{
		if(!Files.exists(Paths.get(execdir + "/DATA/CMAN-Archive"), LinkOption.NOFOLLOW_LINKS))
		{
			System.out.println("CMAN archive not found. Please update the CMAN archive");
			return null;
		}
		File jsonfile = new File(execdir + "/DATA/CMAN-Archive" + modname + ".json");
		if(jsonfile.exists())
		{
            JsonParser parser = new JsonParser();
            JsonElement jsonElement;
			try 
			{
				jsonElement = parser.parse(new FileReader(jsonfile.getAbsoluteFile()));
				JsonObject j = jsonElement.getAsJsonObject();
				return j;
			} 
			catch (JsonIOException e) 
			{
				System.out.println("The JSON file \"" + modname + ".json\"" + "appears to be invalid. Please update the CMAN archive.");
				return null;
			} 
			catch (JsonSyntaxException e) 
			{
				System.out.println("The JSON file \"" + modname + ".json\"" + "appears to be invalid. Please update the CMAN archive.");
				return null;
			} 
			catch (FileNotFoundException e) 
			{
				System.out.println("\"" + modname + "\"" + "doesn't exist.");
				return null;
			}
		}
		else
		{
			System.out.println("\"" + modname + "\"" + "doesn't exist.");
			return null;
		}
	}
	
	/**
	Returns the JsonObject file for the installed version of the modname given. Mod must be installed.
	*/
	public JsonObject get_installed_json(String modname)
	{
		if(!Files.exists(Paths.get(execdir + "/LocalData/ModsDownloaded"), LinkOption.NOFOLLOW_LINKS))
		{
			return null;
		}
		
		File jsonfile = new File(execdir + "/LocalData/ModsDownloaded" + modname + ".installed");
		if(jsonfile.exists())
		{
            JsonParser parser = new JsonParser();
            JsonElement jsonElement;
			try 
			{
				jsonElement = parser.parse(new FileReader(jsonfile.getAbsoluteFile()));
				JsonObject j = jsonElement.getAsJsonObject();
				return j;
			} 
			catch (JsonIOException e) 
			{
				System.out.println("The JSON file \"" + modname + ".installed\"" + "appears to be invalid. Using the CMAN archive.");
				return null;
			} 
			catch (JsonSyntaxException e) 
			{
				System.out.println("The JSON file \"" + modname +  ".installed\"" + "appears to be invalid. Using the CMAN archive.");
				return null;
			} 
			catch (FileNotFoundException e) 
			{
				System.out.println("\"" + modname + "\"" + "doesn't exist.");
				return null;
			}
		}
		else
		{
			System.out.println("\"" + modname + "\"" + "doesn't exist.");
			return null;
		}
	}
	
	/**
	Returns a boolean for if the modname has been installed already.
	*/
	public boolean mod_installed(String modname)
	{
		if(!Files.exists(Paths.get(execdir + "/LocalData/ModsDownloaded"), LinkOption.NOFOLLOW_LINKS))
		{
			return false;
		}
		
		if(new File(execdir + "/LocalData/ModsDownloaded" + modname + ".installed").exists())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	/**
	Returns an array of the json files for all of the installed mods.
	*/
	public JsonObject[] get_installed_jsons()
	{
		File[] jsons = new File(execdir + "/LocalData/ModsDownloaded").listFiles();
		String[] names = new String[jsons.length];
		JsonObject[] json = new JsonObject[jsons.length];
		int dirlength = new String(execdir + "/LocalData/ModsDownloaded/").length();
		int i = 0;
		for(File f : jsons)
		{
			names[i] = jsons[i].getAbsolutePath().substring(dirlength, jsons[i].getAbsolutePath().length() - 10);
			i++;
		}
		i = 0;
		for(String n : names)
		{
			json[i] = get_json(n);
			i++;
		}
		return json;
	}
	
	/**
	Lists all of the installed mods in the console.
	*/
	public void listmods()
	{
		System.out.println("Installed mods:");
		for(int i = 0; i < get_installed_jsons().length; i++)
		{
			System.out.println(get_installed_jsons()[i].get("Name").getAsString());
		}
	}
	
	/**
	Merges two directories.
	*/
	public static void mergedirs(File dir1, File dir2)
	{
		String targetDirPath = dir1.getAbsolutePath();
		File[] files = dir2.listFiles();
		for (File file : files) 
		{
			file.renameTo(new File(targetDirPath+File.separator+file.getName()));
		}
	}
	
	/**
	Changes the name of an installed json.
	*/
	public void fix_names(String path, String oldname, String name)
	{
		File jar = new File(path + oldname + ".jar");
		File json = new File(path + oldname + ".json");
		jar.renameTo(new File(path + name + ".jar"));
		json.renameTo(new File(path + name + ".json"));
		File jsonfile = new File(path + name + ".json");
        JsonParser parser = new JsonParser();
        JsonElement jsonElement;
		try 
		{
			jsonElement = parser.parse(new FileReader(jsonfile.getAbsoluteFile()));
	        JsonObject j = jsonElement.getAsJsonObject();
	        j.addProperty("id", name);
		} 
		catch (JsonIOException e) 
		{
			e.printStackTrace();
		} 
		catch (JsonSyntaxException e) 
		{
			e.printStackTrace();
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println("File \"" + name + ".json\" could not be found.");
		}
	}
	
	/**
	Separates a String[] into one string with commas.
	*/
	public String display_versions(String[] versions)
	{
		String versionstr = "";
		for (String version : versions)
		{
			versionstr = versionstr + version + ", ";
		}
		return versionstr.substring(0, versionstr.length() - 2);
	}
	
	/**
	Returns an array of all requirements of modname.
	*/
	public String[] get_deps(String modname)
	{
		JsonObject json_data = get_json(modname);
		String[] deps = null;
		if(json_data != null)
			{
			JsonArray array = json_data.getAsJsonArray("Requirements");
			deps = new String[array.size()];
			for(int i = 0; i < array.size(); i++)
			{
				deps[i] = array.get(i).getAsString();
			}
		}
		return deps;
	}
}
