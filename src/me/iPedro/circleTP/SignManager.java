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
	private LocaleHandler l10n;
	
	public SignManager(CircleTP plugin, String prefix, LocaleHandler l10n){
		this.plugin = plugin;
		this.prefix = prefix;
		this.l10n = l10n;
	}
	
	@EventHandler(priority=EventPriority.HIGH)
	private void onSignPlace(SignChangeEvent event){
		Player player = event.getPlayer();
		String[] lines = event.getLines();
		if(lines[0].equalsIgnoreCase("[ctp]") || lines[0].equalsIgnoreCase("[circletp]")){
			if(!player.hasPermission("CircleTP.createSign")){
				player.sendMessage(prefix + ChatColor.RED + l10n.getString("error-tpsigns"));
				event.getBlock().breakNaturally();
				return;
			}
			event.setLine(0, ChatColor.DARK_GREEN + "[CTP]");
			event.setLine(1, ChatColor.GOLD + l10n.getString("sign-line1"));
			event.setLine(2, ChatColor.GOLD + l10n.getString("sign-line2"));
			event.setLine(3, ChatColor.GOLD + l10n.getString("sign-line3"));
			player.sendMessage(prefix + ChatColor.GOLD + l10n.getString("info-tpsign-create"));
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
