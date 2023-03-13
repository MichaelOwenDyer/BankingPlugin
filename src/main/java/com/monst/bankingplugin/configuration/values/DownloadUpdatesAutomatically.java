package com.monst.bankingplugin.configuration.values;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.configuration.ConfigurationValue;
import com.monst.bankingplugin.configuration.transform.BooleanTransformer;

public class DownloadUpdatesAutomatically extends ConfigurationValue<Boolean> {

    public DownloadUpdatesAutomatically(BankingPlugin plugin) {
        super(plugin, "download-updates-automatically", true, new BooleanTransformer());
    }

}
