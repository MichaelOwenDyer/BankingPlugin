package com.monst.bankingplugin.listeners;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.banking.Ownable;
import com.monst.bankingplugin.banking.bank.BankField;
import com.monst.bankingplugin.events.account.AccountConfigureEvent;
import com.monst.bankingplugin.events.bank.BankConfigureEvent;
import com.monst.bankingplugin.events.control.PluginConfigureEvent;
import com.monst.bankingplugin.gui.SinglePageGui;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@SuppressWarnings("unused")
public class GuiUpdateListener implements Listener {

    public GuiUpdateListener(BankingPlugin plugin) {
    }

    @EventHandler
    public void onPluginConfigureEvent(PluginConfigureEvent event) {
        // FIXME: Not all fields will be triggered here due to discrepancy in name between bank / plugin config
        if (BankField.getByName(event.getField().substring(0, event.getField().lastIndexOf('.'))) != null)
            SinglePageGui.updateGuis();
    }

    @EventHandler
    public void onBankConfigureEvent(BankConfigureEvent event) {
        update(event.getBank());
        event.getBank().getAccounts().forEach(this::update);
    }

    @EventHandler
    public void onAccountConfigureEvent(AccountConfigureEvent event) {
        update(event.getAccount());
    }

    private void update(Ownable ownable) {
        SinglePageGui.updateGuis(ownable);
    }

}
