package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.MaterialParseException;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AccountInfoItem extends ConfigValue<ItemStack> {

    public AccountInfoItem() {
        super("account-info-item", new ItemStack(Material.STICK));
    }

    @Override
    public ItemStack parse(String input) throws MaterialParseException {
        return Optional.ofNullable(input)
                .map(Material::getMaterial)
                .map(ItemStack::new)
                .orElseThrow(() -> new MaterialParseException(input));
    }

    @Override
    public ItemStack readValueFromFile(MemoryConfiguration config, String path) {
        return Optional.ofNullable(config.getString(path))
                .map(Material::getMaterial)
                .map(ItemStack::new)
                .orElse(null);
    }

    @Override
    public String format(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

}
