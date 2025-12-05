package com.illusivesoulworks.polymorph.server.wrapper;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class IngredientWrapper {

  private final Ingredient ingredient;

  public IngredientWrapper(Ingredient pIngredient) {
    this.ingredient = pIngredient;
  }

  public Ingredient getIngredient() {
    return this.ingredient;
  }

  public boolean matches(IngredientWrapper pIngredient) {

    if (pIngredient == null) {
      return false;
    }
    Ingredient otherIngredient = pIngredient.getIngredient();

    if (otherIngredient == null) {
      return false;
    } else if (otherIngredient.isEmpty()) {
      return this.ingredient.isEmpty();
    } else {
      // Check if any item in this ingredient matches any item in the other ingredient
      for (ItemStack otherStack : pIngredient.getIngredient().items()
          .map(holder -> new ItemStack(holder.value())).toList()) {

        for (ItemStack stack : this.ingredient.items()
            .map(holder -> new ItemStack(holder.value())).toList()) {

          if (ItemStack.matches(stack, otherStack)) {
            return true;
          }
        }
      }
      return false;
    }
  }
}
