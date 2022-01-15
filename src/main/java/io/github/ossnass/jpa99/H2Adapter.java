package io.github.ossnass.jpa99;

/**
 * H2 database adapter
 * <p>
 * The following extra properties are supported:
 * <ol>
 *     <li><strong>Mode:</strong><br>
 *      <ul>
 *          <li>standalone: for embedded database connection (default value)</li>
 *          <li>memory: for in-memory database</li>
 *          <li>tcp: for server based mode</li>
 *      </ul>
 *     </li>
 * </ol>
 */
public class H2Adapter extends DBAdapter {

    @Override
    public String createURL(String host, int port, String database) {
        String url = "";
        String mode = extraProperties.getOrDefault("Mode", "standalone");
        switch (mode.toLowerCase()) {
            case "standalone":
                url = "jdbc:h2:";
                break;
            case "memory":
                url = "jdbc:h2:mem:";
                break;
            case "server":
                if (port != 0)
                    host = host + ":" + port;
                url = "jdbc:h2:tcp://" + host + "/";
                break;
            default:
                throw new IllegalArgumentException("The only supported modes for H2 are: standalone, memory, and server");
        }
        url = url + database;
        return url;
    }

    @Override
    public String getAdapterName() {
        return "H2";
    }


    @Override
    public String[] availableExtraProperties() {
        return new String[]{"Mode"};
    }

    @Override
    public String getChangeUserPassword() {
        return "ALTER USER %s SET PASSWORD '%s';";
    }

    @Override
    public String getAcquireUserRoles() {
        return "SELECT 1;";
    }

    @Override
    public String getDriver() {
        return "org.h2.Driver";
    }
}
