// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core.ast.operators;

import org.febit.wit.core.ast.AssignableExpression;
import org.febit.wit.core.ast.Expression;
import org.febit.wit.core.ast.SelfOperator;
import org.febit.wit.util.ALU;

/**
 *
 * @author zqq90
 */
public final class SelfDiv extends SelfOperator {

    public SelfDiv(AssignableExpression leftExp, Expression rightExp, int line, int column) {
        super(leftExp, rightExp, line, column);
    }

    @Override
    protected Object doOperate(final Object right, final Object left) {
        return ALU.div(left, right);
    }
}
