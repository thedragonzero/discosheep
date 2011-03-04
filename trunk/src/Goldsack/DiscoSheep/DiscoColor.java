package Goldsack.DiscoSheep;

import java.awt.MultipleGradientPaint.ColorSpaceType;
import java.util.Random;

import javax.swing.text.html.parser.Entity;

import org.bukkit.DyeColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftSheep;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;
/**
 * This class goes over all sheeps and changes its color to random
 * When it stops, the sheep still keep their fresh new paint
 * @author goldsack
 *
 */
public class DiscoColor extends Thread{
	private DiscoSheep plugin;
	protected boolean running;
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
	
	public DiscoColor(DiscoSheep discoSheep) {
		plugin = discoSheep;
		running = false;
	}
	
	public void run() {
		try {
		running = true;
			while(running){
				for(World world: plugin.getServer().getWorlds()){
					for(LivingEntity entity: world.getLivingEntities()){
						if(entity != null && entity instanceof Sheep){
							try {
								changeColor((Sheep) entity);
							} catch (Exception e) {
								System.out.println("A sheep could not change colour (DiscoSheep");
							}
							
						}
					}
				}
				sleepParty(475);
			}
			
		} catch (Exception e) {
			System.out.println("DiscoColor has failed. Thread ended");
			running = false;
			//e.printStackTrace();
		}
	}

	/**
	 * Swap wool colour to random on sheep entity
	 * @param entity
	 */
	private void changeColor(Sheep entity) {
		Random r = new Random();
		entity.setColor(dyeColors[r.nextInt(dyeColors.length)]);
	}
	/**
	 * Return true if sheeps are changing colors
	 * @return
	 */
	public boolean isRunning() {
		return running;
	};
	/**
	 * Calls for colorswapping to end
	 */
	public void end() {
		running = false;
		
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
