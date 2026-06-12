package com.coradio.rotation.infrastructure.out.persistense.mapper;

import com.coradio.rotation.application.dto.TrackInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Component
public class TrackInfoMapper implements RowMapper<TrackInfo> {

    @Override
    public TrackInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new TrackInfo(
                rs.getObject("id", UUID.class),
                rs.getString("artist"),
                rs.getString("title"),
                rs.getInt("duration"),
                rs.getString("storage_key")
        );
    }

}
