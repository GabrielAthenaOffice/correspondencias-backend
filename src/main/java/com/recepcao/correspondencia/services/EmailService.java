package com.recepcao.correspondencia.services;

import com.recepcao.correspondencia.entities.Correspondencia;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


import jakarta.mail.internet.MimeMessage;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    // ajuste se quiser fixar um remetente específico
    @Value("${spring.mail.username}")
    private String defaultFrom;

    private static final ZoneId ZONE = ZoneId.of("America/Recife");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withLocale(new Locale("pt", "BR"));

    public void enviarAvisoCorrespondencias(String para, String nomeCliente, List<Correspondencia> itens) {
        if (para == null || para.isBlank()) {
            log.warn("Sem destinatário para envio de aviso de correspondências (nomeCliente={})", nomeCliente);
            return;
        }
        if (itens == null || itens.isEmpty()) {
            log.warn("Sem itens de correspondência para enviar (nomeCliente={}, para={})", nomeCliente, para);
            return;
        }

        String assunto = "Lembrete: correspondências aguardando retirada | ATHENA OFFICE";
        String corpo = montarCorpoTexto(nomeCliente, itens);

        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, false, "UTF-8");

            helper.setTo(para);
            if (defaultFrom != null && !defaultFrom.isBlank()) {
                helper.setFrom(defaultFrom, "ATHENA OFFICE");
            }
            helper.setSubject(assunto);
            helper.setText(corpo, false); // texto puro; mude para true + HTML se quiser

            mailSender.send(msg);
            log.info("Email de aviso de correspondências enviado para {}", para);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}: {}", para, e.getMessage(), e);
        }
    }

    private String montarCorpoTexto(String nomeCliente, List<Correspondencia> itens) {
        StringBuilder sb = new StringBuilder();

        sb.append("Prezado(a) ").append(nomeCliente).append(", essa mensagem é apenas para lembrá-lo que existem correspondências recebidas em nosso escritório que ainda estão aguardando retirada. ")
                .append("Pedimos a gentileza de verificar COM ANTECEDÊNCIA a disponibilidade de data e horário para a retirada na sua unidade através do WhatsApp.\n\n")
                .append("Correspondências ou Encomendas\n\n");

        for (Correspondencia c : itens) {
            String remetente = nvl(c.getRemetente());
            String codRastreio = nvl(null);         // ajuste o getter conforme seu Entity
            String obs = nvl(null);                     // ajuste o getter conforme seu Entity
            String recebidaEm = c.getDataRecebimento() != null
                    ? c.getDataRecebimento().atStartOfDay(ZONE).format(DTF)
                    : "-";

            sb.append("Remetente: ").append(remetente).append("\n")
                    .append("Cód. Rastreio: ").append(codRastreio).append("\n")
                    .append("Recebida em: ").append(recebidaEm).append("\n")
                    .append("Observações: ").append(obs).append("\n\n");
        }

        sb.append("A) ATENÇÃO – CORRESPONDÊNCIAS JUDICIAIS:\n")
                .append("Caso a correspondência seja identificada como judicial, proveniente de órgãos do Poder Judiciário, Tribunais ou entidades relacionadas a processos jurídicos. ")
                .append("Para clientes adimplentes, o conteúdo da correspondência já está disponível em anexo. ")
                .append("O conteúdo foi enviado em anexo no primeiro e-mail de notificação, que foi encaminhado assim que a correspondência chegou. ")
                .append("Este e-mail é apenas um lembrete e não contém o anexo.\n\n")

                .append("B) Após 30 dias do recebimento, a correspondência será devolvida ao remetente/correios. ")
                .append("Lembre-se de que para retirar a correspondência, é necessário regularizar quaisquer débitos em atraso e apresentar os comprovantes necessários.\n\n")

                .append("C) Com o objetivo de preservar sua segurança, lembramos que as correspondências só podem ser entregues a pessoas previamente cadastradas e autorizadas. ")
                .append("Se for necessário cadastrar uma nova pessoa, por favor, envie um e-mail para adm@athenaoffice.com.br com o nome e CPF da pessoa autorizada.\n\n")

                .append("Para esclarecer qualquer dúvida adicional relacionada à correspondência, não hesite em entrar em contato conosco através de um de nossos canais:\n\n")
                .append("WhatsApp: (11) 99368-5792 - Opção CORRESPONDÊNCIA\n")
                .append("Fale conosco: 0800 0800 003 - Opção CORRESPONDÊNCIA\n")
                .append("E-mail: adm@athenaoffice.com.br\n\n")
                .append("Atenciosamente,\n")
                .append("ATHENA OFFICE\n")
                .append("0800 0800 003\n")
                .append("www.athenaoffice.com.br/\n");

        return sb.toString();
    }

    private String nvl(String v) { return (v == null || v.isBlank()) ? "" : v.trim(); }
}

