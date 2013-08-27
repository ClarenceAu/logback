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

import java.util.List;

import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.ContextAwareBase;
import ch.qos.logback.core.spi.FilterAttachableImpl;
import ch.qos.logback.core.spi.FilterReply;
import ch.qos.logback.core.status.WarnStatus;

/**
 * Sets a skeleton implementation for appenders.
 * 
 * <p> For more information about this appender, please refer to the online
 * manual at http://logback.qos.ch/manual/appenders.html#AppenderBase
 *
 * <p>
 *      AppenderBase类提供了一个Appender实现的基本框架，并且可以作为一个Appender实现的Helper类。
 *      从AppenderBase类可以看到一个Appender实现的基本依赖关系。
 *      Appender继承的FilterAttachable接口，在本类中通过委托给FilterAttachableImpl的对象来实现其方法。
 *      而LifeCycle接口则通过一个名为started的布尔值来实现。
 *      而AppenderBase本身也继承自ContextAwareBase类，以获得ContextAware接口实现的功能。
 *      在AppenderBase类中，最重要的一个方法就是，提供了一个doAppend()方法的实现框架，其他继承自AppenderBase的子类只需要实现抽象方法append()
 *      就可以了。
 * </p>
 * <p>
 *     在AppenderBase类中，doAppend方法是synchronized的，也就是说，这个方法是同步并且线程安全的。
 *     在doAppend方法中，最重要的一个点就是它会先判断guard字段是否已经为true，guard字段为true代表doAppend方法已经被调用过了。
 *     因此doAppend方法会在guard为true的时候马上退出。
 *     这样做主要是为了防止在其子类中，实现的append方法会由于某种原因又调用到了doAppend方法，由于是在同一线程中的，因此这时是可以进入doAppend方法的。
 *     如果没有guard字段的判断，这是doAppend方法就会陷入一个无限的循环中。
 * </p>
 * 
 * @author Ceki G&uuml;lc&uuml;
 */
abstract public class AppenderBase<E> extends ContextAwareBase implements
    Appender<E> {

  protected boolean started = false;

  /**
   * The guard prevents an appender from repeatedly calling its own doAppend
   * method.
   */
  private boolean guard = false;

  /**
   * Appenders are named.
   */
  protected String name;

  private FilterAttachableImpl<E> fai = new FilterAttachableImpl<E>();

  public String getName() {
    return name;
  }

  private int statusRepeatCount = 0;
  private int exceptionCount = 0;

  static final int ALLOWED_REPEATS = 5;

  public synchronized void doAppend(E eventObject) {
    // WARNING: The guard check MUST be the first statement in the
    // doAppend() method.

    // prevent re-entry.
    if (guard) {
      return;
    }

    try {
      guard = true;

      if (!this.started) {
        if (statusRepeatCount++ < ALLOWED_REPEATS) {
          addStatus(new WarnStatus(
              "Attempted to append to non started appender [" + name + "].",
              this));
        }
        return;
      }

      if (getFilterChainDecision(eventObject) == FilterReply.DENY) {
        return;
      }

      // ok, we now invoke derived class' implementation of append
      this.append(eventObject);

    } catch (Exception e) {
      if (exceptionCount++ < ALLOWED_REPEATS) {
        addError("Appender [" + name + "] failed to append.", e);
      }
    } finally {
      guard = false;
    }
  }

  abstract protected void append(E eventObject);

  /**
   * Set the name of this appender.
   */
  public void setName(String name) {
    this.name = name;
  }

  public void start() {
    started = true;
  }

  public void stop() {
    started = false;
  }

  public boolean isStarted() {
    return started;
  }

  public String toString() {
    return this.getClass().getName() + "[" + name + "]";
  }

  public void addFilter(Filter<E> newFilter) {
    fai.addFilter(newFilter);
  }

  public void clearAllFilters() {
    fai.clearAllFilters();
  }

  public List<Filter<E>> getCopyOfAttachedFiltersList() {
    return fai.getCopyOfAttachedFiltersList();
  }

  public FilterReply getFilterChainDecision(E event) {
    return fai.getFilterChainDecision(event);
  }

}
