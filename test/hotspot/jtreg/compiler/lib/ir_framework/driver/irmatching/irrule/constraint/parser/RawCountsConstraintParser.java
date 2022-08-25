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

package compiler.lib.ir_framework.driver.irmatching.irrule.constraint.parser;

import compiler.lib.ir_framework.CompilePhase;
import compiler.lib.ir_framework.IRNode;
import compiler.lib.ir_framework.TestFramework;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.Counts;
import compiler.lib.ir_framework.driver.irmatching.irrule.constraint.CountsConstraint;
import compiler.lib.ir_framework.shared.Comparison;
import compiler.lib.ir_framework.shared.ComparisonConstraintParser;
import compiler.lib.ir_framework.shared.TestFormatException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class parses raw counts constraints. The following steps are done:
 * <ul>
 *     <li><p>Replaces the placeholder strings from {@link IRNode} by actual default regexes. If there is no default regex
 *     provided for a compile phase, a format violation is reported. </li>
 *     <li><p>Creates a comparison object based on the count string which is used later to check if the IR contains the
 *     right number of nodes.</li>
 * </ul>
 *
 * This parser returns a ready to be used {@link Counts} constraint object to apply IR regex matching on.
 *
 * @see RawCountsConstraint
 * @see Counts
 */
public class RawCountsConstraintParser extends RawConstraintParser<RawCountsConstraint> {
    private final List<CountsConstraint> constraintResultList = new ArrayList<>();

    private RawCountsConstraintParser() {}

    public List<CountsConstraint> getConstraints() {
        TestFramework.check(!constraintResultList.isEmpty(), "must be non-empty");
        return constraintResultList;
    }

    /**
     * Returns a new {@link Counts} object by parsing the provided {@code rawCountsConstraints} list or null if this
     * list is empty.
     */
    public static List<CountsConstraint> parse(List<RawCountsConstraint> rawCountsConstraints, CompilePhase compilePhase) {
        if (!rawCountsConstraints.isEmpty()) {
            RawCountsConstraintParser constraintParser = new RawCountsConstraintParser();
            constraintParser.parseNonEmptyConstraints(rawCountsConstraints, compilePhase);
            return constraintParser.getConstraints();
        }
        return null;
    }

    @Override
    protected void parseRawConstraint(RawCountsConstraint rawConstraint, CompilePhase compilePhase) {
        ParsedIRNodeInfo parsedIRNodeInfo = parseIRNodeInfo(rawConstraint, compilePhase);
        Comparison<Integer> comparison = parseComparison(rawConstraint);
        constraintResultList.add(new CountsConstraint(parsedIRNodeInfo.regex(), rawConstraint.getConstraintIndex(),
                                                      parsedIRNodeInfo.compilePhase(), comparison));
    }

    private static Comparison<Integer> parseComparison(RawCountsConstraint rawConstraint) {
        try {
            return ComparisonConstraintParser.parse(rawConstraint.getCountString(),
                                                    RawCountsConstraintParser::parsePositiveInt);
        } catch (TestFormatException e) {
            throw new TestFormatException(e.getMessage() + ", node \"" + rawConstraint.getRawNodeString()
                                          + "\", in count string");
        }
    }

    public static int parsePositiveInt(String s) {
        int result = Integer.parseInt(s);
        if (result < 0) {
            throw new NumberFormatException("cannot be negative");
        }
        return result;
    }
}
