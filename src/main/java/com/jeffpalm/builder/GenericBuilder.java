package com.jeffpalm.builder;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A class to implement the builder pattern using reflection and dynamic
 * proxies. Us this if you want to define a builder interface to construct a
 * class without writing anything more than the interfae. The fields will be set
 * by reflection and the builder instance is created using a dynamic proxy.
 * 
 * For example, the following code defines the builder:
 * 
 * <pre>
 * 
 * class Thing {
 * 
 *   private File file;
 *   private int width;
 * 
 *   private Thing() {
 *   }
 * 
 *   interface Builder extends com.jeffpalm.buider.Builder {
 *     Builder setFile(File file);
 * 
 *     Builder setWidth(int width);
 *   }
 * 
 *   // Returns a builder to build Things.
 *   public static Builder newBuilder() {
 *     return new GenericBuilder&lt;Builder&gt;(new Thing(), Builder.class).asBuilder();
 *   }
 * }
 * </pre>
 * 
 * The following code is usage:
 * 
 * <pre>
 * Thing thing = Thing.newBuilder().setFile(file).setWidth(100).build();
 * thing.getWidth() == 100; // true
 * </pre>
 * 
 * @param <B>
 *            builder class
 */
public final class GenericBuilder<B extends Builder<?>> implements InvocationHandler {

  private final Object targetObject;
  private final Class<B> builderClass;
  private B builderProxy;

  /**
   * @param builtObject
   *                     The object containing the fields to set.
   * @param builderClass
   */
  public GenericBuilder(Object builtObject, Class<B> builderClass) {
    this.targetObject = builtObject;
    this.builderClass = builderClass;
  }

  /**
   * Gets this instance as a builder. If called multiple times, the same object
   * will be returned. This should be called by the class being created. The
   * pattern is to have a static method called <code>newBuilder</code> and call
   * this on a newly-constructed instance of {@link GenericBuilder}, e.g:
   * 
   * <pre>
   * class Thing {
   *   interface Builder { ... }
   *   public static Builder new Builder() {
   *     return new GenericBuilder<Builder>(new Thing(), Builder.class).asBuilder();
   *   }
   * }
   * </pre>
   * 
   * @return This as a builder of type <code>B</code>.
   */
  public B asBuilder() {
    if (builderProxy == null) {
      builderProxy = builderClass.cast(Proxy.newProxyInstance(builderClass.getClassLoader(),
          new Class<?>[] { builderClass }, this));
    }
    return builderProxy;
  }

  /**
   * Sets the field with name <code>fieldName</code> by reflection to
   * <code>value</code>.
   * 
   * @param fieldName
   * @param value
   */
  private void set(String fieldName, Object value) {
    Field f = null;
    try {
      f = targetObject.getClass().getField(fieldName);
    } catch (Throwable _) {
    }
    if (f == null) {
      for (Class<?> cls = targetObject.getClass(); cls != null && !cls.equals(Object.class); cls = cls
          .getSuperclass()) {
        try {
          f = cls.getDeclaredField(fieldName);
          f.setAccessible(true);
          break;
        } catch (Throwable _) {
        }
      }
    }
    if (f == null) {
      throw new RuntimeException("Could not set " + fieldName + " of "
          + targetObject.getClass().getName() + " " + targetObject);
    }
    try {
      f.set(targetObject, value);
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    // Invoke Object's methods on this guy
    if (Object.class == method.getDeclaringClass()) {
      String name = method.getName();
      if ("equals".equals(name)) {
        return proxy == args[0];
      } else if ("hashCode".equals(name)) {
        return System.identityHashCode(proxy);
      } else if ("toString".equals(name)) {
        return proxy.getClass().getName() + "@"
            + Integer.toHexString(System.identityHashCode(proxy)) + ", with InvocationHandler "
            + this;
      } else {
        throw new IllegalStateException(String.valueOf(method));
      }
    }

    // Invoke the Builder's methods by setting values on builtObject
    if (method.getDeclaringClass().equals(builderClass)
        || method.getDeclaringClass().equals(Builder.class)) {
      String name = method.getName();
      if (name.startsWith("set") && args.length == 1) {
        // Make the field name the lower case first char plus the rest.
        String fieldName = Character.toLowerCase(name.charAt(3)) + name.substring(4);
        set(fieldName, args[0]);
        return builderProxy;
      }
      if (method.getReturnType().isInstance(targetObject)) {
        return targetObject;
      }
      return null;
    }
    throw new RuntimeException("Cannot invoke " + method);
  }
}
