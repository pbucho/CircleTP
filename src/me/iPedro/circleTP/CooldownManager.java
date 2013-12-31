package me.iPedro2.circleTP;

import java.util.HashMap;

import org.bukkit.entity.Player;

public class CooldownManager {

	private CircleTP plugin;
	private HashMap<String,Integer> cooldownList = new HashMap<String,Integer>();
	private int cooldown = 60;
	private boolean active = true;
	
	public CooldownManager(CircleTP plugin, int timeout){
		this.plugin = plugin;
		this.cooldown = timeout;
		if(cooldown == 0)
			active = false;
		else
			active = true;
	}
	
	public void setCooldown(int timeout){
		this.cooldown = timeout;
		if(timeout == 0)
			active = false;
		else
			active = true;
	}
	
	public int getCooldown(){
		return cooldown;
	}
	
	private void addToList(Player player, int id){
		cooldownList.put(player.getName(), id);
	}
	
	private void removeFromList(Player player){
		cooldownList.remove(player.getName());
	}
	
	private boolean isInList(Player player){
		return cooldownList.containsKey(player.getName());
	}
	
	private int getId(Player player){
		return cooldownList.get(player.getName());
	}
	
	// cooldown
	
	public void initiateCooldown(final Player player){
		if(!active) return;
		int id = plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable(){
			public void run(){
				removeFromList(player);
			}
		}, cooldown*20);
		addToList(player, id);
	}
	
	public boolean isCoolingDown(Player player){
		return isInList(player);
	}
	
	public void cancelCooldown(Player player){
		if(!isCoolingDown(player)) return;
		plugin.getServer().getScheduler().cancelTask(getId(player));
		removeFromList(player);
	}
	
	//active
	
	public boolean isActive(){
		return active;
	}
}
