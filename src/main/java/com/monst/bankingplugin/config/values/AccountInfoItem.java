package com.monst.bankingplugin.config.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.exceptions.parse.MaterialParseException;
import com.monst.bankingplugin.utils.Parser;
import org.bukkit.Material;

import java.util.Optional;

public class AccountInfoItem extends ConfigValue<String, Optional<Material>> implements NonNativeString<Optional<Material>> {

    public AccountInfoItem(BankingPlugin plugin) {
        super(plugin, "account-info-item", Optional.of(Material.STICK));
    }

    @Override
    public Optional<Material> parse(String input) throws MaterialParseException {
        if (input.isEmpty())
            return Optional.empty();
        return Optional.ofNullable(Parser.parseMaterial(input));
    }

    @Override
    public String format(Optional<Material> material) {
        return material.map(Material::toString).orElse("");
    }

    @Override
    boolean isOptional() {
        return true;
    }

}
