package me.iPedro2.circleTP;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

public class CircleTP extends JavaPlugin {

	public static CircleTP plugin;
	public String prefix = (ChatColor.GREEN + "[CTP] " + ChatColor.RESET);
	public String tab = ("     ");
	public int minRadius;
	public int maxRadius;
	public int height;
	public int[] lastTpCoord = new int[3];
	public World lastTpWorld;
	public boolean relativeTP;
	public boolean tell;
	public boolean misconfig = false;
	public boolean metrics = true;
	public Location center = new Location(null, 0, 0, 0);
	Logger log = Bukkit.getLogger();
	File configFile = new File(getDataFolder() + "config.yml");

	@Override
	public void onDisable() {
		try {
			this.getConfig().set("minRadius", minRadius);
			this.getConfig().set("maxRadius", maxRadius);
			this.getConfig().set("forceHeight", height);
			this.getConfig().set("relativeTP", relativeTP);
			this.getConfig().set("tell", tell);
			this.getConfig().set("center.x", (int) center.getX());
			this.getConfig().set("center.z", (int) center.getZ());
			this.getConfig().set("center.world", center.getWorld().getName());
			this.getConfig().set("metrics", metrics);
			this.saveConfig();
		} catch (final Exception e) {
			log.info("Error saving config file.");
			e.printStackTrace();
		}
	}

	@Override
	public void onEnable() {
		if (!(configFile.exists())) {
			log.info("No config file detected. Creating a new one.");
			this.saveDefaultConfig();
		}
		try{
			minRadius = Integer.valueOf(this.getConfig().getInt("minRadius"));
			maxRadius = Integer.valueOf(this.getConfig().getInt("maxRadius"));
			height = Integer.valueOf(this.getConfig().getInt("forceHeight"));
			relativeTP = Boolean.valueOf(this.getConfig().getBoolean("relativeTP"));
			tell = Boolean.valueOf(this.getConfig().getBoolean("tell"));
			center.setX(Double.valueOf(this.getConfig().getDouble("center.x")));
			center.setZ(Double.valueOf(this.getConfig().getDouble("center.z")));
			center.setWorld(this.getServer().getWorld(this.getConfig().getString("center.world")));
			metrics = Boolean.valueOf(this.getConfig().getBoolean("metrics"));
		}catch(final Exception e){
			log.info("Error loading config file.");
		}
		if(metrics){
			try {
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
			} catch (IOException e) {
				//nothing
			}
		}
		if(minRadius > maxRadius && maxRadius != 0){
			log.severe("MINUMUM RADIUS IS GREATER THAN MAXIMUM RADIUS!");
			log.severe("Use \"/ctpadmin checkconfig\" to check the radii and then \"/ctpadmin setminradius/setmaxradius\" to change them.");
			log.severe("When editing the config.yml file, you must make sure that minRadius < maxRadius, otherwise the plugin will not understand where to teleport the player.");
			log.severe("The plugin will not work with misconfigurations.");
			misconfig = true;
		}
		if(height < -1){
			height = -1;
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
		commandLabel.toLowerCase();
		if (commandLabel.equalsIgnoreCase("ctp")) {
			if(args.length > 0){
				teleportOther(sender,args[0]);
			}else{
				teleportSelf(sender);
			}
		} else if (commandLabel.equalsIgnoreCase("ctpadmin")) {
			if(args.length < 1){
				sender.sendMessage(prefix + ChatColor.RED + "Not enough arguments.");
			}else{
				admin(sender,args);
			}
		} else if(commandLabel.equalsIgnoreCase("ctpver")){
			displayVersion(sender);			
		}
		return false;
	}

	public void teleportSelf(CommandSender sender){
		if(sender.hasPermission("CircleTP.ctp")){
			if(misconfig == true){
				sender.sendMessage(prefix + ChatColor.RED + "The plugin is misconfigured and cannot be used (minRadius > maxRadius).");
				sender.sendMessage(prefix + ChatColor.RED + "If you're an admin, please correct this situation.");
			}else{
				if (!(sender instanceof Player)) {
					sender.sendMessage(prefix + ChatColor.GOLD + "You are the console. How do you expect me to teleport you?");
				} else {
					Player issuer = (Player) sender;
					issuer.teleport(getRandomLocation(issuer));
					issuer.sendMessage(prefix + ChatColor.GOLD + "You have been teleported to a random location.");
					log.info(issuer.getName() + " has been teleported to: " + lastTpWorld.getName() + ", (" + lastTpCoord[0] + "," + lastTpCoord[1] + "," + lastTpCoord[2] + ")");
				}
			}
		}else{
			sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to teleport yourself.");
		}
	}

	public void teleportOther(CommandSender sender, String targetName){
		if(sender.hasPermission("CircleTP.ctp.others")){
			if(misconfig == true){
				sender.sendMessage(prefix + ChatColor.RED + "The plugin is misconfigured and cannot be used (minRadius > maxRadius).");
				sender.sendMessage(prefix + ChatColor.RED + "If you're an admin, please correct this situation.");
			}else{
				if (sender.getServer().getPlayer(targetName) == null) {
					sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
				} else {
					Player target = sender.getServer().getPlayer(targetName);
					if(targetName.equalsIgnoreCase(sender.getServer().getName()) && !sender.hasPermission("CircleTP.ctp")){
						sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to teleport yourself.");
					}else{
						target.teleport(getRandomLocation(target));
						sender.sendMessage(prefix + ChatColor.GOLD + "You have teleported " + target.getName() + " to a random location.");
						if(tell == false){
							target.sendMessage(prefix + ChatColor.GOLD + "You have been teleported to a random location.");
						}else{
							target.sendMessage(prefix + ChatColor.GOLD + "You have been teleported to a random location by " + sender.getName() + ".");
						}
						log.info(sender.getName() + " has teleported " + target.getName() + " to: " + lastTpWorld.getName() + ", (" + lastTpCoord[0] + "," + lastTpCoord[1] + "," + lastTpCoord[2] + ")");
					}
				}
			}
		}else{
			sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to teleport other players.");
		}
	}

	public void setCenter(CommandSender sender){
		if (!(sender instanceof Player)) {
			sender.sendMessage(prefix + ChatColor.RED + "This command must be run by a in-game player.");
		} else {
			Player issuer = (Player) sender;
			center = issuer.getLocation();
			sender.sendMessage(prefix + ChatColor.GOLD + "Center defined to: " + center.getWorld().getName() + ", (" + (int) center.getX() + "," + (int) center.getY() + "," + (int) center.getZ() + ").");
			this.getConfig().set("center.x", (int) center.getX());
			this.getConfig().set("center.z", (int) center.getZ());
			this.getConfig().set("center.world", center.getWorld().getName());
			this.saveConfig();
		}
	}

	public void admin(CommandSender sender, String[] args){
		if (!sender.hasPermission("CircleTP.admin")) {
			sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to change configurations.");
		} else {
			args[0].toLowerCase();
			if (args[0].equalsIgnoreCase("setcenter")) {
				setCenter(sender);
			} else if (args[0].equalsIgnoreCase("setminradius") || args[0].equalsIgnoreCase("setminimumradius")) {
				setMinRadius(sender,args);
			} else if (args[0].equalsIgnoreCase("setmaxradius") || args[0].equalsIgnoreCase("setmaximumradius")) {
				setMaxRadius(sender,args);
			} else if (args[0].equalsIgnoreCase("forceheight")) {
				forceHeight(sender,args);
			} else if (args[0].equalsIgnoreCase("tpmode")){
				tpMode(sender,args);
			} else if (args[0].equalsIgnoreCase("checkconfig")) {
				checkConfig(sender);
			} else if (args[0].equalsIgnoreCase("tell") || args[0].equalsIgnoreCase("tellmode")){
				tellMode(sender,args);
			} else if(args[0].equalsIgnoreCase("reload")){
				reloadCfg(sender);
			} else {
				sender.sendMessage(prefix + ChatColor.RED + "Argument not recognized.");
			}
		}
	}

	public void setMinRadius(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + "Please define minimum radius.");
		} else {
			if (maxRadius < Math.abs(Integer.parseInt(args[1]))) {
				sender.sendMessage(prefix + ChatColor.RED + "The minumum radius cannot be greater than the maximum radius.");
			} else {
				minRadius = Math.abs(Integer.parseInt(args[1]));
				misconfig = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Minimum radius has been set to " + minRadius + ".");
				this.getConfig().set("minRadius", minRadius);
				this.saveConfig();
			}
		}
	}

	public void setMaxRadius(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + "Please define maximum radius.");
		} else {
			if (minRadius > Math.abs(Integer.parseInt(args[1])) && Integer.parseInt(args[1]) != 0) {
				sender.sendMessage(prefix + ChatColor.RED + "The maximum radius cannot be smaller than the minimum radius.");
			} else {
				maxRadius = Math.abs(Integer.parseInt(args[1]));
				misconfig = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Maximum radius has been set to " + maxRadius + ".");
				this.getConfig().set("maxRadius", maxRadius);
				this.saveConfig();
			}
		}
	}

	public void forceHeight(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + "Please define height. Use -1 to deactivate this feature.");
		} else {
			height = Integer.parseInt(args[1]);
			if(height < -1)
				height = -1;
			if(height == -1)
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will be teleported to the highest block.");
			else
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will always be teleported y=" + height + ".");
			this.getConfig().set("forceHeight", height);
			this.saveConfig();
		}
	}

	public void tpMode(CommandSender sender, String[] args){
		if(args.length < 2){
			if(relativeTP == true){
				relativeTP = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Relative TP deactivated.");
			}else{
				relativeTP = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Relative TP activated.");
			}
		}else{
			args[1].toLowerCase();
			if(args[1].equalsIgnoreCase("false")){
				relativeTP = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Relative TP deactivated.");
			}else if(args[1].equalsIgnoreCase("true")){
				relativeTP = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Relative TP activated.");
			}else{
				sender.sendMessage(prefix + ChatColor.RED + "Enter \"true\" or \"false\"");
				sender.sendMessage(prefix + ChatColor.RED + "Write nothing to toggle.");
			}
		}
		this.getConfig().set("relativeTP", relativeTP);
		this.saveConfig();
	}

	public void tellMode(CommandSender sender, String[] args){
		if(args.length < 2){
			if(tell == true){
				tell = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will not be told who teleported them.");
			}else{
				tell = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will be told who teleported them.");
			}
		}else{
			args[1].toLowerCase();
			if(args[1].equalsIgnoreCase("false")){
				tell = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will not be told who teleported them.");
			}else if(args[1].equalsIgnoreCase("true")){
				tell = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will be told who teleported them.");
			}else{
				sender.sendMessage(prefix + ChatColor.RED + "Enter \"true\" or \"false\"");
				sender.sendMessage(prefix + ChatColor.RED + "Write nothing to toggle.");
			}
		}
		this.getConfig().set("tell", tell);
	}

	public void checkConfig(CommandSender sender){
		sender.sendMessage(prefix + ChatColor.GOLD + "The plugin is configured as follows:");
		sender.sendMessage(ChatColor.GOLD + "Minimum radius: " + minRadius);
		if(maxRadius == 0){
			sender.sendMessage(ChatColor.GOLD + "Maximum radius: " + ChatColor.RED + "deactivated");
		}else{
			if(misconfig == true){
				sender.sendMessage(ChatColor.GOLD + "Maximum radius: " + ChatColor.RED + maxRadius);
			}else{
				sender.sendMessage(ChatColor.GOLD + "Maximum radius: " + maxRadius);
			}
		}
		if(height == -1){
			sender.sendMessage(ChatColor.GOLD + "Force height: deactivated");
		}else{
			sender.sendMessage(ChatColor.GOLD + "Force height: " + height);
		}
		sender.sendMessage(ChatColor.GOLD + "Tell: " + tell);
		sender.sendMessage(ChatColor.GOLD + "Relative TP: " + relativeTP);
		if(relativeTP == false){
			sender.sendMessage(ChatColor.GOLD + "Center:");
			sender.sendMessage(ChatColor.GOLD + tab + "x=" + (int) center.getX() + ", z=" + (int) center.getZ() + ", world: " + center.getWorld().getName());
		}
	}

	public int randomRangedInt(int min, int max){
		return min + (int)(Math.random() * ((max - min) + 1));
	}

	public int randomSign(){
		if(1 + (int)(Math.random() * (100 - 1)) >= 50)
			return 1;
		else
			return -1;
	}

	public Location getRandomLocation(Player target){
		Location destination = new Location(null, 0, 0, 0);

		if(relativeTP == true){
			lastTpCoord[0] = (int) target.getLocation().getX();
			lastTpCoord[2] = (int) target.getLocation().getZ();
			destination.setWorld(target.getLocation().getWorld());
		}else{
			lastTpCoord[0] = (int) center.getX();
			lastTpCoord[2] = (int) center.getZ();
			destination.setWorld(center.getWorld());
		}
		if(maxRadius != 0){
			lastTpCoord[0] += randomRangedInt(minRadius, maxRadius) * randomSign();
			lastTpCoord[2] += randomRangedInt(minRadius, maxRadius) * randomSign();
		}
		if(maxRadius == 0){
			lastTpCoord[0] += randomRangedInt(minRadius, (int) Double.POSITIVE_INFINITY) * randomSign();
			lastTpCoord[2] += randomRangedInt(minRadius, (int) Double.POSITIVE_INFINITY) * randomSign();
		}
		destination.setX((double) lastTpCoord[0]);
		destination.setZ((double) lastTpCoord[2]);
		if(height != -1){
			lastTpCoord[1] = height;
		}else{
			lastTpCoord[1] = destination.getWorld().getHighestBlockYAt(lastTpCoord[0], lastTpCoord[2]);
		}
		destination.setY((double) lastTpCoord[1]);
		lastTpWorld = destination.getWorld();
		return destination;
	}

	public void displayVersion(CommandSender sender){
		if(sender.hasPermission("CircleTP.ver")){
			PluginDescriptionFile pdf = this.getDescription();
			sender.sendMessage(ChatColor.AQUA + "========== CIRCLE TP ==========");
			sender.sendMessage(ChatColor.GOLD + "Version: " + pdf.getVersion());
			sender.sendMessage(ChatColor.GOLD + "Author:  " + pdf.getAuthors());
			sender.sendMessage(ChatColor.AQUA + "===============================");
		}else{
			sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to view this information.");
		}
	}

	public void reloadCfg(CommandSender sender){
		try{
			this.reloadConfig();
			minRadius = Integer.valueOf(this.getConfig().getInt("minRadius"));
			maxRadius = Integer.valueOf(this.getConfig().getInt("maxRadius"));
			height = Integer.valueOf(this.getConfig().getInt("forceHeight"));
			relativeTP = Boolean.valueOf(this.getConfig().getBoolean("relativeTP"));
			tell = Boolean.valueOf(this.getConfig().getBoolean("tell"));
			center.setX(Double.valueOf(this.getConfig().getDouble("center.x")));
			center.setZ(Double.valueOf(this.getConfig().getDouble("center.z")));
			center.setWorld(this.getServer().getWorld(this.getConfig().getString("center.world")));
			metrics = Boolean.valueOf(this.getConfig().getBoolean("metrics"));
			sender.sendMessage(prefix + ChatColor.GOLD + "Plugin reloaded successfully.");
		}catch(final Exception e){
			sender.sendMessage(prefix + ChatColor.RED + "Error loading config file.");
		}
		if(minRadius > maxRadius && maxRadius != 0){
			sender.sendMessage(ChatColor.RED + "MINUMUM RADIUS IS GREATER THAN MAXIMUM RADIUS!");
			sender.sendMessage(ChatColor.RED + "Use \"/ctpadmin checkconfig\" to check the radii and then \"/ctpadmin setminradius/setmaxradius\" to change them.");
			sender.sendMessage(ChatColor.RED + "When editing the config.yml file, you must make sure that minRadius < maxRadius, otherwise the plugin will not understand where to teleport the player.");
			sender.sendMessage(ChatColor.RED + "The plugin will not work with misconfigurations.");
			misconfig = true;
		}else{
			misconfig = false;
		}
		if(height < -1){
			height = -1;
		}
	}
}
