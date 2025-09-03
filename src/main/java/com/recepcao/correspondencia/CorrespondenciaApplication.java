package com.recepcao.correspondencia;

import com.recepcao.correspondencia.entities.Correspondencia;
import com.recepcao.correspondencia.entities.HistoricoInteracao;
import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import com.recepcao.correspondencia.repositories.CorrespondenciaRepository;
import com.recepcao.correspondencia.repositories.HistoricoInteracaoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootApplication
public class CorrespondenciaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CorrespondenciaApplication.class, args);
	}

	@Bean
	public CommandLineRunner initData(CorrespondenciaRepository correspondenciaRepository, 
									HistoricoInteracaoRepository historicoRepository) {
		return args -> {
			// Add test correspondence
			Correspondencia testCorrespondencia = new Correspondencia();
			testCorrespondencia.setRemetente("Teste Remetente");
			testCorrespondencia.setNomeEmpresaConexa("Empresa Teste");
			testCorrespondencia.setStatusCorresp(StatusCorresp.ANALISE);
			testCorrespondencia.setDataRecebimento(LocalDate.now());
			correspondenciaRepository.save(testCorrespondencia);

			// Add test history
			HistoricoInteracao testHistorico = new HistoricoInteracao();
			testHistorico.setEntidade("Correspondencia");
			testHistorico.setEntidadeId(1L);
			testHistorico.setAcaoRealizada("Teste de Ação");
			testHistorico.setDetalhe("Detalhes do teste");
			testHistorico.setDataHora(LocalDateTime.now());
			historicoRepository.save(testHistorico);

			System.out.println("Test data initialized successfully!");
		};
	}
}
