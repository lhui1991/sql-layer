/**
 * Copyright (C) 2009-2013 Akiban Technologies, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.foundationdb.server.types3;

public final class ReversedLazyList<T> extends LazyListBase<T> {

    @Override
    public T get(int i) {
        i = originalSize -i - 1;
        return original.get(i);
    }

    @Override
    public int size() {
        return originalSize;
    }

    public ReversedLazyList(LazyList<? extends T> original) {
        this.original = original;
        originalSize = original.size();
    }

    private final LazyList<? extends T> original;
    private final int originalSize;
}