/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator.MAX;

import java.util.Collection;
import org.opendaylight.yangtools.yang.model.api.Rfc6020Mapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LengthStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.PatternStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement;
import org.opendaylight.yangtools.yang.parser.spi.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.StringRestrictionsEffectiveStatementImpl;

public class StringRestrictionsImpl extends AbstractDeclaredStatement<String> implements
        TypeStatement.StringRestrictions {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(Rfc6020Mapping
            .TYPE)
            .add(Rfc6020Mapping.LENGTH, 0, 1)
            .add(Rfc6020Mapping.PATTERN, 0, MAX)
            .build();

    protected StringRestrictionsImpl(StmtContext<String, TypeStatement.StringRestrictions, ?> context) {
        super(context);
    }

    public static class Definition
            extends
            AbstractStatementSupport<String, TypeStatement.StringRestrictions, EffectiveStatement<String, TypeStatement.StringRestrictions>> {

        public Definition() {
            super(Rfc6020Mapping.TYPE);
        }

        @Override
        public String parseArgumentValue(StmtContext<?, ?, ?> ctx, String value) {
            return value;
        }

        @Override
        public TypeStatement.StringRestrictions createDeclared(
                StmtContext<String, TypeStatement.StringRestrictions, ?> ctx) {
            return new StringRestrictionsImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, TypeStatement.StringRestrictions> createEffective(
                StmtContext<String, TypeStatement.StringRestrictions, EffectiveStatement<String, TypeStatement.StringRestrictions>> ctx) {
            return new StringRestrictionsEffectiveStatementImpl(ctx);
        }

        @Override
        public void onFullDefinitionDeclared(StmtContext.Mutable<String, StringRestrictions,
                EffectiveStatement<String, StringRestrictions>> stmt) {
            super.onFullDefinitionDeclared(stmt);
            SUBSTATEMENT_VALIDATOR.validate(stmt);
        }
    }

    @Override
    public String getName() {
        return argument();
    }

    @Override
    public LengthStatement getLength() {
        return firstDeclared(LengthStatement.class);
    }

    @Override
    public Collection<? extends PatternStatement> getPatterns() {
        return allDeclared(PatternStatement.class);
    }

}
