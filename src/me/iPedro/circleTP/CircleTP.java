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
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.MetricsLite;

public class CircleTP extends JavaPlugin {

	public String prefix = (ChatColor.GREEN + "[CTP] " + ChatColor.RESET);
	private CooldownManager cDM = new CooldownManager(this, 60);
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
	public String locale = "en-US";
	private Location center = new Location(null, 0, 0, 0);
	private LocaleHandler l10n = new LocaleHandler(this);
	private SignManager signManager = new SignManager(this, prefix, l10n);
	private ForbiddenBlocks forbiddenBlocks = new ForbiddenBlocks(this);
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
			locale = String.valueOf(this.getConfig().getString("locale"));
		}catch(final Exception e){
			log.info("Error loading config file.");
			e.printStackTrace();
		}
		l10n.loadStrings(locale,true);
		forbiddenBlocks.loadForbbidenBlocks();
		if(cooldown < 0)
			cooldown = Math.abs(cooldown);
		cDM.setCooldown(cooldown);
		if(metrics){
			try {
				MetricsLite metrics = new MetricsLite(this);
				metrics.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(minRadius > maxRadius && maxRadius != 0){
			log.severe(l10n.getString("error-misconfig3"));
			log.severe(l10n.getString("error-misconfig4"));
			log.severe(l10n.getString("error-misconfig5"));
			log.severe(l10n.getString("error-misconfig6"));
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
			this.getConfig().set("locale", locale);
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
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-moreinput"));
			}else{
				admin(sender,args);
			}
		}else if(commandLabel.equalsIgnoreCase("ctph") || commandLabel.equalsIgnoreCase("ctphelp"))
			displayHelp(sender);
		else if(commandLabel.equalsIgnoreCase("ctpver"))
			displayVersion(sender);
		return false;
	}

	private void displayHelp(CommandSender sender) {
		if(!sender.hasPermission("CircleTP.help")){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-noinfo"));
			return;
		}
		sender.sendMessage(prefix + ChatColor.GREEN + l10n.getString("help-header"));
		sender.sendMessage(ChatColor.YELLOW + "/ctp" + ChatColor.GOLD + l10n.getString("help-ctp"));
		sender.sendMessage(ChatColor.YELLOW + "/ctp [player]" + ChatColor.GOLD + l10n.getString("help-ctp-other"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin" + ChatColor.GOLD + l10n.getString("help-ctp-admin"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin setcenter" + ChatColor.GOLD + l10n.getString("help-ctp-setcenter"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin setmaxradius" + ChatColor.GOLD + l10n.getString("help-ctp-setmax"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin setminradius" + ChatColor.GOLD + l10n.getString("help-ctp-setmin"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin forceheight" + ChatColor.GOLD + l10n.getString("help-ctp-forceheight"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin tpmode" + ChatColor.GOLD + l10n.getString("help-ctp-tpmode"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin tell" + ChatColor.GOLD + l10n.getString("help-ctp-tell"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin cooldown" + ChatColor.GOLD + l10n.getString("help-ctp-cooldown"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin locale" + ChatColor.GOLD + l10n.getString("help-ctp-setlocale"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin reload" + ChatColor.GOLD + l10n.getString("help-ctp-reload"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpadmin checkconfig" + ChatColor.GOLD + l10n.getString("help-ctp-checkconfig"));
		sender.sendMessage(ChatColor.YELLOW + "/ctpver" + ChatColor.GOLD + l10n.getString("help-ctp-version"));
	}

	public void teleportSelf(CommandSender sender){
		if(!sender.hasPermission("CircleTP.ctp")){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-nopermissionself"));
			return;
		}
		if(misconfig){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-misconfig1"));
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-misconfig2"));
			return;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("error-tpconsole"));
			return;
		}
		Player issuer = (Player) sender;
		if(cDM.isCoolingDown(issuer) && !issuer.hasPermission("CircleTP.override")){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-cooldownwait"));
			return;
		}
		if(issuer.isInsideVehicle()){
			Entity vehicle = issuer.getVehicle();
			if(vehicle.getType().equals(EntityType.PIG)){
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-vehicle") + l10n.getString("error-vehicle-pig"));
				return;
			}
			if(vehicle.getType().equals(EntityType.MINECART)){
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-vehicle") + l10n.getString("error-vehicle-minecart"));
				return;
			}
			if(vehicle.getType().equals(EntityType.BOAT)){
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-vehicle") + l10n.getString("error-vehicle-boat"));
				return;
			}
			if(vehicle.getType().equals(EntityType.HORSE)){
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-vehicle") + l10n.getString("error-vehicle-horse"));
				return;
			}
		}
		cDM.cancelCooldown(issuer);
		issuer.teleport(getRandomLocation(issuer));
		issuer.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-teleported"));
		log.info(issuer.getName() + l10n.getString("info-teleported-other1") + lastTpWorld.getName() + ", (" + lastTpCoord[0] + "," + lastTpCoord[1] + "," + lastTpCoord[2] + ")");
		cDM.initiateCooldown(issuer);
	}

	private void teleportOther(CommandSender sender, String targetName){
		if(!sender.hasPermission("CircleTP.ctp.others")){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-nopermissionother"));
			return;
		}
		if(misconfig){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-misconfig1"));
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-misconfig2"));
			return;
		}
		if (getPlayer(targetName) == null) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-notfound"));
			return;
		}
		Player target = getPlayer(targetName);
		if(targetName.equalsIgnoreCase(sender.getName())){
			if(!target.hasPermission("CircleTP.ctp")){
				target.sendMessage(prefix + ChatColor.RED + l10n.getString("error-nopermissionself"));
				return;
			}
			if(cDM.isCoolingDown(target) && !target.hasPermission("CircleTP.override")){
				target.sendMessage(prefix + ChatColor.RED + l10n.getString("error-cooldownwait"));
				return;
			}
		}
		if(target.isInsideVehicle()){
			Entity vehicle = target.getVehicle();
			if(vehicle.getType().equals(EntityType.PIG)){
				sender.sendMessage(prefix + ChatColor.RED + target.getName() + l10n.getString("error-vehicle2") + l10n.getString("error-vehicle-pig"));
				return;
			}
			if(vehicle.getType().equals(EntityType.MINECART)){
				sender.sendMessage(prefix + ChatColor.RED + target.getName() + l10n.getString("error-vehicle2") + l10n.getString("error-vehicle-minecart"));
				return;
			}
			if(vehicle.getType().equals(EntityType.BOAT)){
				sender.sendMessage(prefix + ChatColor.RED + target.getName() + l10n.getString("error-vehicle2") + l10n.getString("error-vehicle-boat"));
				return;
			}
			if(vehicle.getType().equals(EntityType.HORSE)){
				sender.sendMessage(prefix + ChatColor.RED + target.getName() + l10n.getString("error-vehicle2") + l10n.getString("error-vehicle-horse"));
				return;
			}
		}
		cDM.cancelCooldown(target);
		target.teleport(getRandomLocation(target));
		sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-teleported-other2") + target.getName() + l10n.getString("info-teleported-other3"));
		cDM.initiateCooldown(target);
		if(tell)
			target.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-teleported-other4") + sender.getName() + ".");
		else
			target.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-teleported"));
		log.info(sender.getName() + l10n.getString("info-teleported-log1") + target.getName() + l10n.getString("info-teleported-log2") + lastTpWorld.getName() + ", (" + lastTpCoord[0] + "," + lastTpCoord[1] + "," + lastTpCoord[2] + ")");
	}

	private void setCenter(CommandSender sender){
		if (!(sender instanceof Player)) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-ingamecmd"));
			return;
		}
		Player issuer = (Player) sender;
		center = issuer.getLocation();
		sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-setcenter") + center.getWorld().getName() + ", (" + (int) center.getX() + "," + (int) center.getY() + "," + (int) center.getZ() + ").");
		if(relativeTP){
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-abstpdisabled"));
		}
		this.getConfig().set("center.x", (int) center.getX());
		this.getConfig().set("center.z", (int) center.getZ());
		this.getConfig().set("center.world", center.getWorld().getName());
		this.saveConfig();
	}

	private void admin(CommandSender sender, String[] args){
		if (!sender.hasPermission("CircleTP.admin")) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-changecfg"));
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
		else if(args[0].equalsIgnoreCase("locale"))
			setLocale(sender,args);
		else
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-invalidcmd"));
	}

	private void setLocale(CommandSender sender, String[] args) {
		if(args.length < 2){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-locale1"));
			return;
		}
		String locale = args[1];
		File tmpFile = new File(this.getDataFolder() + File.separator + "l10n" + File.separator + locale + ".yml");
		if(!tmpFile.exists()){
			FileConfiguration tmp = l10n.checkFileExistence(locale, false,false);
			if(tmp == null){
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-locale2"));
				return;
			}
		}
		l10n.loadStrings(locale,false);
		this.locale = locale;
		sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-localeset") + l10n.getString("language-name"));
		this.getConfig().set("locale", locale);
		this.saveConfig();
	}

	private void setCooldown(CommandSender sender, String[] args) {
		if(args.length < 2){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-cooldown"));
			return;
		}
		int cooldown = Integer.parseInt(args[1]);
		if(cooldown < 0)
			cooldown = Math.abs(cooldown);
		cDM.setCooldown(cooldown);
		if(cooldown == 0)
			sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-cooldownoff"));
		else
			sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-cooldownset1") + cooldown + l10n.getString("info-cooldownset2"));
		this.getConfig().set("cooldown", cooldown);
		this.saveConfig();
	}

	private void setMinRadius(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-minradius1"));
			return;
		}
		if (maxRadius < Math.abs(Integer.parseInt(args[1]))) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-minradius2"));
			return;
		}
		minRadius = Math.abs(Integer.parseInt(args[1]));
		misconfig = false;
		sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-minradius") + minRadius + ".");
		this.getConfig().set("minRadius", minRadius);
		this.saveConfig();
	}

	private void setMaxRadius(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-maxradius1"));
			return;
		}
		if (minRadius > Math.abs(Integer.parseInt(args[1])) && Integer.parseInt(args[1]) != 0) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-maxradius2"));
			return;
		}
		maxRadius = Math.abs(Integer.parseInt(args[1]));
		misconfig = false;
		sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-maxradius") + maxRadius + ".");
		this.getConfig().set("maxRadius", maxRadius);
		this.saveConfig();
	}

	private void forceHeight(CommandSender sender, String[] args){
		if (args.length < 2) {
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-height"));
			return;
		}
		height = Integer.parseInt(args[1]);
		if(height < -1)
			height = -1;
		if(height == -1)
			sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-height1"));
		else
			sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-height2") + height + ".");
		this.getConfig().set("forceHeight", height);
		this.saveConfig();
	}

	private void tpMode(CommandSender sender, String[] args){
		if(args.length < 2){
			if(relativeTP){
				relativeTP = false;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-abstpoff"));
			}else{
				relativeTP = true;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-abstpon"));
			}
		}else{
			if(args[1].equalsIgnoreCase("false")){
				relativeTP = false;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-abstpoff"));
			}else if(args[1].equalsIgnoreCase("true")){
				relativeTP = true;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-abstpon"));
			}else{
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-bool"));
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-toggle"));
			}
		}
		this.getConfig().set("relativeTP", relativeTP);
		this.saveConfig();
	}

	private void tellMode(CommandSender sender, String[] args){
		if(args.length < 2){
			if(tell){
				tell = false;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-telloff"));
			}else{
				tell = true;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-tellon"));
			}
		}else{
			if(args[1].equalsIgnoreCase("false")){
				tell = false;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-telloff"));
			}else if(args[1].equalsIgnoreCase("true")){
				tell = true;
				sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-tellon"));
			}else{
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-bool"));
				sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-toggle"));
			}
		}
		this.getConfig().set("tell", tell);
		this.saveConfig();
	}

	private void checkConfig(CommandSender sender){
		sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-plugincfg-header"));
		sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-minradius") + minRadius);
		if(maxRadius == 0)
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-maxradius") + ChatColor.RED + l10n.getString("string-deactivated"));
		else{
			if(misconfig)
				sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-maxradius") + ChatColor.RED + maxRadius);
			else
				sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-maxradius") + maxRadius);
		}
		if(cDM.isActive())
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-cooldown") + cDM.getCooldown());
		else
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-cooldown") + l10n.getString("string-deactivated"));
		if(height == -1)
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-height") + l10n.getString("string-deactivated"));
		else
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-height") + height);
		sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-tell") + tell);
		sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-relativetp") + relativeTP);
		if(!relativeTP){
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-plugincfg-center"));
			sender.sendMessage(ChatColor.GOLD + tab + "x=" + (int) center.getX() + ", z=" + (int) center.getZ() + ", world: " + center.getWorld().getName());
		}
		sender.sendMessage(ChatColor.GOLD + l10n.getString("info-language") + l10n.getString("language-name"));
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
		}else
			lastTpCoord[1] = destination.getWorld().getHighestBlockYAt(lastTpCoord[0], lastTpCoord[2]);
		destination.setY((double) lastTpCoord[1]);
		lastTpWorld = destination.getWorld();
		if(forbiddenBlocks.isForbiddenBlock(destination.add(0,-1,0).getBlock().getType()))
			destination = getRandomLocation(target);
		return destination;
	}

	private void displayVersion(CommandSender sender){
		if(sender.hasPermission("CircleTP.ver")){
			PluginDescriptionFile pdf = this.getDescription();
			sender.sendMessage(ChatColor.AQUA + "========== CIRCLE TP ==========");
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-version") + pdf.getVersion());
			sender.sendMessage(ChatColor.GOLD + l10n.getString("info-author") + pdf.getAuthors());
			sender.sendMessage(ChatColor.AQUA + "===============================");
		}else
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-noinfo"));
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
			locale = this.getConfig().getString("locale");
			sender.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-reloaded"));
		}catch(final Exception e){
			sender.sendMessage(prefix + ChatColor.RED + l10n.getString("error-loading"));
			e.printStackTrace();
		}
		l10n.loadStrings(locale,true);
		forbiddenBlocks.loadForbbidenBlocks();
		if(cooldown < 0){
			cooldown = Math.abs(cooldown);
			this.getConfig().set("cooldown", cooldown);
			this.saveConfig();
		}
		cDM.setCooldown(cooldown);
		if(minRadius > maxRadius && maxRadius != 0){
			sender.sendMessage(ChatColor.RED + l10n.getString("error-misconfig3"));
			sender.sendMessage(ChatColor.RED + l10n.getString("error-misconfig4"));
			sender.sendMessage(ChatColor.RED + l10n.getString("error-misconfig5"));
			sender.sendMessage(ChatColor.RED + l10n.getString("error-misconfig6"));
			misconfig = true;
		}else
			misconfig = false;
		if(height < -1)
			height = -1;
	}

	public Player getPlayer(String name){
		return this.getServer().getPlayer(name);
	}
}
