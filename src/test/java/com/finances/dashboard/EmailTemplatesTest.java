package com.finances.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.finances.dashboard.dto.response.SummaryResponse;
import com.finances.dashboard.dto.response.UserResponse;
import com.finances.dashboard.model.Payment;
import com.finances.dashboard.model.User;
import com.finances.dashboard.service.EmailService;

/**
 * Renders the email templates without Spring or Docker. Each test also writes the HTML to
 * target/email-previews/ so the design can be opened in a browser.
 */
class EmailTemplatesTest {

    // The monthly summary goes out on the 1st, about the previous month.
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-01T12:00:00Z"), ZoneId.of("America/Sao_Paulo"));

    private final EmailService emailService = new EmailService(null, engine(), CLOCK);

    private static SpringTemplateEngine engine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private static void preview(String name, String html) throws IOException {
        Path dir = Path.of("target", "email-previews");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name + ".html"), html, StandardCharsets.UTF_8);
    }

    @Test
    void welcome() throws IOException {
        User user = new User();
        user.setName("Marcos Nascimento");
        user.setEmail("marcos@email.com");

        String html = emailService.buildWelcomeHtml(user);
        preview("welcome", html);

        assertThat(html)
                .contains("Marcos</span>!")
                .contains("Marcos Nascimento")
                .contains("marcos@email.com")
                .contains("Finance Dashboard");
    }

    @Test
    void monthlySummaryWithPositiveBalance() throws IOException {
        String html = emailService.buildMonthlySummaryHtml(
                new SummaryResponse(new BigDecimal("9200.00"), new BigDecimal("5717.10"), new BigDecimal("3482.90")),
                new UserResponse(1L, "Marcos Nascimento", "marcos@email.com"));
        preview("monthly-summary", html);

        assertThat(html)
                .contains("outubro de 2026")
                .contains("3.482,90")
                .contains("9.200,00")
                .contains("5.717,10")
                .contains("62</span>% das receitas comprometidas")
                .contains("width=\"62%\"")
                .contains("Você fechou o mês no azul")
                .doesNotContain("As despesas passaram");
    }

    @Test
    void monthlySummaryWithNegativeBalance() throws IOException {
        String html = emailService.buildMonthlySummaryHtml(
                new SummaryResponse(new BigDecimal("1000.00"), new BigDecimal("1500.00"), new BigDecimal("-500.00")),
                new UserResponse(1L, "Marcos", "marcos@email.com"));
        preview("monthly-summary-negative", html);

        assertThat(html)
                .contains("500,00")
                .contains("#FF4B26")
                .contains("100</span>% das receitas comprometidas")
                .contains("As despesas passaram as receitas")
                .doesNotContain("no azul");
    }

    @Test
    void monthlySummaryWithoutIncome() {
        String html = emailService.buildMonthlySummaryHtml(
                new SummaryResponse(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                new UserResponse(1L, "Marcos", "marcos@email.com"));

        assertThat(html)
                .contains("Nenhuma receita registrada no mês")
                .contains("Mês empatado")
                .doesNotContain("das receitas comprometidas");
    }

    @Test
    void dueDateTomorrow() throws IOException {
        User user = new User();
        user.setName("Marcos Nascimento");
        user.setEmail("marcos@email.com");
        Payment payment = new Payment();
        payment.setUser(user);
        payment.setDescription("Aluguel");
        payment.setAmount(new BigDecimal("1850.00"));
        payment.setDueDate(LocalDate.of(2026, 11, 2));
        payment.setMethod("Boleto");
        payment.setRecurring(true);

        String html = emailService.buildDueDateHtml(payment);
        preview("due-date", html);

        assertThat(html)
                .contains("AMANHÃ")
                .contains("1.850,00")
                .contains("02/11/2026")
                .contains("Boleto")
                .contains("Sim, mensal")
                .contains("Olá, <strong style=\"color: #eef1ff\">Marcos</strong>");
    }
}
