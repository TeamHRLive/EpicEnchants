package com.songoda.epicenchants.utils;

import com.songoda.epicenchants.EpicEnchants;
import com.songoda.epicenchants.objects.Group;
import com.songoda.epicenchants.utils.objects.ItemBuilder;
import de.tr7zw.itemnbtapi.NBTItem;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ThreadLocalRandom;

import static com.songoda.epicenchants.objects.Placeholder.of;
import static com.songoda.epicenchants.utils.single.GeneralUtils.color;

public class SpecialItems {
    private final EpicEnchants instance;

    public SpecialItems(EpicEnchants instance) {
        this.instance = instance;
    }

    public ItemStack getWhiteScroll(Integer amount) {
        NBTItem nbtItem = new ItemBuilder(instance.getFileManager().getConfiguration("items/special-items").getConfigurationSection("white-scroll")).nbt();
        nbtItem.setBoolean("white-scroll", true);
        ItemStack itemStack = nbtItem.getItem();

        if (amount != null) {
            itemStack.setAmount(amount);
        }

        return itemStack;
    }

    public ItemStack getBlackScroll(Integer amount, Integer chance) {
        int successRate = chance == null ? ThreadLocalRandom.current().nextInt(instance.getConfig().getInt("rates.black-scroll-min"), instance.getConfig().getInt("rates.black-scroll-max") + 1) : chance;
        NBTItem nbtItem = new ItemBuilder(instance.getFileManager().getConfiguration("items/special-items").getConfigurationSection("black-scroll"), of("success-rate", successRate)).nbt();

        nbtItem.setBoolean("black-scroll", true);
        nbtItem.setInteger("success-rate", successRate);

        ItemStack itemStack = nbtItem.getItem();

        if (amount != null) {
            itemStack.setAmount(amount);
        }

        return itemStack;
    }

    public ItemStack getMysteryBook(Group group) {
        NBTItem nbtItem = new ItemBuilder(instance.getFileManager().getConfiguration("items/special-items").getConfigurationSection("mystery-book"),
                of("group-color", group.getColor()),
                of("group-name", group.getName())).nbt();

        nbtItem.setBoolean("mystery-book", true);
        nbtItem.setString("group", group.getIdentifier());
        return nbtItem.getItem();
    }


    public ItemStack getSecretDust(NBTItem book) {
        Group group = instance.getEnchantManager().getValueUnsafe(book.getString("enchant")).getGroup();
        return getSecretDust(group, (int) Math.floor(book.getInteger("success-rate") / 10.0));
    }

    public ItemStack getSecretDust(Group group, int max) {
        NBTItem nbtItem = new ItemBuilder(instance.getFileManager().getConfiguration("items/dusts").getConfigurationSection("secret-dust"),
                of("group-color", group.getColor()),
                of("group-name", group.getName()),
                of("max-rate", max),
                of("min-rate", 0)).nbt();

        nbtItem.setBoolean("secret-dust", true);
        nbtItem.setString("group", group.getIdentifier());
        nbtItem.setInteger("max-rate", max);
        nbtItem.setInteger("min-rate", 1);
        return nbtItem.getItem();
    }

    public ItemStack getDust(Group group, @Nullable String type, @Nullable Integer percentage) {
        FileConfiguration dustConfig = instance.getFileManager().getConfiguration("items/dusts");
        int random = ThreadLocalRandom.current().nextInt(101);
        int counter = 0;

        if (type == null) {
            for (String s : dustConfig.getConfigurationSection("dusts").getKeys(false)) {
                int chance = dustConfig.getConfigurationSection("dusts." + s).getInt("chance");
                if (random < (chance + counter) && random >= counter) {
                    type = s;
                }
                counter += chance;
            }
        }

        ConfigurationSection config = dustConfig.getConfigurationSection("dusts." + (type == null ? "mystery" : type));

        if (config.isInt("min-rate") && config.isInt("max-rate") && percentage == null) {
            int minRate = config.getInt("min-rate");
            int maxRate = config.getInt("max-rate");
            percentage = ThreadLocalRandom.current().nextInt(minRate, maxRate + 1);
        } else if (percentage == null) {
            percentage = ThreadLocalRandom.current().nextInt(0, 10);
        }

        NBTItem nbtItem = new ItemBuilder(config,
                of("group-color", group.getColor()),
                of("group-name", group.getName()),
                of("percentage", percentage)).nbt();

        if (type != null && type.equalsIgnoreCase("mystery")) {
            return nbtItem.getItem();
        }

        nbtItem.setBoolean("dust", true);
        nbtItem.setInteger("percentage", percentage);
        nbtItem.setString("group", group.getIdentifier());

        return nbtItem.getItem();
    }

    public String getWhiteScrollLore() {
        return color(instance.getFileManager().getConfiguration("special-items").getConfigurationSection("white-scroll").getString("format"));
    }
}