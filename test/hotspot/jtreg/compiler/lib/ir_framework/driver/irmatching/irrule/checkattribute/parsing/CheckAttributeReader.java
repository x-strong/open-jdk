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

package compiler.lib.ir_framework.driver.irmatching.irrule.checkattribute.parsing;

import compiler.lib.ir_framework.IR;
import compiler.lib.ir_framework.IRNode;
import compiler.lib.ir_framework.driver.irmatching.irrule.checkattribute.parsing.action.ConstraintAction;
import compiler.lib.ir_framework.shared.TestFormat;

import java.util.*;

/**
 * This class represents an iterator on check attribute strings found in ({@link IR#failOn()} or {@link IR#counts()}).
 * The iterator returns a {@link CheckAttributeString} that wraps the check attribute string. If there are no elements
 * left, a special invalid {@link CheckAttributeString} is returned.
 *
 * @see IR#failOn()
 * @see IR#counts()
 * @see CheckAttributeString
 */
public class CheckAttributeReader<E> {
    private final ListIterator<String> iterator;
    private final ConstraintAction<E> constraintAction;

    public CheckAttributeReader(String[] checkAttributeStrings, ConstraintAction<E> constraintAction) {
        this.iterator = Arrays.stream(checkAttributeStrings).toList().listIterator();
        this.constraintAction = constraintAction;
    }

    public void read(Collection<E> result) {
        int index = 1;
        while (iterator.hasNext()) {
            String node = iterator.next();
            CheckAttributeString userPostfix = readUserPostfix(node);
            RawIRNode rawIRNode = new RawIRNode(node, userPostfix);
            result.add(constraintAction.apply(iterator, rawIRNode, index++));
        }
    }

    public final CheckAttributeString readUserPostfix(String node) {
        if (IRNode.isCompositeIRNode(node)) {
            String irNode = IRNode.getIRNodeAccessString(node);
            int nextIndex = iterator.nextIndex();
            TestFormat.checkNoReport(iterator.hasNext(), "Must provide additional value at index " +
                                                         nextIndex + " right after " + irNode);
            CheckAttributeString userPostfix = new CheckAttributeString(iterator.next());
            TestFormat.checkNoReport(userPostfix.isValidUserPostfix(), "Provided empty string for composite node " +
                                                                       irNode + " at index " + nextIndex);
            return userPostfix;
        } else {
            return CheckAttributeString.invalid();
        }
    }
}
