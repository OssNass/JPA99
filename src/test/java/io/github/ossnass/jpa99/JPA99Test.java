package io.github.ossnass.jpa99;

import com.github.javafaker.Faker;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class JPA99Test {

    private final List<Person> people = new ArrayList<>();
    private PersonRepository repo;
    private Faker faker;

    @BeforeAll
    public void preTest() {
        faker = new Faker(new Locale("en_US"), new Random());
        for (int i = 0; i < 8; i++) {
            people.add(new Person().setName(faker.name().fullName()));
        }
    }

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
        assertThat(UserManager.getUserManager().setPackageList(new String[]{getClass().getPackageName()}).setDatabaseAdapter(adapter)
                .setPersistenceUnitName("testPU")
                .setDatabaseURL("", 0, "testdb").logIn("", "")).isTrue();
        assertThat(UserManager.getUserManager().getEntityManagerFactory()).isNotNull();
        repo = (PersonRepository) UserManager.getUserManager().getRepository("Person");
    }


    @Test
    @Order(2)
    @DisplayName("Testing Create")
    public void CreateTest() {
        var res = repo.saveAndFlush(people.get(0));
        assertThat(res).isNotNull();
        assertThat(res.getId()).isNotNull();
        var persons = people.subList(1, people.size());
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
        var stream = repo.createStream();
        assertThat(stream.count()).isEqualTo(people.size());
        var testName = people.get(2).getName();
        assertThat(stream.where(person -> person.getName().equals(testName)).count()).isEqualTo(1);
    }

    @Test
    @Order(4)
    @DisplayName("Testing Update")
    public void UpdateTest() {
        var person = repo.findById(3);
        var people2 = repo.findAllById(List.of(1, 4));
        assertThat(person.isPresent()).isTrue();
        person.get().setName(faker.name().fullName());
        var res = repo.saveAndFlush(person.get());
        assertThat(res.getName()).isEqualTo(person.get().getName());
        for (var pple : people2)
            pple.setName(pple.getName() + "_exe");
        var resPPl = repo.saveAndFlushAll(people2);
        for (int i = 0; i < resPPl.size(); i++)
            assertThat(resPPl.get(i).getName()).isEqualTo(people2.get(i).getName());
    }

    @Test
    @Order(5)
    @DisplayName("Testing Delete")
    public void DeleteTest() {
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
