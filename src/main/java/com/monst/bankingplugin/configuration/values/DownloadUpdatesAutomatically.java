package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.type.BooleanConfigurationValue;

public class DownloadUpdatesAutomatically extends BooleanConfigurationValue {

    public DownloadUpdatesAutomatically(BankingPlugin plugin) {
        super(plugin, "download-updates-automatically", true);
    }

}
