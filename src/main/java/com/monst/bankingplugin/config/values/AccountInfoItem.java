package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class AccountInfoItem extends ConfigValue<String, ItemStack> implements NonNativeString<ItemStack> {

    public AccountInfoItem(BankingPlugin plugin) {
        super(plugin, "account-info-item", new ItemStack(Material.STICK));
    }

    @Override
    public ItemStack parse(String input) throws MaterialParseException {
        return new ItemStack(Parser.parseMaterial(input));
    }

    @Override
    public String format(ItemStack itemStack) {
        return itemStack.getType().toString();
    }

}
