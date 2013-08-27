/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2013, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package ch.qos.logback.core;
  
import ch.qos.logback.core.spi.ContextAware;
import ch.qos.logback.core.spi.FilterAttachable;
import ch.qos.logback.core.spi.LifeCycle;

/**
 * 从这个Appender的接口的继承关系可以看出，每一个Appender的基本结构：
 * ContextAware接口：要实现这个接口，意味着必须在具体实现中有一个Context的实现
 * FilterAttachable接口：意味着每一个Appender都是可以定义一系列的Filter，用于在某些场景下的调用的
 * LifeCycle接口：意味着在实现类中可能会有某些用以支撑识别一个Appender的声明周期的情况
 * @param <E>
 */
public interface Appender<E> extends LifeCycle, ContextAware, FilterAttachable<E> {

  /**
   * Get the name of this appender. The name uniquely identifies the appender.
   * 返回这个Appender的名称
   */
  String getName();

  /**
   * This is where an appender accomplishes its work. Note that the argument 
   * is of type Object.
   * 每一个Appender实际完成日志输出的方法，它的参数是一个泛型参数。
   * @param event
   */
  void doAppend(E event) throws LogbackException;

  /**
   * Set the name of this appender. The name is used by other components to
   * identify this appender.
   * 设置Appender的名称
   */
  void setName(String name);
  
}
