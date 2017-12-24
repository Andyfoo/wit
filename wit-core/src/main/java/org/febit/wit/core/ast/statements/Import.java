// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core.ast.statements;

import java.util.Map;
import org.febit.wit.InternalContext;
import org.febit.wit.Template;
import org.febit.wit.core.VariantIndexer;
import org.febit.wit.core.ast.AssignableExpression;
import org.febit.wit.core.ast.Expression;

/**
 *
 * @author zqq90
 */
public final class Import extends AbstractInclude {

    private final String[] exportNames;
    private final AssignableExpression[] toResetableValues;
    private final boolean exportAll;

    public Import(Expression pathExpr, Expression paramsExpr, String[] exportNames, AssignableExpression[] toContextValues, Template template, int line, int column) {
        super(pathExpr, paramsExpr, template, line, column);
        if (exportNames == null || exportNames.length == 0) {
            this.exportNames = null;
            this.toResetableValues = null;
            this.exportAll = true;
        } else {
            this.exportNames = exportNames;
            this.toResetableValues = toContextValues;
            this.exportAll = false;
        }
    }

    @Override
    public Object execute(final InternalContext context) {
        final Map<String, Object> results = mergeTemplate(context, true);
        if (exportAll) {
            final VariantIndexer destIndexer = context.indexers[context.indexer];
            if (context.indexer == destIndexer.id) {
                final Object[] destVars = context.vars;
                results.forEach((key, val) -> {
                    int index = destIndexer.getCurrentIndex(key);
                    if (index >= 0) {
                        destVars[index] = val;
                    }
                });
            }
        } else {
            final String[] names = this.exportNames;
            final int len = names.length;
            final AssignableExpression[] myToResetableValues = this.toResetableValues;
            for (int i = 0; i < len; i++) {
                myToResetableValues[i].setValue(context, results.get(names[i]));
            }
        }
        return null;
    }
}
