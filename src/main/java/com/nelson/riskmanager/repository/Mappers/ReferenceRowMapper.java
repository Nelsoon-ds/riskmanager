package com.nelson.riskmanager.repository.Mappers;

import com.nelson.riskmanager.model.StandardReference;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ReferenceRowMapper implements RowMapper<StandardReference> {
    @Override
    public StandardReference mapRow(ResultSet rs, int rowNum) throws SQLException {
        StandardReference reference = null;
        reference.name(rs.getString("name");
    }
}
