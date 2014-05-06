package de.payone.prototype.dbtools;

import com.danhaywood.tdd.dbunit.DbUnitRule;
import com.danhaywood.tdd.dbunit.DbUnitRule.JsonData;
import org.apache.commons.lang3.Validate;
import org.dbunit.Assertion;
import org.dbunit.dataset.ITable;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class DbUnitRuleExampleIT {

    @Rule
    public DbUnitRule dbUnit = createRule();

    private DbUnitRule createRule()  {
        Properties props=new Properties();
        try {
            final InputStream resourceAsStream = DbUnitRule.class.getResourceAsStream("/database.properties");
            Validate.notNull(resourceAsStream,"Ressouce not found");
            props.load(resourceAsStream);
            return new DbUnitRule(
                    DbUnitRuleExampleIT.class, Class.forName(props.getProperty("driver")),
                    props.getProperty("url"), props.getProperty("username"), props.getProperty("password"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

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
