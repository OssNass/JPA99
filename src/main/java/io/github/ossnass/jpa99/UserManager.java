package io.github.ossnass.jpa99;

import io.github.classgraph.ClassGraph;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class UserManager {

    public static final String JPA_PROPERTY_DRIVER = "javax.persistence.jdbc.driver";
    public static final String JPA_PROPERTY_URL = "javax.persistence.jdbc.url";
    public static final String JPA_PROPERTY_USERNAME = "javax.persistence.jdbc.user";
    public static final String JPA_PROPERTY_PASSWORD = "javax.persistence.jdbc.password";

    private static UserManager um;
    /**
     * The properties of the URL connection
     */
    private final Properties props;
    /**
     * The roles of the user
     */
    private final Set<String> roles;
    private final Map<String, JPARepository> repositories;
    /**
     * The entity manager factory
     */
    private EntityManagerFactory emf;
    /**
     * The name of the persistence unit
     */
    private String puName;
    /**
     * The database Adapter
     */
    private DBAdapter dbImplAdapter;
    /**
     * The location of scan path for the database
     */
    private String[] packageList;
    private String url;
    private Exception lastException;

    private UserManager() {
        props = new Properties();
        roles = new HashSet<>();
        repositories = new HashMap<>();
    }

    public static UserManager getUserManager() {
        if (um == null)
            um = new UserManager();
        return um;
    }


    /**
     * Returns the list of locations to scan for annotated classes with {@link Repository} annotation
     *
     * @return the list of locations to scan for annotated classes with {@link Repository} annotation
     */
    public String[] getPackageList() {
        return packageList;
    }

    /**
     * Changes the list of locations to scan for annotated class with {@link Repository} annotation
     *
     * @param packageList the list of locations to scan for annotated class with {@link Repository} annotation
     * @return the current UserManager
     */
    public UserManager setPackageList(String[] packageList) {
        this.packageList = packageList;
        return this;
    }

    /**
     * Changes the used database type by changing the adapter.
     * <p>
     * When changing the type, the connection to the old database is closed using {@link UserManager#logOut()}
     *
     * @param adapter the new database adapter
     * @return the modified user manager
     * @throws IllegalArgumentException if the adapter is null
     */
    public UserManager setDatabaseAdapter(DBAdapter adapter) {
        if (adapter == null)
            throw new IllegalArgumentException("DB Adapter cannot be null");
        logOut();
        dbImplAdapter = adapter;
        return this;
    }


    /**
     * Changes the URL to the database.
     * <p>
     * If the user is already logged, will log out first
     *
     * @param host   the host of the database server
     * @param port   the port of the database server
     * @param dbName the name of the database
     * @return the modified user manager
     */
    public UserManager setDatabaseURL(String host, int port, String dbName) {
        logOut();
        url = dbImplAdapter.createURL(host, port, dbName);
        return this;
    }

    /**
     * Returns the currently used persistence unit
     *
     * @return the currently used persistence unit
     */
    public String getPersistenceUnitName() {
        return puName;
    }

    /**
     * Changes the used persistence unit.
     * <p>
     * The old connection is closed when changing the persistence unit name
     *
     * @param puName the new persistence unit
     * @return the user manager after being modified
     * @throws IllegalArgumentException if the puName is null or empty string
     */
    public UserManager setPersistenceUnitName(String puName) {
        if (puName == null)
            throw new IllegalArgumentException("The name of the persistence unit cannot be null");
        if (puName.trim().equals(""))
            throw new IllegalArgumentException("The name of the persistence unit cannot be empty string");
        logOut();
        this.puName = puName;
        return this;
    }

    /**
     * Logs the user out and closes the connection with the database.
     * <p>
     * Please be aware that logging out will clear all connection properties.
     * Therefore, any custom properties must be added again before establishing connection.
     */
    public void logOut() {
        if (emf != null && emf.isOpen()) {
            props.clear();
            emf.close();
            roles.clear();
            repositories.clear();
            emf = null;
        }
    }

    /**
     * Returns whether the user is logged in or not
     *
     * @return whether the user is logged in or not
     */
    public boolean isLoggedIn() {
        return emf != null && emf.isOpen();
    }

    /**
     * Returns the currently in use {@link EntityManagerFactory}
     *
     * @return the currently in use {@link EntityManagerFactory}
     */
    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    /**
     * Returns the last exception to happen when logging in
     *
     * @return the last exception to happen when logging in
     */
    public Exception getLastException() {
        return lastException;
    }

    /**
     * Logs a user into the database.
     * <p>
     * If the logging in a success, the roles of the user are acquired.
     * <p>
     * To preserve custome properties, an exception will be thrown if you attempt to log in while logged in
     *
     * @param username the username, can be null if needed
     * @param password password, can be null if needed
     * @return the value true if connected, false otherwise, see {@link UserManager#getLastException()} to understand why the connection failed.
     * @throws IllegalStateException in case you try to log in while already logged in
     */
    public boolean logIn(String username, String password) {
        if (isLoggedIn())
            throw new IllegalStateException("You need to be logout before logging in again");
        props.put(JPA_PROPERTY_URL, url);
        props.put(JPA_PROPERTY_DRIVER, dbImplAdapter.getDriver());
        props.setProperty(JPA_PROPERTY_USERNAME, username);
        props.setProperty(JPA_PROPERTY_PASSWORD, password);
        boolean res = false;
        try {
            emf = Persistence.createEntityManagerFactory(puName, props);
            EntityManager em = emf.createEntityManager();
            Query q = em.createNativeQuery(this.dbImplAdapter.getAcquireUserRoles());
            roles.clear();
            roles.addAll(q.getResultList());
            lastException = null;
            em.close();
            scanRepositories();
        } catch (Exception e) {
            lastException = e;
            return false;
        }
        return true;
    }

    /**
     * Returns the logged user's roles
     *
     * @return the logged user's roles
     */
    public Set<String> getRoles() {
        return roles;
    }

    private String[] getCallingMethod() {
        StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
        var caller = walker.getCallerClass();
        return new String[]{caller.getPackageName()};
    }

    private void scanRepositories() throws IllegalStateException {
        if (packageList == null)
            packageList = getCallingMethod();
        try (var res = new ClassGraph().enableAnnotationInfo().acceptPackages(packageList).scan()) {
            var cil = res.getClassesWithAnnotation(Repository.class.getCanonicalName());
            for (var cInfo : cil) {
                var dbCi = (Repository) cInfo.getAnnotationInfo(Repository.class.getCanonicalName()).loadClassAndInstantiate();
                if (repositories.containsKey(dbCi.value()))
                    throw new RuntimeException(String.format("Database controller %s already exists", dbCi.value()));
                var classz = cInfo.loadClass();
                repositories.put(dbCi.value(), (JPARepository) classz.getDeclaredConstructor().newInstance());
            }
        } catch (InstantiationException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns an open repository
     *
     * @param name the unique name of the repository
     * @return the repository
     */
    public JPARepository getRepository(String name) {
        return repositories.get(name);
    }
}
