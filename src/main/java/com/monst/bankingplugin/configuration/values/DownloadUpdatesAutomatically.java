package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;

public class DownloadUpdatesAutomatically extends BooleanConfigurationValue {

    public DownloadUpdatesAutomatically(BankingPlugin plugin) {
        super(plugin, "download-updates-automatically", true);
    }

}
