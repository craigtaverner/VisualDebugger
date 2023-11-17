package no.hvl.tk.visual.debugger.manueltests.partsList;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import no.hvl.tk.visual.debugger.manueltests.partsList.domain.Material;
import no.hvl.tk.visual.debugger.manueltests.partsList.domain.Product;
import org.junit.jupiter.api.Test;

class PartsListTest {
  @Test
  void overallCostForFoldingWallTableTest() {
    // Arrange
    final Product folding_wall_table = Product.create("Folding wall table", 5);
    folding_wall_table.addPart(Material.create("Main support", 10), 1);
    folding_wall_table.addPart(Material.create("Hinge", 5), 4);
    folding_wall_table.addPart(Material.create("Wood screw D3,5 x 20mm", 1), 26);
    folding_wall_table.addPart(Material.create("Wood screw D4 x 45mm", 1), 10);

    // Act
    final int cost = folding_wall_table.getOverallCost();

    // Assert
    assertThat(cost, is(71));
  }
}
