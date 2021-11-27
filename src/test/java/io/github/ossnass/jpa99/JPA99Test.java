package io.github.ossnass.jpa99;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JPA99Test {

    @Test
    @Order(1)
    @DisplayName("Testing Connection")
    public void setupConnection() {
        DBAdapter adapter = new H2Adapter();
        adapter.getExtraProperties().put("Mode", "memory");
        assertThat(UserManager.getUserManager().setDatabaseAdapter(adapter)
                .setPersistenceUnitName("testPU")
                .setDatabaseURL("", 0, "testdb").logIn("", "")).isTrue();
        assertThat(UserManager.getUserManager().getEntityManagerFactory()).isNotNull();
    }


    @Test
    @Order(2)
    @DisplayName("Testing Create")
    public void CreateTest() {
        Person p = new Person().setName("Ossama Nasser");
        PersonRepository repo = (PersonRepository) UserManager.getUserManager().getRepository("Person");
        var res = repo.saveAndFlush(p);
        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        var persons = new ArrayList<Person>();
        persons.add(new Person().setName("Anas Nasser"));
        persons.add(new Person().setName("Logain Nasser"));
        persons.add(new Person().setName("Shams Nasser"));
        var newPersons = repo.saveAndFlushAll(persons);
        assertThat(newPersons).isNotNull();
        for (var aperson : newPersons) {
            assertThat(aperson).isNotNull();
            assertThat(aperson.getId()).isNotNull();
        }
    }

    @Test
    @Order(3)
    @DisplayName("Testing Read")
    public void ReadTest() {
        PersonRepository repo = (PersonRepository) UserManager.getUserManager().getRepository("Person");
        var stream = repo.createStream();
        assertThat(stream.count()).isEqualTo(4);
        assertThat(stream.where(person -> person.getName().equals("Shams Nasser")).count()).isEqualTo(1);
    }

    @Test
    @Order(4)
    @DisplayName("Testing Update")
    public void UpdateTest() {
        PersonRepository repo = (PersonRepository) UserManager.getUserManager().getRepository("Person");
        var person = repo.findById(3);
        var people = repo.findAllById(List.of(1, 4));
        assertThat(person.isPresent()).isTrue();
        person.get().setName("Hanan Assad");
        var res = repo.saveAndFlush(person.get());
        assertThat(res.getName()).isEqualTo("Hanan Assad");
        for (var pple : people)
            pple.setName(pple.getName() + "_exe");
        var resPPl = repo.saveAndFlushAll(people);
        for (int i = 0; i < resPPl.size(); i++)
            assertThat(resPPl.get(i).getName()).isEqualTo(people.get(i).getName());
    }

    @Test
    @Order(5)
    @DisplayName("Testing Delete")
    public void DeleteTest() {
        PersonRepository repo = (PersonRepository) UserManager.getUserManager().getRepository("Person");
        repo.deleteById(1);
        assertThat(repo.findById(1).isEmpty()).isTrue();
        repo.deleteAllById(List.of(2, 4));
        assertThat(repo.findAllById(List.of(2, 4))).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Test logging out")
    public void LogoutTest() {
        UserManager.getUserManager().logOut();
        assertThat(UserManager.getUserManager().isLoggedIn()).isFalse();
    }
}
