/*
 * Copyright (c) 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package compiler.lib.ir_framework.driver.irmatching.irrule.constraint;

import compiler.lib.ir_framework.driver.irmatching.FailureMessage;
import compiler.lib.ir_framework.driver.irmatching.MatchResult;
import compiler.lib.ir_framework.driver.irmatching.MatchResultVisitor;

import java.util.List;

/**
 * Base class representing a result of an applied check attribute of an IR rule on a compile phase.
 *
 * @see CheckAttribute
 */
abstract public class CheckAttributeMatchResult implements MatchResult, FailureMessage {
    private List<ConstraintFailure> constraintFailures = null;
    private final CheckAttributeKind checkAttributeKind;

    CheckAttributeMatchResult(CheckAttributeKind checkAttributeKind) {
        this.checkAttributeKind = checkAttributeKind;
    }

    public CheckAttributeKind getCheckAttributeKind() {
        return checkAttributeKind;
    }

    @Override
    public boolean fail() {
        return constraintFailures != null;
    }

    public void setFailures(List<ConstraintFailure> constraintFailures) {
        this.constraintFailures = constraintFailures;
    }

    @Override
    public String buildFailureMessage(int indentationSize) {
        return getIndentation(indentationSize) + "- " + getCheckAttributeMessage() + ":" + System.lineSeparator()
               + buildConstraintFailuresMessage(indentationSize);
    }

    abstract protected String getCheckAttributeMessage();

    private String buildConstraintFailuresMessage(int indentation) {
        StringBuilder failMsg = new StringBuilder();
        for (ConstraintFailure constraintFailure : constraintFailures) {
            failMsg.append(constraintFailure.buildFailureMessage(indentation + 2));
        }
        return failMsg.toString();
    }

    public void accept(MatchResultVisitor visitor) {
        visitor.visit(this);
        constraintFailures.forEach(f -> f.accept(visitor));
    }
}
