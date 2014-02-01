package me.iPedro2.circleTP;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocaleHandler {

	private HashMap<String,String> strings = new HashMap<String,String>();
	private CircleTP plugin;
	//private File l10nFolder = new File(plugin.getDataFolder() + File.separator + "l10n");

	public LocaleHandler(CircleTP plugin){
		this.plugin = plugin;
	}
	
	private File loadLocaleFromJar(String locale) throws IOException{
		InputStream in = plugin.getResource(locale + ".yml");
		OutputStream out = new FileOutputStream(plugin.getDataFolder() + File.separator + "l10n" + File.separator + locale + ".yml");
		
		if(in == null)
			locale = "en-US";
		
		int read = 0; //copying from one place to the other, byte by byte
		byte[] bytes = new byte[1024];
		while ((read = in.read(bytes)) != -1) {
			out.write(bytes, 0, read);
		}
		out.flush(); //write the changes
		if (in != null) { //closes the inputstream
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (out != null) { //closes the outputstream
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return new File(plugin.getDataFolder() + File.separator + "l10n" + locale + ".yml");
		
	}
	
	public FileConfiguration checkFileExistence(String locale, boolean mainLoad, boolean stringCheck){
		File l10nFolder = new File(plugin.getDataFolder() + File.separator + "l10n");
		File stringFile = new File(l10nFolder.getPath() + File.separator + locale + ".yml"); //the file itself
		FileConfiguration stringCfg = YamlConfiguration.loadConfiguration(stringFile); //file contents and manipulation
		if(!stringFile.exists()){
			try{
				InputStream in = plugin.getResource(locale + ".yml"); //gets the selected locale file from the jar archive
				if(in == null && mainLoad && !stringCheck){
					plugin.log.info("The locale " + locale + " was not found. Using default en-US.");
					locale = "en-US";
					plugin.locale = "en-US";
					plugin.getConfig().set("locale", "en-US");
					plugin.saveConfig();
					in = plugin.getResource("en-US.yml"); //gets the en-US.yml file from the jar archive
				}else if(in == null && !mainLoad && !stringCheck)
					return null;
				else if(in == null && stringCheck){
					locale = "en-US";
					in = plugin.getResource("en-US.yml"); //gets the en-US.yml file from the jar archive
				}

				loadLocaleFromJar(locale);

			}catch(Exception e){
				e.printStackTrace();
			}
			stringFile = new File(l10nFolder.getPath() + File.separator + locale + ".yml");
		}
		try {
			stringCfg.load(stringFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		return stringCfg;
	}
	
	public void loadStrings(String locale, boolean mainLoad){
		File l10nFolder = new File(plugin.getDataFolder() + File.separator + "l10n");
		if(!l10nFolder.exists())
			l10nFolder.mkdir();
		
		FileConfiguration stringCfg = checkFileExistence(locale,true,false);
		
		addString("lorem-ipsum", "lorem-ipsum");
		
		addString("language-name", stringCfg.getString("language-name"));
		
		addString("help-header", stringCfg.getString("help-header"));
		addString("help-ctp", stringCfg.getString("help-ctp"));
		addString("help-ctp-other", stringCfg.getString("help-ctp-other"));
		addString("help-ctp-admin", stringCfg.getString("help-ctp-admin"));
		addString("help-ctp-setcenter", stringCfg.getString("help-ctp-setcenter"));
		addString("help-ctp-setmax", stringCfg.getString("help-ctp-setmax"));
		addString("help-ctp-setmin", stringCfg.getString("help-ctp-setmin"));
		addString("help-ctp-forceheight", stringCfg.getString("help-ctp-forceheight"));
		addString("help-ctp-tpmode", stringCfg.getString("help-ctp-tpmode"));
		addString("help-ctp-tell", stringCfg.getString("help-ctp-tell"));
		addString("help-ctp-cooldown", stringCfg.getString("help-ctp-cooldown"));
		addString("help-ctp-setlocale", stringCfg.getString("help-ctp-setlocale"));
		addString("help-ctp-reload", stringCfg.getString("help-ctp-reload"));
		addString("help-ctp-checkconfig", stringCfg.getString("help-ctp-checkconfig"));
		addString("help-ctp-version", stringCfg.getString("help-ctp-version"));
		
		addString("error-nopermissionself", stringCfg.getString("error-nopermissionself"));
		addString("error-nopermissionother", stringCfg.getString("error-nopermissionother"));
		addString("error-misconfig1", stringCfg.getString("error-misconfig1"));
		addString("error-misconfig2", stringCfg.getString("error-misconfig2"));
		addString("error-misconfig3", stringCfg.getString("error-misconfig3"));
		addString("error-misconfig4", stringCfg.getString("error-misconfig4"));
		addString("error-misconfig5", stringCfg.getString("error-misconfig5"));
		addString("error-misconfig6", stringCfg.getString("error-misconfig6"));
		addString("error-tpconsole", stringCfg.getString("error-tpconsole"));
		addString("error-ingamecmd", stringCfg.getString("error-ingamecmd"));
		addString("error-cooldownwait", stringCfg.getString("error-cooldownwait"));
		addString("error-notfound", stringCfg.getString("error-notfound"));
		addString("error-changecfg", stringCfg.getString("error-changecfg"));
		addString("error-invalidcmd", stringCfg.getString("error-invalidcmd"));
		addString("error-cooldown", stringCfg.getString("error-cooldown"));
		addString("error-minradius1", stringCfg.getString("error-minradius1"));
		addString("error-minradius2", stringCfg.getString("error-minradius2"));
		addString("error-maxradius1", stringCfg.getString("error-maxradius1"));
		addString("error-maxradius2", stringCfg.getString("error-maxradius2"));
		addString("error-height", stringCfg.getString("error-height"));
		addString("error-bool", stringCfg.getString("error-bool"));
		addString("error-toggle", stringCfg.getString("error-toggle"));
		addString("error-tpsigns", stringCfg.getString("error-tpsigns"));
		addString("error-noinfo", stringCfg.getString("error-noinfo"));
		addString("error-loading", stringCfg.getString("error-loading"));
		addString("error-vehicle", stringCfg.getString("error-vehicle"));
		addString("error-vehicle2", stringCfg.getString("error-vehicle2"));
		addString("error-vehicle-pig", stringCfg.getString("error-vehicle-pig"));
		addString("error-vehicle-minecart", stringCfg.getString("error-vehicle-minecart"));
		addString("error-vehicle-boat", stringCfg.getString("error-vehicle-boat"));
		addString("error-vehicle-horse", stringCfg.getString("error-vehicle-horse"));
		addString("error-locale1", stringCfg.getString("error-locale1"));
		addString("error-locale2", stringCfg.getString("error-locale2"));
		addString("error-moreinput", stringCfg.getString("error-moreinput"));
		
		addString("info-teleported", stringCfg.getString("info-teleported"));
		addString("info-teleported-other1", stringCfg.getString("info-teleported-other1"));
		addString("info-teleported-other2", stringCfg.getString("info-teleported-other2"));
		addString("info-teleported-other3", stringCfg.getString("info-teleported-other3"));
		addString("info-teleported-other4", stringCfg.getString("info-teleported-other4"));
		addString("info-teleported-log1", stringCfg.getString("info-teleported-log1"));
		addString("info-teleported-log2", stringCfg.getString("info-teleported-log2"));
		addString("info-setcenter", stringCfg.getString("info-setcenter"));
		addString("info-abstpdisabled", stringCfg.getString("info-abstpdisabled"));
		addString("info-cooldownoff", stringCfg.getString("info-cooldownoff"));
		addString("info-cooldownset1", stringCfg.getString("info-cooldownset1"));
		addString("info-cooldownset2", stringCfg.getString("info-cooldownset2"));
		addString("info-minradius", stringCfg.getString("info-minradius"));
		addString("info-maxradius", stringCfg.getString("info-maxradius"));
		addString("info-height1", stringCfg.getString("info-height1"));
		addString("info-height2", stringCfg.getString("info-height2"));
		addString("info-abstpon", stringCfg.getString("info-abstpon"));
		addString("info-abstpoff", stringCfg.getString("info-abstpoff"));
		addString("info-telloff", stringCfg.getString("info-telloff"));
		addString("info-tellon", stringCfg.getString("info-tellon"));
		addString("info-plugincfg-header", stringCfg.getString("info-plugincfg-header"));
		addString("info-plugincfg-minradius", stringCfg.getString("info-plugincfg-minradius"));
		addString("info-plugincfg-maxradius", stringCfg.getString("info-plugincfg-maxradius"));
		addString("info-plugincfg-cooldown", stringCfg.getString("info-plugincfg-cooldown"));
		addString("info-plugincfg-height", stringCfg.getString("info-plugincfg-height"));
		addString("info-plugincfg-tell", stringCfg.getString("info-plugincfg-tell"));
		addString("info-plugincfg-relativetp", stringCfg.getString("info-plugincfg-relativetp"));
		addString("info-plugincfg-center", stringCfg.getString("info-plugincfg-center"));
		addString("info-version", stringCfg.getString("info-version"));
		addString("info-author", stringCfg.getString("info-author"));
		addString("info-tpsign-create", stringCfg.getString("info-tpsign-create"));
		addString("info-nocfg", stringCfg.getString("info-nocfg"));
		addString("info-reloaded", stringCfg.getString("info-reloaded"));
		addString("info-localeset", stringCfg.getString("info-localeset"));
		addString("info-language", stringCfg.getString("info-language"));
		
		addString("string-deactivated", stringCfg.getString("string-deactivated"));
		addString("string-activated", stringCfg.getString("string-activated"));
		
		addString("sign-line1", stringCfg.getString("sign-line1"));
		addString("sign-line2", stringCfg.getString("sign-line2"));
		addString("sign-line3", stringCfg.getString("sign-line3"));
		
		loadFailedStrings(locale,stringCfg);
		
	}
	
	private void loadFailedStrings(String locale, FileConfiguration localeFileCfg){
		Iterator<Entry<String, String>> it = strings.entrySet().iterator();
		FileConfiguration engFileCfg = checkFileExistence("en-US",false,true);
		File localeFile = new File(plugin.getDataFolder() + File.separator + "l10n" + File.separator + locale + ".yml");
		boolean changes = false;
		
		while(it.hasNext()){
			String name = it.next().getKey();
			String string = getString(name);
			if(string == null){
				plugin.log.info("The string " + name + " was not present in the " + locale + ".yml file. Loading from en-US.yml");
				addString(name, engFileCfg.getString(name));
				localeFileCfg.set(name, engFileCfg.getString(name));
				changes = true;
			}
		}
		if(changes){
			try {
				localeFileCfg.save(localeFile);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String addString(String name, String string){
		strings.put(name, string);
		return getString(name);
	}

	private boolean hasString(String name){
		return strings.containsKey(name);
	}

	public String getString(String name){
		if(hasString(name)){
			return strings.get(name);
		}
		return null;
	}

}
