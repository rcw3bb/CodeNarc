/*
 * Copyright 2009 the original author or authors.
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
package org.codenarc.rule.design

import org.codenarc.rule.AbstractRuleTest
import org.codenarc.rule.Rule

/**
 * Tests for StatelessClassRule
 *
 * @author Chris Mair
 * @version $Revision: 69 $ - $Date: 2009-02-25 22:03:41 -0500 (Wed, 25 Feb 2009) $
 */
class StatelessClassRuleTest extends AbstractRuleTest {

    void testRuleProperties() {
        assert rule.priority == 2
        assert rule.name == 'StatelessClass'
    }

    void testNotReady() {
        final SOURCE = ' class MyClass { }'
        rule.applyToFilenames = null
        assert !rule.ready
        assertNoViolations(SOURCE)
    }

    void testApplyTo_HasFields() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
          }
        '''
        assertTwoViolations(SOURCE, 3, 'BigDecimal depositAmount', 4, 'def other')
    }

    void testApplyTo_IgnoreFieldNames_OneExactName() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
          }
        '''
        rule.ignoreFieldNames = 'other'
        assertSingleViolation(SOURCE, 3, 'BigDecimal depositAmount')
    }

    void testApplyTo_IgnoreFieldNames_TwoExactNames() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
          }
        '''
        rule.ignoreFieldNames = 'other,depositAmount'
        assertNoViolations(SOURCE)
    }

    void testApplyTo_IgnoreFieldNames_Wildcards() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
            int count
            long otherMax
          }
        '''
        rule.ignoreFieldNames = 'oth*,deposit??ount'
        assertSingleViolation(SOURCE, 5, 'int count')
    }

    void testApplyTo_IgnoreFieldTypes_OneExactName() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
          }
        '''
        rule.ignoreFieldTypes = 'BigDecimal'
        assertSingleViolation(SOURCE, 4, 'def other')
    }

    void testApplyTo_IgnoreFieldTypes_Wildcards() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
            int count = 23
            long otherMax
            Object lock = new Object()
          }
        '''
        rule.ignoreFieldTypes = '*Decimal,java.lang.Object,l?n?'
        assertTwoViolations(SOURCE, 5, 'int count = 23', 7, 'Object lock = new Object()')
    }

    void testApplyTo_IgnoreFieldNamesAndIgnoreFieldTypes() {
        final SOURCE = '''
          class MyClass {
            BigDecimal depositAmount
            def other
            int count
            long otherMax
          }
        '''
        rule.ignoreFieldNames = 'oth*,XXX'
        rule.ignoreFieldTypes = '*Decimal,YYY,int,l?n?'
        assertNoViolations(SOURCE)
    }


    void testApplyTo_Script_HasField() {
        final SOURCE = '''
            BigDecimal depositAmount        // not considered a field
            xxx = 23                        // not considered a field
            println 'ok'
        '''
        assertNoViolations(SOURCE)
    }

    void testApplyTo_NoFieldDefinition() {
        final SOURCE = ' class MyClass { } '
        assertNoViolations(SOURCE)
    }

    void setUp() {
        super.setUp()
        sourceCodeName = 'MyClass.groovy'
        sourceCodePath = "/$sourceCodeName"
    }

    protected Rule createRule() {
        return new StatelessClassRule(applyToFilenames:'*.groovy')
    }

}