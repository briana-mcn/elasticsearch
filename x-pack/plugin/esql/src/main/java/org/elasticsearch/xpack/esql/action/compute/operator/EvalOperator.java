/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.action.compute.operator;

import org.elasticsearch.xpack.esql.action.compute.data.DoubleBlock;
import org.elasticsearch.xpack.esql.action.compute.data.LongBlock;
import org.elasticsearch.xpack.esql.action.compute.data.Page;

public class EvalOperator implements Operator {

    private final ExpressionEvaluator evaluator;
    private final Class<? extends Number> dataType;

    boolean finished;

    Page lastInput;

    public EvalOperator(ExpressionEvaluator evaluator, Class<? extends Number> dataType) {
        this.evaluator = evaluator;
        this.dataType = dataType;
    }

    @Override
    public Page getOutput() {
        if (lastInput == null) {
            return null;
        }
        Page lastPage;
        if (dataType.equals(Long.TYPE)) {
            long[] newBlock = new long[lastInput.getPositionCount()];
            for (int i = 0; i < lastInput.getPositionCount(); i++) {
                newBlock[i] = ((Number) evaluator.computeRow(lastInput, i)).longValue();
            }
            lastPage = lastInput.appendColumn(new LongBlock(newBlock, lastInput.getPositionCount()));
        } else if (dataType.equals(Double.TYPE)) {
            double[] newBlock = new double[lastInput.getPositionCount()];
            for (int i = 0; i < lastInput.getPositionCount(); i++) {
                newBlock[i] = ((Number) evaluator.computeRow(lastInput, i)).doubleValue();
            }
            lastPage = lastInput.appendColumn(new DoubleBlock(newBlock, lastInput.getPositionCount()));
        } else {
            throw new UnsupportedOperationException();
        }
        lastInput = null;
        return lastPage;
    }

    @Override
    public boolean isFinished() {
        return lastInput == null && finished;
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public boolean needsInput() {
        return lastInput == null && finished == false;
    }

    @Override
    public void addInput(Page page) {
        lastInput = page;
    }

    @Override
    public void close() {

    }

    public interface ExpressionEvaluator {
        Object computeRow(Page page, int position);
    }
}
