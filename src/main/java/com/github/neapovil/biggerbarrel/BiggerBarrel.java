package com.github.neapovil.biggerbarrel;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import com.github.neapovil.biggerbarrel.manager.InventoryManager;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;

public class BiggerBarrel extends JavaPlugin implements Listener
{
    private static BiggerBarrel instance;
    private InventoryManager inventoryManager;

    @Override
    public void onEnable()
    {
        instance = this;

        this.inventoryManager = new InventoryManager();

        this.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
    }

    public static BiggerBarrel getInstance()
    {
        return instance;
    }

    @EventHandler
    private void serverTickStart(ServerTickStartEvent event)
    {
        this.inventoryManager.getInventories().forEach(i -> {
            if (i.getValue().getViewers().isEmpty())
            {
                this.inventoryManager.serializeEntry(i.getKey(), i.getValue());
                this.inventoryManager.removeInventory(i.getKey());
            }
        });
    }

    @EventHandler
    private void playerInteract(PlayerInteractEvent event)
    {
        if (!event.getAction().isRightClick())
        {
            return;
        }

        if (event.getClickedBlock() == null)
        {
            return;
        }

        if (!event.getClickedBlock().getType().equals(Material.BARREL))
        {
            return;
        }

        event.setCancelled(true);

        if (!this.inventoryManager.hasInventory(event.getClickedBlock().getLocation()))
        {
            final Inventory inventory = this.getServer().createInventory(null, 54, Component.translatable("container.barrel"));

            this.inventoryManager.putInventory(event.getClickedBlock().getLocation(), inventory);
        }

        final Inventory inventory = this.inventoryManager.getInventory(event.getClickedBlock().getLocation());

        this.inventoryManager.deserializeEntry(event.getClickedBlock().getLocation(), inventory);

        event.getPlayer().openInventory(inventory);
    }

    @EventHandler
    private void blockBreak(BlockBreakEvent event)
    {
        final NamespacedKey locationkey = this.inventoryManager.locationKey(event.getBlock().getLocation(), 0);
        final PersistentDataContainer pdc = event.getBlock().getChunk().getPersistentDataContainer();

        if (!pdc.has(locationkey))
        {
            return;
        }

        for (int i = 0; i < 54; i++)
        {
            final NamespacedKey key = this.inventoryManager.locationKey(event.getBlock().getLocation(), i);

            if (pdc.has(key, PersistentDataType.BYTE_ARRAY))
            {
                final byte[] bytes = pdc.get(key, PersistentDataType.BYTE_ARRAY);
                final ItemStack itemstack = ItemStack.deserializeBytes(bytes);

                event.getBlock().getWorld().dropItem(event.getBlock().getLocation(), itemstack);
            }

            pdc.remove(key);
        }
    }

    @EventHandler
    private void inventoryOpen(InventoryOpenEvent event)
    {
        if (this.isCustomBarrel(event.getView(), event.getInventory()))
        {
            final Sound sound = Sound.sound(Key.key("minecraft", "block.barrel.open"), Sound.Source.BLOCK, 1f, 1f);

            event.getPlayer().playSound(sound);
        }
    }

    @EventHandler
    private void inventoryClose(InventoryCloseEvent event)
    {
        if (this.isCustomBarrel(event.getView(), event.getInventory()))
        {
            final Sound sound = Sound.sound(Key.key("minecraft", "block.barrel.close"), Sound.Source.BLOCK, 1f, 1f);

            event.getPlayer().playSound(sound);
        }
    }

    private boolean isCustomBarrel(InventoryView view, Inventory inventory)
    {
        return inventory.getType().equals(InventoryType.CHEST) &&
                inventory.getSize() == 54 &&
                view.title() instanceof TranslatableComponent &&
                ((TranslatableComponent) view.title()).key().equals("container.barrel");
    }
}
