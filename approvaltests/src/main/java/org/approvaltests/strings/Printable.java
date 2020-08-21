package org.approvaltests.strings;

import com.spun.util.ObjectUtils;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.lambda.functions.Function1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Printable<T> {

    private T value;
    private final Function1<T, String> toString;

    public Printable(T value, Function1<T, String> toString) {
        this.value = value;
        this.toString = toString;
    }

    public static <T> Printable<T>[] create(Function1<T, String> toString, T... parameters) {
        return Arrays.stream(parameters)
                .map(p -> new Printable<>(p, toString)).toArray(Printable[]::new);
    }

    public static LabelMakerStarter with() {
        return new LabelMakerStarter();
    }

    private static <T> List<Field> getFields(T t) {
        List<Field> fields = new ArrayList<>();
        Class clazz = t.getClass();
        while (clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    public static <T> T wrap(T t, Function1<T, String> toString) {
        Printable printer = new Printable(t, toString);
        Class<T> dynamicType = (Class<T>) new ByteBuddy()
                .subclass(t.getClass())
                .method(ElementMatchers.named("toString"))

                .intercept(MethodDelegation.to(printer))
                .make()
                .load(Printable.class.getClassLoader())
                .getLoaded();
        try {
            final T newT = dynamicType.newInstance();
            for (Field f : getFields(t)) {
                if (!isStatic(f)) {
                    f.setAccessible(true);
                    f.set(newT, f.get(t));
                }

            }
            printer.value = newT;
            return newT;
        } catch (InstantiationException | IllegalAccessException e) {
            throw ObjectUtils.throwAsError(e);
        }
    }

    private static boolean isFinal(Field f) {
        return (f.getModifiers() & Modifier.FINAL) == Modifier.FINAL;
    }
    private static boolean isTransient(Field f) {
        return Modifier.isTransient(f.getModifiers());
    }
    private static boolean isStatic(Field f) {
        return Modifier.isStatic(f.getModifiers());
    }

    public T get() {
        return value;
    }

    @Override
    public String toString() {
        return toString.call(value);
    }

    public static class LabelMakerStarter {
        public <T> LabelMaker<T> label(T value, String label) {
            LabelMaker<T> maker = new LabelMaker<T>();

            return maker.label(value, label);

        }
    }

    public static class LabelMaker<T> {
        private ArrayList<Printable<T>> values = new ArrayList<>();

        public LabelMaker<T> label(T value, String label) {
            values.add(new Printable(value, __ -> label));
            return this;

        }

        public Printable<T>[] toArray() {
            return values.toArray(new Printable[0]);
        }
    }
}
