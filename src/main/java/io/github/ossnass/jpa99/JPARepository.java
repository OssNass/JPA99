package io.github.ossnass.jpa99;

import org.jinq.jpa.JPAJinqStream;
import org.jinq.jpa.JinqJPAStreamProvider;
import org.jinq.orm.stream.JinqStream;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * This class is used to manage entities in the database.
 * <p>
 * Any class extending this must be annotated with {@link Repository} annotation, else the constructor will throw an exception.
 * <p>
 * The repositories are acquired for use when by calling {@link UserManager#getRepository(String)}
 * <p>
 * To maintain minimal memory footprint, the repositories are instantiated using singleton design pattern.
 * <p>
 * It allows for easy CRUD operations, as well as complex ones using JINQ.
 * <p>
 * You can use {@link JPARepository#createStream()} to create a stream and then using
 * {@link JinqStream.Where} to create custom conditions adding them to the stream using {@link JPAJinqStream#where(JinqStream.Where)},
 * and {@link org.jinq.orm.stream.JinqStream.CollectComparable} to create custom sorters using {@link JPAJinqStream#sortedBy(JinqStream.CollectComparable)}
 * for ascending and {@link JPAJinqStream#sortedDescendingBy(JinqStream.CollectComparable)}
 *
 * @param <EntityClass> The type of entity class
 * @param <IdClass>     The type of id class
 */
public abstract class JPARepository<EntityClass, IdClass> {

    /**
     * The entity manager in case you need to use it.
     */
    protected final EntityManager em;

    private final JinqJPAStreamProvider provider;

    public JPARepository() {
        if (getClass().getAnnotation(Repository.class) == null)
            throw new RuntimeException("A JPA repository must be annotated by Repository annotation");
        em = UserManager.getUserManager().getEntityManagerFactory().createEntityManager();
        provider = new JinqJPAStreamProvider(UserManager.getUserManager().getEntityManagerFactory());

    }

    /**
     * Returns the entity class
     *
     * @return the entity class
     */
    public abstract Class<EntityClass> entityClass();

    /**
     * Returns the id class
     *
     * @return the id class
     */
    public abstract Class<IdClass> idClass();

    /**
     * Saves an entity in the database, used for add/edit
     *
     * @param entity the entity to save
     * @return the saved entity
     */
    public EntityClass saveAndFlush(EntityClass entity) {
        var transaction = em.getTransaction();
        transaction.begin();
        entity = em.merge(entity);
        transaction.commit();
        return entity;
    }

    /**
     * Creates a {@link JPAJinqStream} to be used the user for type safe query building
     *
     * @return the created jpa JINQ stream
     */
    public JPAJinqStream<EntityClass> createStream() {
        return provider.streamAll(em, entityClass());
    }

    /**
     * Similar to {@link JPARepository#saveAndFlush(Object)} but with multiple entities
     *
     * @param entities the list of entities to add/edit
     * @return the list of saved entities to add/edit
     */
    public List<EntityClass> saveAndFlushAll(List<EntityClass> entities) {
        var transaction = em.getTransaction();
        transaction.begin();
        for (int i=0;i<entities.size();i++) {
            entities.set(i,em.merge(entities.get(i)));
        }
        transaction.commit();
        return entities;
    }

    /**
     * Finds an entity in the database using its id
     *
     * @param id the id of the entity
     * @return the entity in database, empty Optional if not found.
     */
    public Optional<EntityClass> findById(IdClass id) {
        if (id == null)
            return Optional.empty();
        var entity = em.find(entityClass(), id);
        return Optional.ofNullable(entity);
    }

    /**
     * Finds a set of entities in the database using a list of ids
     *
     * @param ids the list of ids to find
     * @return a list of entities found in the database
     */
    public List<EntityClass> findAllById(List<IdClass> ids) {
        var res = new ArrayList<EntityClass>();
        for (var id : ids) {
            var entity = findById(id);
            entity.ifPresent(res::add);
        }
        return res;
    }

    /**
     * Deletes an entity from the database
     *
     * @param entity the entity to delete
     */
    public void delete(EntityClass entity) {
        var transaction = em.getTransaction();
        transaction.begin();
        em.remove(em.merge(entity));
        transaction.commit();
    }

    /**
     * Deletes a list of entities from the database
     *
     * @param entities the list of entities to delete
     */
    public void deleteAll(List<EntityClass> entities) {
        var transaction = em.getTransaction();
        transaction.begin();
        for (var entity : entities) {
            em.remove(em.merge(entity));
        }
        transaction.commit();
    }

    /**
     * Deletes an entity from the database using its id
     *
     * @param id the id of the entity to delete
     */
    public void deleteById(IdClass id) {
        var entity = findById(id);
        entity.ifPresent(this::delete);
    }

    /**
     * Deletes a list of entities from the database using its list of ids
     *
     * @param ids the list of ids to delete
     */
    public void deleteAllById(List<IdClass> ids) {
        var entities = findAllById(ids);
        deleteAll(entities);
    }

    /**
     * Refresh an entity from database
     *
     * @param entity the entity to refresh
     * @return the refreshed entity
     */
    public EntityClass refresh(EntityClass entity) {
        em.refresh(entity);
        return entity;
    }

    /**
     * Nukes the table and delete all the entities in it
     * <p>
     * Used in testing and might be removed in the final version
     */
    public void deleteEverything() {
        var transaction = em.getTransaction();
        transaction.begin();
        var query = em.createQuery("delete from " + entityClass().getName());
        query.executeUpdate();
        transaction.commit();
    }

    /**
     * Returns the current {@link EntityManager} in case you want to do something special
     * @return the current {@link EntityManager}
     */
    public EntityManager getEntityManager(){
        return em;
    }
}
