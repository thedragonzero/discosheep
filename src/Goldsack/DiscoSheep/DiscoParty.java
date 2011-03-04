package Goldsack.DiscoSheep;

import java.util.LinkedList;
import java.util.Random;

import net.minecraft.server.SpawnerCreature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.NoteBlock;
import org.bukkit.craftbukkit.entity.CraftSheep;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;
/**
 * Discoparty includes a list of all players and their party teams when discoparty was started
 * Also include some setting on how many sheeps to be spawned, beatspeed and block distance for sheeps to spawn
 * @author goldsack
 *
 */
public class DiscoParty extends Thread{
	private DiscoSheep plugin;
	protected boolean running;
	protected LinkedList<DiscoTeam> discoTeams = new LinkedList<DiscoTeam>();
	private int sheepNumbers = 15; //Each player gets this many sheeps
	private int sheepSpawnDistance = 5; //Sheeps spawn within this many blocks from players
	private int beatspeed = 475; //Millisec between beats
	
	public DiscoParty(DiscoSheep discoSheep) {
		plugin = discoSheep;
		running = false;
	}
	
	/**
	 * Cleans up all party teams and remove them.
	 */
	public void cleanUp() {
		for(DiscoTeam team: discoTeams){
			team.cleanUp();

		}
		discoTeams.clear();
	}
	
	/**
	 * Return true if party is running
	 * @return
	 */
	public boolean isRunning() {
		return running;
	};
	/**
	 * Calls for party to end
	 */
	public void end() {
		running = false;
		
	}
	/**
	 * Called when discoparty starts in a new thread
	 */
	public void run() {
		try {
			running = true;
			discoTeams.clear();
			generateTeams();
			
			long timedelta = 0;
			long timeStart = 0;
			while(running){
				timeStart = System.currentTimeMillis();
				for(DiscoTeam team: discoTeams){
					if(team.player.isOnline()){
						partyChange(team);
					}
					else{
						team.cleanUp();
						discoTeams.remove(team);
					}

				}
				timedelta = System.currentTimeMillis() - timeStart;
				if(timedelta > beatspeed){
					timedelta = beatspeed;
				}
				sleepParty(beatspeed - timedelta);
			}	
		} catch (Exception e) {
			System.out.println("DiscoParty has failed. Thread ended");
			running = false;
			cleanUp();
			//e.printStackTrace();
		}

	}
	/**
	 * Make a party teams for each player in server 
	 */
	private void generateTeams() {
		Player players[] = plugin.getServer().getOnlinePlayers();
		DiscoTeam team;
		
		for(Player player: players){			
			team = new DiscoTeam(player);
			
			//Add musicBox and stone
			Block goodLocation = getMusicArea(team.getPlayer().getLocation().getBlock().getRelative(-4, 3, -4));
			buildMusicArea(goodLocation, team);
			//Add torches
			discoTeams.add(team);

			//Add sheeps to team
			addSheeps(team);
		}
	}
	/**
	 * Add sheeps around the player
	 * @param team
	 */
	private void addSheeps(DiscoTeam team) {
		Random rand = new Random();
		for (int i = 0; i < sheepNumbers; i++) {
			int r = rand.nextInt(sheepSpawnDistance * 2 * sheepSpawnDistance * 2); 
			int x = (r%(sheepSpawnDistance * 2)) - sheepSpawnDistance;
			int z = (r/(sheepSpawnDistance * 2)) - sheepSpawnDistance;
			Block spawnPlane = team.getPlayer().getLocation().getBlock().getRelative(x, 0, z);
			Block spawnLoc = findSpawnYLoc(spawnPlane);
			if(spawnLoc != null){
				team.addSheep((Sheep) spawnLoc.getWorld().spawnCreature(spawnLoc.getLocation(), CreatureType.SHEEP));				
			}
		}
	}
	
	/**
	 * Search for a free place above player to install musicbox
	 * @param loc
	 * @return
	 */
	private Block getMusicArea(Block loc) {
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				if(isMiniChunkFree(loc.getRelative(x*3, 0, y*3))){
					return loc.getRelative(x*3, 0, y*3);
				}
			}
		}
		return null;
	}
	
	/**
	 * Check if a 3*2*3 space is free so we can have a music box there.
	 * @param loc
	 * @return
	 */
	private boolean isMiniChunkFree(Block loc) {
		
		for (int x = 0; x < 3; x++) {
				for(int z = 0; z < 3; z++){
					for (int y = 0; y < 3; y++) {
					if(loc.getRelative(x, y, z).getType() != Material.AIR){
						return false;
					}
				}
			}
		}
		return true;
	}
	/**
	 * Allocate the blocks we use for musicbox and torches
	 * @param loc
	 * @param team
	 */
	private void buildMusicArea(Block loc, DiscoTeam team){
		if(loc == null){
			return;
		}
		team.soundBlock = loc.getRelative(1, 1, 1);
		team.soundBlock.setType(Material.NOTE_BLOCK);
		if(team.soundBlock.getState() instanceof NoteBlock){
			((NoteBlock)team.soundBlock.getState()).setNote((byte) 0x11);			
		}
		
		team.stoneBlock = loc.getRelative(1, 0, 1);
		team.stoneBlock.setType(Material.STONE);	
		
		team.torches[0] = loc.getRelative(1, 0, 0);
		team.torches[1] = loc.getRelative(0, 0, 1);
		team.torches[2] = loc.getRelative(1, 0, 2);
		team.torches[3] = loc.getRelative(2, 0, 1);
	}



	/**
	 * Swap wool colour to random on sheep entity
	 * @param entity
	 */
	private void changeColor(Sheep entity) {
		Random r = new Random();
		entity.setColor(DiscoColor.dyeColors[r.nextInt(DiscoColor.dyeColors.length)]);
	}


	/**
	 * Returns a good spot to spawn sheeps in the y axis to the given block.
	 * First checks if its a air block, and then go down 4 blocks to find ground. If no ground found, return null.
	 * The checks if its a solid block and try to find air up to 4 blcoks above given block. If no air found return null.
	 * @param spot
	 * @return Block
	 */
	private Block findSpawnYLoc(Block spot){
		if(spot == null){
			return null;
		}
		
		Block temp = spot;
		if(temp.getType() == Material.AIR){
			for(int i = 0; i < 4; i++){
				if(temp.getRelative(0, -1, 0) != null && temp.getRelative(0, -1, 0).getType() != Material.AIR){
					return temp;
				}
				temp = temp.getRelative(0, -1, 0); //Go down to find ground
			}
		}
		
		temp = spot;
		if(temp.getType() != Material.AIR){
			for(int i = 0; i < 4; i++){
				if(temp.getRelative(0, 1, 0) != null && temp.getRelative(0, 1, 0).getType() == Material.AIR){
					return temp.getRelative(0, 1, 0);
				}
				temp = temp.getRelative(0, 1, 0); //Go up to find air
			}
		}
		
		//Return null if no location found
		return null;
		
	}
	
	/**
	 * Makes the sheep entity twitch/jump randomly to make a MOSH PIT!
	 * @param entity
	 */
	private void sheepJump(Sheep entity) {
		//Requre carftsheep from craftbukkit library to set an upward vector momentum
		CraftSheep sheep = (CraftSheep)entity;
	
		Random r = new Random();
		//Some random jumping
		if(r.nextInt(4) == 0){
			Vector v = sheep.getVelocity();
			
			v.setY(0.5f);

			sheep.setVelocity(v);
		}

	}

	/**
	 * Toggle a bass beat, light, sheepcolor and some sheepjumping
	 * @param team
	 */
	private void partyChange(DiscoTeam team) {
		for(Sheep sheep: team.sheepList){
			if(sheep != null){
				try {
					if(!plugin.discoColor.isRunning()){							
						changeColor(sheep);
					}
					sheepJump(sheep);						
				} catch (Exception e) {
					System.out.println("A sheep could not rock on. Dead sheep? (DiscoSheep)");
				}
			}
		}
		
		sleepParty(100); //Sleep so it seems like sheeps jump more in time with beat
		
		if(team.soundBlock != null && team.soundBlock.getState() instanceof NoteBlock){
			//Play off 3 times at the same time to give some real OMPH in the speaker
			for (int i = 0; i < 3; i++) {
				((NoteBlock)team.soundBlock.getState()).play();					
			}
		}
		team.toggleTorches();
	}
	private void sleepParty(long millisec) {
		try {
			sleep(millisec);
		} catch (InterruptedException e) {
			running = false;
			System.out.println("DiscoSheep Failed sleeping thread, Discosheep has ended");
			e.printStackTrace();

		}
	}
}
