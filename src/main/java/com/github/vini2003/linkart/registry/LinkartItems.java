package com.github.vini2003.linkart.registry;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.item.LinkerItem;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LinkartItems {
    public static Item LINKER_ITEM;
    public static Item CHAIN_ITEM;

    public static void initialize() {
        if (LinkartConfigurations.INSTANCE.getConfig().isLinkerEnabled()) {
            LINKER_ITEM = Registry.register(Registry.ITEM, new Identifier(Linkart.ID, "linker"), new LinkerItem(new Item.Settings().maxCount(1).group(LinkartItemGroups.LINKART_GROUP)));;
        }
        if (LinkartConfigurations.INSTANCE.getConfig().isChainEnabled()) {
            CHAIN_ITEM = Registry.register(Registry.ITEM, new Identifier(Linkart.ID, "chain"), new Item(new Item.Settings().group(LinkartItemGroups.LINKART_GROUP)));
        }
    }
}
