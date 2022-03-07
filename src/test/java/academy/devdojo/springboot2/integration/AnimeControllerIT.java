package academy.devdojo.springboot2.integration;

import academy.devdojo.springboot2.domain.Anime;
import academy.devdojo.springboot2.domain.DevDojoUser;
import academy.devdojo.springboot2.repository.AnimeRepository;
import academy.devdojo.springboot2.repository.DevDojoUserRepository;
import academy.devdojo.springboot2.requests.AnimePostRequestBody;
import academy.devdojo.springboot2.util.AnimeCreator;
import academy.devdojo.springboot2.util.AnimePostRequestBodyCreator;
import academy.devdojo.springboot2.wrapper.PageableResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AnimeControllerIT {

    @Autowired
    @Qualifier(value = "testRestTemplateRoleUser")
    private TestRestTemplate testRestTemplateUser;

    @Autowired
    @Qualifier(value = "testRestTemplateRoleAdmin")
    private TestRestTemplate testRestTemplateAdmin;

    @Autowired
    private AnimeRepository animeRepository;
    @Autowired
    private DevDojoUserRepository devDojoUserRepository;
    Anime savedAnime =  animeRepository.save(AnimeCreator.createAnimeToBeSaved());
   private static final DevDojoUser USER = DevDojoUser.builder()
            .name("Caio")
            .password("{bcrypt}$")
            .username("@Caio")
            .authorities("ROLE_ADMIN")
            .build();
    private static final DevDojoUser ADMIN =  DevDojoUser.builder()
            .name("Mouta")
            .password("{bcrypt}$")
            .username("@Mouta")
            .authorities("ROLE_USER,ROLE_ADMIN")
            .build();


    @TestConfiguration
    @Lazy
    static class Config {

        @Bean (name = "testRestTemplateRoleUser")
        public TestRestTemplate testRestTemplateRoleUserCreator(@Value("${local.server.port}") int port){
             RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                     .rootUri("http://localhost:"+port)
                     .basicAuthentication("devdojo","academy");
             return new TestRestTemplate(restTemplateBuilder);
        }

        @Bean (name = "testRestTemplateRoleAdmin")
        public TestRestTemplate testRestTemplateRoleAdminCreator(@Value("${local.server.port}") int port){
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                    .rootUri("http://localhost:"+port)
                    .basicAuthentication("devdojo","academy");
            return new TestRestTemplate(restTemplateBuilder);
        }
    }


    @Test
    @DisplayName("list returns list of anime inside page object when successful")
    void listAll_ReturnasListOfAnimesInsidePageObject_WhenSucessful() {
        devDojoUserRepository.save(USER);
        Anime savedAnime =  animeRepository.save(AnimeCreator.createAnimeToBeSaved());


        String expectedName = savedAnime.getName();

        PageableResponse<Anime> animePage = testRestTemplateUser.exchange(" /animes", HttpMethod.GET, null,
                new ParameterizedTypeReference<PageableResponse<Anime>>() {
                }).getBody();

        Assertions.assertThat(animePage).isNotNull();

        Assertions.assertThat(animePage.toList()).isNotEmpty().hasSize(1);

        Assertions.assertThat(animePage.toList().get(0).getName()).isEqualTo(expectedName);


    }

    @Test
    @DisplayName("FindById returns anime when successful")
    void findById_ReturnsAnimes_WhenSucessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);
        Long expectedId = savedAnime.getId();

        Anime anime = testRestTemplateUser.getForObject("/animes/{id}", Anime.class,expectedId);


        Assertions.assertThat(anime)
                .isNotNull();

        Assertions.assertThat(anime.getId()).isNotNull().isEqualTo(expectedId);


    }
//
    @Test
    @DisplayName("FindByName returns anime when successful")
    void findByName_ReturnasListOfAnimes_WhenSucessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = savedAnime.getName();
        String url = String.format("/animes/find?name=%s",expectedName);

        List<Anime> animes = testRestTemplateUser.exchange("/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);



        Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);


    }


    @Test
    @DisplayName("ListAll returns list of anime when successful")
    void listAll_ReturnasListOfAnimes_WhenSucessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);

        String expectedName = savedAnime.getName();

        List<Anime> animes = testRestTemplateUser.exchange("/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);



        Assertions.assertThat(animes.get(0).getName()).isEqualTo(expectedName);


    }

    @Test
    @DisplayName("FindByName returns an empty list of anime when anime is not found")
    void findByName_ReturnsEmptyListOfAnime_WhenAnimeIsNotFound() {
        devDojoUserRepository.save(USER);
        List<Anime> animes = testRestTemplateUser.exchange("/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animes)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

    }
//
    @Test
    @DisplayName("save returns anime when successful")
    void save_ReturnsAnime_WhenSucessful() {
        AnimePostRequestBody animePostRequestBody = AnimePostRequestBodyCreator.createAnimePostRequestBody();

        ResponseEntity<Anime> animeResponseEntity = testRestTemplateUser.postForEntity("/animes",animePostRequestBody, Anime.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(animeResponseEntity.getBody()).isNotNull();
        Assertions.assertThat(animeResponseEntity.getBody().getId()).isNotNull();
    }
//
    @Test
    @DisplayName("replace returns anime when successful")
    void replace_UpdatesAnime_WhenSucessful() {

        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);
        savedAnime.setName("new name");

        ResponseEntity<Void> animeResponseEntity = testRestTemplateUser.exchange("/animes",
                HttpMethod.PUT,new HttpEntity<>(savedAnime), Void.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }
//
    @Test
    @DisplayName("delete removes anime when successful")
    void delete_RemoveAnime_WhenSucessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(ADMIN);

        ResponseEntity<Void> animeResponseEntity = testRestTemplateAdmin.exchange("/animes/admin/{id}",
                HttpMethod.DELETE,null, Void.class, savedAnime.getId());

        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    }

    @Test
    @DisplayName("delete returns 403 when user is not admin")
    void delete_Returns403_WhenSucessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);

        ResponseEntity<Void> animeResponseEntity = testRestTemplateUser.exchange("/animes/{id}",
                HttpMethod.DELETE,null, Void.class, savedAnime.getId());

        Assertions.assertThat(animeResponseEntity).isNotNull();
        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }


}