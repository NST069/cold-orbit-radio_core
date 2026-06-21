package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.application.dto.TrackInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrackInfoMapperTest {

    @Mock
    ResultSet rs;

    @Test
    void shouldMapResultSetToTrackInfo() throws SQLException {
        UUID id = UUID.randomUUID();

        //ResultSet rs = mock(ResultSet.class);

        when(rs.getObject("id", UUID.class)).thenReturn(id);
        when(rs.getString("artist")).thenReturn("Artist");
        when(rs.getString("title")).thenReturn("Title");
        when(rs.getInt("duration")).thenReturn(180);
        when(rs.getString("storage_key")).thenReturn("storage-key");

        TrackInfoMapper mapper = new TrackInfoMapper();

        TrackInfo result = mapper.mapRow(rs, 1);

        assertEquals(id, result.id());
        assertEquals("Artist", result.artist());
        assertEquals("Title", result.title());
        assertEquals(180, result.duration());
        assertEquals("storage-key", result.storageKey());
    }

}
