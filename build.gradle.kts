import org.springframework.boot.gradle.tasks.run.BootRun
import java.time.LocalDate

plugins {
    java
    id("org.springframework.boot") version "3.5.7"
    // 使用 BOM(platform) 管理版本，不需要 dependency-management 插件
    // id("io.spring.dependency-management") version "1.1.6"
}

group = "com.getjobs"
version = "0.0.1-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories {
    mavenCentral()
    // 国内镜像可选：
    // maven { url = uri("https://maven.aliyun.com/repository/public") }
}

dependencies {
    // 用 Spring Boot 官方 BOM 管理版本（推荐）
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.5.7"))

    // 受 BOM 管理的依赖（不写版本）
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("org.apache.httpcomponents.client5:httpclient5-fluent")

    // 不在 BOM 中的依赖（写版本）
    implementation("com.microsoft.playwright:playwright:1.51.0")
    implementation("com.baomidou:mybatis-plus-spring-boot3-starter:3.5.9")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    // 代码生成器（MyBatis-Plus Generator + Freemarker 模板）
    implementation("com.baomidou:mybatis-plus-generator:3.5.9")
    implementation("org.freemarker:freemarker:2.3.32")
    implementation("org.json:json:20231013")
    implementation("io.github.cdimascio:dotenv-java:2.2.0")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")

    // JWT 认证
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // Spring Security Crypto (仅使用 BCrypt)
    implementation("org.springframework.security:spring-security-crypto:6.4.2")

    // Lombok（仅 Java 项目需要）
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")
    testCompileOnly("org.projectlombok:lombok:1.18.42")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.42")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

// 显示已过时 API 的详细告警，便于定位并修复
tasks.withType<JavaCompile> {
    options.compilerArgs.addAll(listOf("-Xlint:deprecation"))
}

// 让 bootJar 里带上 build-info（可在 Actuator /info 里看到）
springBoot {
    buildInfo()
}

// 正确地配置 BootRun（注意类型是 BootRun）
tasks.named<BootRun>("bootRun") {
    mainClass.set("com.getjobs.GetJobsApplication")
    systemProperty("file.encoding", "UTF-8")
    systemProperty("sun.stdout.encoding", "UTF-8")
    systemProperty("sun.stderr.encoding", "UTF-8")
    // 示例：把当天日期传给日志或应用
    systemProperty("LOG_DATE", LocalDate.now().toString())
    // 可选：对齐端口
    // systemProperty("server.port", "8888")
}

// 配置 bootJar 任务，生成可执行的 fat jar
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    // 设置 jar 包的名称（不含版本号）
    archiveBaseName.set("get-jobs")
    // 不附加版本号
    archiveVersion.set("")
    // 输出目录
    destinationDirectory.set(file("build/libs"))

    // 处理重复文件策略：覆盖
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    // 添加启动类配置
    manifest {
        attributes(
            "Implementation-Title" to "Get Jobs Application",
            "Implementation-Version" to project.version,
            "Main-Class" to "org.springframework.boot.loader.launch.JarLauncher"
        )
    }
}

// 创建分发包任务 - Windows
tasks.register<Zip>("distZipWindows") {
    group = "distribution"
    description = "Creates a Windows distribution package"
    archiveFileName.set("get-jobs-windows.zip")
    destinationDirectory.set(file("build/distributions"))

    dependsOn("bootJar")

    from("build/libs/get-jobs.jar") {
        into("lib")
    }
    from("scripts/windows") {
        into(".")
    }
    from("src/main/resources/application.yaml") {
        into("config")
    }
    from("README_PACKAGE.txt") {
        into(".")
    }
}

// 创建分发包任务 - Mac/Linux
tasks.register<Tar>("distTarUnix") {
    group = "distribution"
    description = "Creates a Unix distribution package"
    archiveFileName.set("get-jobs-unix.tar.gz")
    destinationDirectory.set(file("build/distributions"))
    compression = Compression.GZIP

    dependsOn("bootJar")

    from("build/libs/get-jobs.jar") {
        into("lib")
    }
    from("scripts/unix") {
        into(".")
    }
    from("src/main/resources/application.yaml") {
        into("config")
    }
    from("README_PACKAGE.txt") {
        into(".")
    }
}

// 创建统一的打包任务
tasks.register("distAll") {
    group = "distribution"
    description = "Creates all distribution packages"
    dependsOn("bootJar", "distZipWindows", "distTarUnix")
}