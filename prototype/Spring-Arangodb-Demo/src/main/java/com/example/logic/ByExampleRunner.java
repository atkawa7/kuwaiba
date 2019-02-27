package com.example.logic;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;

import com.example.entity.Character;
import com.example.repository.CharacterRepository;

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
public class ByExampleRunner implements CommandLineRunner {

    @Autowired
    private CharacterRepository repository;

    /**
     * After we saved a character in the database. We can do a complex searcher
     * inside data base. as we notice, we not use AQL directly.
     *
     * @param args
     * @throws Exception
     */
    @Override
    public void run(final String... args) throws Exception {
        System.out.println("# Query by example");

        final Character nedStark = new Character("Ned", "Stark", false, 41);

        System.out.println(String.format("## Find character which match %s", nedStark));
        final Character foundNedStark = repository.findOne(Example.of(nedStark)).get();
        System.out.println(String.format("Found %s", foundNedStark));

        System.out.println("## Find all dead Starks");
        // because we only care of surname and alive in our entity we have to ignore the other
        // fields in our ExampleMatcher
        final Iterable<Character> allDeadStarks = repository
                .findAll(Example.of(new Character(null, "Stark", false), ExampleMatcher.matchingAll()
                        .withMatcher("surname", match -> match.exact()).withIgnorePaths("name", "age")));
        allDeadStarks.forEach(System.out::println);

        System.out.println("## Find all Starks which are 30 years younger than Ned Stark");
        // instead of changing the age for the Ned Stark entity use a transformer within the ExampleMatcher.
        // Because we are using the entity fetched from the db we have to ignore the field 'id' which isn't null.
        final Iterable<Character> allYoungerStarks = repository.findAll(Example.of(foundNedStark,
                ExampleMatcher.matchingAll().withMatcher("surname", match -> match.exact())
                        .withIgnorePaths("id", "name", "alive")
                        .withTransformer("age", age -> Optional.of(((int) age.get()) - 30))));
        allYoungerStarks.forEach(System.out::println);

        System.out.println("## Find all character which surname ends with 'ark' (ignore case)");
        final Iterable<Character> ark = repository.findAll(Example.of(new Character(null, "ark", false),
                ExampleMatcher.matchingAll().withMatcher("surname", match -> match.endsWith()).withIgnoreCase()
                        .withIgnorePaths("name", "alive", "age")));
        ark.forEach(System.out::println);
    }

}
