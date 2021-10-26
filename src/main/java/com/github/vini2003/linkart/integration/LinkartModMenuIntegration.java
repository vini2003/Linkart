package com.github.vini2003.linkart.integration;

import com.github.vini2003.linkart.Linkart;
import com.github.vini2003.linkart.configuration.LinkartConfiguration;
import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.Optional;
import java.util.function.Supplier;

public class LinkartModMenuIntegration implements ModMenuApi {

//	@Override
//	public String getModId() {
//		return Linkart.ID;
//	}

	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (ConfigScreenFactory<Screen>) screen -> AutoConfig.getConfigScreen(LinkartConfiguration.class, screen).get();
	}
}
