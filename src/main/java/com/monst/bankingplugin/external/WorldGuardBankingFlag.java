package com.monst.bankingplugin.external;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import java.util.Optional;

public class WorldGuardBankingFlag {

    public static void register(final BankingPlugin plugin) {

		Optional<IWrappedFlag<WrappedState>> createBankFlag = WorldGuardWrapper.getInstance()
                .registerFlag("create-bank", WrappedState.class, Config.wgAllowCreateBankDefault
                        ? WrappedState.ALLOW
                        : WrappedState.DENY);
                
		plugin.debug("Flag create-bank: " + createBankFlag.isPresent());
    }

}
