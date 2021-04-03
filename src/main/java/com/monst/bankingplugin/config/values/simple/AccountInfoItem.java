package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.exceptions.ItemStackParseException;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AccountInfoItem extends SimpleValue<ItemStack> {

    public AccountInfoItem() {
        super("account-info-item", new ItemStack(Material.STICK), AccountInfoItem::getItemStack);
    }

    @Override
    public ItemStack parse(String input) throws ItemStackParseException {
        return parseItemStack(input).orElseThrow(ItemStackParseException::new);
    }

    private static ItemStack getItemStack(MemorySection config, String path) {
        return parseItemStack(config.getString(path)).orElse(null);
    }

    private static Optional<ItemStack> parseItemStack(String name) {
        return Optional.ofNullable(name)
                .map(Material::getMaterial)
                .map(ItemStack::new);
    }

}
