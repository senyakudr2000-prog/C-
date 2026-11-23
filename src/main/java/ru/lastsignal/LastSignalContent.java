package ru.lastsignal;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
public final class LastSignalContent{
 public static final Block TERMINAL=Registry.register(Registries.BLOCK,Identifier.of("lastsignal","terminal"),new Block(Block.Settings.create().strength(3.5f,6f).requiresTool().sounds(BlockSoundGroup.METAL)));
 public static final Item TERMINAL_ITEM=Registry.register(Registries.ITEM,Identifier.of("lastsignal","terminal"),new BlockItem(TERMINAL,new Item.Settings()));
 public static final Item RIFLE=Registry.register(Registries.ITEM,Identifier.of("lastsignal","rifle"),new CrossbowItem(new Item.Settings().maxCount(1).maxDamage(465)));
 public static final EntityType<OrlovEntity> ORLOV=Registry.register(Registries.ENTITY_TYPE,Identifier.of("lastsignal","orlov"),EntityType.Builder.create(OrlovEntity::new,SpawnGroup.CREATURE).dimensions(.6f,1.95f).build("lastsignal:orlov"));
 public static final EntityType<GuardEntity> GUARD=Registry.register(Registries.ENTITY_TYPE,Identifier.of("lastsignal","guard"),EntityType.Builder.create(GuardEntity::new,SpawnGroup.MONSTER).dimensions(.6f,1.95f).build("lastsignal:guard"));
 private LastSignalContent(){}
 public static void initialize(){FabricDefaultAttributeRegistry.register(ORLOV,VillagerEntity.createVillagerAttributes());FabricDefaultAttributeRegistry.register(GUARD,PillagerEntity.createPillagerAttributes());}
}