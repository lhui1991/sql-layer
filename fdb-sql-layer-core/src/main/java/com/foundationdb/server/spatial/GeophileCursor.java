/**
 * Copyright (C) 2009-2013 FoundationDB, LLC
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

package com.foundationdb.server.spatial;

import com.foundationdb.qp.operator.CursorBase;
import com.foundationdb.qp.row.Row;
import com.foundationdb.qp.storeadapter.indexcursor.IndexCursor;
import com.geophile.z.Cursor;
import com.geophile.z.Record;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GeophileCursor<RECORD extends Record> extends Cursor<RECORD>
{
    // Cursor interface

    @Override
    public RECORD next() throws IOException, InterruptedException
    {
        assert false;
        return null;
/*
        if (currentCursor == null) {
            // A cursor should have been registered with z-value (0x0, 0).
            currentCursor = cursors.get(0L);
            assert currentCursor != null;
        }
        return currentCursor.next();
*/
    }

    @Override
    public RECORD previous() throws IOException, InterruptedException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void goTo(RECORD key) throws IOException, InterruptedException
    {
/*
        long z = key.z();
        currentCursor = cursors.get(z);
        assert currentCursor != null : z;
        if (!openEarly) {
            currentCursor.open(); // open is idempotent
        }
        currentCursor.goTo(key);
*/
        assert false;
    }

    @Override
    public boolean deleteCurrent() throws IOException, InterruptedException
    {
        return false;
    }

    // GeophileCursor interface

    public void addCursor(long z, IndexCursor indexCursor)
    {
        if (currentCursor != null) {
            // Shouldn't add a cursor after the cursor has been opened.
            throw new IllegalArgumentException();
        }
        CachingCursor cachingCursor = new CachingCursor(z, indexCursor);
        if (openEarly) {
            cachingCursor.open();
        }
        cursors.put(z, cachingCursor);
    }

    public void close()
    {
        for (CachingCursor cursor : cursors.values()) {
            cursor.close();
        }
    }

    public GeophileCursor(GeophileIndex<RECORD> index, boolean openEarly)
    {
        super(index);
        this.index = index;
        this.openEarly = openEarly;
        if (openEarly) {
            for (Map.Entry<Long, CachingCursor> entry : cursors.entrySet()) {
                long z = entry.getKey();
                CachingCursor cursor = entry.getValue();
                cursor.open();
                if (z == 0L) {
                    currentCursor = cursor;
                }
            }
        }
    }

    // Object state

    private final GeophileIndex<RECORD> index;
    private final boolean openEarly;
    private final Map<Long, CachingCursor> cursors = new HashMap<>(); // z -> CachingCursor
    private CachingCursor currentCursor;
}
