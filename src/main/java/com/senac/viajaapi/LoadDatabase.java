package com.senac.viajaapi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    @Bean
    @org.springframework.core.annotation.Order(0)
    CommandLineRunner initDatabase(UserRepository userRepository, CityRepository cityRepository,
            InterestRepository interestRepository, LocalRepository localRepository,
            LocalInterestRepository localInterestRepository, TripRepository tripRepository,
            TripLocalRepository tripLocalRepository, ReviewRepository reviewRepository,
            SubscriptionRepository subscriptionRepository, SavedLocalRepository savedLocalRepository,
            MediaRepository mediaRepository, TripMemoRepository tripMemoRepository,
            TripNoteRepository tripNoteRepository, PasswordEncoder passwordEncoder) {

        return args -> {
            if (userRepository.count() != 0) return;
            LocalDateTime now = LocalDateTime.now();

            User rafael = new User("Rafael Reis", "rafael@email.com",
                    passwordEncoder.encode("senha123"), "tourism", now, now);
            userRepository.save(rafael);

            User maria = new User("Maria Clara", "maria@email.com",
                    passwordEncoder.encode("senha123"), "work", now, now);
            userRepository.save(maria);

            User joao = new User("Joao Silva", "joao@email.com",
                    passwordEncoder.encode("senha123"), "tourism", now, now);
            userRepository.save(joao);

            City rio = new City("Rio de Janeiro", "RJ", "Brasil", "https://exemplo.com/rio.jpg");
            cityRepository.save(rio);

            City sp = new City("Sao Paulo", "SP", "Brasil", "https://exemplo.com/sp.jpg");
            cityRepository.save(sp);

            City floripa = new City("Florianopolis", "SC", "Brasil", "https://exemplo.com/floripa.jpg");
            cityRepository.save(floripa);

            Interest praia = new Interest("Praia", "natureza", now);
            interestRepository.save(praia);
            Interest museu = new Interest("Museu", "cultura", now);
            interestRepository.save(museu);
            Interest restaurante = new Interest("Restaurante", "gastronomia", now);
            interestRepository.save(restaurante);
            Interest cafe = new Interest("Cafe", "gastronomia", now);
            interestRepository.save(cafe);
            Interest hospedagem = new Interest("Hospedagem", "hospedagem", now);
            interestRepository.save(hospedagem);
            Interest trabalho = new Interest("Trabalho", "trabalho", now);
            interestRepository.save(trabalho);

            Local mar = new Local(rio.getId(), "attraction", "Museu de Arte do Rio",
                    "Av. Marechal Flores, 100", new BigDecimal("-22.8882"), new BigDecimal("-43.1945"),
                    15000, 4.5f, 120, "{\"segunda\":\"10:00-18:00\"}", "Museu de arte moderna ao lado da praia",
                    "+55 21 3333-1000", "https://mar.org.br", "mock", null, now, now);
            localRepository.save(mar);

            Local ipanema = new Local(rio.getId(), "other", "Praia de Ipanema",
                    "Praia de Ipanema", new BigDecimal("-22.9876"), new BigDecimal("-43.2218"),
                    0, 4.7f, 850, null, "Praia famosa do Rio de Janeiro", null, null, "mock", null, now, now);
            localRepository.save(ipanema);

            Local coworking = new Local(sp.getId(), "cafe", "Coworking Spaces",
                    "Av. Paulista, 1000", new BigDecimal("-23.5606"), new BigDecimal("-46.6533"),
                    3500, 4.2f, 60, "{\"segunda\":\"08:00-20:00\"}", "Espaco de trabalho compartilhado",
                    "+55 11 4444-5555", "https://coworking.sp.br", "mock", null, now, now);
            localRepository.save(coworking);

            localInterestRepository.save(new LocalInterest(mar.getId(), praia.getId()));
            localInterestRepository.save(new LocalInterest(mar.getId(), museu.getId()));
            localInterestRepository.save(new LocalInterest(ipanema.getId(), praia.getId()));
            localInterestRepository.save(new LocalInterest(coworking.getId(), cafe.getId()));
            localInterestRepository.save(new LocalInterest(coworking.getId(), trabalho.getId()));

            Trip v1 = new Trip(rafael.getId(), rio.getId(), "Ferias no Rio", "tourism",
                    LocalDate.parse("2026-12-01"), LocalDate.parse("2026-12-05"), 300000, "planned", now, now);
            tripRepository.save(v1);

            Trip v2 = new Trip(maria.getId(), sp.getId(), "Viagem a trabalho SP", "work",
                    LocalDate.parse("2026-11-20"), LocalDate.parse("2026-11-23"), 150000, "planned", now, now);
            tripRepository.save(v2);

            Trip v3 = new Trip(joao.getId(), floripa.getId(), "Fim de semana em Floripa", "tourism",
                    LocalDate.parse("2026-12-10"), LocalDate.parse("2026-12-12"), 200000, "planned", now, now);
            tripRepository.save(v3);

            TripLocal tl1 = new TripLocal(v1.getId(), mar.getId(), 1, 1, "Visita guiada");
            tripLocalRepository.save(tl1);

            TripLocal tl2 = new TripLocal(v1.getId(), ipanema.getId(), 2, 1, "Passeio pela praia");
            tripLocalRepository.save(tl2);

            TripLocal tl3 = new TripLocal(v2.getId(), coworking.getId(), 1, 1, "Reuniao de trabalho");
            tripLocalRepository.save(tl3);

            Review r1 = new Review(mar.getId(), 5, now);
            reviewRepository.save(r1);

            Review r2 = new Review(ipanema.getId(), 4, now);
            reviewRepository.save(r2);

            Subscription sub = new Subscription(rafael.getId(), "free", "active", 0,
                    LocalDate.now(), null, now);
            subscriptionRepository.save(sub);

            SavedLocal saved = new SavedLocal(rafael.getId(), mar.getId(), now);
            savedLocalRepository.save(saved);

            Media m1 = new Media("local", mar.getId(), "https://exemplo.com/mar.jpg", "image", now);
            mediaRepository.save(m1);

            Media m2 = new Media("trip", v1.getId(), "https://exemplo.com/rio-trip.jpg", "image", now);
            mediaRepository.save(m2);

            TripMemo memo = new TripMemo(v1.getId(), mar.getId(), "Não esquecer ingressos", now);
            tripMemoRepository.save(memo);

            TripNote note = new TripNote(v1.getId(), "Levar protetor solar", now);
            tripNoteRepository.save(note);

            log.info("Seed concluido: usuarios, cidades, interesses, locais, vinculacoes, "
                    + "viagens, paradas, avaliacoes, assinaturas, salvos, midias, memorias e anotacoes");
        };
    }
}
