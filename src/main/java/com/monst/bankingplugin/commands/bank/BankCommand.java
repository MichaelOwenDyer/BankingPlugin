package com.monst.bankingplugin.commands.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.bank.Bank;
import com.monst.bankingplugin.commands.BankingPluginCommand;
import com.monst.bankingplugin.commands.BankingPluginSubCommand;
import com.monst.bankingplugin.config.Config;
import com.monst.bankingplugin.utils.BankUtils;
import com.monst.bankingplugin.utils.Messages;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BankCommand extends BankingPluginCommand<BankCommand.SubCommand> {

	private static boolean commandCreated = false;

    public BankCommand(final BankingPlugin plugin) {
    	    	
        super(plugin);
        
        if (commandCreated) {
            IllegalStateException e = new IllegalStateException("Command \"" + Config.mainCommandNameBank + "\" has already been registered!");
            plugin.debug(e);
            throw e;
        }
        
        this.name = Config.mainCommandNameBank;
        this.desc = Messages.BANK_COMMAND_DESC;
		this.pluginCommand = super.createPluginCommand();

		addSubCommand(new BankCreate());
		addSubCommand(new BankInfo());
		addSubCommand(new BankLimits());
		addSubCommand(new BankList());
		addSubCommand(new BankRemove());
		addSubCommand(new BankRemoveall());
		addSubCommand(new BankRename());
		addSubCommand(new BankResize());
		addSubCommand(new BankSelect());
		addSubCommand(new BankSet());
		addSubCommand(new BankTransfer());
		addSubCommand(new BankTrust());
		addSubCommand(new BankUntrust());

        register();
        commandCreated = true;

    }

    abstract static class SubCommand extends BankingPluginSubCommand {

		static final BankUtils bankUtils = plugin.getBankUtils();

		SubCommand(String name, boolean playerCommand) {
			super(name, playerCommand);
		}

		Bank getBank(CommandSender sender, String[] args) {
			Bank bank = null;
			if (args.length == 1) {
				if (sender instanceof Player) {
					Player p = (Player) sender;
					bank = bankUtils.getBank(p.getLocation());
					if (bank == null) {
						plugin.debug(p.getName() + " wasn't standing in a bank");
						p.sendMessage(Messages.NOT_STANDING_IN_BANK);
					}
				} else {
					sender.sendMessage(Messages.PLAYER_COMMAND_ONLY);
				}
			} else {
				bank = bankUtils.lookupBank(args[1]);
				if (bank == null) {
					plugin.debugf(Messages.BANK_NOT_FOUND, args[1]);
					sender.sendMessage(String.format(Messages.BANK_NOT_FOUND, args[1]));
				}
			}
			return bank;
		}

		String getCoordLookingAt(Player p, int argLength) {
			Location loc = p.getTargetBlock(null, 150).getLocation();
			switch (argLength % 3) {
				case 0: return "" + loc.getBlockX();
				case 1: return "" + loc.getBlockY();
				case 2: return "" + loc.getBlockZ();
			}
			return "";
		}
	}

}
