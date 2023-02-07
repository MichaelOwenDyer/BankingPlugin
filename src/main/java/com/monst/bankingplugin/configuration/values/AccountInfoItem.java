package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.ConfigurationValue;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * The item used to display account information.
 */
public class AccountInfoItem extends ConfigurationValue<Optional<Material>> {

    public AccountInfoItem(BankingPlugin plugin) {
        super(plugin, "account-info-item", Optional.of(Material.STICK));
    }

    @Override
    public Optional<Material> parse(String input) throws ArgumentParseException {
        if (input.equals("_"))
            return Optional.empty();
        return Optional.of(Optional.ofNullable(Material.matchMaterial(input))
                .orElseThrow(() -> new ArgumentParseException(Message.NOT_A_MATERIAL.with(Placeholder.INPUT).as(input))));
    }

    @Override
    public String format(Optional<Material> material) {
        return material.map(Material::toString).orElse("_");
    }

    @Override
    protected Object convertToYamlType(Optional<Material> material) {
        return format(material);
    }

    public boolean isHeldBy(Player player) {
        return get().map(infoItem ->
                        infoItem == player.getInventory().getItemInMainHand().getType()
                                || infoItem == player.getInventory().getItemInOffHand().getType()
                ).orElse(false);
    }
    
}
