package com.coradio.rotation.infrastructure.out.persistense.adapter;

import com.coradio.rotation.domain.enums.JobStatus;
import com.coradio.rotation.domain.model.ScrobbleJobItem;
import com.coradio.rotation.domain.port.out.persistence.ScrobbleJobRepositoryPort;
import com.coradio.rotation.infrastructure.out.persistense.mapper.ScrobbleJobMapper;
import com.coradio.rotation.infrastructure.out.persistense.repository.ScrobbleJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScrobbleJobAdapter implements ScrobbleJobRepositoryPort {

    private final ScrobbleJobRepository scrobbleJobRepository;

    @Override
    public ScrobbleJobItem save(ScrobbleJobItem scrobbleJobItem) {
        return ScrobbleJobMapper.toDomain(
                scrobbleJobRepository.save(
                        ScrobbleJobMapper.toEntity(scrobbleJobItem)
                )
        );
    }

    @Override
    public List<ScrobbleJobItem> getAllPending() {
        return scrobbleJobRepository.findAllByStatus(JobStatus.CREATED).stream()
                .map(ScrobbleJobMapper::toDomain).toList();
    }
}
