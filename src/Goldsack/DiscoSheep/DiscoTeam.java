package Goldsack.DiscoSheep;

import java.util.LinkedList;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

/**
 * Contains the entitys for each party that belongs to a player.
 * @author goldsack
 *
 */
public class DiscoTeam {
	LinkedList<Sheep> sheepList = new LinkedList<Sheep>();
	Player player;
	Block soundBlock;
	Block stoneBlock;
	Block torches[];
	boolean light;
	
	public DiscoTeam(Player p) {
		player = p;
		light = true;
		torches = new Block[4];
	}
	
	/**
	 * Add sheeps to the sheeplist for this team
	 * @param sheep
	 */
	public void addSheep(Sheep sheep){
		sheepList.add(sheep);
	}
	/**
	 * Return player connected to this team
	 * @return
	 */
	public Player getPlayer() {
		return player;
	}
	/**
	 * Removes all sheeps, boxes and torches in this team.
	 */
	public void cleanUp(){
		cleanUpSheeps();
		cleanUpBoxes();
	}
	/**
	 * Turn torches on or off
	 */
	public void toggleTorches(){
		for(Block torch: torches){
			if(torch != null && (torch.getType() == Material.AIR || torch.getType() == Material.TORCH)){
				if(light){
					torch.setType(Material.AIR);
				}
				else{
					torch.setType(Material.TORCH);	
				}
			}
		}
		light = !light;
	}
	private void cleanUpSheeps(){
		for(Sheep sheep: sheepList){
			if(sheep != null){
				sheep.remove();
			}
		}
		sheepList.clear();
	}
	private void cleanUpBoxes(){
		for (Block torch : torches) {
			if(torch != null){
				torch.setType(Material.AIR);
			}
		}
		if(soundBlock != null){
			soundBlock.setType(Material.AIR);
		}
		if(stoneBlock != null){
			stoneBlock.setType(Material.AIR);
		}
	}

	
}
