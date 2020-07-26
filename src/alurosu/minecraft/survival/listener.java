package alurosu.minecraft.survival;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
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
    	String[] parts = p.getAddress().toString().split(":");
    	
    	if (IPqueue.containsValue(parts[0])) {
        	p.kickPlayer("Anti-flood: please wait a few seconds for the other account to login successfully.");
    	} else {
        	IPqueue.put(p, parts[0]);
        	isLoggedIn.put(p, false);
        	coords.put(p, p.getLocation().getX()+"-"+p.getLocation().getZ());
        	p.setInvulnerable(true);
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
    
    
    public void doLogin(Player p) {
    	isLoggedIn.put(p, true);
    	IPqueue.remove(p);
    	p.setInvulnerable(false);
    }
    
    public boolean isLoggedIn(Player p) {
    	return isLoggedIn.get(p);
    }
    
    public void loginMessage(Player p) {
    	p.sendMessage("/login [pass] or register at §aamongdemons.com");
    }
}
