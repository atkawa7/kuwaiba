
package com.example.logic;

import com.arangodb.springframework.core.ArangoOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;

import com.example.repository.CharacterRepository;
import com.example.entity.Character;
import java.util.Arrays;
import java.util.Collection;
import java.util.Spliterators;
import java.util.stream.StreamSupport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * To run our demo with Spring Boot we have to create a class implementing
 * CommandLineRunner. In this class we can use the @Authowired annotation to
 * inject our CharacterRepository – we created one step earlier – and also
 * ArangoOperations which offers a central support for interactions with the
 * database over a rich feature set. It mostly offers the features from the
 * ArangoDB Java driver with additional exception translation.
 *
 * To get the injection successfully running we have to add @ComponentScan to
 * our runner to define where Spring can find our configuration class
 * DemoConfiguration.
 *
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
@ComponentScan("com.example")
public class CrudRunner implements CommandLineRunner {

    @Autowired
    private ArangoOperations operations;
    @Autowired
    private CharacterRepository repository;

    /**
     * t’s time to save our first entity in the database. Both the database and
     * the collection don’t have to be created manually. This happens
     * automatically as soon as we execute a database request with the
     * components involved. We don’t have to leave the Java world to manage our
     * database.
     *
     * After we saved a character in the database the id in the original entity
     * is updated with the one generated from the database. We can then use this
     * id to find our persisted entity.
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(final String... args) throws Exception {
        // first drop the database so that we can run this multiple times with the same dataset
        operations.dropDatabase();

        // save a single entity in the database
        // there is no need of creating the collection first. This happen automatically
        final Character nedStark = new Character("Ned", "Stark", true, 41);
        repository.save(nedStark);
        // the generated id from the database is set in the original entity
        System.out.println(String.format("Ned Stark saved in the database with id: '%s'", nedStark.getId()));

        // lets take a look whether we can find Ned Stark in the database
        final Character foundNed = repository.findById(nedStark.getId()).orElse(null);;
        System.out.println(String.format("Found %s", foundNed));

        // as everyone probably knows Ned Stark died in the first season.
        // So we have to update his 'alive' flag
        nedStark.setAlive(false);
        repository.save(nedStark);
        final Character deadNed = repository.findById(nedStark.getId()).get();
        System.out.println(String.format("Ned Stark after 'alive' flag was updated: %s", deadNed));

        // lets save some additional characters
        final Collection<Character> createCharacters = createCharacters();
        System.out.println(String.format("Save %s additional characters", createCharacters.size()));
        repository.saveAll(createCharacters);

        final Iterable<Character> all = repository.findAll();
        final long count = StreamSupport.stream(Spliterators.spliteratorUnknownSize(all.iterator(), 0), false).count();
        System.out.println(String.format("A total of %s characters are persisted in the database", count));
        // count with ArangoOperations
        long countCharacters = operations.collection(Character.class).count();
        System.out.println(String.format("A total of %s characters using arango count are persisted in the database", countCharacters));

        System.out.println("## Return all characters sorted by name");
        final Iterable<Character> allSorted = repository.findAll(new Sort(Sort.Direction.ASC, "name"));
        allSorted.forEach(System.out::println);

        System.out.println("## Return the first 5 characters sorted by name");
        final Page<Character> first5Sorted = repository
                .findAll(PageRequest.of(0, 5, new Sort(Sort.Direction.ASC, "name")));
        first5Sorted.forEach(System.out::println);

    }

    public static Collection<Character> createCharacters() {
        return Arrays.asList(new Character("Ned", "Stark", false, 41), new Character("Robert", "Baratheon", false),
                new Character("Jaime", "Lannister", true, 36), new Character("Catelyn", "Stark", false, 40),
                new Character("Cersei", "Lannister", true, 36), new Character("Daenerys", "Targaryen", true, 16),
                new Character("Jorah", "Mormont", false), new Character("Petyr", "Baelish", false),
                new Character("Viserys", "Targaryen", false), new Character("Jon", "Snow", true, 16),
                new Character("Sansa", "Stark", true, 13), new Character("Arya", "Stark", true, 11),
                new Character("Robb", "Stark", false), new Character("Theon", "Greyjoy", true, 16),
                new Character("Bran", "Stark", true, 10), new Character("Joffrey", "Baratheon", false, 19),
                new Character("Sandor", "Clegane", true), new Character("Tyrion", "Lannister", true, 32),
                new Character("Khal", "Drogo", false), new Character("Tywin", "Lannister", false),
                new Character("Davos", "Seaworth", true, 49), new Character("Samwell", "Tarly", true, 17),
                new Character("Stannis", "Baratheon", false), new Character("Melisandre", null, true),
                new Character("Margaery", "Tyrell", false), new Character("Jeor", "Mormont", false),
                new Character("Bronn", null, true), new Character("Varys", null, true), new Character("Shae", null, false),
                new Character("Talisa", "Maegyr", false), new Character("Gendry", null, false),
                new Character("Ygritte", null, false), new Character("Tormund", "Giantsbane", true),
                new Character("Gilly", null, true), new Character("Brienne", "Tarth", true, 32),
                new Character("Ramsay", "Bolton", true), new Character("Ellaria", "Sand", true),
                new Character("Daario", "Naharis", true), new Character("Missandei", null, true),
                new Character("Tommen", "Baratheon", true), new Character("Jaqen", "H'ghar", true),
                new Character("Roose", "Bolton", true), new Character("The High Sparrow", null, true));
    }
}
