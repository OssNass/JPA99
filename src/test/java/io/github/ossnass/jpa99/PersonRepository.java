package io.github.ossnass.jpa99;

@Repository("Person")
public class PersonRepository extends JPARepository<Person, Integer> {
    @Override
    public Class<Person> entityClass() {
        return Person.class;
    }

    @Override
    public Class<Integer> idClass() {
        return Integer.class;
    }
}
