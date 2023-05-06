package cn.forbearance.mybatis.refection;

import cn.forbearance.mybatis.refection.invoker.GetFieldInvoker;
import cn.forbearance.mybatis.refection.invoker.Invoker;
import cn.forbearance.mybatis.refection.invoker.MethodInvoker;
import cn.forbearance.mybatis.refection.invoker.SetFieldInvoker;
import cn.forbearance.mybatis.refection.property.PropertyNamer;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射器，属性 get/set 的映射器
 *
 * @author cristina
 */
public class Reflector {

    private static boolean classCacheEnabled = true;

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * 缓存 Reflector
     */
    private static final Map<Class<?>, Reflector> REFLECTOR_MAP = new ConcurrentHashMap<>();

    private Class<?> type;

    /**
     * 可读属性的名称集合，get属性列表
     */
    private String[] readablePropertyNames = EMPTY_STRING_ARRAY;

    /**
     * 可写属性的名称集合，set属性列表
     */
    private String[] writeablePropertyNames = EMPTY_STRING_ARRAY;

    /**
     * set方法列表
     */
    private Map<String, Invoker> setMethods = new HashMap<>();

    /**
     * get方法列表
     */
    private Map<String, Invoker> getMethods = new HashMap<>();

    /**
     * set类型列表
     */
    private Map<String, Class<?>> setTypes = new HashMap<>();

    /**
     * get类型列表
     */
    private Map<String, Class<?>> getTypes = new HashMap<>();

    /**
     * 构造函数
     */
    private Constructor<?> defaultConstructor;

    /**
     * 记录了所有属性名称的集合
     */
    private Map<String, String> caseInsensitivePropertyMap = new HashMap<>();

    /**
     * 完成属性的初始化操作，解析指定的Class类型，并填充上述的集合信息
     *
     * @param clazz
     */
    public Reflector(Class<?> clazz) {
        this.type = clazz;
        // 设置默认的构造方法
        addDefaultConstructor(clazz);
        // 获取 getter 方法
        addGetMethods(clazz);
        // 获取 setter 方法
        addSetMethods(clazz);
        // 处理没有 getter/setter 方法的字段
        addFields(clazz);
        readablePropertyNames = getMethods.keySet().toArray(new String[getMethods.keySet().size()]);
        writeablePropertyNames = setMethods.keySet().toArray(new String[setMethods.keySet().size()]);
        for (String propName : readablePropertyNames) {
            // 属性名称转大写
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
        for (String propName : writeablePropertyNames) {
            caseInsensitivePropertyMap.put(propName.toUpperCase(Locale.ENGLISH), propName);
        }
    }

    /**
     * 获取默认构造器
     *
     * @param clazz
     */
    private void addDefaultConstructor(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.getParameterTypes().length == 0) {
                // 允许访问私有构造器
                if (canAccessPrivateMethods()) {
                    try {
                        constructor.setAccessible(true);
                    } catch (Exception e) {
                    }
                }
                if (constructor.isAccessible()) {
                    this.defaultConstructor = constructor;
                }
            }
        }
    }

    /**
     * 获取 getter 方法
     *
     * @param clazz
     */
    private void addGetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingGetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 3) {
                if (method.getParameterTypes().length == 1) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            } else if (name.startsWith("is") && name.length() > 2) {
                if (method.getParameterTypes().length == 1) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingGetters, name, method);
                }
            }
        }
        resolveGetterConflicts(conflictingGetters);
    }

    /**
     * 获取 setter 方法
     *
     * @param clazz
     */
    private void addSetMethods(Class<?> clazz) {
        Map<String, List<Method>> conflictingSetters = new HashMap<>();
        Method[] methods = getClassMethods(clazz);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 3) {
                if (method.getParameterTypes().length == 1) {
                    name = PropertyNamer.methodToProperty(name);
                    addMethodConflict(conflictingSetters, name, method);
                }
            }
        }
        resolveSetterConflicts(conflictingSetters);
    }

    /**
     * 获取所有属性
     *
     * @param clazz
     */
    private void addFields(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (canAccessPrivateMethods()) {
                try {
                    field.setAccessible(true);
                } catch (Exception e) {

                }
            }
            if (field.isAccessible()) {
                if (!setMethods.containsKey(field.getName())) {
                    int modifiers = field.getModifiers();
                    if (!Modifier.isFinal(modifiers) && Modifier.isStatic(modifiers)) {
                        addSetField(field);
                    }
                }
                if (!getMethods.containsKey(field.getName())) {
                    addGetField(field);
                }
            }
        }
        if (clazz.getSuperclass() != null) {
            addFields(clazz.getSuperclass());
        }
    }

    /**
     * 是否允许访问私有方法
     *
     * @return
     */
    private static boolean canAccessPrivateMethods() {
        try {
            SecurityManager securityManager = System.getSecurityManager();
            if (null != securityManager) {
                securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
            }
        } catch (SecurityException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取所有方法
     *
     * @param clazz
     * @return
     */
    private Method[] getClassMethods(Class<?> clazz) {
        HashMap<String, Method> uniqueMethods = new HashMap<>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            addUniqueMethods(uniqueMethods, currentClass.getDeclaredMethods());
            // 还需要寻找接口方法，因为该类可能是抽象的
            Class<?>[] interfaces = currentClass.getInterfaces();
            for (Class<?> anInterface : interfaces) {
                addUniqueMethods(uniqueMethods, anInterface.getMethods());
            }
            currentClass = currentClass.getSuperclass();
        }
        Collection<Method> methods = uniqueMethods.values();
        return methods.toArray(new Method[methods.size()]);
    }

    /**
     * 记录冲突 getter/setter 方法，去掉get/is前缀后，可能是同一个属性，也可能存在重载方法
     *
     * @param conflictingGetters
     * @param name
     * @param method
     */
    private void addMethodConflict(Map<String, List<Method>> conflictingGetters, String name, Method method) {
        List<Method> methods = conflictingGetters.computeIfAbsent(name, k -> new ArrayList<>());
        methods.add(method);
    }

    /**
     * 解决冲突 getter 方法
     * <p>
     * 去掉get/is前缀后，可能是同一个属性，也可能存在重载方法。
     * getter方法返回类型相同，如果是boolean类型，优先使用is开头的方法；否则抛异常错误的重载方法
     * getter方法返回类型不相同，子类重载父类方法，优先使用类型范围小的方法
     *
     * @param conflictingGetters
     */
    private void resolveGetterConflicts(Map<String, List<Method>> conflictingGetters) {
        for (String propName : conflictingGetters.keySet()) {
            List<Method> getters = conflictingGetters.get(propName);
            Iterator<Method> iterator = getters.iterator();
            Method firstMethod = iterator.next();
            if (getters.size() == 1) {
                addGetMethod(propName, firstMethod);
            } else {
                Method getter = firstMethod;
                Class<?> getterType = firstMethod.getReturnType();
                while (iterator.hasNext()) {
                    Method method = iterator.next();
                    Class<?> methodType = method.getReturnType();
                    if (methodType.equals(getterType)) {
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    } else if (methodType.isAssignableFrom(getterType)) {
                        // getterType是methodType的子类型，子类重载父类方法，优先使用类型范围小的方法
                    } else if (getterType.isAssignableFrom(methodType)) {
                        // methodType是getterType的子类型，子类重载父类方法，优先使用类型范围小的方法
                        getter = method;
                        getterType = methodType;
                    } else {
                        throw new RuntimeException("Illegal overloaded getter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass()
                                + ".  This breaks the JavaBeans " + "specification and can cause unpredicatble results.");
                    }
                }
                addGetMethod(propName, getter);
            }
        }
    }

    /**
     * 记录属性对应的 getter 方法
     *
     * @param name
     * @param method
     */
    private void addGetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            getMethods.put(name, new MethodInvoker(method));
            getTypes.put(name, method.getReturnType());
        }
    }

    private boolean isValidPropertyName(String name) {
        return !(name.startsWith("$")) || "serialVersionUID".equals(name) || "class".equals(name);
    }

    /**
     * 解决冲突 setter 方法
     * <p>
     * setter方法参数类型，和对应的getter方法返回值类型相同则是完全匹配的。
     *
     * @param conflictingSetters
     */
    private void resolveSetterConflicts(Map<String, List<Method>> conflictingSetters) {
        for (String propName : conflictingSetters.keySet()) {
            List<Method> setters = conflictingSetters.get(propName);
            Method firstMethod = setters.get(0);
            if (setters.size() == 1) {
                addSetMethod(propName, firstMethod);
            } else {
                Class<?> expectedType = getTypes.get(propName);
                if (expectedType == null) {
                    throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                            + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                            "specification and can cause unpredicatble results.");
                } else {
                    Iterator<Method> methods = setters.iterator();
                    Method setter = null;
                    while (methods.hasNext()) {
                        Method method = methods.next();
                        if (method.getParameterTypes().length == 1 &&
                                expectedType.equals(method.getParameterTypes()[0])) {
                            setter = method;
                            break;
                        }
                    }
                    if (setter == null) {
                        throw new RuntimeException("Illegal overloaded setter method with ambiguous type for property "
                                + propName + " in class " + firstMethod.getDeclaringClass() + ".  This breaks the JavaBeans " +
                                "specification and can cause unpredicatble results.");
                    }
                    addSetMethod(propName, firstMethod);
                }
            }
        }
    }

    /**
     * 记录属性对应的 setter 方法
     *
     * @param name
     * @param method
     */
    private void addSetMethod(String name, Method method) {
        if (isValidPropertyName(name)) {
            setMethods.put(name, new MethodInvoker(method));
            setTypes.put(name, method.getParameterTypes()[0]);
        }
    }

    private void addSetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            setMethods.put(field.getName(), new SetFieldInvoker(field));
            setTypes.put(field.getName(), field.getType());
        }
    }

    private void addGetField(Field field) {
        if (isValidPropertyName(field.getName())) {
            getMethods.put(field.getName(), new GetFieldInvoker(field));
            getTypes.put(field.getName(), field.getType());
        }
    }

    /**
     * 记录唯一方法
     *
     * @param uniqueMethods
     * @param methods
     */
    private void addUniqueMethods(HashMap<String, Method> uniqueMethods, Method[] methods) {
        for (Method currentMethod : methods) {
            // isBridge()桥接方法。不是泛型方法，实现类中的方法
            if (!currentMethod.isBridge()) {
                String signature = getSignature(currentMethod);
                if (!uniqueMethods.containsKey(signature)) {
                    if (canAccessPrivateMethods()) {
                        try {
                            currentMethod.setAccessible(true);
                        } catch (Exception e) {
                        }
                    }
                    uniqueMethods.put(signature, currentMethod);
                }
            }
        }
    }

    private String getSignature(Method method) {
        StringBuilder builder = new StringBuilder();
        Class<?> returnType = method.getReturnType();
        if (returnType != null) {
            builder.append(returnType.getName()).append('#');
        }
        builder.append(method.getName());
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            if (i == 0) {
                builder.append(':');
            } else {
                builder.append(',');
            }
            builder.append(parameters[i].getName());
        }
        return builder.toString();
    }

    public Class<?> getType() {
        return type;
    }

    public Constructor<?> getDefaultConstructor() {
        if (defaultConstructor != null) {
            return defaultConstructor;
        } else {
            throw new RuntimeException("There is no default constructor for " + type);
        }
    }

    public boolean hasDefaultConstructor() {
        return defaultConstructor != null;
    }

    public Class<?> getSetterType(String propertyName) {
        Class<?> clazz = setTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    public Invoker getGetInvoker(String propertyName) {
        Invoker method = getMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    public Invoker getSetInvoker(String propertyName) {
        Invoker method = setMethods.get(propertyName);
        if (method == null) {
            throw new RuntimeException("There is no setter for property named '" + propertyName + "' in '" + type + "'");
        }
        return method;
    }

    /**
     * Gets the type for a property getter
     *
     * @param propertyName - the name of the property
     * @return The Class of the propery getter
     */
    public Class<?> getGetterType(String propertyName) {
        Class<?> clazz = getTypes.get(propertyName);
        if (clazz == null) {
            throw new RuntimeException("There is no getter for property named '" + propertyName + "' in '" + type + "'");
        }
        return clazz;
    }

    /**
     * Gets an array of the readable properties for an object
     *
     * @return The array
     */
    public String[] getGetablePropertyNames() {
        return readablePropertyNames;
    }

    /**
     * Gets an array of the writeable properties for an object
     *
     * @return The array
     */
    public String[] getSetablePropertyNames() {
        return writeablePropertyNames;
    }

    /**
     * Check to see if a class has a writeable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a writeable property by the name
     */
    public boolean hasSetter(String propertyName) {
        return setMethods.containsKey(propertyName);
    }

    /**
     * Check to see if a class has a readable property by name
     *
     * @param propertyName - the name of the property to check
     * @return True if the object has a readable property by the name
     */
    public boolean hasGetter(String propertyName) {
        return getMethods.containsKey(propertyName);
    }

    public String findPropertyName(String name) {
        return caseInsensitivePropertyMap.get(name.toUpperCase(Locale.ENGLISH));
    }

    public static void setClassCacheEnabled(boolean classCacheEnabled) {
        Reflector.classCacheEnabled = classCacheEnabled;
    }

    public static boolean isClassCacheEnabled() {
        return classCacheEnabled;
    }

    /**
     * Gets an instance of ClassInfo for the specified class.
     * 得到某个类的反射器，是静态方法，而且要缓存，又要多线程，所以REFLECTOR_MAP是一个ConcurrentHashMap
     *
     * @param clazz The class for which to lookup the method cache.
     * @return The method cache for the class
     */
    public static Reflector forClass(Class<?> clazz) {
        if (classCacheEnabled) {
            Reflector cached = REFLECTOR_MAP.get(clazz);
            if (cached == null) {
                cached = new Reflector(clazz);
                REFLECTOR_MAP.put(clazz, cached);
            }
            return cached;
        } else {
            return new Reflector(clazz);
        }
    }

}
