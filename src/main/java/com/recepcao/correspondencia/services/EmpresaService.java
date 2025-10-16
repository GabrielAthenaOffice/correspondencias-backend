package com.recepcao.correspondencia.services;

import com.recepcao.correspondencia.config.APIExceptions;
import com.recepcao.correspondencia.dto.EmpresaResponse;
import com.recepcao.correspondencia.dto.responses.ConexaCustomerListResponse;
import com.recepcao.correspondencia.dto.responses.CustomerResponse;
import com.recepcao.correspondencia.entities.Customer;
import com.recepcao.correspondencia.entities.Empresa;
import com.recepcao.correspondencia.mapper.enums.Situacao;
import com.recepcao.correspondencia.mapper.enums.StatusEmpresa;
import com.recepcao.correspondencia.repositories.CustomerRepository;
import com.recepcao.correspondencia.repositories.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final CustomerRepository customerRepository;
    private final HistoricoService historicoService;
    private final ModelMapper modelMapper;

    public ConexaCustomerListResponse listarEmpresasModeloConexa(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Customer> customerPage = customerRepository.findAll(pageDetails);

        List<Customer> customers = customerPage.getContent();

        if (customers.isEmpty()) {
            throw new APIExceptions("Nenhuma empresa registrada até o momento");
        }

        List<CustomerResponse> customerDTO = customers.stream().map(customer -> {
            CustomerResponse dto = modelMapper.map(customer, CustomerResponse.class);
            // Preencher statusEmpresa, se não existir, usar valor padrão
            if (customer.getStatusEmpresa() != null && !customer.getStatusEmpresa().isEmpty()) {
                dto.setStatusEmpresa(customer.getStatusEmpresa());
            } else {
                dto.setStatusEmpresa("ANALISE"); // Valor padrão se não existir
            }
            return dto;
        }).toList();


        ConexaCustomerListResponse customerResponse = new ConexaCustomerListResponse();
        customerResponse.setData(customerDTO);
        customerResponse.setPageNumber(customerPage.getNumber());
        customerResponse.setPageSize(customerPage.getSize());
        customerResponse.setTotalElements(customerPage.getTotalElements());
        customerResponse.setTotalPages(customerPage.getTotalPages());
        customerResponse.setLastPage(customerPage.isLast());

        return customerResponse;
    }

    public EmpresaResponse listarEmpresasModeloAthena(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Empresa> empresaPage = empresaRepository.findAll(pageDetails);

        List<Empresa> empresas = empresaPage.getContent();

        EmpresaResponse empresaResponse = new EmpresaResponse();
        empresaResponse.setContent(empresas);
        empresaResponse.setPageNumber(empresaPage.getNumber());
        empresaResponse.setPageSize(empresaPage.getSize());
        empresaResponse.setTotalElements(empresaPage.getTotalElements());
        empresaResponse.setTotalPages(empresaPage.getTotalPages());
        empresaResponse.setLastPage(empresaPage.isLast());

        return empresaResponse;
    }

    public Optional<Empresa> buscarEmpresaPorIdModeloAthena(Long id) {
        Optional<Empresa> empresas = empresaRepository.findById(id);

        if(empresas.isEmpty()) {
            throw new APIExceptions("Nenhuma empresa registrada pela Athena até agora");
        }

        return empresas;
    }


    public Optional<Empresa> buscarEmpresaPorNomeModeloAthena (String nome) {
        Optional<Empresa> empresas = empresaRepository.findByNomeEmpresaIgnoreCase(nome);

        if(empresas.isEmpty()) {
            throw new APIExceptions("Nenhuma empresa registrada pela Athena até agora");
        }

        return empresas;
    }



    public Empresa alterarStatusModeloAthena(Long empresaId, StatusEmpresa novoStatus, Situacao novaSituacao, String novaMensagem) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new APIExceptions("Empresa não encontrada com ID: " + empresaId));

        StatusEmpresa statusAnterior = empresa.getStatusEmpresa();
        Situacao situacaoAnterior = empresa.getSituacao();

        empresa.setStatusEmpresa(novoStatus);
        empresa.setSituacao(novaSituacao);
        empresa.setMensagem(novaMensagem);

        Empresa atualizada = empresaRepository.save(empresa);

        historicoService.registrar(
                "Empresa",
                empresa.getId(),
                "Status/Situação alterados",
                String.format("Status alterado de '%s' para '%s' e situação de '%s' para '%s'.",
                        statusAnterior, novoStatus, situacaoAnterior, novaSituacao)
        );

        return atualizada;
    }

    public void deletarEmpresa(Long id) {
        // busca a empresa no banco
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new APIExceptions("Empresa não encontrada com ID: " + id));

        // deleta a empresa
        empresaRepository.delete(empresa);

        // registra no histórico (mantém rastreabilidade)
        historicoService.registrar(
                "Empresa",
                id,
                "Exclusão",
                String.format("Empresa '%s' (ID: %d) foi excluída do sistema.", empresa.getNomeEmpresa(), id)
        );

        // 4️⃣ Log no console para depuração
        System.out.printf("[EmpresaService] Empresa ID=%d ('%s') removida com sucesso.%n",
                id, empresa.getNomeEmpresa());
    }

    public Empresa atualizarCamposEspecificos(Long id, Empresa updates) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new APIExceptions("Empresa não encontrada com ID: " + id));

        // Atualiza apenas os campos que vierem preenchidos
        if (updates.getNomeEmpresa() != null && !updates.getNomeEmpresa().isBlank()) {
            empresa.setNomeEmpresa(updates.getNomeEmpresa().trim());
        }

        if (updates.getEmail() != null && !updates.getEmail().isEmpty()) {
            empresa.setEmail(updates.getEmail());
        }

        if (updates.getTelefone() != null && !updates.getTelefone().isEmpty()) {
            empresa.setTelefone(updates.getTelefone());
        }

        if (updates.getCnpj() != null && !updates.getCnpj().isBlank()) {
            empresa.setCnpj(updates.getCnpj().trim());
        }

        if (updates.getEndereco() != null && !updates.getEndereco().getBairro().isEmpty()) {
            empresa.setEndereco(updates.getEndereco());
        }

        if (updates.getMensagem() != null) {
            empresa.setMensagem(updates.getMensagem().trim());
        }

        Empresa atualizada = empresaRepository.save(empresa);

        // Registra histórico
        historicoService.registrar(
                "Empresa",
                empresa.getId(),
                "Atualização de dados",
                String.format("Campos da empresa '%s' atualizados manualmente.", empresa.getNomeEmpresa())
        );

        System.out.printf("[EmpresaService] Empresa ID=%d atualizada parcialmente.%n", id);
        return atualizada;
    }


}
