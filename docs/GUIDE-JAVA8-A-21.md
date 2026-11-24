# ☕ De Java 8 à Java 21 (et de Spring Boot 1-2 à Spring Boot 3)

Guide pour une dev expérimentée : uniquement ce qui a changé, avec un « avant / après ».

## 1. Le langage

### `var` (Java 10) : inférence de type locale
```java
// Java 8
Map<String, List<Transaction>> parCompte = new HashMap<>();
// Java 21
var parCompte = new HashMap<String, List<Transaction>>();
```
À utiliser quand le type est évident à droite du `=`.

### Text blocks (Java 15)
```java
String sql = """
        SELECT * FROM account
        WHERE status = 'ACTIVE'
        """;
```

### Records (Java 16) : classes de données immuables
```java
public record Money(BigDecimal amount, String currency) {
    public Money { Objects.requireNonNull(amount); }   // constructeur compact
}
// constructeur, equals, hashCode, toString et accesseurs amount() / currency() générés
```
👉 Parfaits pour les DTO, les événements Kafka et les *value objects*. Voir `Money`, `AccountId`, `TransferRequest`.

### Pattern matching pour `instanceof` (Java 16)
```java
// Java 8
if (obj instanceof Account) { Account a = (Account) obj; ... }
// Java 21
if (obj instanceof Account a && a.getStatus() == ACTIVE) { ... }
```

### Switch expressions (Java 14) et pattern matching dans le switch (Java 21)
```java
// Valeur retournée, plus de break oublié
int frais = switch (segment) {
    case PARTICULIER -> 2;
    case PREMIUM, PROFESSIONNEL -> 0;
};

// Switch sur le TYPE, avec garde "when"
String message = switch (exception) {
    case InsufficientFundsException e -> "Solde insuffisant";
    case AccountNotFoundException e   -> "Compte introuvable";
    case AccountNotActiveException e when e.getMessage().contains("CLOSED") -> "Compte clôturé";
    case AccountNotActiveException e  -> "Compte bloqué";
};
```

### Classes scellées `sealed` (Java 17)
```java
public sealed abstract class DomainException extends RuntimeException
        permits InsufficientFundsException, AccountNotFoundException, AccountNotActiveException { }
```
Combiné au switch ci-dessus, le compilateur **vérifie l'exhaustivité** : si tu ajoutes une sous-classe, chaque switch qui l'oublie ne compile plus (tâche S1-1).

### Record patterns (Java 21)
```java
if (event instanceof TransferCompleted(var id, var from, var to, Money(var montant, var devise), var date)) {
    log.info("{} {} de {} vers {}", montant, devise, from, to);
}
```

## 2. L'API standard

| Besoin | Java 8 | Java 21 |
|---|---|---|
| Liste immuable | `Collections.unmodifiableList(Arrays.asList(...))` | `List.of(...)` |
| Stream → liste | `.collect(Collectors.toList())` | `.toList()` |
| Optional vide ? | `!opt.isPresent()` | `opt.isEmpty()` |
| Optional sinon exception | `orElseThrow(() -> new X())` | `orElseThrow()` (NoSuchElement) |
| Chaîne vide/espaces | `s.trim().isEmpty()` | `s.isBlank()`, aussi `strip()`, `repeat()`, `lines()` |
| Formatage | `String.format("%s", x)` | `"%s".formatted(x)` |
| Premier / dernier élément | `list.get(0)`, `list.get(list.size()-1)` | `list.getFirst()`, `list.getLast()` (*Sequenced Collections*, Java 21) |
| Client HTTP | Apache HttpClient | `java.net.http.HttpClient` (inclus dans le JDK) |

## 3. La concurrence : threads virtuels (Java 21) ⭐
```java
// Java 8 : pool limité, car chaque thread coûte cher
ExecutorService pool = Executors.newFixedThreadPool(200);

// Java 21 : un thread virtuel par tâche, on peut en lancer des millions
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 10_000).forEach(i -> executor.submit(() -> appelLent()));
}   // try-with-resources : attend la fin de toutes les tâches
```
- Utile pour du code qui **attend** (I/O : base, HTTP, Kafka). Aucun gain pour du calcul pur.
- Dans Spring Boot 3.2+ : `spring.threads.virtual.enabled=true`, et Tomcat traite alors chaque requête dans un thread virtuel.
- Piège en Java 21 : un bloc `synchronized` qui fait de l'I/O « épingle » (*pinning*) le thread porteur. Préférer `ReentrantLock` (ce défaut a été corrigé en Java 24).
- Rappels toujours d'actualité : `CompletableFuture`, `ConcurrentHashMap`, `AtomicLong`, `ReentrantLock`, `CountDownLatch`.

## 4. Spring Boot 3 : ce qui change pour toi

| Sujet | Avant (Boot 1/2) | Boot 3 |
|---|---|---|
| Java minimum | 8 | **17** (on utilise 21) |
| Packages Java EE | `javax.persistence`, `javax.validation` | **`jakarta.persistence`, `jakarta.validation`** |
| Erreurs REST | classes d'erreur maison | **`ProblemDetail`** (RFC 9457) natif, voir `ApiExceptionHandler` |
| Tests avec mock | `@MockBean` | **`@MockitoBean`** (Boot 3.4+) |
| Injection | `@Autowired` sur les champs | **injection par constructeur** (sans annotation si un seul constructeur) |
| Spring Batch | `@EnableBatchProcessing`, `JobBuilderFactory` | **`new JobBuilder(name, jobRepository)`**, `new StepBuilder(...)`, sans `@EnableBatchProcessing` |
| Kafka | `ListenableFuture` | `CompletableFuture` (voir `KafkaTransferEventPublisher`) |
| Observabilité | Sleuth | **Micrometer Observation / Tracing** |
| Config | `application.properties` | YAML le plus souvent + `@ConfigurationProperties` avec des records |
| Natif | — | Compilation native possible avec GraalVM |

> ℹ️ Spring Boot 3.5 est la dernière version 3.x et son support open source est terminé (dernière version : 3.5.16, juin 2026). On l'utilise ici comme demandé, car c'est ce qui tourne dans la plupart des entreprises aujourd'hui. Migrer vers Spring Boot 4 est la tâche bonus S9-5.

## 5. Outils autour
- **Maven** : rien de neuf, mais le `spring-boot-starter-parent` gère toutes les versions.
- **Testcontainers** : de vraies bases et un vrai Kafka dans Docker pendant les tests (remplace H2 pour les tests d'intégration sérieux).
- **Flyway** : versionnement du schéma SQL (`V1__...sql`, `V2__...sql`).
- **ArchUnit** : tests d'architecture.
