package Goldsack.DiscoSheep;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * DiscoSheep is a plugin to minecraft servers running minecraft.
 * It changes sheep wool color for fun. 
 * @author goldsack
 *
 */
public class DiscoSheep extends JavaPlugin implements ActionListener{
	protected DiscoColor discoColor;
	protected DiscoParty discoParty;
	protected Timer timer;
	protected int defaultDelay = 10000; //Default time for party to end in millisec

	/**
	 * Called when plugin starts.
	 */
	@Override
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		discoColor = new DiscoColor(this);
		discoParty = new DiscoParty(this);
		timer = new Timer(defaultDelay, this);
		timer.stop(); //Timer starts when its created, so it has to be stopped
	}
	
	/**
	 * Called when plugin ends.
	 * Cancels all threads, and wait for them to end.
	 */
	@Override
	public void onDisable() {
		stopColor();
		stopParty();
		System.out.println("DiscoSheep closed down. Good bye");
	}
	
	/**
	 * Called when /discosheep is sent in game or server
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd,	String commandLabel, String[] args) {
		if(args.length > 0 && (!(sender instanceof Player) || sender.isOp())){

			if(args[0].equalsIgnoreCase("help")){
				return false; //Use plugin.yml to display info
			}
			else if(args[0].equalsIgnoreCase("stop")){
				stopColor();
				stopParty();
				sender.sendMessage("DiscoSheep Stopped");
				return true;
			}
			else if(args[0].equalsIgnoreCase("start")){
				startColor();
				sender.sendMessage("Rainbow sheeps activated");			
				return true;
			}
			else if(args[0].equalsIgnoreCase("party")){
				int partyDelay = defaultDelay;
				
				if(args.length > 1){
					float partyLength = 0;
					try {					
						 partyLength = Float.parseFloat(args[1]);
					} catch (NumberFormatException e) {
						sender.sendMessage(" \"" + args[1] + "\" can not be parsed as an float number. Discosheep does not start a party. BOO D:");
						return true;
					}
					
					partyDelay = (int) (1000 * partyLength);				
				}
				startParty(partyDelay);
				sender.sendMessage("Party ON!");
				return true;
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
	
	/**
	 * Start swapping colors on all sheeps
	 */
	private void startColor(){
		if(!discoColor.isAlive()){
			stopColor();
			discoColor = new DiscoColor(this);
			discoColor.start(); 
		}
	}
	
	/**
	 * Stop swapping colors on all sheeps
	 */
	private void stopColor() {
		try {
			discoColor.end();
			discoColor.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Sheep failed to stop changing colors. Error in DiscoSheep. Failed to stop thread)");
		}
	}
	
	/**
	 * Stop party and cleans up spawned objects
	 */
	private void stopParty() {
		try {
			timer.stop();
			discoParty.end();
			discoParty.join();
			discoParty.cleanUp();
		} catch (InterruptedException e) {
			System.out.println("Party failed to stop. PARTY ON! (This is in fact an error in DiscoSheep. Failed to stop thread)");
			e.printStackTrace();
		}
	}

	/**
	 * Spawn objects and start party
	 */
	public void startParty(int partyTimeMilliSec){
		timer.stop();
		if(!discoParty.isAlive()){
			discoParty = new DiscoParty(this);
			timer.setInitialDelay(partyTimeMilliSec);
			timer.start();
			discoParty.start();
		}
		else{
			timer.setInitialDelay(partyTimeMilliSec);
			timer.start();
		}
		
	}
	
	/**
	 * Called when timer wants to stop party
	 */
	@Override
	public void actionPerformed(ActionEvent arg0) {
		stopParty();		
	}	

}
