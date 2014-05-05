package com.danhaywood.tdd.dbunit;

import de.payone.prototype.dbtools.JSONDataSet;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.flywaydb.core.Flyway;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbUnitRule implements MethodRule {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public static @interface Ddl {
        String[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ ElementType.METHOD })
    public static @interface JsonData {
        String value();
    }

    private final Class<?> resourceBase;

    private IDatabaseTester databaseTester;
    private IDatabaseConnection dbUnitConnection;

    private Connection connection;
    private java.sql.Statement statement;

    public DbUnitRule(Class<?> resourceBase, Class<?> driver, String url, String user, String password) {
        this.resourceBase = resourceBase;
        try {
            Flyway flyway=new Flyway();
            flyway.setDataSource(url,user,password);
            flyway.setLocations("classpath:flyway");
            flyway.migrate();

            databaseTester = new JdbcDatabaseTester(driver.getName(), url, user, password);
            dbUnitConnection = databaseTester.getConnection();
            connection = dbUnitConnection.getConnection();
            statement = connection.createStatement();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Statement apply(final Statement base, final FrameworkMethod method, final Object target) {
        return new Statement() {

            @Override
            public void evaluate() throws Throwable {

                try {
                    JsonData data = method.getAnnotation(JsonData.class);
                    if (data != null) {
                        IDataSet ds = new JSONDataSet(resourceBase.getResourceAsStream(data.value()));
                        databaseTester.setDataSet(ds);
                    }

                    databaseTester.onSetup();
                    base.evaluate();
                } finally {
                    databaseTester.onTearDown();
                }
            }
        };
    }

    public java.sql.Connection getConnection() {
        return connection;
    }

    public void executeUpdate(String sql) throws SQLException {
        statement.executeUpdate(sql);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return statement.executeQuery(sql);
    }

    public IDataSet jsonDataSet(String datasetResource) {
        return new JSONDataSet(resourceBase.getResourceAsStream(datasetResource));
    }

    public ITable createQueryTable(String string, String string2) throws DataSetException, SQLException {
        return dbUnitConnection.createQueryTable(string, string2);
    }
}
