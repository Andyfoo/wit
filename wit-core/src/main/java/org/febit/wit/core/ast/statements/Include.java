// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core.ast.statements;

import org.febit.wit.InternalContext;
import org.febit.wit.Template;
import org.febit.wit.core.ast.Expression;

/**
 *
 * @author zqq90
 */
public final class Include extends AbstractInclude {

    public Include(Expression pathExpr, Expression paramsExpr, Template template, int line, int column) {
        super(pathExpr, paramsExpr, template, line, column);
    }

    @Override
    public Object execute(final InternalContext context) {
        mergeTemplate(context, false);
        return null;
    }
}
