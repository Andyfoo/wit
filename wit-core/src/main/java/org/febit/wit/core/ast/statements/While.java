// Copyright (c) 2013-present, febit.org. All Rights Reserved.
package org.febit.wit.core.ast.statements;

import org.febit.wit.InternalContext;
import org.febit.wit.core.LoopInfo;
import org.febit.wit.core.ast.Expression;
import org.febit.wit.core.ast.Loopable;
import org.febit.wit.core.ast.Statement;
import org.febit.wit.util.ALU;
import org.febit.wit.util.StatementUtil;

import java.util.List;

/**
 * @author zqq90
 */
public final class While extends Statement implements Loopable {

    private final Expression whileExpr;
    private final int indexer;
    private final Statement[] statements;
    private final LoopInfo[] possibleLoops;
    private final int label;

    public While(Expression whileExpr, int indexer, Statement[] statements,
                 LoopInfo[] possibleLoops, int label, int line, int column) {
        super(line, column);
        this.whileExpr = whileExpr;
        this.indexer = indexer;
        this.statements = statements;
        this.possibleLoops = possibleLoops;
        this.label = label;
    }

    @Override
    @SuppressWarnings({
            "squid:LabelsShouldNotBeUsedCheck",
            "squid:S135" // Loops should not contain more than a single "break" or "continue" statement
    })
    public Object execute(final InternalContext context) {
        final Statement[] stats = this.statements;
        final int myLabel = this.label;
        final int preIndex = context.indexer;
        context.indexer = indexer;
        label:
        while (ALU.isTrue(whileExpr.execute(context))) {
            StatementUtil.executeWithLoopCheck(stats, context);
            if (context.noLoop()) {
                continue;
            }
            if (!context.matchLabel(myLabel)) {
                break; //while
            }
            switch (context.getLoopType()) {
                case LoopInfo.BREAK:
                    context.resetLoop();
                    break label; // while
                case LoopInfo.RETURN:
                    //can't deal
                    break label; //while
                case LoopInfo.CONTINUE:
                    context.resetLoop();
                    break; //switch
                default:
                    break label; //while
            }
        }
        context.indexer = preIndex;
        return null;
    }

    @Override
    public List<LoopInfo> collectPossibleLoops() {
        return StatementUtil.asList(possibleLoops);
    }
}
