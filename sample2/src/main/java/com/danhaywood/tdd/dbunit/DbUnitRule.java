package com.danhaywood.tdd.dbunit;

import de.payone.prototype.dbtools.JSONDataSet;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.dbunit.IDatabaseTester;
import org.dbunit.JdbcDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ITable;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import java.io.InputStream;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;

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
                    Ddl ddl = method.getAnnotation(Ddl.class);
                    if (ddl != null) {
                        String[] values = ddl.value();
                        for (String value : values) {
                            final ClassLoaderResourceAccessor resourceAccessor = new ClassLoaderResourceAccessor(DbUnitRule.class.getClassLoader());
                            InputStream is=resourceAccessor.toClassLoader().getResourceAsStream(value);
                            Enumeration <URL> e = resourceAccessor.toClassLoader().getResources("");
                            while (e.hasMoreElements())
                            {
                                System.out.println("ClassLoader Resource: " + e.nextElement());
                            }
                            System.out.println("Class Resource: " + DbUnitRule.class.getResource("/"));
                            System.out.println("Class Resource: " + resourceBase.getClassLoader().getResourceAsStream(value));
                            Liquibase lb=new Liquibase(value, resourceAccessor,new JdbcConnection(connection));
                            lb.update("");
//                            executeUpdate(Resources.toString(
//                                    resourceBase.getResource(value), Charset.defaultCharset()));
                        }
                    }

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
