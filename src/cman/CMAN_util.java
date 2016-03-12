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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.codehaus.plexus.archiver.zip.ZipEntry;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 *
 * @author CMAN Team
 */
public class CMAN_util 
{
	public String error = "@ERROR@";
	public String modfolder = error;
	public String versionsfolder = error;
	public String execdir = error;
	public String instance = error;
	//static Inputs CMAN.input = CMAN.CMAN.input;
	
	/**
	Initialization for util.
	*/
	public void init_config_util(String mf, String vf, String ed, String i)
	{
		modfolder = mf;
		versionsfolder = vf;
		execdir = ed;
		instance = i;
	}
	
	public void delete_recursivly(String dir) throws IOException
	{
		if(new File(dir).exists())
		{
			Path directory = Paths.get(dir);
			Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}				
			});
		}
	}
	
	 public void zipDir(String zipFileName, String dir) throws Exception 
	 {
		 File dirObj = new File(dir);
		 ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFileName));
		 addDir(dirObj, out);
		 out.close();
	 }

	 static void addDir(File dirObj, ZipOutputStream out) throws IOException 
	 {
		 File[] files = dirObj.listFiles();
		 byte[] tmpBuf = new byte[1024];

		 for (int i = 0; i < files.length; i++) 
		 {
			 if (files[i].isDirectory()) 
			 {
				 addDir(files[i], out);
				 continue;
			 }
			 FileInputStream in = new FileInputStream(files[i].getAbsolutePath());
			 out.putNextEntry(new ZipEntry(files[i].getAbsolutePath()));
			 int len;
			 while ((len = in.read(tmpBuf)) > 0) 
			 {
				 out.write(tmpBuf, 0, len);
			 }
			 out.closeEntry();
			 in.close();
		 }
	 }
	
    public void unzip(String zipFilePath, String destDirectory) throws IOException {
        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            destDir.mkdir();
        }
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
        java.util.zip.ZipEntry entry = zipIn.getNextEntry();
        // iterates over entries in the zip file
        while (entry != null) {
            String filePath = destDirectory + File.separator + entry.getName();
            if (!entry.isDirectory()) {
                // if the entry is a file, extracts it
                extractFile(zipIn, filePath);
            } else {
                // if the entry is a directory, make the directory
                File dir = new File(filePath);
                dir.mkdir();
            }
            zipIn.closeEntry();
            entry = zipIn.getNextEntry();
        }
        zipIn.close();
    }
    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
	
	/**
	Reads and creates configuration file. Returns a String[] with two elements, modfolder and versionfolder.
	*/
	public String[] read_config()
	{
		String mFolder = error;
		String vFolder = error;
		String path = CMAN_util.class.getProtectionDomain().getCodeSource().getLocation().toString();
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
		
		File jsonfile = new File(execdir + "/LocalData/config.json");
		if(!new File(execdir + "/LocalData").exists())
		{
			new File(execdir + "/LocalData").mkdir();
		}
		Gson gson = new Gson();
		if(jsonfile.exists())
		{
			try 
			{
	            JsonParser parser = new JsonParser();
	            JsonElement jsonElement = parser.parse(new FileReader(jsonfile.getAbsoluteFile()));
	            JsonObject j = jsonElement.getAsJsonObject();
	            if(instance_exists(instance))
	            {
	            	JsonObject inst = j.getAsJsonObject(instance);
		            JsonElement mfolder = inst.get("modfolder");
		            if(inst.has("modfolder"))
		            {
		            	mFolder = mfolder.getAsString();
		            }
		            else
		            {
		            	System.out.println("Enter mod folder location (absolute path): ");
		            	mFolder = CMAN.input.nextLine();
		            	JsonElement mfelement = new JsonParser().parse(mFolder);
		            	j.getAsJsonObject(instance).add("modfolder", mfelement);
		            }
		            JsonElement vfolder = inst.get("versionsfolder");
		            if(inst.has("versionsfolder"))
		            {
		            	vFolder = vfolder.getAsString();
		            }
		            else
		            {
		            	System.out.println("Enter mod version folder location (absolute path): ");
		            	vFolder = CMAN.input.nextLine();
		            	JsonElement vfelement = new JsonParser().parse("\"" + vFolder + "\"");
		            	j.getAsJsonObject(instance).add("versionsfolder", vfelement);
		            }
		            
		            FileWriter fw = new FileWriter(jsonfile, false);
		            fw.write(gson.toJson(j));
		            fw.close();
	            }
	            else
	            {
	            	System.out.println("Instance \"" + instance + "\" not found.");
	            	System.out.print("Enter mod folder location (absolute path): ");
	            	mFolder = CMAN.input.nextLine();
	            	System.out.print("Enter mod version folder location (absolute path): ");
	            	vFolder = CMAN.input.nextLine();
	            	JsonObject inst = new JsonObject();
	            	System.out.println(mFolder);
	            	JsonElement mElement = new JsonParser().parse("\"" + mFolder + "\"");
	            	JsonElement vElement = new JsonParser().parse("\"" + vFolder + "\"");
	            	inst.add("modfolder", mElement);
	            	inst.add("versionsfolder", vElement);
	            	j.add(instance, inst);
		            FileWriter fw = new FileWriter(jsonfile, false);
		            fw.write(gson.toJson(j));
		            fw.close();
	            }
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
			mFolder = CMAN.input.nextLine();
			System.out.print("Enter mod versions folder location (absolute path): ");
			vFolder = CMAN.input.nextLine();
            
            try 
            {
            	FileWriter fw = new FileWriter(jsonfile, false);
				fw.write("{\"" + instance + "\":{\"modfolder\":\"" + mFolder + "\",\"versionsfolder\":\"" + vFolder + "\"}}");
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
		//System.out.println(execdir + "/Data/CMAN-Archive");
		if(!Files.exists(Paths.get(execdir + "/Data/CMAN-Archive"), LinkOption.NOFOLLOW_LINKS))
		{
			System.out.println("CMAN archive not found. Please update the CMAN archive");
			return null;
		}
		File jsonfile = new File(execdir + "/Data/CMAN-Archive/" + modname + ".json");
		if(jsonfile.exists())
		{
            JsonParser parser = new JsonParser();
            JsonElement jsonElement;
			try 
			{
				FileReader r = new FileReader(jsonfile.getAbsoluteFile());
				jsonElement = parser.parse(r);
				JsonObject j = jsonElement.getAsJsonObject();
				r.close();
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
				System.out.println("\"" + modname + "\"" + " doesn't exist.");
				return null;
			} catch (IOException e) 
			{
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
		if(!Files.exists(Paths.get(execdir + "/LocalData/ModsDownloaded/" + instance), LinkOption.NOFOLLOW_LINKS))
		{
			return null;
		}
		
		File jsonfile = new File(execdir + "/LocalData/ModsDownloaded/" + instance + "/" + modname + ".installed");
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
		if(!Files.exists(Paths.get(execdir + "/LocalData/ModsDownloaded/" + instance), LinkOption.NOFOLLOW_LINKS))
		{
			return false;
		}
		if(new File(execdir + "/LocalData/ModsDownloaded/" + instance + "/" + modname + ".installed").exists())
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public JsonObject[] get_installed_jsons()
	{
		return get_installed_jsons(instance);
	}
	
	/**
	Returns an array of the json files for all of the installed mods.
	*/
	public JsonObject[] get_installed_jsons(String inst)
	{
		if(new File(execdir + "/LocalData/ModsDownloaded/" + inst).exists())
		{
			File[] jsons = new File(execdir + "/LocalData/ModsDownloaded/" + inst).listFiles();
			String[] names = new String[jsons.length];
			JsonObject[] json = new JsonObject[jsons.length];
			int dirlength = new String(execdir + "/LocalData/ModsDownloaded/" + inst + "/").length();
			int i = 0;
			for(File f : jsons)
			{
				names[i] = f.getAbsolutePath().substring(dirlength, jsons[i].getAbsolutePath().length() - 10);
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
		return null;
	}
	
	/**
	Lists all of the installed mods in the console.
	*/
	public void listmods()
	{
		System.out.println("Installed mods:");
		if(get_installed_jsons() != null)
		{
			for(int i = 0; i < get_installed_jsons().length; i++)
			{
				System.out.println("        " + get_installed_jsons()[i].get("Name").getAsString());
			}
		}
		else
		{
			System.out.println("        " + "None");
		}
	}
	
	/**
	Merges two directories.
	*/
	public void mergedirs(File dir1, File dir2)
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
	
	public boolean instance_exists(String inst)
	{
		
		File jsonfile = new File(execdir + "/LocalData/config.json");
		if(jsonfile.exists())
		{
            JsonParser parser = new JsonParser();
            JsonElement jsonElement;
			try 
			{
				jsonElement = parser.parse(new FileReader(jsonfile.getAbsoluteFile()));
				JsonObject j = jsonElement.getAsJsonObject();
				if(j.has(inst))
				{
					return true;
				}
			} 
			catch (JsonIOException e) 
			{
				System.out.println("The config JSON appears to be invalid. Delete it and run CMAN again.");
				return false;
			} 
			catch (JsonSyntaxException e) 
			{
				System.out.println("The config JSON appears to be invalid. Delete it and run CMAN again.");
				return false;
			} 
			catch (FileNotFoundException e) 
			{
				System.out.println("\"config.json\" " + "doesn't exist.");
				return false;
			}
		}
		return false;
	}
	
	public String[] new_config(String _instance)
	{
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonElement jsonElement;
		try 
		{
			jsonElement = parser.parse(new FileReader(execdir + "/LocalData/config.json"));
	        JsonObject j = jsonElement.getAsJsonObject();
	        if(!j.has(_instance))
	        {
	        	System.out.print("Enter mod folder (absolute path): ");
	        	String mf = CMAN.input.nextLine();
	        	JsonElement mfelement = new JsonParser().parse(mf);
	        	System.out.print("Enter versions folder (absolute path): ");
	        	String vf = CMAN.input.nextLine();
	        	JsonElement vfelement = new JsonParser().parse(vf);
	        	JsonObject instobj = new JsonObject();
	        	instobj.add("modfolder", mfelement);
	        	instobj.add("versionsfolder", vfelement);
	        	j.add(_instance, instobj);
	            FileWriter fw = new FileWriter(execdir + "/LocalData/config.json", false);
	            fw.write(gson.toJson(j));
	            fw.close();
	            System.out.println("Done");
	            return new String[] {mf, vf};
	        }
		} 
		catch (JsonIOException | JsonSyntaxException | IOException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public void rm_config(String _instance)
	{
		if(instance.equals(_instance))
		{
			System.out.println("Cannot remove instance while it is active! Select another instance first.");
			return;
		}
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonElement jsonElement;
		try 
		{
			jsonElement = parser.parse(new FileReader(execdir + "/LocalData/config.json"));
	        JsonObject j = jsonElement.getAsJsonObject();
	        if(j.has(_instance))
	        {
	        	j.remove(_instance);
	            FileWriter fw = new FileWriter(execdir + "/LocalData/config.json", false);
	            fw.write(gson.toJson(j));
	            fw.close();
	            System.out.println("Done");
	        }
		} 
		catch (JsonIOException | JsonSyntaxException | IOException e) 
		{
			e.printStackTrace();
		}
	}
}
