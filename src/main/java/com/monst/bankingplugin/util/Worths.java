package com.monst.bankingplugin.util;

import com.earth2me.essentials.Essentials;
import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.entity.Account;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.Map;

public class Worths {
    
    private final BankingPlugin plugin;
    private final Essentials essentials;
    
    public Worths(BankingPlugin plugin, Essentials essentials) {
        this.plugin = plugin;
        this.essentials = essentials;
    }
    
    /**
     * Appraises the contents of the given account, assuming its chest can be found.
     * The account balance is then updated.
     * @param account the account to appraise
     * @see Account#getContents()
     */
    public BigDecimal appraise(Account account) {
        return account.getContents().entrySet().stream()
                .map(this::getWorth)
                .reduce(BigDecimal::add)
                .orElse(BigDecimal.ZERO);
    }
    
    private BigDecimal getWorth(Map.Entry<Material, Integer> materialAmount) {
        return getWorth(materialAmount.getKey(), materialAmount.getValue());
    }
    
    public BigDecimal getWorth(Material material, int amount) {
        return getWorth(material).multiply(BigDecimal.valueOf(amount));
    }
    
    public BigDecimal getWorth(Material material) {
        if (plugin.config().blacklist.contains(material))
            return BigDecimal.ZERO;
        BigDecimal worth = essentials.getWorth().getPrice(essentials, new ItemStack(material));
        return worth != null ? worth : BigDecimal.ZERO;
    }
    
}
