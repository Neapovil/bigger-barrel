package com.github.neapovil.biggerbarrel.manager;

import java.util.Map;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.github.neapovil.biggerbarrel.BiggerBarrel;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class InventoryManager
{
    private final BiggerBarrel plugin = BiggerBarrel.getInstance();
    private final Map<Location, Inventory> inventories = new Object2ObjectOpenHashMap<>();

    public boolean hasInventory(Location location)
    {
        return this.inventories.containsKey(location);
    }

    public Inventory getInventory(Location location)
    {
        return this.inventories.get(location);
    }

    public void putInventory(Location location, Inventory inventory)
    {
        this.inventories.put(location, inventory);
    }

    public void removeInventory(Location location)
    {
        this.inventories.remove(location);
    }

    public Set<Map.Entry<Location, Inventory>> getInventories()
    {
        return this.inventories.entrySet();
    }

    public void serializeEntry(Location location, Inventory inventory)
    {
        final PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();

        for (int i = 0; i < 54; i++)
        {
            final NamespacedKey key = this.locationKey(location, i);
            final ItemStack itemstack = inventory.getItem(i);

            if (itemstack == null)
            {
                pdc.set(key, PersistentDataType.INTEGER, 0);
            }
            else
            {
                pdc.set(key, PersistentDataType.BYTE_ARRAY, itemstack.serializeAsBytes());
            }
        }
    }

    public void deserializeEntry(Location location, Inventory inventory)
    {
        final PersistentDataContainer pdc = location.getChunk().getPersistentDataContainer();

        for (int i = 0; i < 54; i++)
        {
            final NamespacedKey key = this.locationKey(location, i);

            if (pdc.has(key, PersistentDataType.INTEGER))
            {
                inventory.setItem(i, null);
            }

            if (pdc.has(key, PersistentDataType.BYTE_ARRAY))
            {
                final byte[] bytes = pdc.get(key, PersistentDataType.BYTE_ARRAY);
                final ItemStack itemstack = ItemStack.deserializeBytes(bytes);

                inventory.setItem(i, itemstack);
            }
        }
    }

    public NamespacedKey locationKey(Location location, int index)
    {
        final String worldkey = location.getWorld().key().value();
        final String locationkey = location.toVector().toString().replace(",", "");

        return new NamespacedKey(plugin, worldkey + "-" + locationkey + "-itemstack-" + index);
    }
}
