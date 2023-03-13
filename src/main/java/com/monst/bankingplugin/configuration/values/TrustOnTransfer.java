package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class TrustOnTransfer extends ConfigurationValue<Boolean> {

    public TrustOnTransfer(BankingPlugin plugin) {
        super(plugin, "trust-on-transfer", true, new BooleanTransformer());
    }

}
