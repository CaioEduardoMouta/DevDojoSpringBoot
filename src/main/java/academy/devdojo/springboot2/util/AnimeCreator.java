package academy.devdojo.springboot2.util;

import academy.devdojo.springboot2.domain.Anime;

public class AnimeCreator {

        public static Anime createAnimeToBeSaved(){
            return Anime.builder()
                    .name("Hajime no Inpo")
                    .build();
        }

        public static Anime createValidAnime(){
            return Anime.builder()
                    .name("Hajime no Inpo")
                    .id(1L)
                    .build();
        }

        public static Anime createValidUpdateAnime(){
            return Anime.builder()
                    .name("Hajime no Inpo 2")
                    .id(1L)
                    .build();
        }

}
