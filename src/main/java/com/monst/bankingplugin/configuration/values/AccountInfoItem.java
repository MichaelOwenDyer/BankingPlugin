package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.MaterialTransformer;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * The item used to display account information.
 */
public class AccountInfoItem extends ConfigurationValue<Optional<Material>> {

    public AccountInfoItem(BankingPlugin plugin) {
        super(plugin, "account-info-item", Optional.of(Material.STICK),
                new MaterialTransformer().optional());
    }

    public boolean isHeldBy(Player player) {
        return get().map(infoItem ->
                        infoItem == player.getInventory().getItemInMainHand().getType()
                                || infoItem == player.getInventory().getItemInOffHand().getType()
                ).orElse(false);
    }
    
}
