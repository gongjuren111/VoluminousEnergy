package com.veteam.voluminousenergy.compat.jei;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.veteam.voluminousenergy.blocks.screens.CrusherScreen;

import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;

public class CrusherContainerHandler implements IGuiContainerHandler<CrusherScreen> {
    @Override
    public Collection<IGuiClickableArea> getGuiClickableAreas(CrusherScreen containerScreen, double guiMouseX, double guiMouseY) {
        List<IGuiClickableArea> areas = new ArrayList<>();
        areas.add(new IGuiClickableArea() {
            @Override
            public Rect2i getArea() {
                return new Rect2i(78, 32, 28, 23);
            }

            @Override
            public List<Component> getTooltipStrings() {
                List<Component> tooltips = new ArrayList<>();
                tooltips.add(VoluminousEnergyPlugin.SHOW_RECIPES);
                tooltips.addAll(containerScreen.getTooltips());
                return tooltips;
            }

            @Override
            public void onClick(IFocusFactory focusFactory, IRecipesGui recipesGui) {
                recipesGui.showCategories(Lists.newArrayList(VoluminousEnergyPlugin.CRUSHING_UID));
            }
        });

        return areas;
    }
}
