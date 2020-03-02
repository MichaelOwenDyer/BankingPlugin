package com.monst.bankingplugin.external;

import java.util.Optional;

import org.codemc.worldguardwrapper.WorldGuardWrapper;
import org.codemc.worldguardwrapper.flag.IWrappedFlag;
import org.codemc.worldguardwrapper.flag.WrappedState;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;

public class WorldGuardBankingFlag {

    public static void register(final BankingPlugin plugin) {
        WorldGuardWrapper wrapper = WorldGuardWrapper.getInstance();

		Optional<IWrappedFlag<WrappedState>> createBankFlag = wrapper.registerFlag("create-bank", WrappedState.class,
				Config.wgAllowCreateBankDefault ? WrappedState.ALLOW : WrappedState.DENY);
                
        Optional<IWrappedFlag<WrappedState>> createAccountFlag = wrapper.registerFlag("create-account",
                WrappedState.class, Config.wgAllowCreateAccountDefault ? WrappedState.ALLOW : WrappedState.DENY);
        
		plugin.debug("Flag create-bank: " + String.valueOf(createBankFlag.isPresent()));
        plugin.debug("Flag create-account: " + String.valueOf(createAccountFlag.isPresent()));
    }

}
