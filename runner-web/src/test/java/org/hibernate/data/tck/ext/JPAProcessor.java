package org.hibernate.data.tck.ext;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import java.util.Map;

/**
 * Creates and adds a persistence.xml file to the deployment archive.
 */
public class JPAProcessor implements ApplicationArchiveProcessor {
    static final String PERSISTENCE_XML = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <persistence version="3.0" xmlns="https://jakarta.ee/xml/ns/persistence"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="https://jakarta.ee/xml/ns/persistence
                         https://jakarta.ee/xml/ns/persistence/persistence_3_0.xsd">
                        
                <persistence-unit name="jakarta-data-tck">
                    <description>Hibernate Entity Manager for Jakarta Data TCK</description>
                    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
                        
                    <properties>
                        <property name="jakarta.persistence.jdbc.url"
                                  value="jdbc:h2:./target/h2temp;DB_CLOSE_DELAY=-1" />
                        <property name="jakarta.persistence.jdbc.user" value="sa" />
                        <property name="jakarta.persistence.jdbc.password" value="" />
                        
                        <property name="jakarta.persistence.schema-generation.database.action"
                                  value="drop-and-create"/>
                        
                        <property name="hibernate.allow_update_outside_transaction" value="true"/>
                        <!--property name="hibernate.connection.autocommit" value="true"/-->
                        
                        <property name="hibernate.show_sql"   value="true" />
                        <property name="hibernate.format_sql" value="true" />
                        <property name="hibernate.highlight_sql" value="true" />
                        <property name="hibernate.jpa.compliance.strict" value="false" />
                        <property name="hibernate.jpa.compliance.query" value="false"/>
                    </properties>
                </persistence-unit>
            </persistence>
            """;

    @Override
    public void process(Archive<?> archive, TestClass testClass) {
        if(archive instanceof WebArchive) {
            WebArchive webArchive = (WebArchive) archive;
            webArchive.addAsWebInfResource(new StringAsset(PERSISTENCE_XML), "classes/META-INF/persistence.xml");
            webArchive.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
            for (Map.Entry<ArchivePath, Node> e : webArchive.getContent().entrySet()) {
                String path = e.getKey().get();
                if (path.endsWith(".class")) {
                    // Look for X_.class
                    String className = path.substring("/WEB-INF/classes/".length(), path.length() - ".class".length())
                            .replace('/', '.');
                    try {
                        webArchive.addClass(className + "_");
                        System.out.printf("Added %s_\n", className);
                    } catch (IllegalArgumentException ex) {
                        // Ignore
                    }
                }
            }
        }
    }
}
