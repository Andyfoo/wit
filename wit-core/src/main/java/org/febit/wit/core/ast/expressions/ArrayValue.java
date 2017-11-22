// Copyright (c) 2013-2016, febit.org. All Rights Reserved.
package org.febit.wit.core.ast.expressions;

import org.febit.wit.InternalContext;
import org.febit.wit.core.ast.Constable;
import org.febit.wit.core.ast.Expression;
import org.febit.wit.util.StatementUtil;

/**
 *
 * @author zqq90
 */
public final class ArrayValue extends Expression implements Constable {

    private final Expression[] valueExprs;

    public ArrayValue(Expression[] valueExprs, int line, int column) {
        super(line, column);
        StatementUtil.optimize(valueExprs);
        this.valueExprs = valueExprs;
    }

    @Override
    public Object execute(final InternalContext context) {
        return StatementUtil.execute(this.valueExprs, context);
    }

    @Override
    public Object getConstValue() {
        return StatementUtil.calcConstArrayForce(this.valueExprs);
    }
}
