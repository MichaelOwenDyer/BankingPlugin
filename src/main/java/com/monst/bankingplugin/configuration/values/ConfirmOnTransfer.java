package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class ConfirmOnTransfer extends ConfigurationValue<Boolean> {

    public ConfirmOnTransfer(BankingPlugin plugin) {
        super(plugin, "confirm-on-transfer", true, new BooleanTransformer());
    }

}
