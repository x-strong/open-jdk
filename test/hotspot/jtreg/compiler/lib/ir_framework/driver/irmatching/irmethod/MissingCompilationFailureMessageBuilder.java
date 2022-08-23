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

package compiler.lib.ir_framework.driver.irmatching.irmethod;

/**
 * Class to build the failure message of an IR method with a missing compilation output.
 *
 * @see IRMethodMatchResult
 */
class MissingCompilationFailureMessageBuilder extends FailureMessageBuilder {

    public MissingCompilationFailureMessageBuilder(IRMethod irMethod) {
        super(irMethod);
    }

    @Override
    protected String buildMethodHeaderLine() {
        return " Method \"" + irMethod.getMethod() + "\":" + System.lineSeparator();
    }

    @Override
    protected String buildIRRulesFailureMessage(int indentationSize) {
        return getIndentation(indentationSize) + "* Method was not compiled. Did you specify any compiler directives " +
               "preventing a compilation or used a @Run method in STANDALONE mode? In the latter case, make sure to always" +
               " trigger a C2 compilation by " + "invoking the test enough times.";
    }
}
