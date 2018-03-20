package org.actech.smart.common.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ReflectionUtils;
import sun.reflect.generics.repository.ClassRepository;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * Created by paul on 2018/3/19.
 */
public class GenericClassUtils {
    private static final Log logger = LogFactory.getLog(GenericClassUtils.class);

    public static Class<?> getGenericParameterizedType(Class clazz, Class target, int index) {
        Stack<Type> path2Target = traversal(clazz, target, new Stack<Type>());
        if (path2Target == null) {
            logger.error("当前类的父类中没有找到对应的目标对象");
            return null;
        }

        return getGenericParameterizedType(path2Target, index);
    }

    public static Stack<Type> traversal(Type source, Class target, Stack<Type> path) {
        path.push(source);
        if (source instanceof ParameterizedType) {
            Class rawClass = (Class)((ParameterizedType)source).getRawType();
            if (target.getName().equals(rawClass.getName())) {
                return path;
            }
        }

        List<Type> parents = getAllParents(source);

        for (int i=0; i<parents.size(); i++) {
            Stack<Type> path2Target = traversal(parents.get(i), target, path);
            if (path2Target != null) return path2Target;
        }

        path.pop();
        return null;
    }

    public static List<Type> getAllParents(Type source) {
        List<Type> parents = new ArrayList<Type>();

        if (source == null) return parents;

        Class targetClazz = null;
        if (source instanceof Class) targetClazz = (Class)source;
        if (source instanceof ParameterizedType) targetClazz = (Class)((ParameterizedType)source).getRawType();

        Type superClass = targetClazz.getGenericSuperclass();
        if (superClass != null) parents.add(superClass);

        Type[] superInterfaces = targetClazz.getGenericInterfaces();
        if (superInterfaces != null) parents.addAll(Arrays.asList(superInterfaces));

        return parents;
    }


    private static Class<?> getGenericParameterizedType(Stack<Type> path, int index) {
        if (path == null || path.size()==0) return null;
        Type currentType = path.pop();

        if (currentType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType)currentType;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length < index) {
                logger.error("泛型参数比实际需要个数少，无法解析泛型对象");
                return null;
            }

            Type actualType = actualTypeArguments[index];
            if (actualType instanceof Class) {
                return (Class)actualType;
            } else if (actualType instanceof TypeVariable) {
                TypeVariable typeVariable = (TypeVariable)actualType;
                Type superType = path.peek();

                Integer typeVariableIndex = getTypeVariableIndex(superType, typeVariable);
                if (typeVariableIndex == null) {
                    logger.error("泛型继承链异常，无法解析泛型对象");
                    return null;
                }

                return getGenericParameterizedType(path, typeVariableIndex);
            }

            return null;
        } else {
            logger.error("泛型继承链异常，无法解析泛型对象");
            return null;
        }
    }

    private static Integer getTypeVariableIndex(Type superType, TypeVariable typeVariable) {
        if (superType == null) {
            logger.error("泛型继承链异常，无法解析泛型对象");
            return null;
        }

        if (!(superType instanceof ParameterizedType)) {
            logger.error("泛型继承链异常，无法解析泛型对象");
            return null;
        }

        ParameterizedType parameterizedType = (ParameterizedType)superType;
        Type rawType = parameterizedType.getRawType();

        if (!(rawType instanceof Class)) {
            logger.error("泛型继承链异常，无法解析泛型对象");
            return null;
        }

        Class rawClass = (Class)rawType;
        Field field = ReflectionUtils.findField(rawClass.getClass(), "genericInfo");

        if (field == null) {
            logger.error("泛型继承链异常，无法解析泛型对象");
            return null;
        }
        field.setAccessible(true);

        ClassRepository classRepository = (ClassRepository)ReflectionUtils.getField(field, rawClass);
        TypeVariable[] typeVariables = classRepository.getTypeParameters();
        for (int i=0; i<typeVariables.length; i++) {
            if (typeVariables[i] == typeVariable) return i;
        }

        return null;
    }
}
