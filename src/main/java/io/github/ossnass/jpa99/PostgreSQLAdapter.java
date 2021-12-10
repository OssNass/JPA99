package io.github.ossnass.jpa99;

/**
 * This class represent an adapter to PostgreSQL database management system.
 * 
 * Currently no extra properties are supported
 */
public class PostgreSQLAdapter extends DBAdapter {

    public PostgreSQLAdapter() {
        changeUserPassword = "ALTER USER %s PASSWORD '%s';";
        acquireUserRoles = "WITH RECURSIVE cte AS (SELECT oid FROM pg_roles WHERE rolname = current_user " +
                "UNION ALL SELECT m.roleid FROM cte JOIN pg_auth_members m ON m.member = cte.oid) SELECT oid::regrole::text AS rolename FROM cte;";
        driver = "org.postgresql.Driver";
    }

    @Override
    public String createURL(String host, int port, String database) {
        if (host == null)
            throw new IllegalArgumentException("The Host of database cannot be null");
        if (host.trim().equals(""))
            throw new IllegalArgumentException("The Host of database cannot be empty srting");
        if (database == null)
            throw new IllegalArgumentException("The name of database cannot be null");
        if (database.trim().equals(""))
            throw new IllegalArgumentException("The name of database cannot be empty srting");
        if (port == 0)
            throw new IllegalArgumentException("The port of database cannot be zero");
        return String.format("jdbc:postgresql://%s:%d/%s", host, port, database);
    }

    @Override
    public String getAdapterName() {
        return "PostgreSQL";
    }

    @Override
    public String[] availableExtraProperties() {
        return new String[0];
    }
}
