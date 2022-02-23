package dev.vality.magista.util;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

public class BeanUtil {

    public static <T> void merge(T source, T target, String... ignoreProperties) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(source.getClass());
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (writeMethod != null && Arrays.binarySearch(ignoreProperties, propertyDescriptor.getName()) < 0) {
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    try {
                        if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                            readMethod.setAccessible(true);
                        }
                        if (readMethod.invoke(target) == null) {
                            Object value = readMethod.invoke(source);
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        }
                    } catch (Throwable ex) {
                        throw new FatalBeanException(
                                "Could not copy property '" + propertyDescriptor.getName() + "' from source to target",
                                ex);
                    }
                }
            }
        }
    }

}
