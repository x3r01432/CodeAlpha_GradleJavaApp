# CodeAlpha_GradleJavaApp

[![CI](https://github.com/x3r01432/CodeAlpha_GradleJavaApp/actions/workflows/ci.yml/badge.svg)](https://github.com/x3r01432/CodeAlpha_GradleJavaApp/actions/workflows/ci.yml)

CodeAlpha DevOps Internship, **Task 3: Gradle Java app**. A small to-do REST API in Java 21, built with Gradle (Kotlin DSL), with a version catalog, JUnit 5 tests, JaCoCo coverage (≥70% enforced), a runnable fat jar, a Docker image and a GitHub Actions CI/CD workflow.

## Architecture
```
settings.gradle.kts            ← project + "app" module
gradle/libs.versions.toml      ← version catalog (Gson 2.11.0, JUnit 5.11.3, Shadow 8.3.5)
gradle/wrapper/                ← Gradle 8.10.2 wrapper (no local Gradle needed)
app/build.gradle.kts           ← application, jacoco, shadow plugins; Java 21 toolchain
app/src/main/java/.../App.java          ← JDK HttpServer REST endpoints
app/src/main/java/.../TodoService.java  ← thread-safe in-memory store + validation
app/src/test/java/...          ← unit tests + HTTP integration tests
Dockerfile                     ← non-root JRE image running the fat jar
.github/workflows/ci.yml       ← GitHub Actions workflow
```
Pipeline: checkout → JDK 21 → `./gradlew build shadowJar` (compile, test, coverage check) → upload jar + reports as artifacts → build and push image to GHCR (`ghcr.io/x3r01432/codealpha_gradlejavaapp`) → on a `v*` tag, create a GitHub Release with the jar.

## API
| Method | Path | Action |
|---|---|---|
| GET | `/health` | `{"status":"UP"}` |
| GET | `/todos` | list |
| POST | `/todos` `{"title":"..."}` | create (title 1–200 chars) |
| GET | `/todos/{id}` | one item |
| PUT | `/todos/{id}` | mark done |
| DELETE | `/todos/{id}` | delete |

## Run it
```bash
./gradlew build shadowJar                     # test + coverage + fat jar
java -jar app/build/libs/todo-api-1.0.0-all.jar   # PORT env var, default 8080
curl -XPOST localhost:8080/todos -d '{"title":"Learn Gradle"}'
# or with Docker
docker build -t todo-api . && docker run -p 8080:8080 todo-api
```
Reports: `app/build/reports/tests/test/index.html`, `app/build/reports/jacoco/test/html/index.html`.

## Proof (real output)
```
BUILD SUCCESSFUL in 10s
13 actionable tasks: 13 executed
Tests: 6 run, 0 failures, 0 errors
JaCoCo coverage: instructions 92.2%, lines 90.8%, branches 89.2%
app/build/libs/todo-api-1.0.0-all.jar (318 KB)

$ curl localhost:8090/health                → {"status":"UP"}
$ curl -XPOST /todos -d '{"title":"Learn Gradle"}' → {"id":1,"title":"Learn Gradle","done":false}
$ curl -XPUT /todos/1                       → {"id":1,"title":"Learn Gradle","done":true}
$ curl -XPOST /todos -d '{}'                → {"error":"title is required"}
$ docker exec todo whoami                   → app
```

## Releases
Push a tag like `v1.0.0` (`git tag v1.0.0 && git push --tags`) to create a GitHub Release with the jar.

## LinkedIn demo script (60–90 s)
1. (10 s) "For CodeAlpha DevOps Task 3 I built a Java REST API with Gradle." Show the repo.
2. (15 s) Show `libs.versions.toml` and `build.gradle.kts`: version catalog, JaCoCo, fat jar.
3. (20 s) Terminal: `./gradlew build shadowJar` → BUILD SUCCESSFUL; open the JaCoCo report (~90%).
4. (15 s) `java -jar ...-all.jar`, then `curl` to create and complete a to-do.
5. (15 s) Show the GitHub Actions run: build, test, artifact, Docker image on GHCR.
6. (5 s) "Thanks @CodeAlpha #DevOps #Gradle #Java"

## License
MIT © 2026 Steven Rios
