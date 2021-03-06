/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.YangStmtMapping;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FractionDigitsStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RangeStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.TypeStatement.Decimal64Specification;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractDeclaredStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.AbstractStatementSupport;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.SubstatementValidator;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.type.Decimal64SpecificationEffectiveStatementImpl;

public class Decimal64SpecificationImpl extends AbstractDeclaredStatement<String> implements Decimal64Specification {
    private static final SubstatementValidator SUBSTATEMENT_VALIDATOR = SubstatementValidator.builder(YangStmtMapping
            .TYPE)
            .addMandatory(YangStmtMapping.FRACTION_DIGITS)
            .addOptional(YangStmtMapping.RANGE)
            .build();

    protected Decimal64SpecificationImpl(final StmtContext<String, Decimal64Specification, ?> context) {
        super(context);
    }

    public static class Definition extends
            AbstractStatementSupport<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> {

        public Definition() {
            super(YangStmtMapping.TYPE);
        }

        @Override
        public String parseArgumentValue(final StmtContext<?, ?, ?> ctx, final String value) {
            return value;
        }

        @Override
        public Decimal64Specification createDeclared(final StmtContext<String, Decimal64Specification, ?> ctx) {
            return new Decimal64SpecificationImpl(ctx);
        }

        @Override
        public EffectiveStatement<String, Decimal64Specification> createEffective(
                final StmtContext<String, Decimal64Specification, EffectiveStatement<String, Decimal64Specification>> ctx) {
            return new Decimal64SpecificationEffectiveStatementImpl(ctx);
        }

        @Override
        protected SubstatementValidator getSubstatementValidator() {
            return SUBSTATEMENT_VALIDATOR;
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return argument();
    }

    @Nonnull
    @Override
    public FractionDigitsStatement getFractionDigits() {
        return firstDeclared(FractionDigitsStatement.class);
    }

    @Override
    public RangeStatement getRange() {
        return firstDeclared(RangeStatement.class);
    }

}
