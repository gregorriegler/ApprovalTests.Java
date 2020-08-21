package org.approvaltests.combinations;

import org.approvaltests.strings.Printable;
import org.junit.jupiter.api.Test;
import org.lambda.functions.Function1;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrintableTest {
    @Test
    void testOverridingToString() {
        Integer p1[] = {1,2,3,4,5};
        CombinationApprovals.verifyAllCombinations(n -> n.get(), Printable.create(n-> "#"+n, p1));

        Printable<Integer> p2[] = Printable.create(n -> "#"+n, 1,2,3,4,5);
        CombinationApprovals.verifyAllCombinations(n -> n.get(), p2);
    }

    @Test
    void testObjectExtention() {
        Point p = new Point(1,2);
        Point i = Printable.wrap(p, n -> String.format("(%s,%s)",n.x,n.y));
        assertEquals("(1,2)", i.toString());
        assertEquals(1, i.x);
        assertEquals(2, i.y);
        i.x = 3;
        assertEquals("(3,2)", i.toString());
    }
    public static class Person{
        private  String name;
        private  int age;

        public Person(String name, int age){

            this.name = name;
            this.age = age;
        }


        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }
    }
    @Test
    void testObjectExtentionOfObjectWithPrivateFields() {
        List<Integer> orinigal = new ArrayList();
        orinigal.addAll(Arrays.asList(1,2,3));
        Function1<List<Integer>, String> toString = n -> String.format("List[%s] ", n.size());
        List<Integer> wrapper = Printable.wrap(orinigal, toString);
        assertEquals(toString.call(orinigal), wrapper.toString());

    }



    @Test
    void testLabels() {
        Printable<Integer> p[] = Printable.with()
                .label(1,"first")
                .label(2,"second")
                .label(3,"third")
                .label(4, "forth")
                .label(5, "fifth")
                .toArray();
        CombinationApprovals.verifyAllCombinations(n -> n.get(), p);
    }
}
