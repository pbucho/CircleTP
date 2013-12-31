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

	public String prefix = (ChatColor.GREEN + "[CTP] " + ChatColor.RESET);
	private CooldownManager cDM = new CooldownManager(this, 60);
	private SignManager signManager = new SignManager(this, prefix);
	public String tab = ("     ");
	private int minRadius;
	private int maxRadius;
	private int height;
	private int cooldown = 60;
	private int[] lastTpCoord = new int[3];
	private World lastTpWorld;
	private boolean relativeTP;
	private boolean tell;
	private boolean misconfig = false;
	private boolean metrics = true;
	private Location center = new Location(null, 0, 0, 0);
	Logger log = Bukkit.getLogger();
	File configFile = new File(getDataFolder() + "config.yml");

	@Override
	public void onEnable() {
		Bukkit.getServer().getPluginManager().registerEvents(signManager, this);
		if (!configFile.exists()) {
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
			cooldown = Integer.valueOf(this.getConfig().getInt("cooldown"));
		}catch(final Exception e){
			log.info("Error loading config file.");
		}
		if(cooldown < 0)
			cooldown = Math.abs(cooldown);
		cDM.setCooldown(cooldown);
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
			this.getConfig().set("cooldown", cDM.getCooldown());
			this.saveConfig();
		} catch (final Exception e) {
			log.severe("Error saving config file.");
		}
	}
	
	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("ctp")) {
			if(args.length > 0){
				teleportOther(sender,args[0]);
			}else{
				teleportSelf(sender);
			}
		} else if (commandLabel.equalsIgnoreCase("ctpadmin")) {
			if(args.length < 1){
				sender.sendMessage(prefix + ChatColor.GREEN + "CircleTP commands:");
				sender.sendMessage(ChatColor.YELLOW + "/ctp" + ChatColor.GOLD + " - Teleports to a random location.");
				sender.sendMessage(ChatColor.YELLOW + "/ctp [player]" + ChatColor.GOLD + " - Teleports [player] to a random location.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin" + ChatColor.GOLD + " - Performs administrative actions.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin setcenter" + ChatColor.GOLD + " - Sets the center for the absolue teleportation to the current position.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin setmaxradius" + ChatColor.GOLD + " - Sets the maximum teleportation radius.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin setminradius" + ChatColor.GOLD + " - Sets the minimum teleportation radius.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin forceheight" + ChatColor.GOLD + " - Forces teleportation to a given height (-1 to disable).");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin tpmode" + ChatColor.GOLD + " - Toggles between absolute or relative teleportation.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin tell" + ChatColor.GOLD + " - Toggles wether the players are told who teleported them.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin cooldown" + ChatColor.GOLD + " - Sets the cooldown period (0 to disable).");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin reload" + ChatColor.GOLD + " - Reloads the configuration file.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpadmin checkconfig" + ChatColor.GOLD + " - Displays the plugin configurations.");
				sender.sendMessage(ChatColor.YELLOW + "/ctpver" + ChatColor.GOLD + " - Displays the plugin version.");
			}else{
				admin(sender,args);
			}
		} else if(commandLabel.equalsIgnoreCase("ctpver")){
			displayVersion(sender);			
		}
		return false;
	}

	public void teleportSelf(CommandSender sender){
		if(!sender.hasPermission("CircleTP.ctp")){
			sender.sendMessage(prefix + ChatColor.RED + "You don't have permission to teleport yourself.");
			return;
		}
		if(misconfig){
			sender.sendMessage(prefix + ChatColor.RED + "The plugin is misconfigured and cannot be used (minRadius > maxRadius).");
			sender.sendMessage(prefix + ChatColor.RED + "If you're an admin, please correct this situation.");
			return;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(prefix + ChatColor.GOLD + "You are the console. How do you expect to be teleported?");
			return;
		}
		Player issuer = (Player) sender;
		if(cDM.isCoolingDown(issuer) && !issuer.hasPermission("CircleTP.override")){
			sender.sendMessage(prefix + ChatColor.RED + "You have been recently teleported. Please wait before teleporting again.");
			return;
		}
		cDM.cancelCooldown(issuer);
		issuer.teleport(getRandomLocation(issuer));
		issuer.sendMessage(prefix + ChatColor.GOLD + "You have been teleported to a random location.");
		log.info(issuer.getName() + " has been teleported to: " + lastTpWorld.getName() + ", (" + lastTpCoord[0] + "," + lastTpCoord[1] + "," + lastTpCoord[2] + ")");
		cDM.initiateCooldown(issuer);
	}

	private void teleportOther(CommandSender sender, String targetName){
		if(!sender.hasPermission("CircleTP.ctp.others")){
			sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to teleport other players.");
			return;
		}
		if(misconfig){
			sender.sendMessage(prefix + ChatColor.RED + "The plugin is misconfigured and cannot be used (minRadius > maxRadius).");
			sender.sendMessage(prefix + ChatColor.RED + "If you're an admin, please correct this situation.");
			return;
		}
		if (getPlayer(targetName) == null) {
			sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
			return;
		}
		Player target = getPlayer(targetName);
		if(targetName.equalsIgnoreCase(sender.getName())){
			if(!target.hasPermission("CircleTP.ctp")){
				target.sendMessage(prefix + ChatColor.RED + "You do not have permission to teleport yourself.");
				return;
			}
			if(cDM.isCoolingDown(target) && !target.hasPermission("CircleTP.override")){
				target.sendMessage(prefix + ChatColor.RED + "You have been recently teleported. Please wait before teleporting again.");
				return;
			}
		}
		cDM.cancelCooldown(target);
		target.teleport(getRandomLocation(target));
		sender.sendMessage(prefix + ChatColor.GOLD + "You have teleported " + target.getName() + " to a random location.");
		cDM.initiateCooldown(target);
		if(tell)
			target.sendMessage(prefix + ChatColor.GOLD + "You have been teleported to a random location by " + sender.getName() + ".");
		else
			target.sendMessage(prefix + ChatColor.GOLD + "You have been teleported to a random location.");
		log.info(sender.getName() + " has teleported " + target.getName() + " to: " + lastTpWorld.getName() + ", (" + lastTpCoord[0] + "," + lastTpCoord[1] + "," + lastTpCoord[2] + ")");
	}

	private void setCenter(CommandSender sender){
		if (!(sender instanceof Player)) {
			sender.sendMessage(prefix + ChatColor.RED + "This command must be run by a in-game player.");
			return;
		}
		Player issuer = (Player) sender;
		center = issuer.getLocation();
		sender.sendMessage(prefix + ChatColor.GOLD + "Center defined to: " + center.getWorld().getName() + ", (" + (int) center.getX() + "," + (int) center.getY() + "," + (int) center.getZ() + ").");
		if(relativeTP){
			sender.sendMessage(ChatColor.GOLD + "Absolute teleportation is not enabled. Use \"/ctpadmin tpmode\" to enable it.");
		}
		this.getConfig().set("center.x", (int) center.getX());
		this.getConfig().set("center.z", (int) center.getZ());
		this.getConfig().set("center.world", center.getWorld().getName());
		this.saveConfig();
	}

	private void admin(CommandSender sender, String[] args){
		if (!sender.hasPermission("CircleTP.admin")) {
			sender.sendMessage(prefix + ChatColor.RED + "You do not have permission to change configurations.");
			return;
		}
		if (args[0].equalsIgnoreCase("setcenter"))
			setCenter(sender);
		else if (args[0].equalsIgnoreCase("setminradius") || args[0].equalsIgnoreCase("setminimumradius"))
			setMinRadius(sender,args);
		else if (args[0].equalsIgnoreCase("setmaxradius") || args[0].equalsIgnoreCase("setmaximumradius"))
			setMaxRadius(sender,args);
		else if (args[0].equalsIgnoreCase("forceheight"))
			forceHeight(sender,args);
		else if (args[0].equalsIgnoreCase("tpmode"))
			tpMode(sender,args);
		else if (args[0].equalsIgnoreCase("checkconfig"))
			checkConfig(sender);
		else if (args[0].equalsIgnoreCase("tell") || args[0].equalsIgnoreCase("tellmode"))
			tellMode(sender,args);
		else if(args[0].equalsIgnoreCase("reload"))
			reloadCfg(sender);
		else if(args[0].equalsIgnoreCase("cooldown"))
			setCooldown(sender,args);
		else
			sender.sendMessage(prefix + ChatColor.RED + "Invalid command.");
	}

	private void setCooldown(CommandSender sender, String[] args) {
		if(args.length < 2){
			sender.sendMessage(prefix + ChatColor.RED + "Please define cooldown period.");
			return;
		}
		int cooldown = Integer.parseInt(args[1]);
		if(cooldown < 0)
			cooldown = Math.abs(cooldown);
		cDM.setCooldown(cooldown);
		if(cooldown == 0)
			sender.sendMessage(prefix + ChatColor.GOLD + "Cooldown deactivated.");
		else
			sender.sendMessage(prefix + ChatColor.GOLD + "Cooldown set to " + cooldown + " seconds.");
		this.getConfig().set("cooldown", cooldown);
		this.saveConfig();
	}

	private void setMinRadius(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + "Please define minimum radius.");
			return;
		}
		if (maxRadius < Math.abs(Integer.parseInt(args[1]))) {
			sender.sendMessage(prefix + ChatColor.RED + "The minumum radius cannot be greater than the maximum radius.");
			return;
		}
		minRadius = Math.abs(Integer.parseInt(args[1]));
		misconfig = false;
		sender.sendMessage(prefix + ChatColor.GOLD + "Minimum radius has been set to " + minRadius + ".");
		this.getConfig().set("minRadius", minRadius);
		this.saveConfig();
	}

	private void setMaxRadius(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + "Please define maximum radius.");
			return;
		}
		if (minRadius > Math.abs(Integer.parseInt(args[1])) && Integer.parseInt(args[1]) != 0) {
			sender.sendMessage(prefix + ChatColor.RED + "The maximum radius cannot be smaller than the minimum radius.");
			return;
		}
		maxRadius = Math.abs(Integer.parseInt(args[1]));
		misconfig = false;
		sender.sendMessage(prefix + ChatColor.GOLD + "Maximum radius has been set to " + maxRadius + ".");
		this.getConfig().set("maxRadius", maxRadius);
		this.saveConfig();
	}

	private void forceHeight(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + "Please define height. Use -1 to deactivate this feature.");
			return;
		}
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

	private void tpMode(CommandSender sender, String[] args){
		if(args.length < 2){
			if(relativeTP){
				relativeTP = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Absolute TP activated.");
			}else{
				relativeTP = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Relative TP activated.");
			}
		}else{
			if(args[1].equalsIgnoreCase("false")){
				relativeTP = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Absolute TP activated.");
			}else if(args[1].equalsIgnoreCase("true")){
				relativeTP = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Relative TP activated.");
			}else{
				sender.sendMessage(prefix + ChatColor.RED + "Enter \"true\" or \"false\"");
				sender.sendMessage(prefix + ChatColor.RED + "Enter nothing to toggle.");
			}
		}
		this.getConfig().set("relativeTP", relativeTP);
		this.saveConfig();
	}

	private void tellMode(CommandSender sender, String[] args){
		if(args.length < 2){
			if(tell){
				tell = false;
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will not be told who teleported them.");
			}else{
				tell = true;
				sender.sendMessage(prefix + ChatColor.GOLD + "Players will be told who teleported them.");
			}
		}else{
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
		this.saveConfig();
	}

	private void checkConfig(CommandSender sender){
		sender.sendMessage(prefix + ChatColor.GOLD + "The plugin is configured as follows:");
		sender.sendMessage(ChatColor.GOLD + "Minimum radius: " + minRadius);
		if(maxRadius == 0){
			sender.sendMessage(ChatColor.GOLD + "Maximum radius: " + ChatColor.RED + "deactivated");
		}else{
			if(misconfig){
				sender.sendMessage(ChatColor.GOLD + "Maximum radius: " + ChatColor.RED + maxRadius);
			}else{
				sender.sendMessage(ChatColor.GOLD + "Maximum radius: " + maxRadius);
			}
		}
		if(cDM.isActive())
			sender.sendMessage(ChatColor.GOLD + "Cooldown: " + cDM.getCooldown());
		else
			sender.sendMessage(ChatColor.GOLD + "Cooldown: deactivated");
		if(height == -1)
			sender.sendMessage(ChatColor.GOLD + "Force height: deactivated");
		else
			sender.sendMessage(ChatColor.GOLD + "Force height: " + height);
		sender.sendMessage(ChatColor.GOLD + "Tell: " + tell);
		sender.sendMessage(ChatColor.GOLD + "Relative TP: " + relativeTP);
		if(!relativeTP){
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

		if(relativeTP){
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

	private void displayVersion(CommandSender sender){
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

	private void reloadCfg(CommandSender sender){
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
			cooldown = Integer.valueOf(this.getConfig().getInt("cooldown"));
			metrics = Boolean.valueOf(this.getConfig().getBoolean("metrics"));
			sender.sendMessage(prefix + ChatColor.GOLD + "Plugin reloaded successfully.");
		}catch(final Exception e){
			sender.sendMessage(prefix + ChatColor.RED + "Error loading config file.");
		}
		if(cooldown < 0){
			cooldown = Math.abs(cooldown);
			this.getConfig().set("cooldown", cooldown);
			this.saveConfig();
		}
		cDM.setCooldown(cooldown);
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

	public Player getPlayer(String name){
		return this.getServer().getPlayer(name);
	}
}
