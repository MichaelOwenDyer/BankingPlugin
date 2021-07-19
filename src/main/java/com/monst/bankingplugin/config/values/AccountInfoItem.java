package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.exceptions.CorruptedValueException;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class AccountInfoItem extends ConfigValue<String, ItemStack> implements NonNativeValue<String, ItemStack> {

    public AccountInfoItem() {
        super("account-info-item", new ItemStack(Material.STICK));
    }

    @Override
    public ItemStack parse(String input) throws MaterialParseException {
        return new ItemStack(Parser.parseMaterial(input));
    }

    @Override
    public String cast(Object o) {
        return (String) o;
    }

    @Override
    public ItemStack translate(String s) throws CorruptedValueException {
        try {
            return parse(s);
        } catch (MaterialParseException e) {
            throw new CorruptedValueException();
        }
    }

    @Override
    public String format(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

}
