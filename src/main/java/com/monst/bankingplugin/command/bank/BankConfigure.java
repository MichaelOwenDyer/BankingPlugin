package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.Permission;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.configuration.exception.ArgumentParseException;
import com.monst.bankingplugin.configuration.type.ConfigurationValue;
import com.monst.bankingplugin.configuration.values.BankPolicy;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.bank.BankConfigureEvent;
import com.monst.bankingplugin.exception.CommandExecutionException;
import com.monst.bankingplugin.exception.EventCancelledException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.command.Permissions;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BankConfigure extends SubCommand {
    
    private final Map<String, BankPolicy<?>> policies;

    BankConfigure(BankingPlugin plugin) {
		super(plugin, "configure");
        policies = Stream.of(
                        plugin.config().interestRate,
                        plugin.config().accountCreationPrice,
                        plugin.config().minimumBalance,
                        plugin.config().lowBalanceFee,
                        plugin.config().allowedOfflinePayouts,
                        plugin.config().offlineMultiplierDecrement,
                        plugin.config().withdrawalMultiplierDecrement,
                        plugin.config().playerBankAccountLimit,
                        plugin.config().reimburseAccountCreation,
                        plugin.config().payOnLowBalance,
                        plugin.config().interestMultipliers,
                        plugin.config().interestPayoutTimes
                )
                .collect(Collectors.toMap(ConfigurationValue::getPath, Function.identity()));
    }

    @Override
    protected Permission getPermission() {
        return Permissions.BANK_CREATE;
    }

    @Override
    protected Message getUsageMessage() {
        return Message.COMMAND_USAGE_BANK_CONFIGURE;
    }

    @Override
    protected Message getNoPermissionMessage() {
        return Message.NO_PERMISSION_BANK_CONFIGURE;
    }

    @Override
    protected int getMinimumArguments() {
        return 2;
    }

    @Override
    protected void execute(CommandSender sender, String[] args) throws CommandExecutionException, EventCancelledException {
        String bankName = args[0];
        Bank bank = plugin.getBankService().findByName(bankName);
        if (bank == null)
            throw err(Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(bankName));

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isTrusted((Player) sender))
                || Permissions.BANK_CONFIGURE_OTHER.ownedBy(sender)))
            throw err(Message.NO_PERMISSION_BANK_CONFIGURE_OTHER);

        if (bank.isAdminBank() && Permissions.BANK_CONFIGURE_ADMIN.notOwnedBy(sender))
            throw err(Message.NO_PERMISSION_BANK_CONFIGURE_ADMIN);

        String policyName = args[1].toLowerCase();
        BankPolicy<?> policy = policies.get(policyName);
        if (policy == null)
            throw err(Message.NOT_A_BANK_POLICY.with(Placeholder.INPUT).as(policyName));

        String input = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
        String previousValue = policy.toStringAt(bank);

        try {
            if (!policy.parseAndSetAt(bank, input))
                sender.sendMessage(Message.BANK_POLICY_NOT_OVERRIDABLE
                        .with(Placeholder.POLICY).as(policyName)
                        .translate(plugin));
        } catch (ArgumentParseException e) {
            throw err(e.getTranslatableMessage());
        }

        String newValue = policy.toStringAt(bank);

        new BankConfigureEvent(sender, bank, policy, newValue, previousValue).fire();
        plugin.getBankService().update(bank);
        plugin.debug( "%s has changed %s at %s from %s to %s.",
                sender.getName(), policyName, bank.getName(), previousValue, newValue);
        String message = Message.BANK_POLICY_SET
                .with(Placeholder.POLICY).as(policyName)
                .and(Placeholder.BANK_NAME).as(bank.getColorizedName())
                .and(Placeholder.PREVIOUS_VALUE).as(previousValue)
                .and(Placeholder.VALUE).as(newValue)
                .translate(plugin);
        bank.getMailingList(sender).forEach(player -> player.sendMessage(message));
    }

    @Override
    protected List<String> getTabCompletions(Player player, String[] args) {
        if (args.length == 1)
            return plugin.getBankService().findNamesByPlayerAllowedToModify(player,
                            Permissions.BANK_CONFIGURE_OTHER.ownedBy(player),
                            Permissions.BANK_CONFIGURE_ADMIN.ownedBy(player), false)
                    .stream()
                    .filter(name -> StringUtil.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            return Collections.emptyList();
        if (args.length == 2)
            return policies.keySet().stream()
                    .filter(policyName -> StringUtils.startsWithIgnoreCase(policyName, args[1])) // TODO: Use a containsIgnoreCase method instead
                    .sorted()
                    .collect(Collectors.toList());
        BankPolicy<?> policy = policies.get(args[1]);
        if (policy == null || args.length > 3)
            return Collections.emptyList();
        return Collections.singletonList(policy.toStringAt(bank));
    }

}
