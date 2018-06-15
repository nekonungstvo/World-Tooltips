package ninja.genuine.tooltips.system;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TooltipTarget {
    private final ItemStack item;
    private final Entity entity;

    public TooltipTarget(EntityItemFrame entity) {
        this(entity.getDisplayedItem(), entity);
    }

    public TooltipTarget(EntityPlayer entity) {
        this(entity.getHeldItem(), entity);
    }

    public TooltipTarget(EntityItem entity) {
        this(entity.getEntityItem(), entity);
    }

    private TooltipTarget(ItemStack item, Entity entity) {
        this.item = item;
        this.entity = entity;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public Entity getEntity() {
        return entity;
    }
}
