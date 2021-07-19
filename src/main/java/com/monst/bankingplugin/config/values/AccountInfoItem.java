package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AccountInfoItem extends ConfigValue<String, ItemStack> implements NonNativeValue<String, ItemStack> {

    public AccountInfoItem() {
        super("account-info-item", new ItemStack(Material.STICK));
    }

    @Override
    public ItemStack parse(String input) throws MaterialParseException {
        return new ItemStack(Parser.parseMaterial(input));
    }

    @Override
    public String convert(Object o) {
        return (String) o;
    }

    @Override
    public ItemStack convertToActualType(String s) throws CorruptedValueException {
        return Optional.ofNullable(s)
                .map(Material::matchMaterial)
                .map(ItemStack::new)
                .orElseThrow(CorruptedValueException::new);
    }

    @Override
    public String format(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

}
