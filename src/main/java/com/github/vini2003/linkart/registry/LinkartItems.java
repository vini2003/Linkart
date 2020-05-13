package com.github.vini2003.linkart.registry;

import com.github.vini2003.linkart.Linkart;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class LinkartItems {
    public static final Item LINKER_ITEM = Registry.register(Registry.ITEM, new Identifier(Linkart.ID, "linker"), new Item(new Item.Settings().maxCount(1).group(LinkartItemGroups.LINKART_GROUP)));

    public static void initialize() {
        // NO-OP
    }
}
