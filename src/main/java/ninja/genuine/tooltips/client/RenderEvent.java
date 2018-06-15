package ninja.genuine.tooltips.client;

import java.util.List;
import java.util.Objects;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import ninja.genuine.tooltips.WorldTooltips;
import ninja.genuine.tooltips.system.Tooltip;
import ninja.genuine.tooltips.system.TooltipTarget;

public class RenderEvent {

	private Minecraft mc;
	private TooltipTarget target;
	private Tooltip cache;

	public RenderEvent() {}

	public void post() {
		mc = Minecraft.getMinecraft();
	}

	public void syncColors() {
		if (!Objects.isNull(cache))
			cache.syncSettings();
	}

	@SubscribeEvent
	public void render(final RenderWorldLastEvent event) {
	    if (Minecraft.getMinecraft().gameSettings.hideGUI) return;

		target = getMouseOver(mc, event.partialTicks);
		if (!(Objects.isNull(target) || Objects.isNull(target.getItem()))) {
			if (Objects.isNull(cache) || cache.getEntity() != target.getEntity() || cache.getTarget().getItem() != target.getItem())
				cache = new Tooltip(Minecraft.getMinecraft().thePlayer, target);
			cache.renderTooltip3D(mc, event.partialTicks);
		}
	}

	@SubscribeEvent
	public void render(final RenderGameOverlayEvent.Post event) {
		// TODO Let's make it a choice to do 2D or 3D tooltips.
		//      Just need to make a nice anchoring gui first.
		// renderer.renderTooltip2D(mc, item, generateTooltip(mc, mc.player, item.getItem()), event.getPartialTicks());
	}

	@SuppressWarnings("unchecked")
    private static Entity getMouseOver(Minecraft mc, float partialTicks, Class entityClass) {
		EntityLivingBase viewer = mc.renderViewEntity;
		mc.mcProfiler.startSection("world-tooltips");
		double distanceLook = WorldTooltips.maxDistance;
		Vec3 eyes = viewer.getPosition(partialTicks);
		Vec3 look = viewer.getLook(partialTicks);
		Vec3 eyesLook = eyes.addVector(look.xCoord * distanceLook, look.yCoord * distanceLook, look.zCoord * distanceLook);
		float distanceMax = 1;
		List<EntityItemFrame> entityList = mc.theWorld.getEntitiesWithinAABB(entityClass,
				viewer.boundingBox.addCoord(look.xCoord * distanceLook, look.yCoord * distanceLook, look.zCoord * distanceLook).expand(distanceMax, distanceMax, distanceMax));
		double difference = 0;
		Entity target = null;
		for (int i = 0; i < entityList.size(); i++) {
			Entity entity = entityList.get(i);
			if (Objects.isNull(entity) || Objects.isNull(entity.boundingBox))
				continue;
			float boundSize = 0.15F;
			AxisAlignedBB aabb1 = entity.boundingBox;
			AxisAlignedBB aabb2 = AxisAlignedBB.getBoundingBox(aabb1.minX, aabb1.minY, aabb1.minZ, aabb1.maxX, aabb1.maxY, aabb1.maxZ);
			AxisAlignedBB expandedAABB = aabb2.offset(0, 0.25, 0).expand(0.15, 0.1, 0.15).expand(boundSize, boundSize, boundSize);
			MovingObjectPosition objectInVector = expandedAABB.calculateIntercept(eyes, eyesLook);

			boolean isCurrentPlayer = entity == Minecraft.getMinecraft().thePlayer;
			boolean canBeSeen = Minecraft.getMinecraft().thePlayer.canEntityBeSeen(entity);
			if (!isCurrentPlayer && canBeSeen) {
                if (expandedAABB.isVecInside(eyes)) {
                    if (0.0D <= difference) {
                        target = entity;
                        difference = 0;
                    }
                } else if (objectInVector != null) {
                    final double distance = eyes.distanceTo(objectInVector.hitVec);
                    if (distance < difference || difference == 0.0D) {
                        target = entity;
                        difference = distance;
                    }
                }
            }
		}
		mc.mcProfiler.endSection();
		return target;
	}

    @SuppressWarnings("unchecked")
    public static TooltipTarget getMouseOver(Minecraft mc, float partialTicks) {
        Entity entity = null;

        entity = getMouseOver(mc, partialTicks, EntityItemFrame.class);
        if (!Objects.isNull(entity)) return new TooltipTarget((EntityItemFrame) entity);

        entity = getMouseOver(mc, partialTicks, EntityItem.class);
        if (!Objects.isNull(entity)) return new TooltipTarget((EntityItem) entity);

//        entity = getMouseOver(mc, partialTicks, EntityPlayer.class);
//        if (!Objects.isNull(entity)) return new TooltipTarget((EntityPlayer) entity);

        return null;
    }

}
