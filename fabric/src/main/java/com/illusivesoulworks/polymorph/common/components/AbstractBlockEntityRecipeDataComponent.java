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

package com.illusivesoulworks.polymorph.common.components;

import com.illusivesoulworks.polymorph.common.capability.AbstractBlockEntityRecipeData;
import javax.annotation.Nonnull;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.Component;

public abstract class AbstractBlockEntityRecipeDataComponent<M extends BlockEntity>
    extends AbstractBlockEntityRecipeData<M> implements Component {

  public AbstractBlockEntityRecipeDataComponent(M owner) {
    super(owner);
  }

  @Override
  public void readData(@Nonnull ValueInput input) {
    input.child("Data").ifPresent(data ->
        data.getString("SelectedRecipe").ifPresent(str -> {
          var rl = net.minecraft.resources.ResourceLocation.tryParse(str);
          if (rl != null) {
            this.loadedRecipe = rl;
          }
        }));
  }

  @Override
  public void writeData(@Nonnull ValueOutput output) {
    if (this.getSelectedRecipe() != null) {
      ValueOutput data = output.child("Data");
      data.putString("SelectedRecipe", this.getSelectedRecipe().id().location().toString());
    }
  }
}
