package com.monst.bankingplugin.command.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.command.SubCommand;
import com.monst.bankingplugin.configuration.values.BankPolicy;
import com.monst.bankingplugin.entity.Bank;
import com.monst.bankingplugin.event.bank.BankConfigureEvent;
import com.monst.bankingplugin.exception.CancelledException;
import com.monst.bankingplugin.exception.ExecutionException;
import com.monst.bankingplugin.lang.Message;
import com.monst.bankingplugin.lang.Placeholder;
import com.monst.bankingplugin.util.Permission;
import com.monst.bankingplugin.util.Utils;
import com.monst.pluginconfiguration.exception.ArgumentParseException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BankConfigure extends SubCommand {

    public enum PolicyType {
        REIMBURSE_ACCOUNT_CREATION (plugin -> plugin.config().reimburseAccountCreation),
        PAY_ON_LOW_BALANCE (plugin -> plugin.config().payOnLowBalance),
        INTEREST_RATE (plugin -> plugin.config().interestRate),
        ACCOUNT_CREATION_PRICE (plugin -> plugin.config().accountCreationPrice),
        MINIMUM_BALANCE (plugin -> plugin.config().minimumBalance),
        LOW_BALANCE_FEE (plugin -> plugin.config().lowBalanceFee),
        ALLOWED_OFFLINE_PAYOUTS (plugin -> plugin.config().allowedOfflinePayouts),
        OFFLINE_MULTIPLIER_DECREMENT (plugin -> plugin.config().offlineMultiplierDecrement),
        WITHDRAWAL_MULTIPLIER_DECREMENT (plugin -> plugin.config().withdrawalMultiplierDecrement),
        PLAYER_BANK_ACCOUNT_LIMIT (plugin -> plugin.config().playerBankAccountLimit),
        INTEREST_MULTIPLIERS (plugin -> plugin.config().interestMultipliers),
        INTEREST_PAYOUT_TIMES (plugin -> plugin.config().interestPayoutTimes);

        private final Function<BankingPlugin, BankPolicy<?>> getter;
        PolicyType(Function<BankingPlugin, BankPolicy<?>> getter) {
            this.getter = getter;
        }

        static BankPolicy<?> lookUp(BankingPlugin plugin, String name) {
            return Stream.of(values())
                    .filter(field -> field.toString().equalsIgnoreCase(name))
                    .findFirst()
                    .map(p -> p.getter.apply(plugin))
                    .orElse(null);
        }

        @Override
        public String toString() {
            return name().toLowerCase().replace('_', '-');
        }

    }

    BankConfigure(BankingPlugin plugin) {
		super(plugin, "configure");
    }

    @Override
    protected Permission getPermission() {
        return Permission.BANK_CREATE;
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
    protected void execute(CommandSender sender, String[] args) throws ExecutionException, CancelledException {
        String bankName = args[0];
        Bank bank = plugin.getBankService().findByName(bankName);
        if (bank == null)
            throw new ExecutionException(plugin, Message.BANK_NOT_FOUND.with(Placeholder.INPUT).as(bankName));

        if (bank.isPlayerBank() && !((sender instanceof Player && bank.isTrusted((Player) sender))
                || Permission.BANK_CONFIGURE_OTHER.ownedBy(sender)))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_CONFIGURE_OTHER);

        if (bank.isAdminBank() && Permission.BANK_CONFIGURE_ADMIN.notOwnedBy(sender))
            throw new ExecutionException(plugin, Message.NO_PERMISSION_BANK_CONFIGURE_ADMIN);

        String policyName = args[1].toLowerCase();
        BankPolicy<?> policy = PolicyType.lookUp(plugin, policyName);
        if (policy == null)
            throw new ExecutionException(plugin, Message.NOT_A_BANK_POLICY.with(Placeholder.INPUT).as(policyName));

        String input = Arrays.stream(args).skip(2).collect(Collectors.joining(" "));
        String previousValue = policy.toStringAt(bank);

        try {
            if (!policy.parseAndSetAt(bank, input))
                sender.sendMessage(Message.BANK_POLICY_NOT_OVERRIDABLE
                        .with(Placeholder.POLICY).as(policyName)
                        .translate(plugin));
        } catch (ArgumentParseException e) {
            throw new ExecutionException(e.getLocalizedMessage());
        }

        String newValue = policy.toStringAt(bank);

        new BankConfigureEvent(sender, bank, policy, newValue, previousValue).fire();
        plugin.getBankService().update(bank);
        plugin.debugf( "%s has changed %s at %s from %s to %s.",
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
            return plugin.getBankService().findByPlayerAllowedToModify(player, Permission.BANK_CONFIGURE_OTHER, Permission.BANK_CONFIGURE_ADMIN, false)
                    .stream()
                    .map(Bank::getName)
                    .filter(name -> Utils.startsWithIgnoreCase(name, args[0]))
                    .sorted()
                    .collect(Collectors.toList());
        Bank bank = plugin.getBankService().findByName(args[0]);
        if (bank == null)
            return Collections.emptyList();
        if (args.length == 2)
            return Stream.of(PolicyType.values())
                    .map(String::valueOf)
                    .filter(field -> Utils.containsIgnoreCase(field, args[1]))
                    .collect(Collectors.toList());
        BankPolicy<?> policy = PolicyType.lookUp(plugin, args[1]);
        if (policy == null || args.length > 3)
            return Collections.emptyList();
        return Collections.singletonList(policy.toStringAt(bank));
    }

}
