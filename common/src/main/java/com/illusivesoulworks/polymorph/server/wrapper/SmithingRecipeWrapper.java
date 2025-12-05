package com.illusivesoulworks.polymorph.server.wrapper;

import java.util.Optional;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SmithingRecipe;

public class SmithingRecipeWrapper extends RecipeWrapper {

  public SmithingRecipeWrapper(RecipeHolder<?> pRecipe) {
    super(pRecipe);
  }

  @Override
  public boolean conflicts(RecipeWrapper pOther) {
    Recipe<?> recipe = this.getRecipe();
    Recipe<?> otherRecipe = pOther.getRecipe();

    if (!(recipe instanceof SmithingRecipe smithingRecipe) ||
        !(otherRecipe instanceof SmithingRecipe otherSmithingRecipe)) {
      return super.conflicts(pOther);
    }

    // Get ingredients using the SmithingRecipe interface methods
    Optional<Ingredient> template = smithingRecipe.templateIngredient();
    Ingredient base = smithingRecipe.baseIngredient();
    Optional<Ingredient> addition = smithingRecipe.additionIngredient();

    Optional<Ingredient> otherTemplate = otherSmithingRecipe.templateIngredient();
    Ingredient otherBase = otherSmithingRecipe.baseIngredient();
    Optional<Ingredient> otherAddition = otherSmithingRecipe.additionIngredient();

    // Check if any optional ingredients are missing
    if (template.isEmpty() || otherTemplate.isEmpty() ||
        addition.isEmpty() || otherAddition.isEmpty()) {
      return super.conflicts(pOther);
    }

    IngredientWrapper baseWrapper = new IngredientWrapper(base);
    IngredientWrapper otherBaseWrapper = new IngredientWrapper(otherBase);
    IngredientWrapper additionWrapper = new IngredientWrapper(addition.get());
    IngredientWrapper otherAdditionWrapper = new IngredientWrapper(otherAddition.get());
    IngredientWrapper templateWrapper = new IngredientWrapper(template.get());
    IngredientWrapper otherTemplateWrapper = new IngredientWrapper(otherTemplate.get());

    return super.conflicts(pOther) &&
        baseWrapper.matches(otherBaseWrapper) &&
        additionWrapper.matches(otherAdditionWrapper) &&
        templateWrapper.matches(otherTemplateWrapper);
  }
}
