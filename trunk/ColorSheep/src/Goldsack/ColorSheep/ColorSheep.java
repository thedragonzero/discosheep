package Goldsack.ColorSheep;


import java.util.Random;

import net.minecraft.server.WorldServer;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * ColorsSheep is a plugin to minecraft servers running minecraft.
 * It changes sheep wool color around the player who called the method. 
 * @author goldsack
 *
 */
public class ColorSheep extends JavaPlugin{

	protected ColorSettings settings;
	protected ColorPermission permit;
	public static final DyeColor dyeColors[] = {
		DyeColor.WHITE,
		DyeColor.ORANGE,
		DyeColor.MAGENTA,
		DyeColor.LIGHT_BLUE,
		DyeColor.YELLOW,
		DyeColor.LIME,
		DyeColor.PINK,
		DyeColor.GRAY,
		DyeColor.SILVER,
		DyeColor.CYAN,
		DyeColor.PURPLE,
		DyeColor.BLUE,
		DyeColor.BROWN,
		DyeColor.GREEN,
		DyeColor.RED,
		DyeColor.BLACK
		};
	

	/**
	 * Called when plugin starts.
	 */
	@Override
	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();	
		
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println( "[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is enabled!" );
		permit = new ColorPermission(this);
		settings = new ColorSettings(this);
	}
	
	/**
	 * Called when plugin ends.
	 * Cancels all threads, and wait for them to end.
	 */
	@Override
	public void onDisable() {

		System.out.println("[ColorSheep] closed down. Good bye");
	}
	
	/**
	 * Called when /color is sent in game or server
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
		int itemID = ((IntS)settings.getSetting("itemID")).getV();
		int numberOfItems = ((IntS)settings.getSetting("numberOfItems")).getV();
		int range = ((IntS)settings.getSetting("radius")).getV();
		String itemName = getItemName(itemID);
		if(args.length == 0){
			if(sender instanceof Player){
				if(permit.isPermittet(sender, permit.USE, true)){
					Player p = (Player)sender;
					if(p.getItemInHand().getTypeId() == itemID){
						if(p.getItemInHand().getAmount() >= numberOfItems){
							int i = transformSheeps(p);
							if(i == 0){
								sender.sendMessage("No sheeps within " + range + " blocks range, " + itemName +" has not been consumed.");
								return true;
								
							}
							else{
								p.getItemInHand().setAmount( p.getItemInHand().getAmount() - numberOfItems);
								sender.sendMessage( i + " sheeps has been transformed!");
								return true;
							}
							
							
							//Count sheeps number
						}
						else{
							sender.sendMessage("You need to hold at least " + numberOfItems + " " + itemName + " in your hand to transform sheeps.");
							return true;
						}
					}
					else{
						sender.sendMessage("You need to hold at least " + numberOfItems + " " + itemName + " in your hand to transform sheeps. " +
								"You are holding " + getItemName(p.getItemInHand().getTypeId()) + ".");
						return true;
					}	
				}
				else{
					//User was not permittet
					return true;
				}
			}
			else{
				//Send message to server
				sender.sendMessage("You need to add the name of player who are going to recive a sheep transformation. Example \"color playerName\"");
				return true;
			}
		}
		
		else if(args.length == 1){
			if(args[0].equalsIgnoreCase("reload")){
				if(permit.isPermittet(sender, permit.RELOAD, true)){
					settings.reload();
					sender.sendMessage("ColorSheep.properties has been reloaded");
				}
				return true;
			}
			if(args[0].equalsIgnoreCase("debug")){
				if(permit.isPermittet(sender, permit.DEBUG, true)){
					sender.sendMessage(printDebug());
				}
				return true;
			}	
			if(args[0].equalsIgnoreCase("help")){
				if(permit.isPermittet(sender, permit.HELP, true)){
					return false; //Use plugin.yml to display help
				}
				return true;
			}	
		}
		return false;
	}


	private int transformSheeps(Player p) {
		World world = getServer().getWorld(p.getWorld().getName());
		
		int n = 0;
		
		for(Entity e : world.getLivingEntities()){
			if(e instanceof Sheep){
				if(sheepInDistance(p, (Sheep)e)){
					n++;
					changeColor((Sheep) e);
					e.setVelocity(e.getVelocity().setY(0.5)); //Make the sheeps jump
				}
			}
		}
		return n;
	}
	/**
	 * See if sheep is within distance of player according to radius in ColorSheep.properties
	 * @param player
	 * @param sheep
	 * @return
	 */
	private boolean sheepInDistance(Player player, Sheep sheep) {
		
		int maxDistance = ((IntS)settings.getSetting("radius")).getV();
		Location p = player.getLocation();
		Location e = sheep.getLocation();
		double mag = Math.pow(p.getX() - e.getX(), 2) + Math.pow(p.getY() - e.getY(), 2) + Math.pow(p.getZ() - e.getZ(), 2);
		double absLength = Math.sqrt(mag);
		if(absLength <= maxDistance){
			return true;
		}
		return false;
	}

	private String getItemName(int itemID) {
		for(Material m : Material.values()){
			if(m.getId() == itemID){
				return m.toString().toLowerCase();
			}
		}
		return null;
	}
	
	/**
	 * Swap wool colour to random on sheep entity
	 * @param entity
	 */
	private void changeColor(Sheep entity) {
		try {
			Random r = new Random();
			entity.setColor(dyeColors[r.nextInt(dyeColors.length)]);
		} catch (Exception e) {
			System.out.println("[ColorSheep] Error: A sheep could not change colour");
		}

	}
	public String printDebug(){
		String ln = "\n";
		StringBuilder s = new StringBuilder();
		
		s.append("permissions on? : " + permit.usePermit			+ ln);
		s.append("Permissions obj : " + permit.permit				+ ln);
		

		for(Setting setting: settings.settings){
			if(setting instanceof BoolS){
				s.append(setting.sN + " : " + ((BoolS) setting).getV() + ln);
			}
			else if(setting instanceof IntS){
				s.append(setting.sN + " : " + ((IntS) setting).getV() + ln);
				
			}
			else if(setting instanceof FloS){
				s.append(setting.sN + " : " + ((FloS) setting).getV() + ln);
				
			}
			else{
				System.out.println("[ColorSheep] The plugindeveloper has forgot to add support for " + setting.getClass().getName());
			}
			
		}

		return s.toString();
	}

}
