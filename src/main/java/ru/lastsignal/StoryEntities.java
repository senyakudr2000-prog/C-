package ru.lastsignal;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
final class OrlovEntity extends VillagerEntity {
 OrlovEntity(EntityType<? extends VillagerEntity> type,World world){super(type,world);setAiDisabled(true);setInvulnerable(true);}
}
final class GuardEntity extends PillagerEntity {
 GuardEntity(EntityType<? extends PillagerEntity> type,World world){super(type,world);setStackInHand(Hand.MAIN_HAND,new ItemStack(Items.CROSSBOW));setPersistent();}
}