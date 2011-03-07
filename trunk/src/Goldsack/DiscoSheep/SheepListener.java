package Goldsack.DiscoSheep;

import org.bukkit.entity.Entity;

import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

public class SheepListener extends EntityListener{
	DiscoSheep plugin;
	public SheepListener(DiscoSheep discoSheep) {
		plugin = discoSheep;
	}
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		super.onEntityDamage(event);
		Entity entity = event.getEntity();
		if(!plugin.settings.dropItems){
			event.setCancelled(plugin.discoParty.isOurEntity(entity));			
		}
		
	}

}
