package com.illusivesoulworks.polymorph.common.components;

import com.illusivesoulworks.polymorph.api.common.capability.IBlockEntityRecipeData;
import javax.annotation.Nonnull;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class WrappedRecipeDataComponent<M extends BlockEntity>
    extends AbstractBlockEntityRecipeDataComponent<M> {

  public final IBlockEntityRecipeData recipeData;

  public WrappedRecipeDataComponent(IBlockEntityRecipeData recipeData) {
    super((M) recipeData.getOwner());
    this.recipeData = recipeData;
  }

  @Override
  public void readData(@Nonnull ValueInput input) {
    input.child("Data").ifPresent(data ->
        data.getString("SelectedRecipe").ifPresent(str -> {
          var rl = ResourceLocation.tryParse(str);
          if (rl != null) {
            // Store in the wrapped recipeData's loadedRecipe field
            // This will be resolved later when getRecipe is called
            this.loadedRecipe = rl;
          }
        }));
  }

  @Override
  public void writeData(@Nonnull ValueOutput output) {
    if (this.recipeData.getSelectedRecipe() != null) {
      ValueOutput data = output.child("Data");
      data.putString("SelectedRecipe", this.recipeData.getSelectedRecipe().id().location().toString());
    }
  }

  @Override
  protected NonNullList<ItemStack> getInput() {
    return NonNullList.create();
  }

  @Override
  public void tick() {
    this.recipeData.tick();
  }

  @Override
  public boolean isEmpty() {
    return this.recipeData.isEmpty();
  }
}
