package com.recepcao.correspondencia.services;

import com.recepcao.correspondencia.clients.ConexaClients;
import com.recepcao.correspondencia.dto.CorrespondenciaResponse;
import com.recepcao.correspondencia.config.APIExceptions;
import com.recepcao.correspondencia.dto.contracts.EmailServiceDTO;
import com.recepcao.correspondencia.dto.record.AnexoDTO;
import com.recepcao.correspondencia.dto.record.ConexaContractResponse;
import com.recepcao.correspondencia.dto.record.EmailResponseRecord;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.*;
import com.recepcao.correspondencia.feign.*;
import com.recepcao.correspondencia.mapper.CustomerResponseMapper;
import com.recepcao.correspondencia.mapper.EmpresaMapper;
import com.recepcao.correspondencia.mapper.UnidadeMapper;
import com.recepcao.correspondencia.mapper.enums.Situacao;
import com.recepcao.correspondencia.mapper.enums.StatusCorresp;
import com.recepcao.correspondencia.mapper.enums.StatusEmpresa;
import com.recepcao.correspondencia.repositories.CorrespondenciaRepository;
import com.recepcao.correspondencia.repositories.CustomerRepository;
import com.recepcao.correspondencia.repositories.EmpresaRepository;
import com.recepcao.correspondencia.services.arquivos.StorageService;
import com.recepcao.correspondencia.services.arquivos.UnidadeService;
import com.recepcao.correspondencia.services.arquivos.email.EmailService;
import com.recepcao.correspondencia.services.arquivos.email.ResendEmailServiceAdapter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import com.recepcao.correspondencia.dto.CorrespondenciaComEmpresaDTO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class CorrespondenciaService {

    private static final Logger log = LoggerFactory.getLogger(CorrespondenciaService.class);

    private final CorrespondenciaRepository correspondenciaRepository;
    private final EmpresaRepository empresaRepository;
    private final ConexaClients conexaClient;
    private final EmailService emailService;
    private final AditivoRepository aditivoRepository;
    private final AditivoClient aditivoClient;
    private final UnidadeService unidadeService;
    // enderecoValidatorService removido porque não é utilizado neste serviço
    private final HistoricoService historicoService;
    private final CustomerRepository customerRepository;
    // imports: Pattern, Comparator, Objects, LocalDate, etc.
    private static final java.util.regex.Pattern EMAIL_RX =
            java.util.regex.Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", java.util.regex.Pattern.CASE_INSENSITIVE);
    private final ResendEmailServiceAdapter resendEmail;
    private final StorageService storageService;


    public CorrespondenciaResponse listarTodas(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        // Validate sortBy field exists in the entity
        if (!isValidSortField(sortBy)) {
            sortBy = "id"; // Default to id if invalid field
        }

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Correspondencia> correspondenciaPage = correspondenciaRepository.findAll(pageDetails);

        List<Correspondencia> correspondenciaPageContent = correspondenciaPage.getContent();

        // Always return a response, even if empty
        CorrespondenciaResponse correspondenciaResponse = new CorrespondenciaResponse();
        correspondenciaResponse.setContent(correspondenciaPageContent);
        correspondenciaResponse.setPageNumber(correspondenciaPage.getNumber());
        correspondenciaResponse.setPageSize(correspondenciaPage.getSize());
        correspondenciaResponse.setTotalElements(correspondenciaPage.getTotalElements());
        correspondenciaResponse.setTotalPages(correspondenciaPage.getTotalPages());
        correspondenciaResponse.setLastPage(correspondenciaPage.isLast());

        return correspondenciaResponse;

    }

    /**
     * Validates if the sort field exists in the Correspondencia entity
     */
    private boolean isValidSortField(String sortBy) {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return false;
        }
        
        // Valid fields in Correspondencia entity
        String[] validFields = {"id", "remetente", "nomeEmpresaConexa", "statusCorresp", "dataRecebimento", "dataAvisoConexa", "fotoCorrespondencia"};
        for (String field : validFields) {
            if (field.equalsIgnoreCase(sortBy)) {
                return true;
            }
        }
        return false;
    }

    /**
     * CRIA O ADITIVO BASEADO NA EMPRESA PRESENTE
     */
    public AditivoResponseDTO solicitarCriacaoAditivo(String nomeUnidade, Empresa empresa, AditivoRequestDTO dadosFormulario) {
        UnidadeService.UnidadeInfo info = unidadeService.getUnidadeInfo(nomeUnidade);

        if(info == null) {
            throw new APIExceptions("Unidade não encontrada: " + nomeUnidade);
        }

        AditivoRequestDTO aditivoRequestDTO = new AditivoRequestDTO();

        aditivoRequestDTO.setEmpresaId(String.valueOf(empresa.getId()));

        aditivoRequestDTO.setUnidadeNome(nomeUnidade);
        aditivoRequestDTO.setUnidadeCnpj(info.cnpj());
        aditivoRequestDTO.setUnidadeEndereco(info.endereco());

        aditivoRequestDTO.setPessoaFisicaNome(empresa.getNomeEmpresa());
        aditivoRequestDTO.setPessoaFisicaEndereco(String.valueOf(empresa.getEndereco().enderecoFormatado()));
        aditivoRequestDTO.setPessoaFisicaCpf(dadosFormulario.getPessoaFisicaCpf());

        aditivoRequestDTO.setDataInicioContrato(dadosFormulario.getDataInicioContrato());

        aditivoRequestDTO.setPessoaJuridicaNome(dadosFormulario.getPessoaJuridicaNome());
        aditivoRequestDTO.setPessoaJuridicaCnpj(dadosFormulario.getPessoaJuridicaCnpj());
        aditivoRequestDTO.setPessoaJuridicaEndereco(info.endereco());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));

        String localData = info.cidade() + ", " + LocalDate.now().format(formatter);

        aditivoRequestDTO.setLocalData(localData);

        AditivoContratual aditivoContratual = UnidadeMapper.toEntity(aditivoRequestDTO);

        ResponseEntity<AditivoResponseDTO> response = aditivoClient.criarAditivo(aditivoRequestDTO);

        AditivoContratual salvo = aditivoRepository.save(aditivoContratual);

        historicoService.registrar(
                "Aditivo Criado para Empresa",
                salvo.getId(),
                "Aviso de Aditivo Enviado",
                "Enviado e-mail solicitando mudança de CPF para CNPJ."
        );

        return response.getBody();
    }

    /**
     * RETORNAR EMPRESA NO BANCO DE DADOS DA ATHENA
     */
    public Empresa buscarEmpresaPeloNomeAthena(String nome) {
        Empresa empresa = empresaRepository.findByNomeEmpresa(nome)
                .orElseThrow(() -> new APIExceptions("Empresa não existente"));

        return empresa;
    }

    /**
     * Consulta no Conexa buscando por nome da empresa
     */
    public List<CustomerResponse> verificarEmpresaConexa(String nomeEmpresa) {
        List<CustomerResponse> todasEmpresas = conexaClient.buscarEmpresasPorNome(nomeEmpresa);
        if (todasEmpresas == null || todasEmpresas.isEmpty()) return List.of();

        String nomeBusca = nomeEmpresa.toLowerCase();
        return todasEmpresas.stream()
                .filter(e -> (e.getName() != null && e.getName().toLowerCase().contains(nomeBusca))
                        || (e.getFirstName() != null && e.getFirstName().toLowerCase().contains(nomeBusca)))
                .collect(Collectors.toList());
    }

    @Transactional
    public Correspondencia processarCorrespondencia(Correspondencia correspondencia) {
        log.debug("Iniciando processamento de correspondencia para empresa='{}' remetente='{}'", correspondencia.getNomeEmpresaConexa(), correspondencia.getRemetente());
        List<CustomerResponse> empresasEncontradas = verificarEmpresaConexa(correspondencia.getNomeEmpresaConexa());

        log.debug("Resultado da busca no Conexa ({} resultados) para '{}'", empresasEncontradas.size(), correspondencia.getNomeEmpresaConexa());
        correspondencia.setDataRecebimento(LocalDateTime.now(java.time.ZoneId.of("America/Recife")));

        if (!empresasEncontradas.isEmpty()) {
            CustomerResponse customer = empresasEncontradas.get(0);

            // Verificar se o customer já existe antes de salvar
            Optional<Customer> customerExistente = customerRepository.findById(customer.getCustomerId());
            if (customerExistente.isEmpty()) {
                Customer customerEntity = CustomerResponseMapper.fromCustomerResponse(customer);
                Customer savedCustomer = customerRepository.save(customerEntity);
                log.debug("Customer salvo no banco com id={} name={}", savedCustomer.getId(), savedCustomer.getName());
            }

            return processarCorrespondenciaComEmpresaConexa(correspondencia, customer);

            // ABAIXO É O METODO DE PESQUISA FORA DO CONEXA
        } else {
            return processarCorrespondenciaForaDoConexa(correspondencia);
        }
    }

    /**
     * A GENTE ATÉ TENTA ARRUMAR. VAMOS VER SE FUNCIONA.
     */
    private Correspondencia processarCorrespondenciaComEmpresaConexa(Correspondencia correspondencia, CustomerResponse customer) {

        if (isPessoaFisica(customer)) {
            return tratarEmpresaPessoaFisica(correspondencia, customer);
        } else {
            return tratarEmpresaPessoaJuridica(correspondencia, customer);
        }
    }

    private Correspondencia tratarEmpresaPessoaFisica(Correspondencia correspondencia, CustomerResponse customer) {
        correspondencia.setStatusCorresp(StatusCorresp.ANALISE);

        Empresa empresa = empresaRepository.findByNomeEmpresa(customer.getName())
                .orElseGet(() -> criarEmpresaPessoaFisica(customer));

        log.debug("Empresa CPF tratada: id={} nome={}", empresa.getId(), empresa.getNomeEmpresa());

        // E-mail será configurado futuramente
        historicoService.registrar(
                "Empresa",
                empresa.getId(),
                "Aviso de Aditivo Enviado",
                "Enviado e-mail solicitando mudança de CPF para CNPJ."
        );

        return correspondenciaRepository.save(correspondencia);
    }

    private Empresa criarEmpresaPessoaFisica(CustomerResponse customer) {
        Optional<String> cpfPessoaFisica = buscarCpfPorCustomerId(customer.getCustomerId());
        Empresa nova = EmpresaMapper.fromCustomerResponse(customer);

        nova.setStatusEmpresa(StatusEmpresa.FALTA_ADITIVO);
        nova.setCnpj(cpfPessoaFisica.get());
        nova.setSituacao(Situacao.CPF);
        nova.setMensagem("Necessário aditivo contratual para alterar CPF para CNPJ");
        return empresaRepository.save(nova);
    }

    private Correspondencia tratarEmpresaPessoaJuridica(Correspondencia correspondencia, CustomerResponse customer) {
        correspondencia.setStatusCorresp(StatusCorresp.ANALISE);

        Empresa empresa = empresaRepository.findByNomeEmpresa(customer.getName())
                .orElseGet(() -> criarEmpresaPessoaJuridica(customer));

        historicoService.registrar(
                "Correspondencia",
                empresa.getId(),
                "Aviso de Correspondência",
                "Correspondência informada ao cliente '" + customer.getName() + "'."
        );

        return correspondenciaRepository.save(correspondencia);
    }

    private Empresa criarEmpresaPessoaJuridica(CustomerResponse customer) {
        Empresa nova = EmpresaMapper.fromCustomerResponse(customer);

        return empresaRepository.save(nova);
    }

    private Correspondencia processarCorrespondenciaForaDoConexa(Correspondencia correspondencia) {
        if (correspondencia.getNomeEmpresaConexa() == null || correspondencia.getNomeEmpresaConexa().isBlank()) {
            return tratarCorrespondenciaSemNome(correspondencia);
        } else {
            return tratarCorrespondenciaDevolvida(correspondencia);
        }
    }

    private Correspondencia tratarCorrespondenciaSemNome(Correspondencia correspondencia) {
        Correspondencia salvo = correspondenciaRepository.save(correspondencia);
        historicoService.registrar(
                "Correspondencia",
                salvo.getId(),
                "Recebimento sem nome cadastrado",
                "Correspondência recebida do remetente '" + salvo.getRemetente() + "' sem destino definido."
        );
        return salvo;
    }

    private Correspondencia tratarCorrespondenciaDevolvida(Correspondencia correspondencia) {
        correspondencia.setStatusCorresp(StatusCorresp.DEVOLVIDA);

        empresaRepository.findByNomeEmpresa(correspondencia.getNomeEmpresaConexa())
                .orElseGet(() -> criarEmpresaFallback(correspondencia));

        historicoService.registrar(
                "Correspondencia",
                correspondencia.getId(),
                "Sem uso de quaisquer endereço",
                "Correspondência recebida da empresa '" + correspondencia.getNomeEmpresaConexa() + "'."
        );

        return correspondenciaRepository.save(correspondencia);
    }

    private Empresa criarEmpresaFallback(Correspondencia correspondencia) {
        Empresa nova = new Empresa();
        nova.setNomeEmpresa(correspondencia.getNomeEmpresaConexa());
        nova.setStatusEmpresa(StatusEmpresa.AGUARDANDO);
        nova.setSituacao(Situacao.CNPJ);
        return empresaRepository.save(nova);
    }



    /**
     * Busca o CPF de um cliente (pessoa física) no Conexa a partir do personId.
     * Retorna Optional.empty() se não encontrar.
     */
    public Optional<String> buscarCpfPorCustomerId(Long customerId) {
        return conexaClient.buscarCpfPorCustomerId(customerId);
    }


    private boolean isPessoaFisica(CustomerResponse customer) {
        return customer.getLegalPerson() == null
                || customer.getLegalPerson().getCnpj() == null
                || customer.getLegalPerson().getCnpj().isEmpty();
    }


    public Correspondencia alterarStatusCorrespondencia(Long id, StatusCorresp novoStatus) {
        Correspondencia correspondencia = correspondenciaRepository.findById(id)
                .orElseThrow(() -> new APIExceptions("Correspondência não encontrada com ID: " + id));

        StatusCorresp statusAnterior = correspondencia.getStatusCorresp();
        correspondencia.setStatusCorresp(novoStatus);
        Correspondencia atualizada = correspondenciaRepository.save(correspondencia);

        historicoService.registrar(
                "Correspondencia",
                atualizada.getId(),
                "Status alterado",
                "Status alterado de '" + statusAnterior + "' para '" + novoStatus + "'."
        );

        return atualizada;
    }

    /**
     * Atualiza a data de aviso de uma correspondência
     */
    public Correspondencia atualizarDataAviso(Long id, java.time.LocalDate dataAviso) {
    Correspondencia correspondencia = correspondenciaRepository.findById(id)
        .orElseThrow(() -> new APIExceptions("Correspondência não encontrada com ID: " + id));

    correspondencia.setDataAvisoConexa(dataAviso);
    Correspondencia atualizada = correspondenciaRepository.save(correspondencia);

    historicoService.registrar(
        "Correspondencia",
        atualizada.getId(),
        "Data de aviso atualizada",
        "Data de aviso alterada para '" + dataAviso + "'."
    );

    return atualizada;
    }

    /**
     * Lista correspondências com dados da empresa associada
     */
    public List<CorrespondenciaComEmpresaDTO> listarComEmpresa(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Correspondencia> correspondenciaPage = correspondenciaRepository.findAll(pageDetails);

        return correspondenciaPage.getContent().stream()
                .map(this::mapearParaDTOComEmpresa)
                .collect(Collectors.toList());
    }

    /**
     * Mapeia correspondência para DTO com dados da empresa
     */
    private CorrespondenciaComEmpresaDTO mapearParaDTOComEmpresa(Correspondencia correspondencia) {
        CorrespondenciaComEmpresaDTO dto = new CorrespondenciaComEmpresaDTO();
        dto.setId(correspondencia.getId());
        dto.setRemetente(correspondencia.getRemetente());
        dto.setNomeEmpresaConexa(correspondencia.getNomeEmpresaConexa());
        dto.setStatusCorresp(correspondencia.getStatusCorresp() != null ? correspondencia.getStatusCorresp().name() : null);
        dto.setDataRecebimento(LocalDate.from(correspondencia.getDataRecebimento()));
        dto.setDataAvisoConexa(correspondencia.getDataAvisoConexa());
        dto.setFotoCorrespondencia(correspondencia.getAnexos());

        // Buscar dados da empresa se existir
        if (correspondencia.getNomeEmpresaConexa() != null && !correspondencia.getNomeEmpresaConexa().isEmpty()) {
            String nomeBusca = correspondencia.getNomeEmpresaConexa().trim();
            Optional<Empresa> empresa = empresaRepository.findByNomeEmpresa(nomeBusca);
            
            if (empresa.isPresent()) {
                Empresa emp = empresa.get();
                dto.setNomeEmpresa(emp.getNomeEmpresa());
                dto.setCnpj(emp.getCnpj());
                dto.setEmail(emp.getEmail());
                dto.setTelefone(emp.getTelefone());
                dto.setStatusEmpresa(emp.getStatusEmpresa() != null ? emp.getStatusEmpresa().name() : null);
                dto.setSituacao(emp.getSituacao() != null ? emp.getSituacao().name() : null);
                dto.setMensagem(emp.getMensagem());
            }
        }

                try {
                    // Evitar criar duplicata: verificar existência por nome (trim e ignorar case se possível)
                    String nomeBusca = correspondencia.getNomeEmpresaConexa() != null ? correspondencia.getNomeEmpresaConexa().trim() : "";
                    Optional<Empresa> empresaExistenteFallback = empresaRepository.findByNomeEmpresa(nomeBusca);
                    Empresa empresaSalva = null;

                    if (empresaExistenteFallback.isEmpty()) {
                        Empresa novaEmpresa = new Empresa();
                        novaEmpresa.setNomeEmpresa(nomeBusca);
                        novaEmpresa.setCnpj(null);
                        novaEmpresa.setUnidade(null);
                        novaEmpresa.setEmail(null);
                        novaEmpresa.setTelefone(null);
                        novaEmpresa.setEndereco(null);
                        novaEmpresa.setStatusEmpresa(StatusEmpresa.AGUARDANDO);
                        // Marcar como CNPJ por padrão — pode ser ajustado manualmente depois
                        novaEmpresa.setSituacao(Situacao.CNPJ);
                        novaEmpresa.setMensagem(null);

                        empresaSalva = empresaRepository.save(novaEmpresa);
                        log.debug("Empresa criada a partir de correspondencia (fallback) id={} nome={}", empresaSalva.getId(), empresaSalva.getNomeEmpresa());

                        historicoService.registrar(
                                "Empresa",
                                empresaSalva.getId(),
                                "Empresa criada via correspondencia",
                                "Empresa criada automaticamente a partir de correspondência com nome '" + empresaSalva.getNomeEmpresa() + "'."
                        );
                    } else {
                        empresaSalva = empresaExistenteFallback.get();
                        log.debug("Empresa já existe (fallback) id={} nome={}", empresaSalva.getId(), empresaSalva.getNomeEmpresa());
                    }
                } catch (Exception e) {
                    log.error("Erro ao criar empresa fallback: {}", e.getMessage());
                }
                
        return dto;
    }

    public void apagarCorrespondencia(Long id){
        log.info("Apagando correspondência id={}", id);
        Correspondencia correspondencia = correspondenciaRepository.findById(id)
                .orElseThrow(() -> new APIExceptions("Correspondência não encontrada com ID: " + id));
        correspondenciaRepository.delete(correspondencia);
        log.info("Correspondência id={} deletada do repositório", id);

        // Registrar historico de exclusão
        try {
            historicoService.registrar(
                "Correspondencia",
                id,
                "EXCLUIR",
                "Correspondência de '" + correspondencia.getRemetente() + "' excluída."
            );
            log.info("Historico de exclusão registrado para correspondência id={}", id);
        } catch (Exception e) {
            log.error("Falha ao registrar historico de exclusão para id={}: {}", id, e.getMessage());
            // não rethrow — historico falhar não deve impedir operação principal já realizada
        }
    }

    /**
     * Atualiza parcialmente uma correspondência existente.
     */
    public Correspondencia atualizarCorrespondencia(Long id, Correspondencia updates) {
    Correspondencia correspondencia = correspondenciaRepository.findById(id)
        .orElseThrow(() -> new APIExceptions("Correspondência não encontrada com ID: " + id));

    // Atualiza apenas campos não-nulos do objeto de updates
    if (updates.getRemetente() != null) correspondencia.setRemetente(updates.getRemetente());
    if (updates.getNomeEmpresaConexa() != null) correspondencia.setNomeEmpresaConexa(updates.getNomeEmpresaConexa());
    if (updates.getStatusCorresp() != null) correspondencia.setStatusCorresp(updates.getStatusCorresp());
    if (updates.getDataAvisoConexa() != null) correspondencia.setDataAvisoConexa(updates.getDataAvisoConexa());
    if (updates.getAnexos() != null) correspondencia.setAnexos(updates.getAnexos());

    Correspondencia atualizada = correspondenciaRepository.save(correspondencia);

    historicoService.registrar(
        "Correspondencia",
        atualizada.getId(),
        "ATUALIZAR",
        "Correspondência atualizada"
    );

    return atualizada;
    }

    public Map<String, Object> analisarContrato(Long contractId) {
        ConexaContractResponse c = conexaClient.buscarContratoPorId(contractId);
        if (c == null) throw new APIExceptions("Contrato não encontrado");

        boolean inadimplente = conexaClient.estaInadimplente(contractId);

        return Map.of(
                "contractId", c.contractId(),
                "startDate",  c.startDate(),
                "inadimplente", inadimplente
        );
    }

    public EmailResponseRecord envioEmailCorrespondencia(EmailServiceDTO emailServiceDTO) {
        List<Correspondencia> correspondencias = correspondenciaRepository.findByNomeEmpresaConexaIgnoreCase(emailServiceDTO.getNomeEmpresaConexa());

        Empresa empresa = empresaRepository.findByNomeEmpresa(emailServiceDTO.getNomeEmpresaConexa())
                .orElseThrow(() -> new APIExceptions("Não foi possível localizar empresa no banco de dados"));

        historicoService.registrar(
                "Correspondencia",
                empresa.getId(),
                "Aviso enviado",
                "Aviso de correspondência enviado para '" + emailServiceDTO.getNomeEmpresaConexa() + "' (" + emailServiceDTO.getEmailDestino() + ")."
        );

        // supondo que você já tem a lista de correspondências do cliente
        emailService.enviarAvisoCorrespondencias(
                emailServiceDTO.getEmailDestino(),              // destino (pode vir do Customer/Empresa)
                emailServiceDTO.getNomeEmpresaConexa(),            // nome do cliente
                correspondencias   // List<Correspondencia>
        );

        // 3) data mais recente (evita NPE do getLast)
        LocalDateTime dataMaisRecente = correspondencias.stream()
                .map(Correspondencia::getDataRecebimento) // LocalDateTime
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(LocalDateTime.now(ZoneId.of("America/Recife")));

        return new EmailResponseRecord(
                "Enviado",
                emailServiceDTO.getNomeEmpresaConexa(),
                dataMaisRecente
                );
    }

    // 2.1) Sem upload (anexos vindos por URL já salvos) OU só aviso
    public EmailResponseRecord envioEmailCorrespondenciaResend(EmailServiceDTO dto) {
        String nome  = dto.getNomeEmpresaConexa() == null ? "" : dto.getNomeEmpresaConexa();

        // tenta achar empresa (nome exato ou contendo)
        Empresa empresa = empresaRepository.findByNomeEmpresa(nome)
                .orElseThrow(() -> new APIExceptions("Empresa não encontrada: " + nome));

        List<String> emails = empresa.getEmail();

        if (emails == null || emails.isEmpty())
            throw new APIExceptions("Empresa não possui e-mail cadastrado.");

        String email = emails.get(0); // agora é seguro

        System.out.printf("[Resend] nome='%s' email='%s' anexos=%s urls=%s%n",
                nome, email, dto.isAnexos(), dto.getAnexosUrls());

        if (nome.isBlank()) throw new APIExceptions("Nome da empresa é obrigatório");
        if (!EMAIL_RX.matcher(email).matches())
            throw new APIExceptions("E-mail de destino inválido: " + email);

        // tenta achar correspondências; se não houver, continua
        var correspondencias = correspondenciaRepository.findByNomeEmpresaConexaIgnoreCase(nome);
        if (correspondencias == null) correspondencias = new ArrayList<>();

        // monta anexos
        List<AnexoDTO> anexos = List.of();

        // (A) se vieram URLs explicitamente
        if (dto.isAnexos() && dto.getAnexosUrls() != null && !dto.getAnexosUrls().isEmpty()) {
            anexos = carregarAnexosDoStorage(dto.getAnexosUrls());
        }
        // (B) se dto.isAnexos() = true, mas sem URLs → tenta carregar do banco
        else if (dto.isAnexos()) {
            List<String> keys = correspondencias.stream()
                    .filter(c -> c.getAnexos() != null)
                    .flatMap(c -> c.getAnexos().stream())
                    .toList();
            anexos = carregarAnexosDoStorage(keys);
        }

        // envia
        resendEmail.enviarAvisoCorrespondencias(email, nome, correspondencias, anexos);

        // registra histórico
        historicoService.registrar(
                "Correspondencia",
                empresa.getId(),
                anexos.isEmpty() ? "Aviso (Resend)" : "Aviso+anexos (Resend)",
                "Enviado para '" + nome + "' (" + email + ")."
        );

        // pega data mais recente de recebimento
        var dataMaisRecente = correspondencias.stream()
                .map(Correspondencia::getDataRecebimento)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(LocalDateTime.now(ZoneId.of("America/Recife")));

        return new EmailResponseRecord("Enviado", nome, dataMaisRecente);
    }


    // 2.2) Com upload direto de arquivos (controller passa MultipartFile; service converte)
    public EmailResponseRecord envioEmailCorrespondenciaResendUpload(
            String nomeEmpresaConexa,
            List<MultipartFile> arquivosUpload
    ) {

        Empresa empresa = empresaRepository.findByNomeEmpresa(nomeEmpresaConexa)
                        .orElseThrow(() -> new APIExceptions("Empresa não encontrada"));

        String emailDestino = empresa.getEmail().get(0);

        System.out.printf("[ResendUpload] nome='%s' email='%s' arquivos=%d%n",
                nomeEmpresaConexa, emailDestino,
                (arquivosUpload == null ? 0 : arquivosUpload.size()));

        if (nomeEmpresaConexa == null || nomeEmpresaConexa.isBlank())
            throw new APIExceptions("Nome da empresa é obrigatório");
        if (emailDestino == null || emailDestino.isBlank())
            throw new APIExceptions("E-mail de destino não informado");
        if (!EMAIL_RX.matcher(emailDestino).matches())
            throw new APIExceptions("E-mail de destino inválido: " + emailDestino);

        // prepara DTO base
        EmailServiceDTO base = new EmailServiceDTO();
        base.setNomeEmpresaConexa(nomeEmpresaConexa.trim());
        base.setEmailDestino(emailDestino.trim());
        base.setAnexos(arquivosUpload != null && !arquivosUpload.isEmpty());

        // converte anexos se houver
        List<AnexoDTO> anexos = (arquivosUpload == null || arquivosUpload.isEmpty())
                ? List.of()
                : mapMultipart(arquivosUpload);

        return envioCore(nomeEmpresaConexa, emailDestino, anexos);
    }


    // ===== Core compartilhado (evita duplicação) =====
    private EmailResponseRecord envioCore(String nome, String email, List<AnexoDTO> anexos) {
        if (nome == null || nome.isBlank()) throw new APIExceptions("Nome da empresa é obrigatório");
        if (email == null || !EMAIL_RX.matcher(email).matches()) throw new APIExceptions("E-mail de destino inválido");

        var correspondencias = correspondenciaRepository.findByNomeEmpresaConexaIgnoreCase(nome.trim());
        if (correspondencias == null || correspondencias.isEmpty())
            throw new APIExceptions("Nenhuma correspondência encontrada para '" + nome + "'");

        var empresa = empresaRepository.findByNomeEmpresaIgnoreCase(nome.trim())
                .orElseThrow(() -> new APIExceptions("Empresa não encontrada: " + nome));

        resendEmail.enviarAvisoCorrespondencias(email, nome, correspondencias, anexos == null ? List.of() : anexos);

        historicoService.registrar("Correspondencia", empresa.getId(),
                (anexos == null || anexos.isEmpty() ? "Aviso (upload vazio)" : "Aviso+anexos (upload)"),
                "Enviado para '" + nome + "' (" + email + ").");

        var dataMaisRecente = correspondencias.stream()
                .map(Correspondencia::getDataRecebimento)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .orElse(LocalDateTime.now(ZoneId.of("America/Recife")));

        return new EmailResponseRecord("Enviado", nome, dataMaisRecente);
    }


    private List<AnexoDTO> mapMultipart(List<org.springframework.web.multipart.MultipartFile> files) {
        if (files == null) return List.of();
        List<AnexoDTO> out = new ArrayList<>();
        for (var f : files) {
            try {
                out.add(new AnexoDTO(
                        f.getOriginalFilename(),
                        f.getContentType() == null ? "application/octet-stream" : f.getContentType(),
                        f.getBytes()
                ));
            } catch (Exception e) {
                throw new APIExceptions("Falha lendo arquivo: " + f.getOriginalFilename());
            }
        }
        return out;
    }

    private List<AnexoDTO> carregarAnexosDoStorage(List<String> keys) {
        if (keys == null || keys.isEmpty()) return List.of();
        List<AnexoDTO> out = new ArrayList<>();
        for (String k : keys) {
            if (k == null || k.isBlank() || !storageService.exists(k)) continue;
            byte[] data = storageService.read(k);
            String filename = storageService.originalFilename(k);
            String contentType = storageService.contentType(k);
            out.add(new AnexoDTO(filename, contentType, data));
        }
        return out;
    }

}
