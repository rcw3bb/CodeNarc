/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codenarc.rule.unnecessary

import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.PackageNode
import org.codehaus.groovy.ast.stmt.Statement
import org.codenarc.rule.AbstractAstVisitor
import org.codenarc.rule.AbstractAstVisitorRule
import org.codenarc.rule.Violation
import org.codenarc.source.SourceCode
import org.codenarc.util.AstUtil
import org.codenarc.util.ImportUtil

/**
 * Semicolons as line terminators are not required in Groovy: remove them. Do not use a semicolon as a replacement for empty braces on for and while loops; this is a confusing practice.
 *
 * @author Hamlet D'Arcy
 * @author Chris Mair
 */
class UnnecessarySemicolonRule extends AbstractAstVisitorRule {

    protected static final String MESSAGE = 'Semicolons as line endings can be removed safely'

    String name = 'UnnecessarySemicolon'
    int priority = 3
    Class astVisitorClass = UnnecessarySemicolonAstVisitor

    @Override
    void applyTo(SourceCode sourceCode, List<Violation> violations) {
        super.applyTo(sourceCode, violations)
        processPackage(sourceCode, violations)
        processImports(sourceCode, violations)
    }

    private void processPackage(SourceCode sourceCode, List<Violation> violations) {
        PackageNode packageNode = sourceCode.ast?.package
        if (packageNode) {
            checkLastLineForSemicolon(sourceCode, violations, packageNode)
        }
    }

    private void processImports(SourceCode sourceCode, List<Violation> violations) {
        ImportUtil.getAllImports(sourceCode).each { importNode ->
            processImportNode(sourceCode, violations, importNode)
        }
    }

    private void processImportNode(SourceCode sourceCode, List<Violation> violations, ImportNode node) {
        checkLastLineForSemicolon(sourceCode, violations, node)
    }

    private void checkLastLineForSemicolon(SourceCode sourceCode, List<Violation> violations, AnnotatedNode node) {
        int lineNumber = node.getLastLineNumber()
        String line = sourceCode.getLines().get(lineNumber - 1) + ' '   // to make it easier to extract the final chars
        int lastColumn = node.lastColumnNumber
        boolean lastCharIsSemicolon = line[lastColumn - 1] == ';'

        if (lastCharIsSemicolon) {
            violations << createViolation(lineNumber, line, MESSAGE)
        }
    }

}

class UnnecessarySemicolonAstVisitor extends AbstractAstVisitor {

    @Override
    protected void visitStatement(Statement statement) {
        if (AstUtil.isFromGeneratedSourceCode(statement)) {
            return
        }

        int lastColumn = statement.lastColumnNumber
        String line = lastSourceLine(statement) + ' '   // to make it easier to extract the final chars

        // Some statements (e.g. "for") have lastColumnNumber in different relative positions
        boolean lastCharIsSemicolon = line[lastColumn - 2] == ';' || line[lastColumn - 1] == ';'

        def lineNumber = statement.lastLineNumber

        // An earlier violation for this same line means its semicolon was actually okay
        removeAnyViolationsForSameLine(lineNumber)

        if (lastCharIsSemicolon) {
            addViolation(new Violation(rule: rule, lineNumber: lineNumber, sourceLine: line, message: rule.MESSAGE))
        }

        super.visitStatement(statement)
    }

    private void removeAnyViolationsForSameLine(int lineNumber) {
        Violation violation = violations.find { v -> v.lineNumber == lineNumber }
        if (violation) {
            violations.remove(violation)
        }
    }

}
