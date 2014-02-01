package me.iPedro2.circleTP;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class ForbiddenBlocks {

	private HashSet<Integer> forbiddenBlocks = new HashSet<Integer>();
	private CircleTP plugin;
	private FileConfiguration cfgFile;
	
	public ForbiddenBlocks(CircleTP plugin){
		this.plugin = plugin;
	}
	
	public void loadForbbidenBlocks(){
		this.cfgFile = plugin.getConfig();
		clearForbiddenBlocks();
		@SuppressWarnings("unchecked")
		List<Integer> fileOut = (List<Integer>) cfgFile.getList("forbiddenBlocks");
		Iterator<Integer> it = fileOut.iterator();
		while(it.hasNext()){
			addForbiddenBlock(it.next());
		}
	}
	
	private void addForbiddenBlock(int block){
		forbiddenBlocks.add(block);
	}
	
	@SuppressWarnings("deprecation")
	public boolean isForbiddenBlock(Material block){
		return forbiddenBlocks.contains(block.getId()); //yes, i know. deprecated :(
	}
	
	private void clearForbiddenBlocks(){
		forbiddenBlocks.clear();
	}
	
}
