package org.actech.smart.common.utils;

import org.junit.Test;

import java.lang.reflect.Type;
import java.util.Stack;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by paul on 2018/3/19.
 */
public class GenericClassUtilsTest {

    @Test
    public void shouldFindGenericClassType() {
        Class type = GenericClassUtils.getGenericParameterizedType(ClassB.class, ClassA.class, 0);
        assertThat(type.getSimpleName(), is(String.class.getSimpleName()));
    }

    @Test
    public void shouldFindGenericInterfaceType() {
        Class type = GenericClassUtils.getGenericParameterizedType(ClassC.class, InterfaceA.class, 0);
        assertThat(type.getSimpleName(), is(String.class.getSimpleName()));
    }

    @Test
    public void shouldFindSecondGenericInterfaceType() {
        Class type = GenericClassUtils.getGenericParameterizedType(ClassD.class, InterfaceA.class, 0);
        assertThat(type.getSimpleName(), is(String.class.getSimpleName()));
    }

    @Test
    public void shouldFindGenericInterfaceGenericType() {
        Class type = GenericClassUtils.getGenericParameterizedType(ClassD.class, InterfaceA.class, 0);
        assertThat(type.getSimpleName(), is(String.class.getSimpleName()));
    }

    @Test
    public void shouldRecordsTheSearchPath() {
        Stack<Type> path2Target = GenericClassUtils.traversal(ClassI.class, ClassG.class, new Stack<Type>());
        assertThat(path2Target, is(notNullValue()));
    }

    @Test
    public void shouldKnowTheGenericType() {
        Class<?> entity = GenericClassUtils.getGenericParameterizedType(ClassI.class, ClassG.class, 1);
        assertThat(entity.getSimpleName(), is("String"));
    }

    @Test
    public void shouldKnowDeepGenericType() {
        Class<?> entity = GenericClassUtils.getGenericParameterizedType(ClassI.class, ClassG.class, 0);
        assertThat(entity.getSimpleName(), is("Double"));
    }




    class ClassA<T> {}
    class ClassB extends ClassA<String> {}

    interface InterfaceA<T>{}
    class ClassC implements InterfaceA<String> {}

    interface InterfaceB<X> extends InterfaceA<String> {}
    class ClassD implements InterfaceB<Double> {}

    interface InterfaceC<X, T>{}
    class ClassE implements InterfaceC<Double ,String>{}

    class ClassF extends ClassA implements InterfaceB<String>, InterfaceC<Double, String> {}

    class ClassG<A, B>{}

    class ClassH<C, D> extends ClassG<C, String>{}

    class ClassI extends ClassH<Double, Object> implements InterfaceC<Integer, Boolean>, InterfaceA<Float>{}

}