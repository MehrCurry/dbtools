package de.payone.prototype.dbtools;

import com.danhaywood.tdd.dbunit.DbUnitRule;
import com.danhaywood.tdd.dbunit.DbUnitRule.Ddl;
import com.danhaywood.tdd.dbunit.DbUnitRule.JsonData;
import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.hsqldb.jdbcDriver;
import org.junit.Rule;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.Statement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DbUnitRuleExample {

    @Rule
    public DbUnitRule dbUnit = new DbUnitRule(
            DbUnitRuleExample.class, jdbcDriver.class,
            "jdbc:hsqldb:mem:testdb", "SA", "");

    @Ddl("/customer.ddl")
    @JsonData("/customer.json")
    @Test
    public void update_lastName() throws Exception {
        // when
        Statement statement = dbUnit.getConnection().createStatement();
        statement.executeUpdate("update customer set last_name='Bloggs' where id=2");

        // then (verify directly)
        ResultSet rs2 = dbUnit.executeQuery("select last_name from customer where id = 2");
        assertThat(rs2.next(), is(true));
        assertThat(rs2.getString("last_name"), equalTo("Bloggs"));

        // then (verify using datasets)
        ITable actualTable = dbUnit.createQueryTable("customer", "select * from customer order by id");
        ITable expectedTable = dbUnit.jsonDataSet("/customer-updated.json").getTable("customer");

        Assertion.assertEquals(expectedTable, actualTable);
    }
}
