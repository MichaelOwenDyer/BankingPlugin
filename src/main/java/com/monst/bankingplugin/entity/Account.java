package com.monst.bankingplugin.entity;

import com.monst.bankingplugin.converter.OfflinePlayerConverter;
import com.monst.bankingplugin.entity.geo.location.AccountLocation;
import jakarta.persistence.*;
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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Account extends AbstractEntity implements Ownable {

    @ManyToOne(optional = false, cascade = CascadeType.MERGE)
    private Bank bank;
    @Column(nullable = false)
    @Convert(converter = OfflinePlayerConverter.class)
    private OfflinePlayer owner;
    @ElementCollection
    @CollectionTable(name = "co_owns_account",
            joinColumns = @JoinColumn(name = "account_id", referencedColumnName = "id")
    )
    @Column(name = "co_owner_id")
    @Convert(converter = OfflinePlayerConverter.class)
    private Set<OfflinePlayer> co_owners;
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private AccountLocation location;
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal balance; // Persist balance so that it does not have to be appraised constantly during runtime
    @Column(nullable = false, precision = 16, scale = 2)
    private BigDecimal previousBalance;
    private int multiplierStage;
    private int remainingOfflinePayouts;
    private String customName;

    public Account() {}

    public Account(OfflinePlayer owner, AccountLocation loc) {
        this.generateID();
        this.owner = owner;
        this.co_owners = new HashSet<>();
        this.location = loc;
        this.balance = BigDecimal.ZERO;
        this.previousBalance = BigDecimal.ZERO;
        this.multiplierStage = 0;
    }

    public Bank getBank() {
        return bank;
    }

    public void setBank(Bank bank) {
        this.bank = bank;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
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
    }

    public void setCustomName(String customName) {
        this.customName = customName;
        updateChestTitle();
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
        InventoryHolder ih = location.findChest().orElse(null);
        if (ih == null)
            return;
        if (isDoubleChest()) {
            DoubleChest dc = (DoubleChest) ih;
            Chest left = (Chest) dc.getLeftSide();
            Chest right = (Chest) dc.getRightSide();
            if (left != null) {
                left.setCustomName(name);
                left.update();
            }
            if (right != null) {
                right.setCustomName(name);
                right.update();
            }
        } else {
            Chest chest = (Chest) ih;
            chest.setCustomName(name);
            chest.update();
        }
    }

    public EnumMap<Material, Integer> getContents() {
        EnumMap<Material, Integer> contents = new EnumMap<>(Material.class);
        InventoryHolder ih = location.findChest().orElse(null);
        if (ih == null)
            return contents;
        for (ItemStack item : ih.getInventory().getContents()) {
            if (item == null) // Individual items may be null
                continue;
            contents.putIfAbsent(item.getType(), 0);
            contents.put(item.getType(), contents.get(item.getType()) + item.getAmount());
            if (item.getItemMeta() instanceof BlockStateMeta) {
                BlockStateMeta im = (BlockStateMeta) item.getItemMeta();
                if (im.getBlockState() instanceof ShulkerBox) {
                    ShulkerBox shulkerBox = (ShulkerBox) im.getBlockState();
                    for (ItemStack innerItem : shulkerBox.getInventory().getContents()) {
                        if (innerItem == null)
                            continue;
                        contents.putIfAbsent(innerItem.getType(), 0);
                        contents.put(innerItem.getType(), contents.get(innerItem.getType()) + innerItem.getAmount());
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
    }

    public void incrementMultiplierByOne(int max) {
        multiplierStage = Math.min(multiplierStage + 1, max);
    }

    public void decrementMultiplier(int decrement) {
        if (decrement > 0)
            multiplierStage = Math.max(0, multiplierStage - decrement);
        else if (decrement < 0)
            multiplierStage = 0;
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
    }

    @Override
    public void untrustPlayer(OfflinePlayer player) {
        if (player == null)
            return;
        getCoOwners().remove(player);
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + getID() +
                ", bank=" + bank +
                ", owner=" + owner +
                ", location=" + location +
                ", balance=" + balance +
                ", customName='" + customName + '\'' +
                '}';
    }

}
