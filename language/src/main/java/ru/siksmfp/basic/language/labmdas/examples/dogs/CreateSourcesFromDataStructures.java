package ru.siksmfp.basic.language.labmdas.examples.dogs;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @author Artem Karnov @date 03.05.2017.
 *         artem.karnov@t-systems.com
 */

public class CreateSourcesFromDataStructures {
    //Get all children of node parent as stream
    public static Stream<Node> children(Node parent) {
        NodeList nodeList = parent.getChildNodes();
        return IntStream
                .range(0, nodeList.getLength())
                .mapToObj(nodeList::item);
    }

    //Create source with index
    public static <T> Stream<IndexExampleClass<T>> withIndex(List<T> list) {
        return IntStream
                .range(0, list.size())
                .mapToObj(idx -> new IndexExampleClass<>(idx, list.get(idx)));
    }

    //Cartesian product of 3 lists
    public static Stream<String> limitedCartesianProduct() {
        List<List<String>> input = Arrays.asList(
                Arrays.asList("a", "b", "c"),
                Arrays.asList("x", "y"),
                Arrays.asList("1", "2", "3")
        );

        return input.get(0).stream().flatMap(a ->
                input.get(1).stream().flatMap(b ->
                        input.get(2).stream()
                                .map(c -> a + b + c)));
    }

    //Cartesian product of n lists
    public static Stream<String> unlimitedCartesianProduct() {
        List<List<String>> input = Arrays.asList(
                Arrays.asList("a", "b", "c"),
                Arrays.asList("x", "y"),
                Arrays.asList("1", "2", "3")
        );

        Supplier<Stream<String>> s = input.stream()
                //Stream<List<String>>
                .<Supplier<Stream<String>>>map(list -> list::stream)
                //Stream<Supplier<Stream<String>>>
                .reduce((sup1, sup2) -> () -> sup1.get()
                        .flatMap(e1 -> sup2.get().map(e2 -> e1 + e2)))
                //Optional<Supplier<Stream<String>>>
                .orElse(() -> Stream.of(""));

        return s.get();
    }
}
