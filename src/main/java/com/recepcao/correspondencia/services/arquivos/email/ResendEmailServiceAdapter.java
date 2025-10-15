package com.recepcao.correspondencia.services.arquivos.email;

import com.recepcao.correspondencia.dto.record.AnexoDTO;
import com.recepcao.correspondencia.entities.Correspondencia;
import com.resend.Resend;
import com.resend.services.emails.model.Attachment;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class ResendEmailServiceAdapter {

    @Value("${email.resend.api.key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from; // "ATHENA OFFICE <adm@athenaoffice.com.br>"

    private static final ZoneId ZONE = ZoneId.of("America/Recife");
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withLocale(new Locale("pt", "BR"));

    public void enviarAvisoCorrespondencias(String para, String nomeCliente, List<Correspondencia> itens, List<AnexoDTO> anexos ) {
        if (para == null || para.isBlank() || itens == null || itens.isEmpty())
            throw new IllegalArgumentException("Destinatário/itens inválidos");

        String subject = "Lembrete: correspondências aguardando retirada | ATHENA OFFICE";
        String body = montarTexto(nomeCliente, itens);

        try {
            Resend client = new Resend(apiKey);

            List<Attachment> attachments = new ArrayList<>();

            if(anexos != null) {
                for (AnexoDTO a : anexos) {
                    String b64 = java.util.Base64.getEncoder().encodeToString(a.data());
                    attachments.add(
                            com.resend.services.emails.model.Attachment.builder()
                                    .fileName(a.filename())
                                    .content(b64)           // base64
                                    .build()
                    );
                }
            }
            CreateEmailOptions req = CreateEmailOptions.builder()
                    .from(from)
                    .to(para)
                    .subject(subject)
                    .text(body)     // troque para .html(...) se quiser HTML
                    .build();

            client.emails().send(req);
            log.info("Resend OK -> to={}", para);
        } catch (Exception e) {
            log.error("Resend falhou -> to={} err={}", para, e.getMessage(), e);
            throw new RuntimeException("Falha ao enviar via Resend: " + e.getMessage());
        }
    }

    private String montarTexto(String nomeCliente, List<Correspondencia> itens) {
        StringBuilder sb = new StringBuilder();

        // linha inicial sem vírgula após o nome (como você especificou)
        sb.append("Prezado(a) ").append(nomeCliente)
                .append(" essa mensagem é apenas para lembrá-lo que existem correspondências recebidas em nosso escritório que ainda estão aguardando retirada. ")
                .append("Pedimos a gentileza de verificar COM ANTECEDÊNCIA a disponibilidade de data e horário para a retirada na sua unidade através do WhatsApp.\n\n");

        sb.append("Correspondências\n\n");

        for (Correspondencia c : itens) {
            String remetente = nvl(c.getRemetente());
            String recebidaEm = c.getDataRecebimento() != null
                    ? c.getDataRecebimento().atZone(ZONE).format(DTF)
                    : "-";

            sb.append("Remetente: ").append(remetente).append("\n")
                    .append("Recebida em: ").append(recebidaEm).append("\n")
                    .append("Observações:").append("\n\n");
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
                .append("www.athenaoffice.com.br/\n");

        return sb.toString();
    }


    private String nvl(String v) { return (v == null || v.isBlank()) ? "" : v.trim(); }
}
