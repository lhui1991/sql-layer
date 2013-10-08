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

package com.foundationdb.server.types.mcompat.aggr;

import com.foundationdb.server.types.TAggregator;
import com.foundationdb.server.types.TAggregatorBase;
import com.foundationdb.server.types.TInstance;
import com.foundationdb.server.types.TOverloadResult;
import com.foundationdb.server.types.pvalue.PValue;
import com.foundationdb.server.types.pvalue.PValueSource;
import com.foundationdb.server.types.pvalue.PValueTarget;
import com.foundationdb.server.types.pvalue.PValueTargets;

public class MFirst extends TAggregatorBase {

    public static final TAggregator[] INSTANCES = {
        new MFirst ("FIRST"),
    };
    
    protected MFirst(String name) {
        super(name, null);
    }

    @Override
    public void input(TInstance instance, PValueSource source,
            TInstance stateType, PValue state, Object option) {
        if (source.isNull())
            return;

        if (!state.hasAnyValue())
            PValueTargets.copyFrom(source, state);
    }

    @Override
    public void emptyValue(PValueTarget state) {
        state.putNull();
    }

    @Override
    public TOverloadResult resultType() {
        return TOverloadResult.picking();
    }
}