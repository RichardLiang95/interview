import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

public class ObjectUtil {

    public static void copyProperties(Object source, Object target, String[] properties) {
        // 第一步，检查参数是否有效
        if (properties == null || properties.length == 0) {
            throw new InvalidParameterException("参数无效");
        }


        for (String property : properties) {
            try {
                Field sourceField = getField(source.getClass(), property);
                Field targetField = getField(target.getClass(), property);

                if (sourceField == null || targetField == null) {
                    continue;  // 如果源或目标都没有该字段，则跳过
                }

                if (!sourceField.getType().equals(targetField.getType())) {
                    throw new RuntimeException("属性类型匹配错误: " + property);
                }

                sourceField.setAccessible(true);
                targetField.setAccessible(true);

                Object sourceValue = sourceField.get(source);
                Object deepCopiedValue = deepCopy(sourceValue);

                targetField.set(target, deepCopiedValue);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("复制属性错误：" + property, e);
            }
        }
    }

    // 通过反射获取字段
    private static Field getField(Class<?> clazz, String property) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(property);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass(); // 如果没有找到字段，移动到超类
            }
        }
        return null;
    }

    // 深拷贝
    private static Object deepCopy(Object source) throws IllegalAccessException {
        if (source == null) {
            return null;
        }

        Class<?> sourceClass = source.getClass();
        //对于数组类型，递归深拷贝每一个元素
        if (sourceClass.isArray()) {
            int length = Array.getLength(source);
            Object newArray = Array.newInstance(sourceClass.getComponentType(), length);
            for (int i = 0; i < length; i++) {
                Array.set(newArray, i, deepCopy(Array.get(source, i)));
            }
            return newArray;

        } else if (sourceClass.isPrimitive() || sourceClass == String.class || Number.class.isAssignableFrom(sourceClass)
                || Boolean.class == sourceClass || Character.class == sourceClass) {
            return source;  // 对于原始类型、String、Number、Boolean、Character 等不可变对象，直接返回原对象
        } else {
            try {
                //对于其他复杂对象，反射调用无参构造方法创建新实例，并递归拷贝所有字段。
                Object target = sourceClass.getDeclaredConstructor().newInstance();
                for (Field field : sourceClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    Object fieldValue = field.get(source);
                    Object copiedValue = deepCopy(fieldValue);
                    field.set(target, copiedValue);
                }
                return target;
            } catch (Exception e) {
                throw new RuntimeException("对象类型拷贝失败: " + sourceClass, e);
            }
        }
    }

}
