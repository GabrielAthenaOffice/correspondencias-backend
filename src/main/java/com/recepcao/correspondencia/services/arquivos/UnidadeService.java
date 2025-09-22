package com.recepcao.correspondencia.services.arquivos;

import com.recepcao.correspondencia.dto.UnidadeDTO;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@Service
public class UnidadeService {
    private final Map<String, UnidadeInfo> unidades = Map.ofEntries(
            Map.entry("ATHENA BELEM LTDA", new UnidadeInfo("55.504.722/0001-75", "Travessa Quintino Bocaiúva, 230, Belém, PA, 66060-232")),
            Map.entry("ATHENA BELO HORIZONTE LTDA", new UnidadeInfo("50.672.664/0001-75", "Rua Rio Grande do Norte, 1560, Belo Horizonte, MG, 30130-130")),
            Map.entry("ATHENA BRASILIA LTDA", new UnidadeInfo("54.790.270/0001-72", "")), //
            Map.entry("ATHENA CAMPINA GRANDE LTDA", new UnidadeInfo("54.735.201/0001-66", "Rua José Bernardino, 97, Campina Grande, PB, 58408-027")),
            Map.entry("ATHENA CONSUL NEG. E SERV. LTDA", new UnidadeInfo("", "")),
            Map.entry("ATHENA CUIABA LTDA", new UnidadeInfo("54.734.257/0001-04", "")),
            Map.entry("ATHENA CURITIBA LTDA", new UnidadeInfo("50.640.354/0001-79", "Avenida Sete de Setembro, 5388, Curitiba, PR, 80240-000")),
            Map.entry("ATHENA FLORIANOPOLIS LTDA", new UnidadeInfo("50.978.416/0001-57", "Avenida Desembargador Vitor Lima, 260, Florianópolis, SC, 88040-400")),
            Map.entry("ATHENA FORTALEZA LTDA", new UnidadeInfo("52.723.557/0001-54", "Avenida Dom Luís, 500, Fortaleza, CE, 60160-230")),
            Map.entry("ATHENA GOIANIA LTDA", new UnidadeInfo("53.672.579/0001-03", "Avenida República do Líbado, 2341, Goiânia, GO, 74125-904")),
            Map.entry("ATHENA GUARARAPES LTDA", new UnidadeInfo("59.651.668/0001-41", "Avenida Ayrton Senna da Silva, 2198, Jaboatão dos Guararapes, PE, 54410-240")),
            Map.entry("ATLAS JP LTDA", new UnidadeInfo("53.191.817/0001-50", "Avenida Governador Argemiro de Figueiredo, 210, João Pessoa, PB, 58037-030")),
            Map.entry("ATHENA MANAUS LTDA", new UnidadeInfo("52.738.365/0001-11", "Rua Franco de Sá, 310, Manaus, AM, 69079-210")),
            Map.entry("ATHENA NATAL LTDA", new UnidadeInfo("50.770.572/0001-28", "Rua DR. Luiz Felipe Camara, 55, Rio Grande do Norte, RN")),
            Map.entry("ATHENA PALMAS LTDA", new UnidadeInfo("54.493.774/0001-20", "")),
            Map.entry("ATHENA PORTO ALEGRE LTDA", new UnidadeInfo("50.648.711/0001-45", "Rua General Neto, 71, Porto Alegre, RS, 90560-020")),
            Map.entry("ATHENA RECIFE LTDA", new UnidadeInfo("50.773.013/0001-71", "José Aderval Chaves, 78, Recife, PE, 51111-030")),
            Map.entry("ATHENA RIO DE JANEIRO LTDA", new UnidadeInfo("51.932.328/0001-87", "Avenida Ayrton Senna, 2500, Rio de Janeiro, RJ, 22775-003")),
            Map.entry("ATHENA SALVADOR BA LTDA", new UnidadeInfo("54.967.721/0001-02", "Avenida Tancredo Neves, 1485, Salvador, BA, 41820-020")),
            Map.entry("ATHENA SAO LUIS LTDA", new UnidadeInfo("54.570.537/0001-16", "Rua MITRA, 10, São Luís, Maranhão 65075-770")),
            Map.entry("ATHENA SAO PAULO LTDA", new UnidadeInfo("50.673.215/0001-41", "Avenida Paulista, 1471, São Paulo, SP, 01311-200")),
            Map.entry("ATHENA VITORIA LTDA", new UnidadeInfo("50.733.676/0001-62", "Avenida Américo Buaiz, 501, Vitória, ES, 29050-423"))
    );

    public record UnidadeInfo(String cnpj, String endereco) {}

    public UnidadeInfo getUnidadeInfo(String nomeUnidade) {
        return unidades.get(nomeUnidade);
    }

    public Set<String> listarUnidades() {
        return unidades.keySet();
    }
}
