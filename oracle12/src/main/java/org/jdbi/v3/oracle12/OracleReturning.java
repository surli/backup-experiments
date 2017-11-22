/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.oracle12;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.jdbi.v3.core.result.ResultBearing;
import org.jdbi.v3.core.result.ResultProducer;
import org.jdbi.v3.core.result.ResultSetException;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.StatementCustomizer;

import oracle.jdbc.OraclePreparedStatement;

/**
 * Returns a {@link ResultBearing} from Oracle's "DML Returning" features introduced in 10.2. To use,
 * add a {@link #returnParameters()} customizer to the statement and register with one or more return parameters. Then
 * execute the statement with {@link #returningDml()} result producer:
 * <p>
 * <pre>
 * List&lt;Integer&gt; ids = handle.createUpdate("insert into something (id, name) values (17, 'Brian') returning id into ?")
 *     .addCustomizer(OracleReturning.returnParameters().register(1, OracleTypes.INTEGER))
 *     .execute(OracleReturning.returningDml())
 *     .mapTo(int.class)
 *     .list();
 *
 * assertThat(ids).containsExactly(17);
 * </pre>
 * <p>
 * This class still is beta, and may be changed incompatibly or removed at any time.
 */
public class OracleReturning {
    public static ReturnParameters returnParameters() {
        return new ReturnParameters();
    }

    public static class ReturnParameters implements StatementCustomizer {
        private final List<int[]> binds = new ArrayList<>();

        ReturnParameters() {
        }

        @Override
        public void beforeExecution(PreparedStatement stmt, StatementContext ctx) throws SQLException {
            if (!stmt.isWrapperFor(OraclePreparedStatement.class)) {
                throw new IllegalStateException("Statement is not an instance of, nor a wrapper of, OraclePreparedStatement");
            }
            OraclePreparedStatement statement = stmt.unwrap(OraclePreparedStatement.class);
            for (int[] bind : binds) {
                try {
                    statement.registerReturnParameter(bind[0], bind[1]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        /**
         * Registers a return parameter on the Oracle prepared statement.
         *
         * @param index      0-based index of the return parameter
         * @param oracleType one of the values from {@link oracle.jdbc.OracleTypes}
         * @return The same instance, for method chaning
         */
        public ReturnParameters register(int index, int oracleType) {
            binds.add(new int[]{index+1, oracleType});
            return this;
        }
    }

    /**
     * Result producer that returns a {@link ResultBearing} over the statement "DML returning" parameters. Used in
     * conjunction with {@link #returnParameters()} to register return parameters.
     *
     * @return ResultBearing of returned columns.
     * @see OraclePreparedStatement#getReturnResultSet()
     */
    public static ResultProducer<ResultBearing> returningDml() {
        return (supplier, ctx) -> ResultBearing.of(getReturnResultSet(supplier, ctx), ctx);
    }

    private static Supplier<ResultSet> getReturnResultSet(Supplier<PreparedStatement> supplier, StatementContext ctx) {
        return () -> {
            PreparedStatement stmt = supplier.get();
            try {
                if (!stmt.isWrapperFor(OraclePreparedStatement.class)) {
                    throw new IllegalStateException("Statement is not an instance of, nor a wrapper of, OraclePreparedStatement");
                }

                OraclePreparedStatement statement = stmt.unwrap(OraclePreparedStatement.class);

                ResultSet rs = statement.getReturnResultSet();
                if (rs != null) {
                    ctx.addCleanable(rs::close);
                }

                return rs;
            } catch (SQLException e) {
                throw new ResultSetException("Unable to retrieve return result set", e, ctx);
            }
        };
    }
}
