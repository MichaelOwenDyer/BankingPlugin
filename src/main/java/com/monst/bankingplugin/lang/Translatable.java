package com.monst.bankingplugin.lang;

import com.monst.bankingplugin.BankingPlugin;

public interface Translatable {
    
    String inEnglish();
    
    String translate(BankingPlugin plugin);
    
}
