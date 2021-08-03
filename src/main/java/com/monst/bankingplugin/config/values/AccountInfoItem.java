package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class AccountInfoItem extends ConfigValue<String, Optional<ItemStack>> implements NonNativeString<Optional<ItemStack>> {

    public AccountInfoItem(BankingPlugin plugin) {
        super(plugin, "account-info-item", Optional.of(new ItemStack(Material.STICK)));
    }

    @Override
    public Optional<ItemStack> parse(String input) throws MaterialParseException {
        if (input.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(Parser.parseMaterial(input)).map(ItemStack::new);
    }

    @Override
    public String format(Optional<ItemStack> itemStack) {
        return itemStack.map(ItemStack::getType).map(Material::toString).orElse("");
    }

    @Override
    boolean nonOptional() {
        return false;
    }

}
