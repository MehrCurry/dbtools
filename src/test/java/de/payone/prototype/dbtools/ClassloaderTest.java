package de.payone.prototype.dbtools;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * Created with IntelliJ IDEA.
 * User: Guido Zockoll
 * Date: 26.04.13
 * Time: 14:40
 * To change this template use File | Settings | File Templates.
 */
public class ClassloaderTest {

    @Test
    public void testGetResourceAsStream() {
        InputStream stream=getClass().getClassLoader().getResourceAsStream("migrations/db.changelog-master.xml");
        assertNotNull(stream);

    }
}
