// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.resolvers;

/**
 *
 * @author zqq90
 * @param <T>
 */
public interface Resolver<T> {

    Class<T> getMatchClass();
}
