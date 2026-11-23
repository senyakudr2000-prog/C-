package ru.lastsignal;

import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class LastSignalContent {
    public static final Block TERMINAL = Registry.register(
            Registries.BLOCK,
            Identifier.of("lastsignal", "terminal"),
            new Block(Block.Settings.create()
                    .strength(3.5f, 6.0f)
                    .requiresTool()
                    .sounds(BlockSoundGroup.METAL))
    );

    public static final Item TERMINAL_ITEM = Registry.register(
            Registries.ITEM,
            Identifier.of("lastsignal", "terminal"),
            new BlockItem(TERMINAL, new Item.Settings())
    );

    private LastSignalContent() {}

    public static void initialize() {
        // Calling this method forces static content registration before the mission starts.
    }
}
