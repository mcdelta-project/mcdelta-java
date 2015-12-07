package cman;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;

import com.google.gson.JsonObject;

public class CMAN 
{
	String version = "0.6.0";
	
	public String modfolder = "@ERROR@";
	public String versionsfolder = "@ERROR@";
	public String execdir = "@ERROR@";
	public static Scanner input = new Scanner(System.in);
	CMAN_util util = new CMAN_util();
	CMAN_install install = new CMAN_install();
	CMAN_remove remove = new CMAN_remove();
	
	public void delete_recursivly(String dir) throws IOException
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
	
	public void check_for_updates()
	{
		URL url;
		try 
		{
			url = new URL("http://raw.githubusercontent.com/Comprehensive-Minecraft-Archive-Network/CMAN-Java/master/version.txt");
			Scanner s = new Scanner(url.openStream());
			String latestversion = s.nextLine();
			if(latestversion != version)
			{
				System.out.println("WARNING! YOU ARE USING OLD VERSION " + version + "! NEWEST VERSION IS " + latestversion + "!");
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
		}	
	}
	
	public void update_archive()
	{
		if(new File(execdir + "/Data/CMAN-Archive").exists())
		{
			try 
			{
				delete_recursivly(execdir + "/Data/CMAN-Archive");
			} catch (IOException e) {e.printStackTrace();}
		}
		new File(execdir + "/Data").mkdir();
		//new File(execdir + "/Data/CMAN-Archive").mkdir();
		URL url;
		String file_name = "CMAN.tar.gz";
		try 
		{
			System.out.println("Downloading Archive");
			url = new URL("https://github.com/Comprehensive-Minecraft-Archive-Network/CMAN-Archive/archive/master.zip");
			ReadableByteChannel rbc = Channels.newChannel(url.openStream());
			FileOutputStream fos = new FileOutputStream(execdir + "/Data/" + file_name);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
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
		System.out.println("Extracting " + sourceFile.getName() + " to " + endDir.getName());
		UnArchiver ua = new TarGZipUnArchiver(sourceFile);
		ua.setSourceFile(sourceFile);
		destDir.mkdirs();
		ua.setDestDirectory(destDir);
		ua.extract("CMAN-Archive-master/", destDir);
		endDir.renameTo(new File(execdir + "/Data/CMAN-Archive"));
		System.out.println("Renamed CMAN-Archive-master to CMAN-Archive");
		sourceFile.delete();
		System.out.println("Deleting CMAN.tar.gz");
		System.out.println("Done");
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
			System.out.println(" Version: " + json_data.get("Version").getAsString() + " (" + stable + ")");
			System.out.println(" Author(s): " + json_data.get("Author").getAsString());
			System.out.println(" Description: " + json_data.get("Desc").getAsString());
			System.out.println(" Requirements: " + reqs);
			System.out.println(" Known Incompatibilities: " + incomp);
			System.out.println(" Download Link: " + json_data.get("Link").getAsString());
			System.out.println(" License: " + json_data.get("License").getAsString());
		}
	}
	
	public static void main(String[] args) 
	{
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
		CMAN cman = new CMAN();
		cman.execdir = decodedPath;
		System.out.println(decodedPath);
		//cman.update_archive();
		cman.util.execdir = cman.execdir;
		String[] places = cman.util.read_config();
		cman.util.init_config_util(places[0], places[1], cman.execdir);
		cman.install.init_config_install(places[0], places[1], cman.execdir);
		cman.remove.init_config_remove(places[0], places[1], cman.execdir);
		//cman.get_info("DeeperCaves");
		//cman.remove.remove_mod("DeeperCaves");
		//cman.install.install_mod("DeeperCaves");
		//System.out.println("Something");
	}

}
