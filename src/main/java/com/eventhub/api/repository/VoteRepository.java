package com.eventhub.api.repository;

import com.eventhub.api.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // 1. Método de Segurança (Anti-fraude): 
    // Retorna 'true' se o Banco de Dados achar um voto desse Usuário específico nessa Data específica.
    // Usaremos isso para impedir que a mesma pessoa dê 10 votos na mesma data.
    boolean existsByUserIdAndEventDateOptionId(Long userId, Long eventDateOptionId);
    
    // 2. Método Analítico: 
    // Retorna o número total de votos que uma data recebeu (para podermos eleger a data vencedora depois).
    long countByEventDateOptionId(Long eventDateOptionId);
    
    // 3. Método de Remoção (Opcional):
    // Caso a pessoa resolva "descurtir" a data, precisamos achar o voto dela para deletar.
    Optional<Vote> findByUserIdAndEventDateOptionId(Long userId, Long eventDateOptionId);
}
