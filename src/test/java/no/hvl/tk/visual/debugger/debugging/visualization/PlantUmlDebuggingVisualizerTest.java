package no.hvl.tk.visual.debugger.debugging.visualization;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import no.hvl.tk.visual.debugger.domain.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class PlantUmlDebuggingVisualizerTest {

  private final AtomicInteger counter = new AtomicInteger();

  @Test
  void toPlantUMLStringEmptyDiagramTest() {
    final ObjectDiagram diagram = new ObjectDiagram();
    final String plantUMLString = PlantUmlDebuggingVisualizer.toPlantUMLString(diagram);
    assertThat(
        PlantUmlDebuggingVisualizerTest.normalizeString(plantUMLString),
        is(
            """
                                                                                               @startuml
                                                                                               !pragma layout smetana
                                                                                               @enduml
                                                                                               """));
  }

  @Test
  void toPlantUMLStringPrimitiveVarsTest() {
    final ObjectDiagram diagram = new ObjectDiagram();
    diagram.addPrimitiveRootValue(new ODPrimitiveRootValue("int", "Integer", "1"));
    diagram.addPrimitiveRootValue(new ODPrimitiveRootValue("double", "Double", "1.2"));
    diagram.addPrimitiveRootValue(new ODPrimitiveRootValue("byte", "Byte", "1"));
    final String plantUMLString = PlantUmlDebuggingVisualizer.toPlantUMLString(diagram);

    assertThat(
        PlantUmlDebuggingVisualizerTest.normalizeString(plantUMLString),
        is(
            """
                                                                                               @startuml
                                                                                               !pragma layout smetana
                                                                                               object "PrimitiveVariables" as primitiveVariables {
                                                                                               byte=1
                                                                                               double=1.2
                                                                                               int=1
                                                                                               }
                                                                                               @enduml
                                                                                               """));
  }

  @Test
  void toPlantUMLStringAttributesTest() {
    final ObjectDiagram diagram = new ObjectDiagram();
    final ODObject odObject = new ODObject(1, "Product", "foldingWallTable");
    diagram.addObject(odObject);
    odObject.addAttribute(new ODAttributeValue("name", "String", "\"folding wall table\""));
    odObject.addAttribute(new ODAttributeValue("price", "Integer", "25"));

    final String plantUMLString = PlantUmlDebuggingVisualizer.toPlantUMLString(diagram);

    assertThat(
        PlantUmlDebuggingVisualizerTest.normalizeString(plantUMLString),
        is(
            """
                                                                                               @startuml
                                                                                               !pragma layout smetana
                                                                                               object "foldingWallTable:Product" as 1 {
                                                                                               name="folding wall table"
                                                                                               price=25
                                                                                               }
                                                                                               @enduml
                                                                                               """));
  }

  @NotNull private static String normalizeString(final String plantUMLString) {
    return plantUMLString.replace("\r\n", "\n");
  }

  @Test
  void toPlantUMLStringLinksTest() {
    final ObjectDiagram diagram = new ObjectDiagram();
    final ODObject person1 = new ODObject(1, "Person", "1");
    final ODObject person2 = new ODObject(2, "Person", "2");
    final ODLink friendLink1 = new ODLink(person1, person2, "friends");
    final ODLink friendLink2 = new ODLink(person2, person1, "friends");
    person1.addLink(friendLink1);
    person2.addLink(friendLink2);
    diagram.addObject(person1);
    diagram.addObject(person2);
    diagram.addLink(friendLink1);
    diagram.addLink(friendLink2);

    final String plantUMLString = PlantUmlDebuggingVisualizer.toPlantUMLString(diagram);

    assertThat(
        PlantUmlDebuggingVisualizerTest.normalizeString(plantUMLString),
        is(
            """
                                                                                               @startuml
                                                                                               !pragma layout smetana
                                                                                               object "1:Person" as 1
                                                                                               object "2:Person" as 2
                                                                                               1 --> 2 : friends
                                                                                               2 --> 1 : friends
                                                                                               @enduml
                                                                                               """));
  }

  @Test
  void toPlantUMLStringPrimitiveMapsTest() {
    final ObjectDiagram diagram = new ObjectDiagram();
    final ODObject hashMap = new ODObject(1, "java.util.HashMap", "hashMap");
    final ODObject hashMapNode1 = new ODObject(2, "java.util.HashMap$Node", "0");
    final ODObject hashMapNode2 = new ODObject(3, "java.util.HashMap$Node", "1");
    final ODObject hashMapNode3 = new ODObject(4, "java.util.HashMap$Node", "2");
    this.addKeyValueToNode(hashMapNode1);
    this.addKeyValueToNode(hashMapNode2);
    this.addKeyValueToNode(hashMapNode3);
    final ODLink node1Link = new ODLink(hashMap, hashMapNode1, "0");
    final ODLink node2Link = new ODLink(hashMap, hashMapNode2, "1");
    final ODLink node3Link = new ODLink(hashMap, hashMapNode3, "2");
    hashMap.addLink(node1Link);
    diagram.addLink(node1Link);
    hashMap.addLink(node2Link);
    diagram.addLink(node2Link);
    hashMap.addLink(node3Link);
    diagram.addLink(node3Link);

    final ODObject otherMap = new ODObject(5, "java.util.SomeOtherMap", "otherMap");
    final ODObject otherMapNode1 = new ODObject(6, "java.util.SomeOtherMap$Node", "0");
    final ODObject otherMapNode2 = new ODObject(7, "java.util.SomeOtherMap$Node", "1");
    this.addKeyValueToNode(otherMapNode1);
    this.addKeyValueToNode(otherMapNode2);
    final ODLink otherMapNode1Link = new ODLink(otherMap, otherMapNode1, "0");
    final ODLink otherMapNode2Link = new ODLink(otherMap, otherMapNode2, "1");
    otherMap.addLink(otherMapNode1Link);
    diagram.addLink(otherMapNode1Link);
    otherMap.addLink(otherMapNode2Link);
    diagram.addLink(otherMapNode2Link);

    diagram.addObject(hashMap);
    diagram.addObject(hashMapNode1);
    diagram.addObject(hashMapNode2);
    diagram.addObject(hashMapNode3);
    diagram.addObject(otherMap);
    diagram.addObject(otherMapNode1);
    diagram.addObject(otherMapNode2);

    final String plantUMLString = PlantUmlDebuggingVisualizer.toPlantUMLString(diagram);

    assertThat(
        PlantUmlDebuggingVisualizerTest.normalizeString(plantUMLString),
        is(
            """
                                                                                               @startuml
                                                                                               !pragma layout smetana
                                                                                               map "hashMap:HashMap" as 1 {
                                                                                               key1 => value2
                                                                                               key3 => value4
                                                                                               key5 => value6
                                                                                               }
                                                                                               map "otherMap:SomeOtherMap" as 5 {
                                                                                               key7 => value8
                                                                                               key9 => value10
                                                                                               }
                                                                                               @enduml
                                                                                               """));
  }

  @Test
  void toPlantUMLStringPrimitiveMapsWithNullKeyAndValueTest() {
    final ObjectDiagram diagram = new ObjectDiagram();
    final ODObject hashMap = new ODObject(1, "java.util.HashMap", "hashMap");
    final ODObject hashMapNode1 = new ODObject(2, "java.util.HashMap$Node", "0");
    final ODObject hashMapNode2 = new ODObject(3, "java.util.HashMap$Node", "1");
    final ODObject hashMapNode3 = new ODObject(4, "java.util.HashMap$Node", "2");
    this.addKeyValueToNode(hashMapNode1);
    // Only key means the value is null.
    hashMapNode2.addAttribute(
        new ODAttributeValue("key", "someType", "key" + this.counter.incrementAndGet()));
    // Only value means the key is null.
    hashMapNode3.addAttribute(
        new ODAttributeValue("value", "someType", "value" + this.counter.incrementAndGet()));
    final ODLink node1Link = new ODLink(hashMap, hashMapNode1, "0");
    final ODLink node2Link = new ODLink(hashMap, hashMapNode2, "1");
    final ODLink node3Link = new ODLink(hashMap, hashMapNode3, "2");
    hashMap.addLink(node1Link);
    diagram.addLink(node1Link);
    hashMap.addLink(node2Link);
    diagram.addLink(node2Link);
    hashMap.addLink(node3Link);
    diagram.addLink(node3Link);

    diagram.addObject(hashMap);
    diagram.addObject(hashMapNode1);
    diagram.addObject(hashMapNode2);
    diagram.addObject(hashMapNode3);

    final String plantUMLString = PlantUmlDebuggingVisualizer.toPlantUMLString(diagram);

    assertThat(
        PlantUmlDebuggingVisualizerTest.normalizeString(plantUMLString),
        is(
            """
                                                                                               @startuml
                                                                                               !pragma layout smetana
                                                                                               map "hashMap:HashMap" as 1 {
                                                                                               key1 => value2
                                                                                               key3 => null
                                                                                               null => value4
                                                                                               }
                                                                                               @enduml
                                                                                               """));
  }

  private void addKeyValueToNode(final ODObject hashMapNode1) {
    hashMapNode1.addAttribute(
        new ODAttributeValue("key", "someType", "key" + this.counter.incrementAndGet()));
    hashMapNode1.addAttribute(
        new ODAttributeValue("value", "someType", "value" + this.counter.incrementAndGet()));
  }
}
