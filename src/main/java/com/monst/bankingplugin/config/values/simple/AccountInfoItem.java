package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.ItemStackParseException;
import org.bukkit.Material;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AccountInfoItem extends ConfigValue<ItemStack> {

    public AccountInfoItem() {
        super("account-info-item", new ItemStack(Material.STICK));
    }

    @Override
    public ItemStack readValueFromFile(MemoryConfiguration config, String path) {
        return Optional.ofNullable(config.getString(path))
                .map(Material::getMaterial)
                .map(ItemStack::new)
                .orElse(null);
    }

    @Override
    public ItemStack parse(String input) throws ItemStackParseException {
        return parseItemStack(input).orElseThrow(ItemStackParseException::new);
    }

    private static Optional<ItemStack> parseItemStack(String name) {
        return Optional.ofNullable(name)
                .map(Material::getMaterial)
                .map(ItemStack::new);
    }

    @Override
    public String format(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

}
