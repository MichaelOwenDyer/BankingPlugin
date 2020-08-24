package com.monst.bankingplugin.banking.bank;

import com.monst.bankingplugin.BankingPlugin;
import com.monst.bankingplugin.config.Config;
import org.apache.commons.lang.WordUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents all configurable fields at a given {@link Bank}.
 * Each BankField is declared with a data type and corresponds with a particular variable in {@link BankConfig}
 * as well as a static {@link Config.ConfigPair} in the {@link Config}.
 */
public enum BankField {

    COUNT_INTEREST_DELAY_OFFLINE (Boolean.class),
    REIMBURSE_ACCOUNT_CREATION (Boolean.class),
    PAY_ON_LOW_BALANCE (Boolean.class),
    INTEREST_RATE (Double.class),
    ACCOUNT_CREATION_PRICE (Double.class),
    MINIMUM_BALANCE (Double.class),
    LOW_BALANCE_FEE (Double.class),
    INITIAL_INTEREST_DELAY (Integer.class),
    ALLOWED_OFFLINE_PAYOUTS (Integer.class),
    ALLOWED_OFFLINE_PAYOUTS_BEFORE_RESET (Integer.class),
    OFFLINE_MULTIPLIER_DECREMENT (Integer.class),
    WITHDRAWAL_MULTIPLIER_DECREMENT (Integer.class),
    PLAYER_BANK_ACCOUNT_LIMIT (Integer.class),
    MULTIPLIERS (List.class),
    INTEREST_PAYOUT_TIMES (List.class);

    private final String name;
    private final Class<?> dataType;
    private Field localField;
    private Field configPair;

    BankField(Class<?> dataType) {
        this.name = toString().toLowerCase().replace("_", "-");
        this.dataType = dataType;
        try {
            // Deduce name of field from name of enum constant
            // e.g. ACCOUNT_CREATION_PRICE -> ACCOUNT CREATION PRICE -> Account Creation Price -> AccountCreationPrice -> accountCreationPrice
            String fieldName = WordUtils.capitalizeFully(toString().replace("_", " ")).replace(" ", "");
            fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1);
            this.localField = BankConfig.class.getDeclaredField(fieldName);
            this.configPair = Config.class.getField(fieldName);
        } catch (NoSuchFieldException e) {
            BankingPlugin.getInstance().debug(e);
        }
    }

    /**
     * @return the name of this field
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type of this field, e.g. Double, Integer, Boolean, or List
     */
    public Class<?> getDataType() {
        return dataType;
    }

    /**
     * @return the bank-specific value of this field, stored in {@link BankConfig}
     */
    Field getLocalVariable() {
        return localField;
    }

    /**
     * @return the {@link com.monst.bankingplugin.config.Config.ConfigPair} containing
     * the default value and overrideable attribute for this field, stored in the {@link Config}
     */
    Config.ConfigPair<?> getConfigPair() {
        try {
            return (Config.ConfigPair<?>) configPair.get(null);
        } catch (IllegalAccessException e) {
            BankingPlugin.getInstance().debug(e);
            return null;
        }
    }

    /**
     * @return a {@link Stream<BankField>} containing all fields
     */
    public static Stream<BankField> stream() {
        return Stream.of(BankField.values());
    }

    /**
     * @param types the types to match
     * @return a {@link Stream<BankField>} containing all fields that match one of the specified data types
     * @see #getDataType()
     */
    public static Stream<BankField> stream(Class<?>... types) {
        List<Class<?>> list = Arrays.asList(types);
        return stream().filter(f -> list.contains(f.getDataType()));
    }

    /**
     * @param name the name of the field
     * @return the field with the specified name
     */
    public static BankField getByName(String name) {
        return stream().filter(field -> field.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
