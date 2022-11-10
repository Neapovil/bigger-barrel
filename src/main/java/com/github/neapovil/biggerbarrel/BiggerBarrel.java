package com.github.neapovil.biggerbarrel;

import org.bukkit.plugin.java.JavaPlugin;

public class BiggerBarrel extends JavaPlugin
{
    private static BiggerBarrel instance;

    @Override
    public void onEnable()
    {
        instance = this;
    }

    @Override
    public void onDisable()
    {
    }

    public static BiggerBarrel getInstance()
    {
        return instance;
    }
}
