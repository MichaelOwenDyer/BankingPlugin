package com.monst.bankingplugin.config.values.simple;

import com.monst.bankingplugin.config.values.ConfigValue;
import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
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
        return new ItemStack(Parser.parseMaterial(input));
    }

    @Override
    public ItemStack readFromFile(MemoryConfiguration config, String path) throws CorruptedValueException {
        return Optional.ofNullable(config.getString(path))
                .map(Material::matchMaterial)
                .map(ItemStack::new)
                .orElseThrow(CorruptedValueException::new);
    }

    @Override
    public String format(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

    @Override
    public Object convertToSettableType(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

}
