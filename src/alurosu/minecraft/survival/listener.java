package alurosu.minecraft.survival;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class listener implements Listener{	
	private Map<Player, String> coords = new HashMap<>();
	public Map<Player, Boolean> isLoggedIn = new HashMap<>();
	private Map<Player, String> IPqueue = new HashMap<>();
	
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
    	Player p = event.getPlayer();
    	p.setInvulnerable(false);
    	String[] parts = p.getAddress().toString().split(":");
    	
    	if (IPqueue.containsValue(parts[0])) {
        	p.kickPlayer("Anti-flood: please wait a few seconds for the other account to login successfully.");
    	} else {
        	IPqueue.put(p, parts[0]);
        	isLoggedIn.put(p, false);
        	coords.put(p, p.getLocation().getX()+"-"+p.getLocation().getZ());
        	plugin.reloadConnection(p);
    	}
    }
    
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
    	Player p = event.getPlayer();
    	isLoggedIn.remove(p);
    	coords.remove(p);
    	IPqueue.remove(p);
    }
    
    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
    	String m = event.getMessage();
    	if (m.contains("admin") || m.contains("owner")) {
        	event.getPlayer().sendMessage("Contact the admin on Discord. You can find the invitation link at §aamongdemons.com");
    	} else if (m.contains("kit")) {
        	event.getPlayer().sendMessage("You can get §6kits §fwith §bsouls §fat §aamongdemons.com");
    	}
    }
    
    
    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
    	Player p = (Player) event.getWhoClicked();
    	if (!isLoggedIn.get(p)) {
			loginMessage(p);
	        event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
			loginMessage(p);
	        event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
    		loginMessage(p);
	        event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
			loginMessage(p);
	        event.setCancelled(true);
    	}
    }
    
    @EventHandler
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p)) {
    		String s = event.getTo().getX()+"-"+event.getTo().getZ();
    		if (!s.equals(coords.get(p))) {
    			loginMessage(p);
    	        event.setCancelled(true);
    		}
    	}
    }
    
    @EventHandler
    public void onPlayerDeathEvent(PlayerDeathEvent event) {
    	Player p = event.getEntity();
    	p.sendMessage("§7You died at "+ (int)p.getLocation().getX() + " / " + (int)p.getLocation().getY() + " / " + (int)p.getLocation().getZ() + " in '" + p.getLocation().getWorld().getName() + "'");
    }
    
    @EventHandler
    public void onPlayerBucketEmptyEvent(PlayerBucketEmptyEvent event) {
    	Player p = event.getPlayer();
    	if (isLoggedIn.get(p)) {
    		if(event.getBucket() == Material.LAVA_BUCKET) {
    			int y = event.getBlockClicked().getY();
    			if (y>62) {
    				p.sendMessage("§cAnti-grief: §fYou can't place lava above sea level. (height 62)");
    				event.setCancelled(true);
    			}
    		}
    	}
    }
    
    @EventHandler
    public void onBlockDispenseEvent(BlockDispenseEvent event) {
		if(event.getItem().getType() == Material.LAVA_BUCKET) {
			int y = event.getBlock().getY();
			if (y>62) {
				event.setCancelled(true);
			}
		}
    }
    
    @EventHandler
    public void onPlayerAdvancementDoneEvent(PlayerAdvancementDoneEvent event) {
    	Player p = event.getPlayer();
		if (!event.getAdvancement().getKey().toString().contains("recipes/")) {
			plugin.provider.depositPlayer(p, 20);
			p.sendTitle("", "                                                 §6+20 §bsouls§f", 10, 70, 10);
		}
    }
    
    @EventHandler
    public void onEntityDamage(EntityDamageEvent event){
    	double damage = event.getDamage();
    	
        if(event instanceof EntityDamageByEntityEvent){
            EntityDamageByEntityEvent edbeEvent = (EntityDamageByEntityEvent)event;
            if(!(edbeEvent.getDamager() instanceof Player)){
            	event.setDamage(damage*2);
            }
        } else event.setDamage(damage*2);
    }
    
    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event){
    	Player p = event.getPlayer();
    	if (!isLoggedIn.get(p) && !(event.getMessage().contains("/l") || event.getMessage().contains("/login"))) {
    		event.setCancelled(true);
    		loginMessage(p);
    	} 
    }
    
    public void doLogin(Player p) {
    	isLoggedIn.put(p, true);
    	IPqueue.remove(p);
    }
    
    public boolean isLoggedIn(Player p) {
    	return isLoggedIn.get(p);
    }
    
    public void loginMessage(Player p) {
    	p.sendMessage("Register on our website §aamongdemons.com");
    	p.sendMessage("§fthen use §7/login password");
		p.sendTitle("/login password", "Use your §aamongdemons.com§f account", 10, 70, 10);
    }
}
