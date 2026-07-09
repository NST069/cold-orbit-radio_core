package com.coradio.rotation.domain.port.out.persistence;

import com.coradio.rotation.domain.model.ScrobbleJobItem;
import java.util.List;

public interface ScrobbleJobRepositoryPort {
    ScrobbleJobItem save(ScrobbleJobItem scrobbleJobItem);

    List<ScrobbleJobItem> getAllPending();
}
