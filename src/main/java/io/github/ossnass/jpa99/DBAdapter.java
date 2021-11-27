package io.github.ossnass.jpa99;

import java.util.HashMap;
import java.util.Map;

/**
 * This class provides an abstraction layer over the needed elements to establish a connection with a database.
 * <p>
 * These elements include
 * <ol>
 *     <li>the query to change the user's password</li>
 *     <li>the query to acquire the user's roles</li>
 *     <li>The FQDN of the driver class</li>
 * </ol>
 */
public abstract class DBAdapter {
    /**
     * The query to change the user's password
     */
    protected String changeUserPassword;
    /**
     * The query to acquire the user's roles
     */
    protected String acquireUserRoles;
    /**
     * The FQDN of the database driver class
     */
    protected String driver;
    /**
     * Extra set of properties specific to the database
     */
    protected Map<String, String> extraProperties = new HashMap<>();

    /**
     * This method combines the database URL elements into the correct form for the connection
     * <p>
     * If you need extra information to create the URL, you can use {@link DBAdapter#extraProperties} to provide more information
     *
     * @param host     the host of the database server
     * @param port     the port of the database server
     * @param database the name of the database
     * @return the database URL to be used by {@link UserManager}
     */
    public abstract String createURL(String host, int port, String database);

    /**
     * Returns the adapter name, this should be unique as it is used to differentiate between different RDBMS
     *
     * @return the adapter name
     */
    public abstract String getAdapterName();

    /**
     * Returns the names of the extra properties.
     * <p>
     * Theses are the keys used in {@link DBAdapter#extraProperties}, provide them here so the user can easily select and change the values
     *
     * @return a List of supported extra properties by the database
     */
    public abstract String[] availableExtraProperties();

    /**
     * Returns the query used to change the user's password
     *
     * @return the query used to change the user's password
     */
    public String getChangeUserPassword() {
        return changeUserPassword;
    }

    /**
     * Returns the query used to acquire user's roles
     *
     * @return the query used to acquire user's roles
     */
    public String getAcquireUserRoles() {
        return acquireUserRoles;
    }

    /**
     * Returns the FQDN of the database driver
     *
     * @return the FQDN of the database driver
     */
    public String getDriver() {
        return driver;
    }

    /**
     * Returns the current set of used extra properties
     *
     * @return the current set of used extra properties
     */
    public Map<String, String> getExtraProperties() {
        return extraProperties;
    }
}