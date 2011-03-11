package Goldsack.ColorSheep;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class ColorPermission {
	private ColorSheep plugin;
	//Permission plugin settings
	protected PermissionHandler permit = null;
	protected boolean usePermit = false;

	//To be used in permission. Etc - 'colorsheep.*'
	private final String COLORSHEEP = "colorsheep"; 
	private final char DOT 			= '.';
	//Commands
	public final String USE 		= "use";
	public final String HELP 		= "help";
	public final String RELOAD		= "reload";
	public final String DEBUG		= "debug";
	
	public ColorPermission(ColorSheep discoSheep) {
		plugin = discoSheep;
		enablePermissons();
	}
	/**
	 * Checks if we have Permissions installed, if not we set usePermit to false and let ops.txt handle it.
	 */
	public void enablePermissons(){
		Plugin pTemp = plugin.getServer().getPluginManager().getPlugin("Permissions");
		
		if(pTemp == null){
			System.out.println("[ColorSheep] Permissions plugin not detected. Will use ops.txt");
			usePermit = false;
			return;
		}
		else{
			plugin.getServer().getPluginManager().enablePlugin(pTemp);
			permit = ((Permissions)pTemp).getHandler();
			usePermit = true;
			System.out.println("[ColorSheep] Permissions plugin detected!");
		}		
	}
	/**
	 * User permissions or ops.txt to decide if user can call command
	 * 
	 * send message is set to false when you don't want to send a message to sender on failed permissions
	 * @param sender
	 * @param command
	 * @return
	 */
	
	public boolean isPermittet(CommandSender sender, String command, boolean sendMessage){
		if(!(sender instanceof Player)){
			//Console wants to run command, always allowed
			return true;
		}
		if(usePermit){
			Player p = (Player)sender;
			
			String[][] pList = {
					{USE, 		COLORSHEEP+DOT+USE},
					{HELP, 		COLORSHEEP+DOT+HELP},
					{RELOAD, 	COLORSHEEP+DOT+RELOAD},
					{DEBUG, 	COLORSHEEP+DOT+DEBUG},
			};
			
			for (int i = 0; i < pList.length; i++) {
				int result = pCheck(p, command, pList[i][0], pList[i][1]);
				if(result == 1){return true;}
				if(result == 0){
					if(sendMessage){
						sender.sendMessage("[ColorSheep] You are not registered in permissions to use \"" + pList[i][1] + "\"");
					}
					return false;
				}
				//if result == -1 then this was not the command we were looking for.
				//Continue to check next element in pList
			}
			
			sender.sendMessage("[ColorShep] Permissions do not know what to do with \"" + command + "\"." +
					"\nPlease tell the developer (me) about this and tell his lazy ass he forgot to add it");
			return false;
		}
		
		//Else, if we do not use permission, use ops.txt
		else{
			if(sender.isOp()){
				return true;
			}
			else{
				if(sendMessage){
					sender.sendMessage("You are not OP and can't use [ColorSheep] plugin");
				}
				return false;
			}
			
		}
	}

	private int pCheck(Player p, String command, String small, String full){
		
		//Example, small is the command "use"
		//If command equals "use"
		if(command.equalsIgnoreCase(small)){
			//Then we ask if player can use full where full equals "colorsheep.use"
			if(permit.has(p, full)){
				return 1;
			}
			else{
				return 0; 
			}
		}
		return -1; //Not matching case
	}
	


}
