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

package compiler.lib.ir_framework.driver.irmatching.reporting;

import compiler.lib.ir_framework.driver.irmatching.IRMatcher;
import compiler.lib.ir_framework.driver.irmatching.irrule.checkattribute.CheckAttribute;
import compiler.lib.ir_framework.driver.irmatching.irrule.checkattribute.CheckAttributeMatchResult;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.Constraint;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.ConstraintFailure;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class representing a failure when applying a constraint (i.e. regex matching) on a compile phase output.
 *
 * @see Constraint
 * @see CheckAttribute
 * @see CheckAttributeMatchResult
 */
abstract public class ConstraintFailureMessageBuilder implements FailureMessage {
    protected final int indentation;

    public ConstraintFailureMessageBuilder(int indentation) {
        this.indentation = indentation;
    }

    protected String buildConstraintHeader(ConstraintFailure constraintFailure) {
        return getIndentation(indentation) + "* Constraint "
               + constraintFailure.getConstraintIndex() + ": \"" + constraintFailure.getNodeRegex() + "\""
               + System.lineSeparator();
    }

    protected String buildMatchedNodesMessage(ConstraintFailure constraintFailure) {
        return buildMatchedNodesHeader(constraintFailure) + buildMatchedNodesBody(constraintFailure);
    }

    private String buildMatchedNodesHeader(ConstraintFailure constraintFailure) {
        int matchCount = constraintFailure.getMatchedNodes().size();
        return getIndentation(indentation + 2) + "- " + getMatchedPrefix(constraintFailure)
               + " node" + (matchCount > 1 ? "s (" + matchCount + ")" : "") + ":" + System.lineSeparator();
    }

    abstract protected String getMatchedPrefix(ConstraintFailure constraintFailure);

    /**
     * Builds a failure message in a pretty format to be used by {@link IRMatcher} to report IR matching failures.
     */
    abstract public String build();

    private String buildMatchedNodesBody(ConstraintFailure constraintFailure) {
        StringBuilder builder = new StringBuilder();
        List<String> matches = addWhiteSpacePrefixForEachLine(constraintFailure.getMatchedNodes());
        matches.forEach(match -> builder.append(getIndentation(indentation + 4))
                                        .append("* ").append(match).append(System.lineSeparator()));
        return builder.toString();
    }


    private List<String> addWhiteSpacePrefixForEachLine(List<String> matches) {
        String indentationString = getIndentation(indentation + 6);
        return matches.stream()
                      .map(s -> s.replaceAll(System.lineSeparator(), System.lineSeparator() + indentationString))
                      .collect(Collectors.toList());
    }
}
