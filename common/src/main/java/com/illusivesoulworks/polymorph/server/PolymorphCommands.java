/*
 * Copyright (C) 2020-2022 Illusive Soulworks
 *
 * Polymorph is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * Polymorph is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Polymorph.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.illusivesoulworks.polymorph.server;

import com.illusivesoulworks.polymorph.PolymorphConstants;
import com.illusivesoulworks.polymorph.platform.Services;
import com.illusivesoulworks.polymorph.server.wrapper.CraftingRecipeWrapper;
import com.illusivesoulworks.polymorph.server.wrapper.RecipeWrapper;
import com.illusivesoulworks.polymorph.server.wrapper.SmithingRecipeWrapper;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

public class PolymorphCommands {

  public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
    final int opPermissionLevel = 2;
    LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("polymorph")
        .requires(player -> player.hasPermission(opPermissionLevel));
    command.then(
        Commands.literal("conflicts").executes(context -> findConflicts(context.getSource())));
    dispatcher.register(command);
  }

  private static int findConflicts(CommandSourceStack source) {
    CompletableFuture.runAsync(() -> {
      source.sendSuccess(() -> Component.translatable("commands.polymorph.conflicts.starting"),
          true);
      ServerLevel world = source.getLevel();
      RecipeManager recipeManager = world.recipeAccess();
      List<String> output = new ArrayList<>();
      int count = 0;

      int craftingConflicts = scanRecipes(RecipeType.CRAFTING, output, recipeManager, CraftingRecipeWrapper::new, source, "crafting");
      count += craftingConflicts;

      int smeltingConflicts = scanRecipes(RecipeType.SMELTING, output, recipeManager, RecipeWrapper::new, source, "smelting");
      count += smeltingConflicts;

      int blastingConflicts = scanRecipes(RecipeType.BLASTING, output, recipeManager, RecipeWrapper::new, source, "blasting");
      count += blastingConflicts;

      int smokingConflicts = scanRecipes(RecipeType.SMOKING, output, recipeManager, RecipeWrapper::new, source, "smoking");
      count += smokingConflicts;

      int smithingConflicts = scanRecipes(RecipeType.SMITHING, output, recipeManager, SmithingRecipeWrapper::new, source, "smithing");
      count += smithingConflicts;

      if (count > 0) {
        try {
          Files.write(Paths.get(Services.PLATFORM.getGameDir() + "/logs/polymorph-conflicts.log"),
              output, StandardCharsets.UTF_8);
        } catch (IOException e) {
          PolymorphConstants.LOG.error(
              "Whoops! Something went wrong writing down your conflicts :(");
          e.printStackTrace();
        }
      }
      int finalCount = count;
      source.sendSuccess(
          () -> Component.translatable("commands.polymorph.conflicts.success", finalCount), true);
    });
    return Command.SINGLE_SUCCESS;
  }

  @SuppressWarnings("unchecked")
  private static <I extends RecipeInput, T extends Recipe<I>> int scanRecipes(RecipeType<T> pType,
                                                                              List<String> pOutput,
                                                                              RecipeManager pRecipeManager,
                                                                              Function<RecipeHolder<?>, RecipeWrapper> pFactory,
                                                                              CommandSourceStack pSource,
                                                                              String pLabel) {
    List<RecipeWrapper> recipes =
        pRecipeManager.getRecipes().stream()
            .filter(holder -> holder.value().getType() == pType)
            .map(h -> pFactory.apply((RecipeHolder<?>) h)).toList();
    List<Set<ResourceLocation>> conflicts = new ArrayList<>();
    Set<ResourceLocation> skipped = new TreeSet<>();
    Set<ResourceLocation> processed = new HashSet<>();

    int totalRecipes = recipes.size();
    pSource.sendSuccess(() -> Component.literal("Scanning " + totalRecipes + " " + pLabel + " recipes..."), true);

    int checked = 0;
    int lastReportedPercent = 0;

    for (RecipeWrapper recipe : recipes) {
      ResourceLocation id = recipe.getId();
      checked++;

      // Report progress every 10%
      int currentPercent = (checked * 100) / totalRecipes;
      if (currentPercent >= lastReportedPercent + 10) {
        lastReportedPercent = (currentPercent / 10) * 10;
        int finalChecked = checked;
        int finalPercent = lastReportedPercent;
        pSource.sendSuccess(() -> Component.literal("  Checked " + finalChecked + "/" + totalRecipes + " recipes (" + finalPercent + "%)"), true);
      }

      if (processed.contains(id)) {
        continue;
      }
      processed.add(id);

      if (recipe.getRecipe() instanceof CustomRecipe) {
        skipped.add(id);
        continue;
      }
      Set<ResourceLocation> currentGroup = new TreeSet<>();

      for (RecipeWrapper otherRecipe : recipes) {

        if (processed.contains(otherRecipe.getId())) {
          continue;
        }

        if (otherRecipe.conflicts(recipe)) {
          currentGroup.add(id);
          currentGroup.add(otherRecipe.getId());
          processed.add(otherRecipe.getId());
        }
      }

      if (!currentGroup.isEmpty()) {
        conflicts.add(currentGroup);
      }
    }
    pOutput.add("===================================================================");
    pOutput.add(
        BuiltInRegistries.RECIPE_TYPE.getKey(pType) + " recipe conflicts (" + conflicts.size() +
            ")");
    pOutput.add("===================================================================");
    pOutput.add("");
    int count = 1;

    for (Set<ResourceLocation> conflict : conflicts) {
      StringJoiner joiner = new StringJoiner(", ");
      conflict.stream().map(ResourceLocation::toString).forEach(joiner::add);
      pOutput.add(count + ": " + joiner);
      pOutput.add("");
      count++;
    }

    if (!skipped.isEmpty()) {
      pOutput.add("Skipped special recipes: ");

      for (ResourceLocation resourceLocation : skipped) {
        pOutput.add(resourceLocation.toString());
      }
      pOutput.add("");
    }
    int conflictCount = conflicts.size();
    pSource.sendSuccess(() -> Component.literal("Found " + conflictCount + " " + pLabel + " conflicts"), true);
    return conflictCount;
  }
}
