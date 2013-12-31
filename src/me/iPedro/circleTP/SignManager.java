package me.iPedro2.circleTP;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;

public class SignManager implements Listener{
	
	private CircleTP plugin;
	private String prefix;
	
	public SignManager(CircleTP plugin, String prefix){
		this.plugin = plugin;
		this.prefix = prefix;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	private void onSignPlace(SignChangeEvent event){
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		if(lines[0].equalsIgnoreCase("[ctp]") || lines[0].equalsIgnoreCase("[circletp]")){
			if(!player.hasPermission("CircleTP.createSign")){
				player.sendMessage(prefix + ChatColor.RED + "You don't have permission to create teleportation signs.");
				event.getBlock().breakNaturally();
				return;
			}
			event.setLine(0, ChatColor.DARK_GREEN + "[CTP]");
			event.setLine(1, ChatColor.GOLD + "Right-click");
			event.setLine(2, ChatColor.GOLD + "here to be");
			event.setLine(3, ChatColor.GOLD + "teleported");
			player.sendMessage(prefix + ChatColor.GOLD + "Teleportation sign created.");
		}
	}
	
	@EventHandler
	private void onSignClick(PlayerInteractEvent event){
		if(!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
		Block signBl = event.getClickedBlock();
		if(!signBl.getType().equals(Material.SIGN_POST) && !signBl.getType().equals(Material.WALL_SIGN)) return;
		Sign sign = (Sign) signBl.getState();
		if(!sign.getLine(0).equalsIgnoreCase(ChatColor.DARK_GREEN + "[CTP]")) return;
		plugin.teleportSelf(event.getPlayer());
	}
}
