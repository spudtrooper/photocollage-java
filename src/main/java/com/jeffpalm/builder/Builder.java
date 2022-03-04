package com.jeffpalm.builder;

/**
 * An interface to build instances of type <code>T</code>.
 * 
 * @param <T>
 *          Instances that are built by this interface.
 */
public interface Builder<T> {

  /**
   * Builds an instance of type <code>T</code>.
   * 
   * @return The built instance.
   * */
  T build();
}
