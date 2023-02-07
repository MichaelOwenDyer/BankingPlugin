package com.monst.bankingplugin.entity;

import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import com.monst.bankingplugin.util.Observable;
import com.monst.bankingplugin.util.Observer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.ShulkerBox;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import java.math.BigDecimal;
import java.util.*;

public class Account extends Entity implements Ownable, Observable {
    
    private Bank bank;
    private OfflinePlayer owner;
    private final Set<OfflinePlayer> co_owners = new HashSet<>();
    private AccountLocation location;
    private BigDecimal balance = BigDecimal.ZERO;
    private BigDecimal previousBalance = BigDecimal.ZERO;
    private int multiplierStage;
    private int remainingOfflinePayouts;
    private String customName;
    
    public Account(Bank bank, OfflinePlayer owner, AccountLocation loc) {
        this.bank = bank;
        this.owner = owner;
        this.location = loc;
    }
    
    public Account(int id, Bank bank, OfflinePlayer owner, Set<OfflinePlayer> coOwners, AccountLocation location,
                   BigDecimal balance, BigDecimal previousBalance, int multiplierStage, int remainingOfflinePayouts,
                   String customName) {
        super(id);
        this.bank = bank;
        this.owner = owner;
        this.co_owners.addAll(coOwners);
        this.location = location;
        this.balance = balance;
        this.previousBalance = previousBalance;
        this.multiplierStage = multiplierStage;
        this.remainingOfflinePayouts = remainingOfflinePayouts;
        this.customName = customName;
    }
    
    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        if (this.bank.equals(bank))
            return;
        this.bank.removeAccount(this);
        this.bank = bank;
        this.bank.addAccount(this);
        notifyObservers();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
        notifyObservers();
    }

    public BigDecimal getPreviousBalance() {
        return previousBalance;
    }

    public void updatePreviousBalance() {
        previousBalance = balance;
    }

    public AccountLocation getLocation() {
        return location;
    }

    public void setLocation(AccountLocation location) {
        this.location = location;
        notifyObservers();
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        updateChestTitle();
        // Would notify observers here, but cannot update the name of an inventory
    }
    
    public String getCustomName() {
        return customName;
    }

    public String getName() {
        if (customName != null)
            return ChatColor.translateAlternateColorCodes('&', customName) + ChatColor.RESET;
        return ChatColor.DARK_GREEN + "Account #" + getID() + ChatColor.RESET;
    }

    public void resetChestTitle() {
        setChestTitle("");
    }

    public void updateChestTitle() {
        setChestTitle(getName());
    }

    private void setChestTitle(String name) {
        Optional<InventoryHolder> chest = location.findChest();
        if (!chest.isPresent())
            return;
        if (isDoubleChest()) {
            DoubleChest doubleChest = (DoubleChest) chest.get();
            Chest left = (Chest) doubleChest.getLeftSide();
            Chest right = (Chest) doubleChest.getRightSide();
            if (left != null) {
                left.setCustomName(name);
                left.update();
            }
            if (right != null) {
                right.setCustomName(name);
                right.update();
            }
        } else {
            Chest singleChest = (Chest) chest.get();
            singleChest.setCustomName(name);
            singleChest.update();
        }
    }

    public Map<Material, Integer> getContents() {
        Map<Material, Integer> contents = new EnumMap<>(Material.class);
        InventoryHolder ih = location.findChest().orElse(null);
        if (ih == null)
            return contents;
        for (ItemStack item : ih.getInventory().getContents()) {
            if (item == null) // Individual items may be null
                continue;
            contents.putIfAbsent(item.getType(), 0);
            contents.computeIfPresent(item.getType(), (type, current) -> current + item.getAmount());
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                    for (ItemStack innerItem : shulkerBox.getInventory().getContents()) {
                        if (innerItem == null)
                            continue;
                        contents.putIfAbsent(innerItem.getType(), 0);
                        contents.computeIfPresent(innerItem.getType(), (type, current) -> current + innerItem.getAmount());
                    }
                }
            }
        }
        return contents;
    }

    /**
     * Gets the current multiplier stage of this account. This is an index on the list of multipliers specified by the bank.
     * @return the current multiplier stage
     */
    public int getInterestMultiplierStage() {
        return multiplierStage;
    }

    /**
     * Sets the interest multiplier stage.
     * @param multiplierStage the stage to set the multiplier to
     */
    public void setMultiplierStage(int multiplierStage) {
        this.multiplierStage = Math.max(0, multiplierStage);
        notifyObservers();
    }

    public void incrementMultiplierByOne(int max) {
        multiplierStage = Math.min(multiplierStage + 1, max);
        notifyObservers();
    }

    public void decrementMultiplier(int decrement) {
        if (decrement > 0)
            multiplierStage = Math.max(0, multiplierStage - decrement);
        else if (decrement < 0)
            multiplierStage = 0;
        notifyObservers();
    }

    public int getInterestMultiplier(List<Integer> multipliers) {
        if (multiplierStage < 0)
            multiplierStage = 0;
        if (multiplierStage >= multipliers.size())
            multiplierStage = multipliers.size() - 1;
        return multipliers.get(multiplierStage);
    }

    /**
     * Gets the current number of remaining offline payouts at this account. This specifies how many (more) consecutive
     * times this account will generate interest while all account holders are offline.
     *
     * @return the current number of remaining offline payouts
     */
    public int getRemainingOfflinePayouts() {
        return remainingOfflinePayouts;
    }

    /**
     * Sets the remaining offline payouts. This determines how many more times an account will be able to generate
     * interest offline.
     * @param remaining the number of payouts to allow
     */
    public void setRemainingOfflinePayouts(int remaining) {
        remainingOfflinePayouts = remaining;
        notifyObservers();
    }

    public String getCoordinates() {
        return location.toString();
    }

    public byte getSize() {
        return location.getSize();
    }

    public boolean isDoubleChest() {
        return getSize() == 2;
    }

    public boolean isSingleChest() {
        return getSize() == 1;
    }

    @Override
    public OfflinePlayer getOwner() {
        return owner;
    }

    @Override
    public void setOwner(OfflinePlayer newOwner) {
        owner = newOwner;
        untrustPlayer(newOwner); // Remove from co-owners if new owner was a co-owner
        notifyObservers();
    }

    @Override
    public Set<OfflinePlayer> getCoOwners() {
        return new HashSet<>(co_owners);
    }
    
    @Override
    public void trustPlayer(OfflinePlayer player) {
        if (player == null)
            return;
        getCoOwners().add(player);
        notifyObservers();
    }

    @Override
    public void untrustPlayer(OfflinePlayer player) {
        if (player == null)
            return;
        getCoOwners().remove(player);
        notifyObservers();
    }
    
    private final Set<Observer> observers = new HashSet<>();
    @Override
    public Set<Observer> getObservers() {
        return observers;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", bank=" + bank.getName() +
                ", owner=" + owner.getName() +
                ", location=" + location +
                ", balance=" + balance +
                ", customName='" + customName + '\'' +
                ", multiplierStage=" + multiplierStage +
                ", remainingOfflinePayouts=" + remainingOfflinePayouts +
                '}';
    }
    
}
