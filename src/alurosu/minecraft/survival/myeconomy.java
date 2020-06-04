package alurosu.minecraft.survival;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class myeconomy implements Economy{

	@Override
	public EconomyResponse bankBalance(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse bankDeposit(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankHas(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse bankWithdraw(String arg0, double arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse createBank(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public boolean createPlayerAccount(String arg0) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(String arg0, String arg1) {
		return false;
	}

	@Override
	public boolean createPlayerAccount(OfflinePlayer arg0, String arg1) {
		return false;
	}

	@Override
	public String currencyNamePlural() {
		return "souls";
	}

	@Override
	public String currencyNameSingular() {
		return "soul";
	}

	@Override
	public EconomyResponse deleteBank(String arg0) {
		return null;
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, double arg1) {
		return depositPlayer(Bukkit.getPlayer(arg0),arg1);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer offlinePlayer, double amount) {    
        try {
	    	plugin.connection = plugin.getConnection();

	    	// update souls
	    	String sql = "UPDATE users SET souls = souls + "+amount+" WHERE user='"+offlinePlayer.getName()+"'";
    		plugin.connection.prepareStatement(sql).execute();
                
    		return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Funds withdrawn from account.");
		} catch (SQLException e) {
			e.printStackTrace();
	        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "SQL error");
		}
	}

	@Override
	public EconomyResponse depositPlayer(String arg0, String arg1, double arg2) {
		return depositPlayer(arg0, arg2);
	}

	@Override
	public EconomyResponse depositPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		return depositPlayer(arg0,arg2);
	}

	@Override
	public String format(double arg0) {
		return ""+(int) arg0;
	}

	@Override
	public int fractionalDigits() {
		return -1;
	}

	@Override
	public double getBalance(String arg0) {
		try {
	    	plugin.connection = plugin.getConnection();

	    	// get souls
	    	String sql = "SELECT souls FROM users WHERE user='"+arg0+"'";
    		ResultSet results = plugin.connection.prepareStatement(sql).executeQuery();
    		if (results.first()) {
    			return results.getInt(1);
    		}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return 0;
	}

	@Override
	public double getBalance(OfflinePlayer arg0) {
		return getBalance(arg0.getName());
	}

	@Override
	public double getBalance(String arg0, String arg1) {
		return getBalance(arg0);
	}

	@Override
	public double getBalance(OfflinePlayer arg0, String arg1) {
		return getBalance(arg0);
	}

	@Override
	public List<String> getBanks() {
		return null;
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public boolean has(String arg0, double arg1) {
		return has(Bukkit.getPlayer(arg0),arg1);
	}

	@Override
	public boolean has(OfflinePlayer player, double amount) {		
		if (getBalance(player)>=amount)
			return true;
		return false;
	}

	@Override
	public boolean has(String arg0, String arg1, double arg2) {
		return has(arg0, arg2);
	}

	@Override
	public boolean has(OfflinePlayer arg0, String arg1, double arg2) {
		return has(arg0, arg2);
	}

	@Override
	public boolean hasAccount(String arg0) {
		return true;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0) {
		return true;
	}

	@Override
	public boolean hasAccount(String arg0, String arg1) {
		return true;
	}

	@Override
	public boolean hasAccount(OfflinePlayer arg0, String arg1) {
		return true;
	}

	@Override
	public boolean hasBankSupport() {
		return false;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankMember(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, String arg1) {
		return null;
	}

	@Override
	public EconomyResponse isBankOwner(String arg0, OfflinePlayer arg1) {
		return null;
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, double arg1) {
		return withdrawPlayer(Bukkit.getPlayer(arg0),arg1);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer offlinePlayer, double amount) {
        if (has(offlinePlayer, amount)) {            
            try {
    	    	plugin.connection = plugin.getConnection();

    	    	// update souls
    	    	String sql = "UPDATE users SET souls = souls - "+amount+" WHERE user='"+offlinePlayer.getName()+"'";
        		plugin.connection.prepareStatement(sql).execute();
                    
        		return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.SUCCESS, "Funds withdrawn from account.");
    		} catch (SQLException e) {
    			e.printStackTrace();
    	        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "SQL error");
    		}
        } else {
            return new EconomyResponse(amount, getBalance(offlinePlayer), EconomyResponse.ResponseType.FAILURE, "PlayerAccount lacking funds.");
        }
	}

	@Override
	public EconomyResponse withdrawPlayer(String arg0, String arg1, double arg2) {
		return withdrawPlayer(arg0, arg2);
	}

	@Override
	public EconomyResponse withdrawPlayer(OfflinePlayer arg0, String arg1, double arg2) {
		return withdrawPlayer(arg0, arg2);
	}

}
