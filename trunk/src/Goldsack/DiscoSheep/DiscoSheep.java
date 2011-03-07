package Goldsack.DiscoSheep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijikokun.bukkit.Permissions.Permissions;
import com.nijiko.permissions.PermissionHandler;

/**
 * DiscoSheep is a plugin to minecraft servers running minecraft.
 * It changes sheep wool color for fun. 
 * @author goldsack
 *
 */
public class DiscoSheep extends JavaPlugin implements ActionListener{
	protected SheepListener sheepListener = new SheepListener(this);
	protected DiscoParty discoParty;
	protected DiscoSettings settings;
	protected Timer timer;
	
	//Permission plugin settings
	protected PermissionHandler permit = null;
	protected boolean usePermit = false;
	private final String discosheep = "discosheep."; //To be used in permission. Etc - 'discosheep.*'
	
	//Commands
	private final String debug = "debug";
	private final String help  = "help";
	private final String color = "color";
	private final String party = "party";
	private final String stop  = "stop";
	private final String reload= "reload";


	/**
	 * Called when plugin starts.
	 */
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();	
		pm.registerEvent(Event.Type.ENTITY_DAMAGED, sheepListener, Priority.Normal, this);
		
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println( "[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!" );
		
		settings = new DiscoSettings(this);
		discoParty = new DiscoParty(this);
		discoParty.start();
		timer = new Timer(settings.defaultPartyTime, this);
		timer.stop(); //Timer starts when its created, so it has to be stopped
		
		enablePermissons();
		
		
	}
	
	/**
	 * Called when plugin ends.
	 * Cancels all threads, and wait for them to end.
	 */
	@Override
	public void onDisable() {
		stopParty();
		discoParty.end();
		try {
			discoParty.join(2000);
		} catch (InterruptedException e) {
			System.out.println("[Discosheep] failed to end thread. Did not close down properly");
		}
		System.out.println("[DiscoSheep] closed down. Good bye");
	}
	
	/**
	 * Called when /discosheep is sent in game or server
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
		if(args.length > 0){

			if(args[0].equalsIgnoreCase(help)){
				if(isPermittet(sender, help)){
					return false; //Use plugin.yml to display info
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase(debug)){
				if(isPermittet(sender, debug)){
					sender.sendMessage(printDebug()); 					
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase(reload)){
				if(isPermittet(sender, reload)){
					settings.reload();
					sender.sendMessage("Reloaded DiscoSheep.properties file");
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase(stop)){
				if(isPermittet(sender, stop)){
					if(discoParty.flagPartyEnabled){
						sender.sendMessage("Party Stopped, you little joykiller");						
					}
					else{
						sender.sendMessage("There is no party running, or it just ended. Nothing to stop");						
					}
					stopParty();
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase(color)){
				if(isPermittet(sender, color)){
					toggleColor();
					sender.sendMessage("Rainbow sheeps " + (discoParty.isColorOn() ? "Activated":"Deactivated"));
				}
				return true;
			}
			else if(args[0].equalsIgnoreCase(party)){
				return analyzeParty(sender, args);
			}
			else{
				sender.sendMessage("DiscoSheep dont know command " + args[0]);
			}

		}
		if(sender instanceof Player && !sender.isOp()){
			sender.sendMessage("You must be OP to use DiscoSheep");
			return true;
		}

		return false;
	}

	private boolean analyzeParty(CommandSender sender, String[] args) {
		if(isPermittet(sender, party)){
			int partyDelay = settings.defaultPartyTime;
			int partySize = settings.sheepNumber;
			int spawnRange = settings.spawnDistance;
			Player[] players = getServer().getOnlinePlayers();
			if(args.length == 1){
				//Create a normal party to all players, use default settings
			}
			else if(args.length > 1){
				//Unless the argument is all players, check for playername
				if(!args[1].equalsIgnoreCase("all")){
					//Its a big party for one player
					partyDelay = settings.bigPartyDefaultPartyTime;
					partySize = settings.bigPartySheepNumber;
					spawnRange = settings.bigPartySpawnDistance;
					
					//If player wants to spawn party at his location
					if(args[1].equalsIgnoreCase("here")){
						if(sender instanceof Player){
							players = new Player[1];
							players[0] = (Player) sender;
							sender.sendMessage("Party at your place. Good going");
						}
						else{
							sender.sendMessage("Brother, you are the server. You can't spawn a party \"here\"! \nShame on you admin. For shame.");
							return true; //It's true since we want to message the server we have recived a technically correct message.
						}
					}
					else{
						LinkedList<Player> selectedPlayers = new LinkedList<Player>();
						for (Player p : players) {
							if(args[1].equalsIgnoreCase(p.getName()) && p.isOnline()){
								selectedPlayers.add(p);
							}
						}
						players = new Player[selectedPlayers.size()];
						int i = 0;
						for(Player p : selectedPlayers){
							players[i] = p;
							i++;
						}	
						
					}
					
					//Parse and check that time for big party is not bigger then limit
					if(args.length > 2){
						try {					
							partyDelay = Integer.parseInt(args[2]);
						} catch (NumberFormatException e) {
							sender.sendMessage(" \"" + args[2] + "\" can not be parsed as an integer. Discosheep does not start a party. BOO D:");
							return true;
						}
					}
					if(partyDelay > settings.bigPartyMaxPartyTime){
						partyDelay = settings.bigPartyMaxPartyTime;
						sender.sendMessage("Hit max big partytime. Change DiscoSheep.properties for longer party. Party will last for " + partyDelay + " sec.");
					}
					
					//Parse and check that number of sheeps for big party is not bigger then limit
					if(args.length > 3){
						try {					
							partySize = Integer.parseInt(args[3]);
						} catch (NumberFormatException e) {
							sender.sendMessage(" \"" + args[3] + "\" can not be parsed as an integer. Discosheep does not start a party. BOO D:");
							return true;
						}
					}
					if(partySize > settings.bigPartyMaxSheepNumber){
						partySize = settings.bigPartyMaxSheepNumber;
						sender.sendMessage("Hit max number of sheeps in big party. Change DiscoSheep.properties for longer party. Party redused to " + partySize + " sheeps.");
					}
					
					
				}//endif
				else{
					//Small party for everyone
					
					//Parse and check that time for small party is not bigger then limit
					if(args.length > 2){
						try {					
							partyDelay = Integer.parseInt(args[2]);
						} catch (NumberFormatException e) {
							sender.sendMessage(" \"" + args[2] + "\" can not be parsed as an integer. Discosheep does not start a party. BOO D:");
							return true;
						}
					}
					if(partyDelay > settings.maxPartyTime){
						partyDelay = settings.maxPartyTime;
						sender.sendMessage("Hit max partytime. Change DiscoSheep.properties for longer party. Party will last for " + partyDelay + " sec.");
					}
					
					//Parse and check that number of sheeps for small party is not bigger then limit
					if(args.length > 3){
						try {					
							partySize = Integer.parseInt(args[3]);
						} catch (NumberFormatException e) {
							sender.sendMessage(" \"" + args[3] + "\" can not be parsed as an integer. Discosheep does not start a party. BOO D:");
							return true;
						}
					}
					if(partySize > settings.maxSheepNumber){
						partySize = settings.maxSheepNumber;
						sender.sendMessage("Hit max number of sheeps in small party. Change DiscoSheep.properties for longer party. Party redused to " + partySize + " sheeps.");
					}
				}
				
			}
			
			if(players.length > 0){
				startParty(players, partyDelay, partySize, spawnRange);
				sender.sendMessage("Party ON!");				
			}
			else if(args.length > 1 && !args[1].equalsIgnoreCase("all")){
				sender.sendMessage("Could not give a party to player \"" + args[1] + "\". Bummer...");				
			}
			else{
				if(getServer().getOnlinePlayers().length == 0){
					sender.sendMessage("The server is empty, seems like the admin is forever alone");									
				}
				else{
					sender.sendMessage("Somehow, you have escaped all my if statements. Please tell the creator of this plugin what on earth you have done");
				}
					
			}
		}
		return true;
	}
	
	/**
	 * Start swapping colors on all sheeps
	 */
	private void toggleColor(){
		//Try to restart if crashed
		recover();
		
		discoParty.toggleColor();
	}
	
	/**
	 * Spawn objects and start party
	 */
	public void startParty(Player[] players, int partyTime, int size, int spawnRange){
		
		timer.stop();
		
		//Try to restart if crashed
		recover();
		
		timer.setInitialDelay(partyTime * 1000);
		timer.start();
		discoParty.enableParty(players, size, spawnRange);
	}
	
	/**
	 * Disables party
	 */
	private void stopParty() {
		timer.stop();
		discoParty.stopParty();
	}
	/**
	 * Method to restart thread if it ended unexpected
	 */
	private void recover(){
		if(!discoParty.isAlive()){
			System.out.println("[DiscoSheep] is trying to recover from unexpected thread ending");
			discoParty.cleanUp();
			discoParty = new DiscoParty(this);
			discoParty.start();
		}
	}
	
	/**
	 * Called when timer wants to stop party
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		stopParty();		
	}
	/**
	 * User permissions or ops.txt to decide if user can call command
	 * @param sender
	 * @param command
	 * @return
	 */
	public boolean isPermittet(CommandSender sender, String command){
		if(!(sender instanceof Player)){
			//Console wants to run command, always allowed
			return true;
		}
		if(usePermit){
			Player p = (Player)sender;
			if(command.equalsIgnoreCase(help)){
				if(permit.has(p, discosheep+help)){
					return true;
				}else{ pDenied(sender, command); return false; }
			}
			if(command.equalsIgnoreCase(debug)){
				if(permit.has(p, discosheep+debug)){
					return true;
				}else{ pDenied(sender, command); return false; }
			}
			if(command.equalsIgnoreCase(stop)){
				if(permit.has(p, discosheep+stop)){
					return true;
				}else{ pDenied(sender, command); return false; }
			}
			if(command.equalsIgnoreCase(reload)){
				if(permit.has(p, discosheep+reload)){
					return true;
				}else{ pDenied(sender, command); return false; }
			}
			if(command.equalsIgnoreCase(party)){
				if(permit.has(p, discosheep+party)){
					return true;
				}else{ pDenied(sender, command); return false; }
			}
			if(command.equalsIgnoreCase(color)){
				if(permit.has(p, discosheep+color)){
					return true;
				}else{ pDenied(sender, command); return false; }
			}
			sender.sendMessage("[DiscoShep] Permissions do not know what to do with \"" + command + "\"." +
					"\nPlease tell the developer (me) about this and tell his lazy ass he forgot to add it");
			return false;
		}
		
		//Else, if we do not use permission, use ops.txt
		else{
			if(sender.isOp()){
				return true;
			}
			else{
				sender.sendMessage("You are not OP and can't use [DiscoSheep] plugin");
				return false;
			}
			
		}
	}
	private void pDenied(CommandSender sender, String command){
		sender.sendMessage("[DiscoSheep] Permissions do not allow you to use \"" + command + "\"");
	}
	/**
	 * Checks if we have Permissions installed, if not we set usePermit to false and let ops.txt handle it.
	 */
	public void enablePermissons(){
		Plugin pTemp = getServer().getPluginManager().getPlugin("Permissions");
		
		if(pTemp == null){
			System.out.println("[DiscoSheep] Permissions plugin not detected. Will use ops.txt");
			usePermit = false;
			return;
		}
		else{
			getServer().getPluginManager().enablePlugin(pTemp);
			permit = ((Permissions)pTemp).getHandler();
			usePermit = true;
			System.out.println("[DiscoSheep] Permissions plugin detected!");
		}
	
		
	}
	public String printDebug(){
		String ln = "\n";
		StringBuilder s = new StringBuilder();
		
		s.append("timer tick delay: " + timer.getInitialDelay()		+ ln);
		s.append("permissions on? : " + usePermit					+ ln);
		s.append("Permissions obj : " + permit						+ ln);
		s.append("Item looting on : " + settings.dropItems			+ ln);
		s.append("defaultPartyTime: " + settings.defaultPartyTime	+ ln);
		s.append("maxPartyTime    : " + settings.maxPartyTime		+ ln);
		s.append("sheepNumber     : " + settings.sheepNumber		+ ln);
		s.append("maxSheepSpawn   : " + settings.maxSheepNumber		+ ln);
		s.append("spawnDistance   : " + settings.spawnDistance		+ ln);
		s.append("BPdefauPartyTime: " + settings.bigPartyDefaultPartyTime	+ ln);
		s.append("BPmaxPartyTime  : " + settings.bigPartyMaxPartyTime		+ ln);
		s.append("BPsheepNumber   : " + settings.bigPartySheepNumber		+ ln);
		s.append("BPmaxSheepSpawn : " + settings.bigPartyMaxSheepNumber		+ ln);
		s.append("BPspawnDistance : " + settings.bigPartySpawnDistance		+ ln);	
		
		s.append(discoParty.printDebug());
		
		return s.toString();
	}

}
