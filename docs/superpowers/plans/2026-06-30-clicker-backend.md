# Clicker Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Spring Boot REST API that counts clicks in an H2 in-memory database with two endpoints: POST /api/click and GET /api/count.

**Architecture:** Single Counter entity with one row initialized at startup. ClickerService encapsulates the increment/read logic. ClickerController exposes REST endpoints. CORS configured globally for localhost.

**Tech Stack:** Java 21, Spring Boot 3.x, Spring Data JPA, H2 embedded, Maven

## Global Constraints

- Java 21+
- Spring Boot 3.x (use Spring Initializr defaults)
- H2 in-memory (data resets on restart — intentional)
- CORS: allow all origins on localhost (any port)
- Counter table always has exactly one row (id=1)
- All API responses: `{"count": <long>}`

---

### Task 1: Bootstrap Spring Boot project

**Files:**
- Create: `backend/pom.xml`
- Create: `backend/src/main/java/com/example/clicker/ClickerApplication.java`
- Create: `backend/src/main/resources/application.properties`

**Interfaces:**
- Produces: runnable Spring Boot application skeleton at `backend/`

- [ ] **Step 1: Generate project via Spring Initializr**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test
curl -s https://start.spring.io/starter.tgz \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.4.1 \
  -d baseDir=backend \
  -d groupId=com.example \
  -d artifactId=clicker \
  -d name=clicker \
  -d packageName=com.example.clicker \
  -d javaVersion=21 \
  -d dependencies=web,data-jpa,h2 \
  | tar -xzvf -
```

- [ ] **Step 2: Configure application.properties**

Replace contents of `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:clickerdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

- [ ] **Step 3: Verify project compiles**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw compile
```

Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test
git init
git add backend/
git commit -m "feat: bootstrap Spring Boot project"
```

---

### Task 2: Counter entity and repository

**Files:**
- Create: `backend/src/main/java/com/example/clicker/Counter.java`
- Create: `backend/src/main/java/com/example/clicker/CounterRepository.java`

**Interfaces:**
- Produces:
  - `Counter` entity with fields `Long id`, `long count`
  - `CounterRepository extends JpaRepository<Counter, Long>`

- [ ] **Step 1: Write the failing test**

Create `backend/src/test/java/com/example/clicker/CounterRepositoryTest.java`:

```java
package com.example.clicker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CounterRepositoryTest {

    @Autowired
    private CounterRepository repository;

    @Test
    void savesAndReadsCounter() {
        Counter counter = new Counter();
        counter.setCount(5L);
        Counter saved = repository.save(counter);

        assertThat(repository.findById(saved.getId())).isPresent();
        assertThat(repository.findById(saved.getId()).get().getCount()).isEqualTo(5L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test -Dtest=CounterRepositoryTest
```

Expected: FAIL — `CounterRepository` not found

- [ ] **Step 3: Create Counter entity**

Create `backend/src/main/java/com/example/clicker/Counter.java`:

```java
package com.example.clicker;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Counter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private long count;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
}
```

- [ ] **Step 4: Create CounterRepository**

Create `backend/src/main/java/com/example/clicker/CounterRepository.java`:

```java
package com.example.clicker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CounterRepository extends JpaRepository<Counter, Long> {
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test -Dtest=CounterRepositoryTest
```

Expected: `BUILD SUCCESS`, 1 test passed

- [ ] **Step 6: Commit**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
git add src/
git commit -m "feat: add Counter entity and repository"
```

---

### Task 3: ClickerService with initialization

**Files:**
- Create: `backend/src/main/java/com/example/clicker/ClickerService.java`

**Interfaces:**
- Consumes: `CounterRepository` (from Task 2)
- Produces:
  - `ClickerService.getCount(): long`
  - `ClickerService.increment(): long` — increments count, returns new value
  - On startup: ensures row with id=1 exists with count=0

- [ ] **Step 1: Write the failing test**

Create `backend/src/test/java/com/example/clicker/ClickerServiceTest.java`:

```java
package com.example.clicker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ClickerServiceTest {

    @Autowired
    private ClickerService clickerService;

    @Autowired
    private CounterRepository counterRepository;

    @BeforeEach
    void reset() {
        Counter counter = counterRepository.findById(1L).orElseThrow();
        counter.setCount(0L);
        counterRepository.save(counter);
    }

    @Test
    void initialCountIsZero() {
        assertThat(clickerService.getCount()).isEqualTo(0L);
    }

    @Test
    void incrementIncreasesCountByOne() {
        long first = clickerService.increment();
        assertThat(first).isEqualTo(1L);
        assertThat(clickerService.getCount()).isEqualTo(1L);
    }

    @Test
    void multipleIncrementsAccumulate() {
        clickerService.increment();
        clickerService.increment();
        long third = clickerService.increment();
        assertThat(third).isEqualTo(3L);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test -Dtest=ClickerServiceTest
```

Expected: FAIL — `ClickerService` not found

- [ ] **Step 3: Implement ClickerService**

Create `backend/src/main/java/com/example/clicker/ClickerService.java`:

```java
package com.example.clicker;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClickerService {

    private final CounterRepository counterRepository;

    public ClickerService(CounterRepository counterRepository) {
        this.counterRepository = counterRepository;
    }

    @PostConstruct
    public void init() {
        if (counterRepository.count() == 0) {
            Counter counter = new Counter();
            counter.setCount(0L);
            counterRepository.save(counter);
        }
    }

    @Transactional(readOnly = true)
    public long getCount() {
        return counterRepository.findById(1L)
                .map(Counter::getCount)
                .orElse(0L);
    }

    @Transactional
    public long increment() {
        Counter counter = counterRepository.findById(1L).orElseThrow();
        counter.setCount(counter.getCount() + 1);
        counterRepository.save(counter);
        return counter.getCount();
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test -Dtest=ClickerServiceTest
```

Expected: `BUILD SUCCESS`, 3 tests passed

- [ ] **Step 5: Commit**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
git add src/
git commit -m "feat: add ClickerService with init and increment"
```

---

### Task 4: REST controller with CORS

**Files:**
- Create: `backend/src/main/java/com/example/clicker/ClickerController.java`
- Create: `backend/src/main/java/com/example/clicker/CorsConfig.java`

**Interfaces:**
- Consumes: `ClickerService.getCount()`, `ClickerService.increment()` (from Task 3)
- Produces:
  - `POST /api/click` → `200 {"count": <long>}`
  - `GET /api/count` → `200 {"count": <long>}`
  - CORS allowed for all origins

- [ ] **Step 1: Write the failing test**

Create `backend/src/test/java/com/example/clicker/ClickerControllerTest.java`:

```java
package com.example.clicker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ClickerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CounterRepository counterRepository;

    @BeforeEach
    void reset() {
        Counter counter = counterRepository.findById(1L).orElseThrow();
        counter.setCount(0L);
        counterRepository.save(counter);
    }

    @Test
    void getCountReturnsZeroInitially() throws Exception {
        mockMvc.perform(get("/api/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    void postClickIncrementsAndReturnsNewCount() throws Exception {
        mockMvc.perform(post("/api/click"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void multipleClicksAccumulate() throws Exception {
        mockMvc.perform(post("/api/click"));
        mockMvc.perform(post("/api/click"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test -Dtest=ClickerControllerTest
```

Expected: FAIL — 404 for `/api/click`

- [ ] **Step 3: Implement ClickerController**

Create `backend/src/main/java/com/example/clicker/ClickerController.java`:

```java
package com.example.clicker;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class ClickerController {

    private final ClickerService clickerService;

    public ClickerController(ClickerService clickerService) {
        this.clickerService = clickerService;
    }

    @PostMapping("/click")
    public Map<String, Long> click() {
        return Map.of("count", clickerService.increment());
    }

    @GetMapping("/count")
    public Map<String, Long> count() {
        return Map.of("count", clickerService.getCount());
    }
}
```

- [ ] **Step 4: Configure CORS globally**

Create `backend/src/main/java/com/example/clicker/CorsConfig.java`:

```java
package com.example.clicker;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST");
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test -Dtest=ClickerControllerTest
```

Expected: `BUILD SUCCESS`, 3 tests passed

- [ ] **Step 6: Run all tests**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw test
```

Expected: `BUILD SUCCESS`, all tests passed

- [ ] **Step 7: Commit**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
git add src/
git commit -m "feat: add REST controller and CORS config"
```

---

### Task 5: Smoke test — run the app

**Files:** none (verification only)

- [ ] **Step 1: Start the application**

```bash
cd /Users/tr1-galki/test/neiro-test/v0-test/backend
./mvnw spring-boot:run
```

Expected: `Started ClickerApplication` in the logs, listening on port 8080

- [ ] **Step 2: Test GET /api/count**

In a new terminal:

```bash
curl -s http://localhost:8080/api/count
```

Expected: `{"count":0}`

- [ ] **Step 3: Test POST /api/click**

```bash
curl -s -X POST http://localhost:8080/api/click
```

Expected: `{"count":1}`

- [ ] **Step 4: Confirm increment**

```bash
curl -s http://localhost:8080/api/count
```

Expected: `{"count":1}`

- [ ] **Step 5: Stop the app**

Press `Ctrl+C` in the Spring Boot terminal.
