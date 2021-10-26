package com.github.vini2003.linkart.registry;

import com.github.vini2003.linkart.configuration.LinkartConfiguration;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigHolder;
import me.sargunvohra.mcmods.autoconfig1u.serializer.GsonConfigSerializer;

public class LinkartConfigurations {
	public static ConfigHolder<LinkartConfiguration> INSTANCE;

	private LinkartConfigurations() {
		// NO-OP
	}

	public static void initialize() {
		AutoConfig.register(LinkartConfiguration.class, GsonConfigSerializer::new);

		INSTANCE = AutoConfig.getConfigHolder(LinkartConfiguration.class);
	}
}
